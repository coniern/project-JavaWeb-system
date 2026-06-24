package com.example.projectmanagement.controller;

import com.example.projectmanagement.controller.BaseController.ApiResponse;
import com.example.projectmanagement.dto.AiAssistRequest;
import com.example.projectmanagement.dto.AiAssistResponse;
import com.example.projectmanagement.entity.Project;
import com.example.projectmanagement.service.AiAssistantService;
import com.example.projectmanagement.service.AiInsightService;
import com.example.projectmanagement.service.ProjectService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController extends BaseController {

    private final AiInsightService aiInsightService;
    private final ProjectService projectService;
    private final AiAssistantService aiAssistantService;

    public AiController(AiInsightService aiInsightService, ProjectService projectService, AiAssistantService aiAssistantService) {
        this.aiInsightService = aiInsightService;
        this.projectService = projectService;
        this.aiAssistantService = aiAssistantService;
    }

    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> getAiStatus() {
        return success(aiInsightService.getAiStatus());
    }

    @GetMapping("/dashboard-insight")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER', 'TESTER')")
    public ApiResponse<Map<String, Object>> getDashboardInsight() {
        List<Project> projects = projectService.list();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("insight", aiInsightService.generateDashboardInsight(projects));
        result.put("projectCount", projects.size());
        return success(result);
    }

    @GetMapping("/projects/{id}/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER', 'TESTER')")
    public ApiResponse<Map<String, Object>> summarizeProject(@PathVariable Long id) {
        Project project = projectService.getById(id);
        if (project == null) {
            return error("项目不存在");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("projectId", id);
        result.put("summary", aiInsightService.summarizeProject(project));
        return success(result);
    }

    @GetMapping("/projects/{id}/risk")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER', 'TESTER')")
    public ApiResponse<Map<String, Object>> analyzeProjectRisk(@PathVariable Long id) {
        Project project = projectService.getById(id);
        if (project == null) {
            return error("项目不存在");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("projectId", id);
        result.put("riskAdvice", aiInsightService.analyzeProjectRisk(project));
        return success(result);
    }

    @PostMapping("/assist")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER', 'TESTER')")
    public ApiResponse<AiAssistResponse> assist(@RequestBody AiAssistRequest request) {
        return success(aiAssistantService.assist(request));
    }
}
