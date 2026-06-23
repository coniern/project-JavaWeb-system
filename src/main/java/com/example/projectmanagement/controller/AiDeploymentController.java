package com.example.projectmanagement.controller;

/*
 * AI 部署分析模块保留为后续扩展，当前最小可运行版本不注册该控制器。
 */

import com.example.projectmanagement.entity.Deployment;
import com.example.projectmanagement.service.DeploymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI辅助部署控制器
 * 提供AI辅助部署失败排查相关的API接口
 */
public class AiDeploymentController {

    @Autowired
    private DeploymentService deploymentService;

    /**
     * 获取部署失败的AI建议
     * @param deploymentId 部署ID
     * @return 包含AI建议的部署详情
     */
    @GetMapping("/suggestion/{deploymentId}")
    public Result getAiSuggestion(@PathVariable Long deploymentId) {
        try {
            Deployment deployment = deploymentService.getById(deploymentId);
            if (deployment == null) {
                return Result.error("部署记录不存在");
            }
            
            // 如果部署状态不是失败且没有AI建议，可以手动触发AI分析
            if (!"failed".equals(deployment.getStatus()) && deployment.getAiSuggestion() == null) {
                deploymentService.analyzeDeploymentFailure(deploymentId);
                deployment = deploymentService.getById(deploymentId);
            }
            
            return Result.success(deployment);
        } catch (Exception e) {
            return Result.error("获取AI建议失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发AI分析
     * @param deploymentId 部署ID
     * @return 分析结果
     */
    @PostMapping("/analyze/{deploymentId}")
    public Result analyzeDeployment(@PathVariable Long deploymentId) {
        try {
            boolean result = deploymentService.analyzeDeploymentFailure(deploymentId);
            if (result) {
                Deployment deployment = deploymentService.getById(deploymentId);
                return Result.success(deployment);
            } else {
                return Result.error("AI分析失败");
            }
        } catch (Exception e) {
            return Result.error("AI分析异常: " + e.getMessage());
        }
    }

    /**
     * 获取常见错误类型及解决方案
     * @return 常见错误解决方案列表
     */
    @GetMapping("/common-errors")
    public Result getCommonErrors() {
        try {
            Map<String, String> commonErrors = deploymentService.getCommonErrors();
            return Result.success(commonErrors);
        } catch (Exception e) {
            return Result.error("获取常见错误信息失败: " + e.getMessage());
        }
    }

    /**
     * 自定义结果类，用于统一API响应格式
     */
    public static class Result {
        private int code;
        private String message;
        private Object data;

        public Result(int code, String message, Object data) {
            this.code = code;
            this.message = message;
            this.data = data;
        }

        public static Result success(Object data) {
            return new Result(200, "success", data);
        }

        public static Result error(String message) {
            return new Result(500, message, null);
        }

        // getter and setter
        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }
    }
}
