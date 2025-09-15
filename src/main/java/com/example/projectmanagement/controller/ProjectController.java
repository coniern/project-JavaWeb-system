package com.example.projectmanagement.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.projectmanagement.controller.BaseController.ApiResponse;
import com.example.projectmanagement.entity.Project;
import com.example.projectmanagement.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

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
    public ApiResponse<Page<Project>> getProjectList(
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
        return success(page);
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
    public ApiResponse<Project> getProjectDetail(@PathVariable Long id) {
        Project project = projectService.getById(id);
        if (project == null) {
            return error("项目不存在");
        }
        return success(project);
    }
    
    /**
     * 更新项目信息
     * @param id 项目ID
     * @param project 项目信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ApiResponse<Project> updateProject(@PathVariable Long id, @Valid @RequestBody Project project) {
        // 确保ID一致
        project.setProjectId(id);
        
        // 验证技术栈是否在允许的范围内
        List<String> allowedTechStacks = List.of("Spring Boot", "SSM", "Spring Cloud");
        if (!allowedTechStacks.contains(project.getTechStack())) {
            return error("技术栈只能选择：Spring Boot, SSM, Spring Cloud");
        }
        
        // 获取当前登录用户ID（这里假设从Security上下文中获取）
        Long currentUserId = 1L; // 临时值，实际应该从上下文中获取
        
        if (projectService.updateProject(project, currentUserId)) {
            return success(project);
        } else {
            return error("更新项目失败，您可能没有权限");
        }
    }
    
    /**
     * 删除项目
     * @param id 项目ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteProject(@PathVariable Long id) {
        // 获取当前登录用户ID（这里假设从Security上下文中获取）
        Long currentUserId = 1L; // 临时值，实际应该从上下文中获取
        
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
}