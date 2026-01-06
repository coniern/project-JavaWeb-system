package com.example.projectmanagement.controller;

import com.example.projectmanagement.controller.BaseController.ApiResponse;
import com.example.projectmanagement.entity.ProjectMember;
import com.example.projectmanagement.service.ProjectMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 项目成员Controller
 * 处理项目成员相关的HTTP请求
 */
@RestController
@RequestMapping("/api/project-members")
public class ProjectMemberController extends BaseController {
    
    @Autowired
    private ProjectMemberService projectMemberService;
    
    /**
     * 获取项目成员列表
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER', 'TESTER')")
    public ApiResponse<List<ProjectMember>> getProjectMembers(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long userId) {
        
        List<ProjectMember> members;
        
        if (projectId != null) {
            // 按项目ID查询
            members = projectMemberService.findByProjectId(projectId);
        } else if (userId != null) {
            // 按用户ID查询
            members = projectMemberService.findByUserId(userId);
        } else {
            return error("请至少提供projectId或userId参数");
        }
        
        return success(members);
    }
    
    /**
     * 添加项目成员
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ApiResponse<Boolean> addProjectMember(@Valid @RequestBody ProjectMemberRequest request) {
        // 验证角色参数
        validateRole(request.getRole());
        
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return error("当前用户未登录");
        }
        
        // 添加成员
        boolean result = projectMemberService.addMember(
                request.getProjectId(), 
                request.getUserId(), 
                request.getRole(),
                currentUserId);
        
        if (!result) {
            return error("添加成员失败");
        }
        
        return success(true);
    }
    
    /**
     * 更新项目成员角色
     */
    @PutMapping("/{projectId}/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ApiResponse<Boolean> updateProjectMemberRole(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateRoleRequest request) {
        
        // 验证角色参数
        validateRole(request.getRole());
        
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return error("当前用户未登录");
        }
        
        // 更新角色
        boolean result = projectMemberService.updateMemberRole(
                projectId, 
                userId, 
                request.getRole(),
                currentUserId);
        
        if (!result) {
            return error("更新角色失败");
        }
        
        return success(true);
    }
    
    /**
     * 移除项目成员
     */
    @DeleteMapping("/{projectId}/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ApiResponse<Boolean> removeProjectMember(
            @PathVariable Long projectId,
            @PathVariable Long userId) {
        
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return error("当前用户未登录");
        }
        
        boolean result = projectMemberService.removeMember(projectId, userId, currentUserId);
        
        if (!result) {
            return error("移除成员失败");
        }
        
        return success(true);
    }
    
    /**
     * 获取用户在项目中的角色
     */
    @GetMapping("/role")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER', 'TESTER')")
    public ApiResponse<String> getUserRoleInProject(
            @RequestParam Long projectId,
            @RequestParam Long userId) {
        
        String role = projectMemberService.getUserRoleInProject(projectId, userId);
        
        return success(role);
    }
    
    /**
     * 验证用户是否为项目成员
     */
    @GetMapping("/check")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER', 'TESTER')")
    public ApiResponse<Boolean> checkProjectMember(
            @RequestParam Long projectId,
            @RequestParam Long userId) {
        
        boolean isMember = projectMemberService.isProjectMember(projectId, userId);
        
        return success(isMember);
    }
    
    /**
     * 验证角色参数
     */
    private void validateRole(String role) {
        if (!List.of("ADMIN", "DEVELOPER", "TESTER").contains(role)) {
            throw new IllegalArgumentException("角色只能选择：ADMIN, DEVELOPER, TESTER");
        }
    }
    
    // 添加项目成员请求参数类
    public static class ProjectMemberRequest {
        private Long projectId;
        private Long userId;
        private String role;
        
        // getter和setter
        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
    
    // 更新角色请求参数类
    public static class UpdateRoleRequest {
        private String role;
        
        // getter和setter
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}