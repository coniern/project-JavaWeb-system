package com.example.projectmanagement.service.impl;

import com.example.projectmanagement.config.AiProperties;
import com.example.projectmanagement.dto.AiAssistRequest;
import com.example.projectmanagement.dto.AiAssistResponse;
import com.example.projectmanagement.dto.AiTaskSuggestion;
import com.example.projectmanagement.service.AiAssistantService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class AiAssistantServiceImpl implements AiAssistantService {

    private final AiProperties aiProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AiAssistantServiceImpl(AiProperties aiProperties, RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper) {
        this.aiProperties = aiProperties;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(aiProperties.getTimeoutSeconds()))
                .setReadTimeout(Duration.ofSeconds(aiProperties.getTimeoutSeconds()))
                .build();
        this.objectMapper = objectMapper;
    }

    @Override
    public AiAssistResponse assist(AiAssistRequest request) {
        validateRequest(request);
        try {
            if (aiProperties.isEnabled()) {
                if ("ollama".equalsIgnoreCase(aiProperties.getProvider())) {
                    return parseResponse(callOllama(request), request, "ollama");
                }
                if ("openai-compatible".equalsIgnoreCase(aiProperties.getProvider())) {
                    return parseResponse(callOpenAiCompatible(request), request, "openai-compatible");
                }
            }
        } catch (Exception ignored) {
        }
        return buildFallbackResponse(request);
    }

    private String callOllama(AiAssistRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", aiProperties.getModel());
        body.put("prompt", buildPrompt(request));
        body.put("stream", false);

        ResponseEntity<Map> response = restTemplate.exchange(
                aiProperties.getBaseUrl() + "/generate",
                HttpMethod.POST,
                new HttpEntity<>(body, jsonHeaders()),
                Map.class
        );
        if (response.getBody() == null) {
            throw new RestClientException("Empty Ollama response");
        }
        return Objects.toString(response.getBody().get("response"), "");
    }

    private String callOpenAiCompatible(AiAssistRequest request) {
        HttpHeaders headers = jsonHeaders();
        if (aiProperties.getApiKey() != null && !aiProperties.getApiKey().isBlank()) {
            headers.setBearerAuth(aiProperties.getApiKey());
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", aiProperties.getModel());
        body.put("temperature", 0.6);
        body.put("messages", List.of(
                Map.of("role", "system", "content", "你是项目管理助手。只返回 JSON，不要输出 Markdown。JSON 必须包含 summary、riskTips、milestoneSuggestions、taskSuggestions 四个字段。taskSuggestions 为包含 title、description、priority 的数组，priority 只允许 1、2、3。"),
                Map.of("role", "user", "content", buildPrompt(request))
        ));

        ResponseEntity<Map> response = restTemplate.exchange(
                aiProperties.getBaseUrl() + "/chat/completions",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Map.class
        );
        if (response.getBody() == null) {
            throw new RestClientException("Empty model response");
        }
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RestClientException("No choices returned");
        }
        Map<String, Object> first = choices.get(0);
        Map<String, Object> message = (Map<String, Object>) first.get("message");
        return message == null ? "" : Objects.toString(message.get("content"), "");
    }

    private AiAssistResponse parseResponse(String rawContent, AiAssistRequest request, String provider) throws Exception {
        String cleanContent = stripCodeFence(rawContent);
        JsonNode root = objectMapper.readTree(cleanContent);
        AiAssistResponse response = new AiAssistResponse();
        response.setSummary(root.path("summary").asText(buildFallbackResponse(request).getSummary()));
        response.setRiskTips(readStringList(root.path("riskTips")));
        response.setMilestoneSuggestions(readStringList(root.path("milestoneSuggestions")));
        response.setTaskSuggestions(readTaskSuggestions(root.path("taskSuggestions")));
        response.setProvider(provider);
        response.setUsedFallback(false);

        if (response.getSummary() == null || response.getSummary().isBlank() || response.getTaskSuggestions().isEmpty()) {
            return buildFallbackResponse(request);
        }
        return response;
    }

    private List<String> readStringList(JsonNode node) {
        List<String> values = new ArrayList<>();
        if (node.isArray()) {
            node.forEach(item -> {
                String value = item.asText("").trim();
                if (!value.isEmpty()) {
                    values.add(value);
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
        response.setSummary("%s 目标是%s，建议先锁定 MVP 范围，再按迭代方式推进开发、联调与上线准备。"
                .formatted(safe(request.getName()), safeGoal(request.getGoal())));
        response.setRiskTips(buildRiskTips(request));
        response.setMilestoneSuggestions(List.of(
                "第 1 周：梳理需求、确认范围与角色分工",
                "第 2 周：完成核心流程与关键页面开发",
                "第 3 周：联调、自测与缺陷收敛",
                "第 4 周：上线准备、验收与复盘"
        ));
        response.setTaskSuggestions(List.of(
                new AiTaskSuggestion("梳理需求与验收标准", "冻结功能边界、页面范围和接口字段。", 1),
                new AiTaskSuggestion("完成核心流程开发", "优先实现最关键的业务路径和关键数据表单。", 1),
                new AiTaskSuggestion("安排联调与回归测试", "覆盖异常场景、权限边界和核心路径。", 2),
                new AiTaskSuggestion("整理上线清单与复盘项", "补齐部署说明、监控项和后续优化计划。", 3)
        ));
        response.setProvider("local-fallback");
        response.setUsedFallback(true);
        return response;
    }

    private List<String> buildRiskTips(AiAssistRequest request) {
        List<String> values = new ArrayList<>();
        values.add("尽早冻结 MVP 范围，避免开发阶段反复扩项。");
        if (request.getTeamSize() == null || request.getTeamSize() <= 2) {
            values.add("团队规模较小，建议减少并行任务并明确主责人。");
        } else {
            values.add("多人协作时需先约定接口字段和联调节奏。");
        }
        if (request.getDeadline() == null || request.getDeadline().isBlank()) {
            values.add("尚未提供截止日期，建议补充阶段节点以便跟踪风险。");
        } else {
            values.add("请在 %s 前预留完整测试和修复窗口。".formatted(request.getDeadline()));
        }
        return values;
    }

    private String buildPrompt(AiAssistRequest request) {
        return """
                你是资深项目经理。请根据以下项目输入，给出项目摘要、风险提示、里程碑建议和任务拆解。
                项目名称：%s
                项目描述：%s
                项目目标：%s
                团队人数：%s
                截止日期：%s
                """.formatted(
                safe(request.getName()),
                safe(request.getDescription()),
                safeGoal(request.getGoal()),
                request.getTeamSize() == null ? "未提供" : request.getTeamSize(),
                safe(request.getDeadline())
        );
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private int normalizePriority(int priority) {
        return priority < 1 || priority > 3 ? 2 : priority;
    }

    private String stripCodeFence(String content) {
        String trimmed = content == null ? "" : content.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(?:json)?", "").replaceFirst("```$", "").trim();
        }
        return trimmed;
    }

    private void validateRequest(AiAssistRequest request) {
        if (request == null || request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("项目名称不能为空");
        }
    }

    private String safe(Object value) {
        return value == null ? "未提供" : String.valueOf(value).trim();
    }

    private String safeGoal(String value) {
        return value == null || value.isBlank() ? "提升项目交付效率" : value.trim();
    }
}
