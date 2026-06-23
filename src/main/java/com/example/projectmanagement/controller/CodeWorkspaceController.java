package com.example.projectmanagement.controller;

import com.example.projectmanagement.controller.BaseController.ApiResponse;
import com.example.projectmanagement.service.AiInsightService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/workspace")
public class CodeWorkspaceController extends BaseController {

    private final AiInsightService aiInsightService;
    private final Path projectRoot = Path.of("/Users/mac/Desktop/毕业论文/project-JavaWeb-system");

    public CodeWorkspaceController(AiInsightService aiInsightService) {
        this.aiInsightService = aiInsightService;
    }

    @GetMapping("/files")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER', 'TESTER')")
    public ApiResponse<List<String>> listFiles(@RequestParam(defaultValue = "src") String scope) throws IOException {
        Path base = resolveScope(scope);
        List<String> files = Files.walk(base)
                .filter(Files::isRegularFile)
                .map(path -> projectRoot.relativize(path).toString())
                .filter(path -> path.endsWith(".java") || path.endsWith(".html") || path.endsWith(".jsp") || path.endsWith(".yml") || path.endsWith(".sql"))
                .sorted()
                .collect(Collectors.toList());
        return success(files);
    }

    @GetMapping("/file-content")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER', 'TESTER')")
    public ApiResponse<Map<String, Object>> getFileContent(@RequestParam String path) throws IOException {
        Path target = normalizeInsideProject(path);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("path", path);
        result.put("content", Files.readString(target, StandardCharsets.UTF_8));
        return success(result);
    }

    @GetMapping("/previews")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER', 'TESTER')")
    public ApiResponse<List<Map<String, String>>> listPreviewPages() {
        List<Map<String, String>> previews = List.of(
                Map.of("name", "首页", "url", "/project-management/index.html"),
                Map.of("name", "登录页", "url", "/project-management/login.html"),
                Map.of("name", "注册页", "url", "/project-management/register.html"),
                Map.of("name", "项目管理页", "url", "/project-management/projects.html")
        );
        return success(previews);
    }

    @PostMapping("/ai-complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER', 'TESTER')")
    public ApiResponse<Map<String, Object>> completeCode(@RequestBody CodeCompletionRequest request) {
        String completion = aiInsightService.completeCode(request.getLanguage(), request.getPrompt(), request.getCodeContext());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("language", request.getLanguage());
        result.put("prompt", request.getPrompt());
        result.put("completion", completion);
        return success(result);
    }

    private Path resolveScope(String scope) {
        return switch (scope) {
            case "web" -> projectRoot.resolve("web");
            case "resources" -> projectRoot.resolve("src/main/resources");
            default -> projectRoot.resolve("src/main/java");
        };
    }

    private Path normalizeInsideProject(String relativePath) {
        Path normalized = projectRoot.resolve(relativePath).normalize();
        if (!normalized.startsWith(projectRoot)) {
            throw new IllegalArgumentException("非法路径");
        }
        return normalized;
    }

    public static class CodeCompletionRequest {
        private String language;
        private String prompt;
        private String codeContext;

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }

        public String getCodeContext() {
            return codeContext;
        }

        public void setCodeContext(String codeContext) {
            this.codeContext = codeContext;
        }
    }
}
