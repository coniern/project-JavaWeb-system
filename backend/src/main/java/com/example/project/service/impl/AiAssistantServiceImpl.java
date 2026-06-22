package com.example.project.service.impl;

import com.example.project.dto.AiAssistRequest;
import com.example.project.dto.AiAssistResponse;
import com.example.project.dto.AiTaskSuggestion;
import com.example.project.service.AiAssistantService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AiAssistantServiceImpl implements AiAssistantService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${ai.endpoint:}")
    private String endpoint;

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.model:gpt-4.1-mini}")
    private String model;

    public AiAssistantServiceImpl(ObjectMapper objectMapper) {
        this.restClient = RestClient.builder().build();
        this.objectMapper = objectMapper;
    }

    @Override
    public AiAssistResponse assist(AiAssistRequest request) {
        validateRequest(request);
        if (isRemoteAiEnabled()) {
            try {
                return callRemoteAi(request);
            } catch (Exception ignored) {
            }
        }
        return buildFallbackResponse(request);
    }

    private boolean isRemoteAiEnabled() {
        return endpoint != null && !endpoint.isBlank() && apiKey != null && !apiKey.isBlank();
    }

    private AiAssistResponse callRemoteAi(AiAssistRequest request) throws Exception {
        Map<String, Object> response = restClient.post()
                .uri(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + apiKey)
                .body(Map.of(
                        "model", model,
                        "temperature", 0.6,
                        "messages", List.of(
                                Map.of(
                                        "role", "system",
                                        "content", "你是项目管理助手。请严格返回 JSON，不要输出 Markdown。JSON 结构必须包含 summary、riskTips、milestoneSuggestions、taskSuggestions 四个字段，其中 taskSuggestions 为 title、description、priority 组成的数组。priority 只允许 1、2、3。"
                                ),
                                Map.of(
                                        "role", "user",
                                        "content", buildUserPrompt(request)
                                )
                        )
                ))
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {
                });

        String content = extractMessageContent(response);
        String cleanContent = stripCodeFence(content);
        JsonNode root = objectMapper.readTree(cleanContent);

        AiAssistResponse assistResponse = new AiAssistResponse();
        assistResponse.setSummary(root.path("summary").asText(buildFallbackResponse(request).getSummary()));
        assistResponse.setRiskTips(readStringList(root.path("riskTips")));
        assistResponse.setMilestoneSuggestions(readStringList(root.path("milestoneSuggestions")));
        assistResponse.setTaskSuggestions(readTaskSuggestions(root.path("taskSuggestions")));
        assistResponse.setProvider("remote");
        assistResponse.setUsedFallback(false);

        if (assistResponse.getRiskTips().isEmpty() || assistResponse.getTaskSuggestions().isEmpty()) {
            return buildFallbackResponse(request);
        }
        return assistResponse;
    }

    private String buildUserPrompt(AiAssistRequest request) {
        return """
                项目名称：%s
                项目描述：%s
                目标：%s
                团队人数：%s
                截止日期：%s
                请给出摘要、风险提示、里程碑建议和任务拆解。
                """.formatted(
                safeText(request.getName()),
                safeText(request.getDescription()),
                safeText(request.getGoal()),
                request.getTeamSize() == null ? "未提供" : request.getTeamSize(),
                safeText(request.getDeadline())
        );
    }

    @SuppressWarnings("unchecked")
    private String extractMessageContent(Map<String, Object> response) {
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new IllegalStateException("AI 返回结果为空");
        }
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        if (message == null || message.get("content") == null) {
            throw new IllegalStateException("AI 返回内容为空");
        }
        return String.valueOf(message.get("content"));
    }

    private List<String> readStringList(JsonNode node) {
        List<String> values = new ArrayList<>();
        if (node.isArray()) {
            node.forEach(item -> {
                if (!item.asText().isBlank()) {
                    values.add(item.asText());
                }
            });
        }
        return values;
    }

    private List<AiTaskSuggestion> readTaskSuggestions(JsonNode node) {
        List<AiTaskSuggestion> values = new ArrayList<>();
        if (node.isArray()) {
            node.forEach(item -> values.add(new AiTaskSuggestion(
                    item.path("title").asText("待确认任务"),
                    item.path("description").asText("请补充任务描述"),
                    normalizePriority(item.path("priority").asInt(2))
            )));
        }
        return values;
    }

    private AiAssistResponse buildFallbackResponse(AiAssistRequest request) {
        AiAssistResponse response = new AiAssistResponse();
        response.setSummary(buildSummary(request));
        response.setRiskTips(buildRiskTips(request));
        response.setMilestoneSuggestions(buildMilestones(request));
        response.setTaskSuggestions(buildTaskSuggestions(request));
        response.setProvider("local-fallback");
        response.setUsedFallback(true);
        return response;
    }

    private String buildSummary(AiAssistRequest request) {
        return "%s 目标是%s，建议先完成需求澄清和范围锁定，再按迭代方式推进核心功能、联调测试和上线准备。"
                .formatted(
                        safeText(request.getName()),
                        safeText(request.getGoal()).equals("未提供") ? "提升交付效率" : safeText(request.getGoal())
                );
    }

    private List<String> buildRiskTips(AiAssistRequest request) {
        List<String> tips = new ArrayList<>();
        tips.add("优先冻结 MVP 范围，避免开发中途频繁扩项。");
        if (request.getTeamSize() == null || request.getTeamSize() <= 2) {
            tips.add("当前团队规模偏小，建议尽早明确分工并减少并行任务数量。");
        } else {
            tips.add("多人协作时需要提前约定接口字段和联调节奏，避免后期返工。");
        }
        if (request.getDeadline() == null || request.getDeadline().isBlank()) {
            tips.add("尚未提供截止时间，建议补充里程碑节点用于跟踪风险。");
        } else {
            tips.add("请为 %s 前预留至少一轮完整测试和修复时间。".formatted(request.getDeadline()));
        }
        return tips;
    }

    private List<String> buildMilestones(AiAssistRequest request) {
        return List.of(
                "第 1 周：需求梳理、角色分工、原型确认",
                "第 2 周：完成核心数据结构与关键页面开发",
                "第 3 周：完成联调、自测和缺陷修复",
                "第 4 周：上线准备、验收和复盘"
        );
    }

    private List<AiTaskSuggestion> buildTaskSuggestions(AiAssistRequest request) {
        String name = safeText(request.getName());
        return List.of(
                new AiTaskSuggestion("梳理 " + name + " 需求范围", "确认项目目标、页面范围、接口边界和验收标准。", 1),
                new AiTaskSuggestion("完成核心功能开发", "优先实现最关键的业务流程和数据录入能力。", 1),
                new AiTaskSuggestion("联调并补齐异常处理", "完成前后端接口联调、错误提示和数据校验。", 2),
                new AiTaskSuggestion("准备上线与复盘", "整理部署说明、回归测试结果和后续优化项。", 3)
        );
    }

    private int normalizePriority(int priority) {
        if (priority < 1 || priority > 3) {
            return 2;
        }
        return priority;
    }

    private String stripCodeFence(String content) {
        String trimmed = content == null ? "" : content.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(?:json)?", "").replaceFirst("```$", "").trim();
        }
        return trimmed;
    }

    private void validateRequest(AiAssistRequest request) {
        if (request == null || request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("项目名称不能为空");
        }
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "未提供" : value.trim();
    }
}
