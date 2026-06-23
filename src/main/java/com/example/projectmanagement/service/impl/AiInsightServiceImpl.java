package com.example.projectmanagement.service.impl;

import com.example.projectmanagement.config.AiProperties;
import com.example.projectmanagement.entity.Project;
import com.example.projectmanagement.service.AiInsightService;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AiInsightServiceImpl implements AiInsightService {

    private final AiProperties aiProperties;
    private final RestTemplate restTemplate;

    public AiInsightServiceImpl(AiProperties aiProperties, RestTemplateBuilder restTemplateBuilder) {
        this.aiProperties = aiProperties;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(aiProperties.getTimeoutSeconds()))
                .setReadTimeout(Duration.ofSeconds(aiProperties.getTimeoutSeconds()))
                .build();
    }

    @Override
    public String summarizeProject(Project project) {
        String prompt = """
                你是资深项目经理，请基于以下项目信息，用中文输出一段 120 字以内的项目总结，突出项目目标、当前阶段和下一步重点。
                项目名称：%s
                项目描述：%s
                技术栈：%s
                状态：%s
                进度：%s%%
                """.formatted(
                safe(project.getName()),
                safe(project.getDescription()),
                safe(project.getTechStack()),
                safe(project.getStatus()),
                safe(project.getProgress())
        );
        return askModel(prompt, fallbackSummary(project));
    }

    @Override
    public String analyzeProjectRisk(Project project) {
        String prompt = """
                你是软件项目风险顾问。请基于以下项目信息，用中文输出：
                1. 2 条主要风险
                2. 2 条可执行建议
                每条简洁，不超过 25 字。
                项目名称：%s
                项目描述：%s
                技术栈：%s
                状态：%s
                进度：%s%%
                """.formatted(
                safe(project.getName()),
                safe(project.getDescription()),
                safe(project.getTechStack()),
                safe(project.getStatus()),
                safe(project.getProgress())
        );
        return askModel(prompt, fallbackRisk(project));
    }

    @Override
    public String generateDashboardInsight(List<Project> projects) {
        String projectDigest = projects.stream()
                .map(project -> "%s(%s,%s%%)".formatted(project.getName(), safe(project.getStatus()), safe(project.getProgress())))
                .collect(Collectors.joining("；"));
        String prompt = """
                你是项目管理平台的 AI 助手。请根据以下项目概况，用中文输出一段 150 字以内的仪表盘洞察，包含：
                1. 总体进展判断
                2. 哪类项目最值得关注
                3. 一条管理建议
                项目概况：%s
                """.formatted(projectDigest);
        return askModel(prompt, fallbackDashboard(projects));
    }

    @Override
    public String completeCode(String language, String prompt, String codeContext) {
        String finalPrompt = """
                你是资深 %s 开发助手。请基于用户要求补全或改写代码，只返回代码或高质量实现片段，不要解释。
                用户要求：
                %s

                当前代码上下文：
                %s
                """.formatted(language == null ? "Java" : language, safe(prompt), safe(codeContext));
        return askModel(finalPrompt, fallbackCodeCompletion(language, prompt, codeContext));
    }

    @Override
    public Map<String, Object> getAiStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("enabled", aiProperties.isEnabled());
        status.put("provider", aiProperties.getProvider());
        status.put("model", aiProperties.getModel());
        status.put("baseUrl", aiProperties.getBaseUrl());
        status.put("fallbackEnabled", aiProperties.isUseFallback());
        status.put("connected", pingProvider());
        return status;
    }

    private String askModel(String prompt, String fallback) {
        if (!aiProperties.isEnabled()) {
            return fallback;
        }
        try {
            if ("ollama".equalsIgnoreCase(aiProperties.getProvider())) {
                return callOllama(prompt);
            }
            if ("openai-compatible".equalsIgnoreCase(aiProperties.getProvider())) {
                return callOpenAiCompatible(prompt);
            }
        } catch (Exception ignored) {
        }
        return aiProperties.isUseFallback() ? fallback : "AI 服务暂不可用。";
    }

    private String callOllama(String prompt) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", aiProperties.getModel());
        body.put("prompt", prompt);
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
        Object answer = response.getBody().get("response");
        return answer == null ? "" : answer.toString().trim();
    }

    private String callOpenAiCompatible(String prompt) {
        HttpHeaders headers = jsonHeaders();
        if (aiProperties.getApiKey() != null && !aiProperties.getApiKey().isBlank()) {
            headers.setBearerAuth(aiProperties.getApiKey());
        }

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", aiProperties.getModel());
        body.put("messages", List.of(message));
        body.put("temperature", 0.4);

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
        Map<String, Object> messageResp = (Map<String, Object>) first.get("message");
        return messageResp == null ? "" : Objects.toString(messageResp.get("content"), "").trim();
    }

    private boolean pingProvider() {
        if (!aiProperties.isEnabled()) {
            return false;
        }
        try {
            if ("ollama".equalsIgnoreCase(aiProperties.getProvider())) {
                ResponseEntity<String> response = restTemplate.getForEntity(aiProperties.getBaseUrl().replace("/api", "") + "/api/tags", String.class);
                return response.getStatusCode().is2xxSuccessful();
            }
            if ("openai-compatible".equalsIgnoreCase(aiProperties.getProvider())) {
                return aiProperties.getApiKey() != null && !aiProperties.getApiKey().isBlank();
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String fallbackSummary(Project project) {
        return "项目“%s”当前处于%s阶段，技术栈为%s，整体进度约%s%%。下一步建议聚焦关键任务推进和阶段结果验收。"
                .formatted(safe(project.getName()), mapStatus(project.getStatus()), safe(project.getTechStack()), safe(project.getProgress()));
    }

    private String fallbackRisk(Project project) {
        String status = safe(project.getStatus());
        if ("planning".equals(status)) {
            return "风险1：需求边界不清。\n风险2：负责人投入不足。\n建议1：尽快拆解里程碑。\n建议2：先确认资源与排期。";
        }
        if ("testing".equals(status)) {
            return "风险1：缺陷收敛速度慢。\n风险2：上线窗口受压缩。\n建议1：按优先级关闭缺陷。\n建议2：提前准备回滚方案。";
        }
        if ("developing".equals(status)) {
            return "风险1：开发任务堆积。\n风险2：联调阻塞进度。\n建议1：每日跟踪关键路径。\n建议2：提前对接接口依赖。";
        }
        return "风险1：上线后稳定性观察不足。\n风险2：经验复盘沉淀不够。\n建议1：安排上线巡检。\n建议2：补充复盘记录。";
    }

    private String fallbackDashboard(List<Project> projects) {
        long developing = projects.stream().filter(project -> "developing".equals(project.getStatus())).count();
        long testing = projects.stream().filter(project -> "testing".equals(project.getStatus())).count();
        return "当前项目整体处于稳步推进状态，其中开发中项目 %s 个、待测试项目 %s 个。建议优先关注测试阶段项目的缺陷收敛与上线节奏，避免后期集中风险。"
                .formatted(developing, testing);
    }

    private String fallbackCodeCompletion(String language, String prompt, String codeContext) {
        if (language != null && language.toLowerCase().contains("java")) {
            return """
                    // AI 回退补全建议
                    // 目标：%s
                    // 建议先补充参数校验、核心业务逻辑和异常处理
                    %s
                    """.formatted(safe(prompt), safe(codeContext));
        }
        return """
                // AI 回退补全建议
                // 目标：%s
                %s
                """.formatted(safe(prompt), safe(codeContext));
    }

    private String mapStatus(String status) {
        return switch (status) {
            case "planning" -> "规划";
            case "developing" -> "开发";
            case "testing" -> "测试";
            case "deployed" -> "已部署";
            default -> "未知";
        };
    }

    private String safe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
