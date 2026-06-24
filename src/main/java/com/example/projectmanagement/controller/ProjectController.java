package com.example.projectmanagement.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.projectmanagement.entity.Project;
import com.example.projectmanagement.entity.User;
import com.example.projectmanagement.service.ProjectService;
import com.example.projectmanagement.service.TaskService;
import com.example.projectmanagement.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@Validated
public class ProjectController extends BaseController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ProjectService projectService;
    private final UserService userService;
    private final TaskService taskService;

    public ProjectController(ProjectService projectService, UserService userService, TaskService taskService) {
        this.projectService = projectService;
        this.userService = userService;
        this.taskService = taskService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ApiResponse<Map<String, Object>> createProject(@Valid @RequestBody Project project) {
        normalizeProject(project);
        if (project.getLeaderId() == null) {
            project.setLeaderId(getCurrentUserId());
        }
        if (project.getLeaderId() == null) {
            return error("负责人不能为空");
        }
        if (!projectService.createProject(project)) {
            return error("创建项目失败");
        }
        return success(toView(projectService.getById(project.getProjectId())));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER', 'TESTER')")
    public ApiResponse<List<Map<String, Object>>> getProjectList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "50") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        if (status != null && !List.of("planning", "developing", "testing", "deployed").contains(status)) {
            return error("状态只能选择：planning, developing, testing, deployed");
        }
        Page<Project> page = projectService.findPage(pageNum, pageSize, status, keyword);
        return success(page.getRecords().stream().map(this::toView).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER', 'TESTER')")
    public ApiResponse<Map<String, Object>> getProjectDetail(@PathVariable Long id) {
        Project project = projectService.getById(id);
        if (project == null) {
            return error(404, "项目不存在");
        }
        return success(toView(project));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ApiResponse<Map<String, Object>> updateProject(@PathVariable Long id, @Valid @RequestBody Project project) {
        project.setProjectId(id);
        normalizeProject(project);
        if (project.getLeaderId() == null) {
            Project existing = projectService.getById(id);
            if (existing != null) {
                project.setLeaderId(existing.getLeaderId());
            }
        }
        if (!projectService.updateProject(project, getCurrentUserId())) {
            return error("更新项目失败，您可能没有权限");
        }
        return success(toView(projectService.getById(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteProject(@PathVariable Long id) {
        if (!projectService.deleteProject(id, getCurrentUserId())) {
            return error("删除项目失败，您可能没有权限");
        }
        return success();
    }

    @PutMapping("/{id}/progress")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ApiResponse<Void> updateProgress(@PathVariable Long id, @RequestParam Integer progress) {
        if (!projectService.updateProgress(id, progress)) {
            return error("更新进度失败");
        }
        return success();
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER', 'TESTER')")
    public ApiResponse<Map<String, Object>> getDashboardStats() {
        List<Project> projects = projectService.list();
        long totalProjects = projects.size();
        long planningProjects = projects.stream().filter(project -> "planning".equals(project.getStatus())).count();
        long activeProjects = projects.stream().filter(project -> "developing".equals(project.getStatus())).count();
        long testingProjects = projects.stream().filter(project -> "testing".equals(project.getStatus())).count();
        long completedProjects = projects.stream().filter(project -> "deployed".equals(project.getStatus())).count();
        double averageProgress = totalProjects == 0 ? 0 : projects.stream()
                .map(Project::getProgress)
                .filter(progress -> progress != null)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);
        Map<String, Object> taskStats = taskService.buildStats(null);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalProjects", totalProjects);
        stats.put("planningProjects", planningProjects);
        stats.put("activeProjects", activeProjects);
        stats.put("testingProjects", testingProjects);
        stats.put("completedProjects", completedProjects);
        stats.put("registeredUsers", userService.list().size());
        stats.put("totalTasks", taskStats.get("total"));
        stats.put("todoTasks", taskStats.get("todo"));
        stats.put("inProgressTasks", taskStats.get("inProgress"));
        stats.put("doneTasks", taskStats.get("done"));
        stats.put("averageProgress", Math.round(averageProgress));
        return success(stats);
    }

    private Map<String, Object> toView(Project project) {
        User leader = userService.getById(project.getLeaderId());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", project.getProjectId());
        data.put("name", project.getName());
        data.put("description", project.getDescription() == null ? "" : project.getDescription());
        data.put("techStack", project.getTechStack());
        data.put("statusCode", project.getStatus());
        data.put("status", mapStatusLabel(project.getStatus()));
        data.put("progress", project.getProgress() == null ? 0 : project.getProgress());
        data.put("leaderId", project.getLeaderId());
        data.put("manager", leader == null ? "未分配" : displayName(leader));
        data.put("startDate", formatDate(project.getStartTime()));
        data.put("endDate", formatDate(project.getEndTime()));
        data.put("createdAt", formatDate(project.getCreateTime()));
        data.put("updatedAt", formatDate(project.getUpdateTime()));
        return data;
    }

    private void normalizeProject(Project project) {
        if (project.getStatus() == null || project.getStatus().isBlank()) {
            project.setStatus("planning");
        }
        if (project.getProgress() == null) {
            project.setProgress(0);
        }
        if (project.getTechStack() != null) {
            project.setTechStack(project.getTechStack().trim());
        }
    }

    private String mapStatusLabel(String status) {
        if (status == null) {
            return "待规划";
        }
        return switch (status) {
            case "planning" -> "待规划";
            case "developing" -> "进行中";
            case "testing" -> "待审核";
            case "deployed" -> "已完成";
            default -> status;
        };
    }

    private String displayName(User user) {
        return user.getNickname() == null || user.getNickname().isBlank() ? user.getUsername() : user.getNickname();
    }

    private String formatDate(LocalDateTime value) {
        return value == null ? "" : value.format(DATE_FORMATTER);
    }
}
