package com.example.projectmanagement.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.projectmanagement.controller.BaseController.ApiResponse;
import com.example.projectmanagement.entity.Deployment;
import com.example.projectmanagement.service.DeploymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 部署Controller
 * 处理部署相关的HTTP请求
 */
@RestController
@RequestMapping("/api/deployments")
public class DeploymentController extends BaseController {
    
    @Autowired
    private DeploymentService deploymentService;
    
    /**
     * 获取部署历史列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param projectId 项目ID（可选）
     * @param env 环境类型（可选）
     * @param status 部署状态（可选）
     * @return 部署历史分页列表
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER', 'TESTER')")
    public ApiResponse<Page<Deployment>> getDeploymentHistory(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String env,
            @RequestParam(required = false) String status) {
        
        // 验证环境类型参数
        if (env != null) {
            validateEnvType(env);
        }
        
        // 验证部署状态参数
        if (status != null) {
            validateDeploymentStatus(status);
        }
        
        Page<Deployment> page = deploymentService.findPage(pageNum, pageSize, projectId, env, status);
        return success(page);
    }
    
    /**
     * 获取部署详情
     * @param id 部署ID
     * @return 部署详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER', 'TESTER')")
    public ApiResponse<Deployment> getDeploymentDetail(@PathVariable Long id) {
        Deployment deployment = deploymentService.getById(id);
        if (deployment == null) {
            return error("部署记录不存在");
        }
        return success(deployment);
    }
    
    /**
     * 执行部署操作
     * @param projectId 项目ID
     * @param env 环境类型
     * @param version 版本号
     * @param file 部署包文件
     * @return 部署结果
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ApiResponse<Long> executeDeployment(
            @RequestParam Long projectId,
            @RequestParam String env,
            @RequestParam String version,
            @RequestPart(value = "file") MultipartFile file) {
        
        // 验证环境类型参数
        validateEnvType(env);
        
        // 验证文件
        if (file == null || file.isEmpty()) {
            return error("请上传部署包文件");
        }
        
        // 获取当前登录用户ID（这里假设从Security上下文中获取）
        Long deployerId = 1L; // 临时值，实际应该从上下文中获取
        
        Long deployId = deploymentService.executeDeployment(projectId, env, version, file, deployerId);
        if (deployId == null) {
            return error("部署失败");
        }
        
        return success(deployId);
    }
    
    /**
     * 获取项目在指定环境的最新部署记录
     * @param projectId 项目ID
     * @param env 环境类型
     * @return 最新的部署记录
     */
    @GetMapping("/latest")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER', 'TESTER')")
    public ApiResponse<Deployment> getLatestDeployment(
            @RequestParam Long projectId,
            @RequestParam String env) {
        
        // 验证环境类型参数
        validateEnvType(env);
        
        Deployment deployment = deploymentService.findLatestByProjectIdAndEnv(projectId, env);
        return success(deployment);
    }
    
    /**
     * 验证环境类型参数
     */
    private void validateEnvType(String env) {
        if (!List.of("development", "testing", "production").contains(env)) {
            throw new IllegalArgumentException("环境类型只能选择：development, testing, production");
        }
    }
    
    /**
     * 验证部署状态参数
     */
    private void validateDeploymentStatus(String status) {
        if (!List.of("pending", "deploying", "success", "failed").contains(status)) {
            throw new IllegalArgumentException("部署状态只能选择：pending, deploying, success, failed");
        }
    }
}