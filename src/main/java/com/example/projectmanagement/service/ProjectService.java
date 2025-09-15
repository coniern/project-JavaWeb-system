package com.example.projectmanagement.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.projectmanagement.entity.Project;

import java.util.List;

/**
 * 项目服务接口
 * 提供项目相关的业务逻辑
 */
public interface ProjectService extends IService<Project> {
    
    /**
     * 分页查询项目列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param status 项目状态
     * @param keyword 搜索关键词
     * @return 项目分页列表
     */
    Page<Project> findPage(Integer pageNum, Integer pageSize, String status, String keyword);
    
    /**
     * 创建项目
     * @param project 项目信息
     * @return 创建结果
     */
    boolean createProject(Project project);
    
    /**
     * 更新项目信息
     * @param project 项目信息
     * @param currentUserId 当前用户ID
     * @return 更新结果
     */
    boolean updateProject(Project project, Long currentUserId);
    
    /**
     * 根据状态筛选项目
     * @param status 项目状态
     * @return 项目列表
     */
    List<Project> findByStatus(String status);
    
    /**
     * 根据负责人ID查询项目
     * @param leaderId 负责人ID
     * @return 项目列表
     */
    List<Project> findByLeaderId(Long leaderId);
    
    /**
     * 根据技术栈查询项目
     * @param techStack 技术栈
     * @return 项目列表
     */
    List<Project> findByTechStack(String techStack);
    
    /**
     * 删除项目
     * @param projectId 项目ID
     * @param currentUserId 当前用户ID
     * @return 删除结果
     */
    boolean deleteProject(Long projectId, Long currentUserId);
    
    /**
     * 更新项目进度
     * @param projectId 项目ID
     * @param progress 进度百分比
     * @return 更新结果
     */
    boolean updateProgress(Long projectId, Integer progress);
}