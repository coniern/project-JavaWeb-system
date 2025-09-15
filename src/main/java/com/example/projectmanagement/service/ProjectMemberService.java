package com.example.projectmanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.projectmanagement.entity.ProjectMember;

import java.util.List;

/**
 * 项目成员服务接口
 * 提供项目成员相关的业务逻辑
 */
public interface ProjectMemberService extends IService<ProjectMember> {
    
    /**
     * 根据项目ID查询项目成员列表
     * @param projectId 项目ID
     * @return 项目成员列表
     */
    List<ProjectMember> findByProjectId(Long projectId);
    
    /**
     * 根据用户ID查询项目成员列表
     * @param userId 用户ID
     * @return 项目成员列表
     */
    List<ProjectMember> findByUserId(Long userId);
    
    /**
     * 添加项目成员
     * @param projectId 项目ID
     * @param userId 用户ID
     * @param role 角色
     * @param currentUserId 当前用户ID
     * @return 添加结果
     */
    boolean addMember(Long projectId, Long userId, String role, Long currentUserId);
    
    /**
     * 更新项目成员角色
     * @param projectId 项目ID
     * @param userId 用户ID
     * @param role 角色
     * @param currentUserId 当前用户ID
     * @return 更新结果
     */
    boolean updateMemberRole(Long projectId, Long userId, String role, Long currentUserId);
    
    /**
     * 删除项目成员
     * @param projectId 项目ID
     * @param userId 用户ID
     * @param currentUserId 当前用户ID
     * @return 删除结果
     */
    boolean removeMember(Long projectId, Long userId, Long currentUserId);
    
    /**
     * 获取用户在项目中的角色
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return 用户角色
     */
    String getUserRoleInProject(Long projectId, Long userId);
    
    /**
     * 检查用户是否为项目成员
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return 是否为项目成员
     */
    boolean isProjectMember(Long projectId, Long userId);
}