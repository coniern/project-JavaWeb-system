package com.example.projectmanagement.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.projectmanagement.controller.BaseController.ApiResponse;
import com.example.projectmanagement.entity.Project;
import com.example.projectmanagement.entity.User;
import com.example.projectmanagement.service.ProjectService;
import com.example.projectmanagement.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

/**
 * 项目Controller
 * 处理项目相关的HTTP请求
 */
@RestController
@RequestMapping("/api/projects")
@Validated
public class ProjectController extends BaseController {
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 创建项目
     * @param project 项目信息
     * @return 创建结果
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ApiResponse<Project> createProject(@Valid @RequestBody Project project) {
        // 验证技术栈是否在允许的范围内
        List<String> allowedTechStacks = List.of("Spring Boot", "SSM", "Spring Cloud");
        if (!allowedTechStacks.contains(project.getTechStack())) {
            return error("技术栈只能选择：Spring Boot, SSM, Spring Cloud");
        }
        if (project.getLeaderId() == null) {
            Long currentUserId = getCurrentUserId();
            if (currentUserId != null) {
                project.setLeaderId(currentUserId);
            }
        }
        if (project.getLeaderId() == null) {
            return error("负责人ID不能为空");
        }
        
        if (projectService.createProject(project)) {
            return success(project);
        } else {
            return error("创建项目失败");
        }
    }
    
    /**
     * 获取项目列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param status 项目状态（可选）
     * @param keyword 搜索关键词（可选）
     * @return 项目分页列表
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER', 'TESTER')")
    public ApiResponse<List<Map<String, Object>>> getProjectList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        
        // 验证状态参数
        if (status != null) {
            List<String> allowedStatuses = List.of("planning", "developing", "testing", "deployed");
            if (!allowedStatuses.contains(status)) {
                return error("状态只能选择：planning, developing, testing, deployed");
            }
        }
        
        Page<Project> page = projectService.findPage(pageNum, pageSize, status, keyword);
        List<Map<String, Object>> projects = page.getRecords().stream().map(project -> {
            User leader = userService.getById(project.getLeaderId());
            return Map.<String, Object>of(
                    "id", project.getProjectId(),
                    "name", project.getName(),
                    "description", project.getDescription() == null ? "" : project.getDescription(),
                    "statusCode", project.getStatus() == null ? "" : project.getStatus(),
                    "status", mapStatusLabel(project.getStatus()),
                    "createdAt", project.getCreateTime() == null ? "" : project.getCreateTime().toLocalDate().toString(),
                    "manager", leader == null ? "未分配" : (leader.getNickname() == null ? leader.getUsername() : leader.getNickname()),
                    "progress", project.getProgress() == null ? 0 : project.getProgress(),
                    "techStack", project.getTechStack(),
                    "leaderId", project.getLeaderId()
            );
        }).collect(Collectors.toList());
        return success(projects);
    }
    
    /**
     * 根据状态筛选项目
     * @param status 项目状态
     * @return 项目列表
     */
    @GetMapping("/by-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER', 'TESTER')")
    public ApiResponse<List<Project>> getProjectsByStatus(@RequestParam String status) {
        // 验证状态参数
        List<String> allowedStatuses = List.of("planning", "developing", "testing", "deployed");
        if (!allowedStatuses.contains(status)) {
            return error("状态只能选择：planning, developing, testing, deployed");
        }
        
        List<Project> projects = projectService.findByStatus(status);
        return success(projects);
    }
    
    /**
     * 获取项目详情
     * @param id 项目ID
     * @return 项目详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER', 'TESTER')")
    public ApiResponse<Map<String, Object>> getProjectDetail(@PathVariable Long id) {
        Project project = projectService.getById(id);
        if (project == null) {
            return error("项目不存在");
        }
        User leader = userService.getById(project.getLeaderId());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", project.getProjectId());
        data.put("name", project.getName());
        data.put("description", project.getDescription());
        data.put("techStack", project.getTechStack());
        data.put("status", project.getStatus());
        data.put("statusLabel", mapStatusLabel(project.getStatus()));
        data.put("progress", project.getProgress());
        data.put("leaderId", project.getLeaderId());
        data.put("manager", leader == null ? "未分配" : (leader.getNickname() == null ? leader.getUsername() : leader.getNickname()));
        data.put("createdAt", project.getCreateTime() == null ? "" : project.getCreateTime().toLocalDate().toString());
        data.put("updatedAt", project.getUpdateTime() == null ? "" : project.getUpdateTime().toLocalDate().toString());
        return success(data);
    }
    
    /**
     * 更新项目信息
     * @param id 项目ID
     * @param project 项目信息
     * @param authentication 认证信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ApiResponse<Project> updateProject(@PathVariable Long id, @Valid @RequestBody Project project, Authentication authentication) {
        // 确保ID一致
        project.setProjectId(id);
        
        // 验证技术栈是否在允许的范围内
        List<String> allowedTechStacks = List.of("Spring Boot", "SSM", "Spring Cloud");
        if (!allowedTechStacks.contains(project.getTechStack())) {
            return error("技术栈只能选择：Spring Boot, SSM, Spring Cloud");
        }
        
        // 从Security上下文获取当前登录用户ID
        Long currentUserId = getCurrentUserId();
        
        if (project.getLeaderId() == null) {
            project.setLeaderId(currentUserId);
        }
        if (projectService.updateProject(project, currentUserId)) {
            return success(project);
        } else {
            return error("更新项目失败，您可能没有权限");
        }
    }
    
    /**
     * 删除项目
     * @param id 项目ID
     * @param authentication 认证信息
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteProject(@PathVariable Long id, Authentication authentication) {
        // 从Security上下文获取当前登录用户ID
        Long currentUserId = getCurrentUserId();
        
        if (projectService.deleteProject(id, currentUserId)) {
            return success();
        } else {
            return error("删除项目失败，您可能没有权限");
        }
    }
    
    /**
     * 更新项目进度
     * @param id 项目ID
     * @param progress 进度百分比
     * @return 更新结果
     */
    @PutMapping("/{id}/progress")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ApiResponse<Void> updateProgress(@PathVariable Long id, @RequestParam Integer progress) {
        if (projectService.updateProgress(id, progress)) {
            return success();
        } else {
            return error("更新进度失败");
        }
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER', 'TESTER')")
    public ApiResponse<Map<String, Object>> getDashboardStats() {
        List<Project> projects = projectService.list();
        long totalProjects = projects.size();
        long activeProjects = projects.stream().filter(project -> "developing".equals(project.getStatus())).count();
        long testingProjects = projects.stream().filter(project -> "testing".equals(project.getStatus())).count();
        long completedProjects = projects.stream().filter(project -> "deployed".equals(project.getStatus())).count();
        double averageProgress = totalProjects == 0 ? 0 : projects.stream()
                .map(Project::getProgress)
                .filter(progress -> progress != null)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalProjects", totalProjects);
        stats.put("activeProjects", activeProjects);
        stats.put("testingProjects", testingProjects);
        stats.put("completedProjects", completedProjects);
        stats.put("registeredUsers", userService.list().size());
        stats.put("onTimeRate", 85);
        stats.put("satisfactionRate", 98);
        stats.put("averageProgress", Math.round(averageProgress));
        stats.put("deploymentSuccessRateTrend", List.of(92, 95, 94, 96, 98, 99));
        stats.put("deploymentTimeTrend", List.of(15, 12, 10, 8, 6, 5));
        return success(stats);
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
}
