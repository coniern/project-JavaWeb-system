package com.example.projectmanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.projectmanagement.entity.EnvironmentConfig;

import java.util.List;

/**
 * 环境配置服务接口
 * 提供环境配置相关的业务逻辑
 */
public interface EnvironmentConfigService extends IService<EnvironmentConfig> {
    
    /**
     * 根据项目ID查询环境配置列表
     * @param projectId 项目ID
     * @return 环境配置列表
     */
    List<EnvironmentConfig> findByProjectId(Long projectId);
    
    /**
     * 根据项目ID和环境类型查询环境配置
     * @param projectId 项目ID
     * @param envType 环境类型
     * @return 环境配置信息
     */
    EnvironmentConfig findByProjectIdAndEnvType(Long projectId, String envType);
    
    /**
     * 创建环境配置
     * @param environmentConfig 环境配置信息
     * @param currentUserId 当前用户ID
     * @return 创建结果
     */
    boolean createEnvironmentConfig(EnvironmentConfig environmentConfig, Long currentUserId);
    
    /**
     * 更新环境配置
     * @param environmentConfig 环境配置信息
     * @param currentUserId 当前用户ID
     * @return 更新结果
     */
    boolean updateEnvironmentConfig(EnvironmentConfig environmentConfig, Long currentUserId);
    
    /**
     * 删除环境配置
     * @param id 环境配置ID
     * @param currentUserId 当前用户ID
     * @return 删除结果
     */
    boolean deleteEnvironmentConfig(Long id, Long currentUserId);
    
    /**
     * 根据环境类型查询启用的环境配置
     * @param envType 环境类型
     * @return 环境配置列表
     */
    List<EnvironmentConfig> findEnabledByEnvType(String envType);
    
    /**
     * 切换环境配置的启用状态
     * @param id 环境配置ID
     * @param enabled 是否启用
     * @param currentUserId 当前用户ID
     * @return 切换结果
     */
    boolean toggleEnvironmentConfig(Long id, boolean enabled, Long currentUserId);
}