package com.example.projectmanagement.controller;

import com.example.projectmanagement.controller.BaseController.ApiResponse;
import com.example.projectmanagement.entity.EnvironmentConfig;
import com.example.projectmanagement.service.EnvironmentConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
        
        // 获取当前登录用户ID（这里假设从Security上下文中获取）
        Long currentUserId = 1L; // 临时值，实际应该从上下文中获取
        
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
        
        // 获取当前登录用户ID（这里假设从Security上下文中获取）
        Long currentUserId = 1L; // 临时值，实际应该从上下文中获取
        
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
        
        // 获取当前登录用户ID（这里假设从Security上下文中获取）
        Long currentUserId = 1L; // 临时值，实际应该从上下文中获取
        
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
        
        boolean result = environmentConfigService.toggleEnvironmentConfig(id);
        
        return success(result);
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