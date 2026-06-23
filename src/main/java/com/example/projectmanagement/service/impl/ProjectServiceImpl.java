package com.example.projectmanagement.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.projectmanagement.entity.Project;
import com.example.projectmanagement.mapper.ProjectMapper;
import com.example.projectmanagement.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目服务实现类
 * 实现项目相关的业务逻辑
 */
@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {
    
    @Autowired
    private ProjectMapper projectMapper;
    
    @Override
    public Page<Project> findPage(Integer pageNum, Integer pageSize, String status, String keyword) {
        Page<Project> page = new Page<>(pageNum, pageSize);
        return lambdaQuery()
                .eq(status != null && !status.isBlank(), Project::getStatus, status)
                .and(keyword != null && !keyword.isBlank(), wrapper ->
                        wrapper.like(Project::getName, keyword).or().like(Project::getDescription, keyword))
                .orderByDesc(Project::getUpdateTime)
                .page(page);
    }
    
    @Override
    public boolean createProject(Project project) {
        // 验证技术栈是否在允许的范围内
        List<String> allowedTechStacks = List.of("Spring Boot", "SSM", "Spring Cloud");
        if (!allowedTechStacks.contains(project.getTechStack())) {
            return false;
        }
        
        project.setCreateTime(LocalDateTime.now());
        project.setUpdateTime(LocalDateTime.now());
        if (project.getProgress() == null) {
            project.setProgress(0);
        }
        if (project.getStatus() == null || project.getStatus().isBlank()) {
            project.setStatus("planning");
        }
        
        return save(project);
    }
    
    @Override
    public boolean updateProject(Project project, Long currentUserId) {
        // 检查是否有权限更新项目
        Project existingProject = getById(project.getProjectId());
        if (existingProject == null) {
            return false;
        }
        
        // 只有负责人和系统管理员才能更新项目
        // 这里可以根据实际的权限系统进行调整
        boolean hasPermission = hasAdminRole() || existingProject.getLeaderId().equals(currentUserId);
        if (!hasPermission) {
            return false;
        }
        
        // 验证技术栈是否在允许的范围内
        List<String> allowedTechStacks = List.of("Spring Boot", "SSM", "Spring Cloud");
        if (!allowedTechStacks.contains(project.getTechStack())) {
            return false;
        }
        
        project.setUpdateTime(LocalDateTime.now());
        return updateById(project);
    }
    
    @Override
    public List<Project> findByStatus(String status) {
        return lambdaQuery().eq(Project::getStatus, status).list();
    }
    
    @Override
    public List<Project> findByLeaderId(Long leaderId) {
        return lambdaQuery().eq(Project::getLeaderId, leaderId).list();
    }
    
    @Override
    public List<Project> findByTechStack(String techStack) {
        return lambdaQuery().eq(Project::getTechStack, techStack).list();
    }
    
    @Override
    public boolean deleteProject(Long projectId, Long currentUserId) {
        // 检查是否有权限删除项目
        Project project = getById(projectId);
        if (project == null) {
            return false;
        }
        
        // 只有负责人和系统管理员才能删除项目
        boolean hasPermission = hasAdminRole() || project.getLeaderId().equals(currentUserId);
        if (!hasPermission) {
            return false;
        }
        
        return removeById(projectId);
    }
    
    @Override
    public boolean updateProgress(Long projectId, Integer progress) {
        // 验证进度值是否在有效范围内
        if (progress < 0 || progress > 100) {
            return false;
        }
        
        Project project = new Project();
        project.setProjectId(projectId);
        project.setProgress(progress);
        project.setUpdateTime(LocalDateTime.now());
        
        return updateById(project);
    }

    private boolean hasAdminRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}
