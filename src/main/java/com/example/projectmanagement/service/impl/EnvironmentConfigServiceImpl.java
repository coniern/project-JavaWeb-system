package com.example.projectmanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.projectmanagement.entity.EnvironmentConfig;
import com.example.projectmanagement.mapper.EnvironmentConfigMapper;
import com.example.projectmanagement.service.EnvironmentConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 环境配置服务实现类
 * 实现环境配置相关的业务逻辑
 */
@Service
public class EnvironmentConfigServiceImpl extends ServiceImpl<EnvironmentConfigMapper, EnvironmentConfig> implements EnvironmentConfigService {
    
    @Autowired
    private EnvironmentConfigMapper environmentConfigMapper;
    
    @Override
    public List<EnvironmentConfig> findByProjectId(Long projectId) {
        return environmentConfigMapper.findByProjectId(projectId);
    }
    
    @Override
    public EnvironmentConfig findByProjectIdAndEnvType(Long projectId, String envType) {
        return environmentConfigMapper.findByProjectIdAndEnvType(projectId, envType);
    }
    
    @Override
    public boolean createEnvironmentConfig(EnvironmentConfig environmentConfig, Long currentUserId) {
        // 验证环境类型是否在允许的范围内
        List<String> allowedEnvTypes = List.of("development", "testing", "production");
        if (!allowedEnvTypes.contains(environmentConfig.getEnvType())) {
            return false;
        }
        
        // 检查是否已经存在相同项目和环境类型的配置
        EnvironmentConfig existingConfig = findByProjectIdAndEnvType(environmentConfig.getProjectId(), environmentConfig.getEnvType());
        if (existingConfig != null) {
            return false;
        }
        
        // 可以在这里添加权限验证，确保只有项目负责人和系统管理员才能创建环境配置
        
        // 设置创建和更新时间
        environmentConfig.setCreateTime(LocalDateTime.now());
        environmentConfig.setUpdateTime(LocalDateTime.now());
        environmentConfig.setEnabled(true); // 默认启用
        
        // 加密存储敏感信息（如SSH密码）
        // 这里可以添加加密逻辑
        
        return save(environmentConfig);
    }
    
    @Override
    public boolean updateEnvironmentConfig(EnvironmentConfig environmentConfig, Long currentUserId) {
        // 验证环境类型是否在允许的范围内
        List<String> allowedEnvTypes = List.of("development", "testing", "production");
        if (!allowedEnvTypes.contains(environmentConfig.getEnvType())) {
            return false;
        }
        
        // 检查环境配置是否存在
        EnvironmentConfig existingConfig = getById(environmentConfig.getId());
        if (existingConfig == null) {
            return false;
        }
        
        // 可以在这里添加权限验证，确保只有项目负责人和系统管理员才能更新环境配置
        
        // 设置更新时间
        environmentConfig.setUpdateTime(LocalDateTime.now());
        
        // 加密存储敏感信息（如SSH密码）
        // 这里可以添加加密逻辑
        
        return updateById(environmentConfig);
    }
    
    @Override
    public boolean deleteEnvironmentConfig(Long id, Long currentUserId) {
        // 检查环境配置是否存在
        EnvironmentConfig existingConfig = getById(id);
        if (existingConfig == null) {
            return false;
        }
        
        // 可以在这里添加权限验证，确保只有项目负责人和系统管理员才能删除环境配置
        
        return removeById(id);
    }
    
    @Override
    public List<EnvironmentConfig> findEnabledByEnvType(String envType) {
        return environmentConfigMapper.findEnabledByEnvType(envType);
    }
    
    @Override
    public boolean toggleEnvironmentConfig(Long id, boolean enabled, Long currentUserId) {
        // 检查环境配置是否存在
        EnvironmentConfig existingConfig = getById(id);
        if (existingConfig == null) {
            return false;
        }
        
        // 可以在这里添加权限验证，确保只有项目负责人和系统管理员才能切换环境配置状态
        
        // 更新启用状态
        existingConfig.setEnabled(enabled);
        existingConfig.setUpdateTime(LocalDateTime.now());
        
        return updateById(existingConfig);
    }
}