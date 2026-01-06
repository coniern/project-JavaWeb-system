package com.example.projectmanagement.controller;

import com.example.projectmanagement.controller.BaseController.ApiResponse;
import com.example.projectmanagement.entity.EnvironmentConfig;
import com.example.projectmanagement.service.EnvironmentConfigService;
import com.example.projectmanagement.utils.LogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 环境配置Controller
 * 处理环境配置相关的HTTP请求
 */
@RestController
@RequestMapping("/api/environment-configs")
public class EnvironmentConfigController extends BaseController {
    
    @Autowired
    private EnvironmentConfigService environmentConfigService;
    
    @Autowired
    private LogUtils logUtils;
    
    /**
     * 获取环境配置列表
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ApiResponse<List<EnvironmentConfig>> getEnvironmentConfigs(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String envType) {
        
        List<EnvironmentConfig> configs;
        
        if (projectId != null) {
            // 按项目ID查询
            configs = environmentConfigService.findByProjectId(projectId);
        } else if (envType != null) {
            // 按环境类型查询已启用的配置
            validateEnvType(envType);
            configs = environmentConfigService.findEnabledByEnvType(envType);
        } else {
            // 查询所有配置
            configs = environmentConfigService.list();
        }
        
        return success(configs);
    }
    
    /**
     * 获取环境配置详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ApiResponse<EnvironmentConfig> getEnvironmentConfig(@PathVariable Long id) {
        
        EnvironmentConfig config = environmentConfigService.getById(id);
        if (config == null) {
            return error("环境配置不存在");
        }
        
        return success(config);
    }
    
    /**
     * 创建环境配置
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ApiResponse<Boolean> createEnvironmentConfig(@Valid @RequestBody EnvironmentConfig config) {
        
        // 验证环境类型
        validateEnvType(config.getEnvType());
        
        // 获取当前登录用户ID
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return error("当前用户未登录");
        }
        
        boolean result = environmentConfigService.createEnvironmentConfig(config, currentUserId);
        
        if (!result) {
            return error("创建环境配置失败");
        }
        
        return success(true);
    }
    
    /**
     * 更新环境配置
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ApiResponse<Boolean> updateEnvironmentConfig(
            @PathVariable Long id,
            @Valid @RequestBody EnvironmentConfig config) {
        
        // 设置ID
        config.setId(id);
        
        // 验证环境类型
        validateEnvType(config.getEnvType());
        
        // 获取当前登录用户ID
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return error("当前用户未登录");
        }
        
        boolean result = environmentConfigService.updateEnvironmentConfig(config, currentUserId);
        
        if (!result) {
            return error("更新环境配置失败");
        }
        
        return success(true);
    }
    
    /**
     * 删除环境配置
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ApiResponse<Boolean> deleteEnvironmentConfig(
            @PathVariable Long id) {
        
        // 获取当前登录用户ID
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return error("当前用户未登录");
        }
        
        boolean result = environmentConfigService.deleteEnvironmentConfig(id, currentUserId);
        
        if (!result) {
            return error("删除环境配置失败");
        }
        
        return success(true);
    }
    
    /**
     * 切换环境配置状态（启用/禁用）
     */
    @PostMapping("/{id}/toggle")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ApiResponse<Boolean> toggleEnvironmentConfig(@PathVariable Long id) {
        try {
            // 获取当前用户ID
            Long currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                return error("当前用户未登录");
            }
            
            // 获取当前环境配置
            EnvironmentConfig config = environmentConfigService.getById(id);
            if (config == null) {
                return error("环境配置不存在");
            }
            
            // 切换启用状态
            boolean newEnabled = !config.getEnabled();
            boolean result = environmentConfigService.toggleEnvironmentConfig(id, newEnabled, currentUserId);
            
            return success(result);
        } catch (Exception e) {
            logUtils.logException("环境配置管理", "切换状态", e);
            return error("切换状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证环境类型参数
     */
    private void validateEnvType(String envType) {
        if (!List.of("development", "testing", "production").contains(envType)) {
            throw new IllegalArgumentException("环境类型只能选择：development, testing, production");
        }
    }
}