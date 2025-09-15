package com.example.projectmanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.projectmanagement.entity.ProjectMember;
import com.example.projectmanagement.mapper.ProjectMemberMapper;
import com.example.projectmanagement.service.ProjectMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目成员服务实现类
 * 实现项目成员相关的业务逻辑
 */
@Service
public class ProjectMemberServiceImpl extends ServiceImpl<ProjectMemberMapper, ProjectMember> implements ProjectMemberService {
    
    @Autowired
    private ProjectMemberMapper projectMemberMapper;
    
    @Override
    public List<ProjectMember> findByProjectId(Long projectId) {
        return projectMemberMapper.findByProjectId(projectId);
    }
    
    @Override
    public List<ProjectMember> findByUserId(Long userId) {
        return projectMemberMapper.findByUserId(userId);
    }
    
    @Override
    public boolean addMember(Long projectId, Long userId, String role, Long currentUserId) {
        // 验证角色是否在允许的范围内
        List<String> allowedRoles = List.of("ADMIN", "DEVELOPER", "TESTER");
        if (!allowedRoles.contains(role)) {
            return false;
        }
        
        // 检查用户是否已经是项目成员
        ProjectMember existingMember = projectMemberMapper.findByProjectIdAndUserId(projectId, userId);
        if (existingMember != null) {
            return false;
        }
        
        // 创建项目成员
        ProjectMember projectMember = new ProjectMember();
        projectMember.setProjectId(projectId);
        projectMember.setUserId(userId);
        projectMember.setRole(role);
        projectMember.setCreateTime(LocalDateTime.now());
        projectMember.setUpdateTime(LocalDateTime.now());
        
        return save(projectMember);
    }
    
    @Override
    public boolean updateMemberRole(Long projectId, Long userId, String role, Long currentUserId) {
        // 验证角色是否在允许的范围内
        List<String> allowedRoles = List.of("ADMIN", "DEVELOPER", "TESTER");
        if (!allowedRoles.contains(role)) {
            return false;
        }
        
        // 检查用户是否是项目成员
        ProjectMember existingMember = projectMemberMapper.findByProjectIdAndUserId(projectId, userId);
        if (existingMember == null) {
            return false;
        }
        
        // 更新角色
        existingMember.setRole(role);
        existingMember.setUpdateTime(LocalDateTime.now());
        
        return updateById(existingMember);
    }
    
    @Override
    public boolean removeMember(Long projectId, Long userId, Long currentUserId) {
        // 检查用户是否是项目成员
        ProjectMember existingMember = projectMemberMapper.findByProjectIdAndUserId(projectId, userId);
        if (existingMember == null) {
            return false;
        }
        
        return removeById(existingMember.getId());
    }
    
    @Override
    public String getUserRoleInProject(Long projectId, Long userId) {
        ProjectMember projectMember = projectMemberMapper.findByProjectIdAndUserId(projectId, userId);
        return projectMember != null ? projectMember.getRole() : null;
    }
    
    @Override
    public boolean isProjectMember(Long projectId, Long userId) {
        return projectMemberMapper.findByProjectIdAndUserId(projectId, userId) != null;
    }
}