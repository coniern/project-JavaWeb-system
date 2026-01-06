package com.example.projectmanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 部署历史实体类
 * 用于记录项目的部署历史信息
 */
@EqualsAndHashCode(callSuper = false)
@TableName("deployment")
public class Deployment implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "deploy_id", type = IdType.AUTO)
    private Long deployId;

    // 项目ID
    private Long projectId;

    // 环境类型：development(开发环境), testing(测试环境), production(生产环境)
    private String env;

    // 部署版本
    private String version;

    // 部署开始时间
    private LocalDateTime startTime;

    // 部署结束时间
    private LocalDateTime endTime;

    // 部署状态：pending(待部署), deploying(部署中), success(成功), failed(失败)
    private String status;

    // 部署日志
    private String log;

    // 部署人ID
    private Long deployerId;

    // 部署包路径
    private String packagePath;

    // 服务器IP
    private String serverIp;
    
    // AI分析建议
    private String aiSuggestion;

    // Getters and Setters
    public Long getDeployId() {
        return deployId;
    }

    public void setDeployId(Long deployId) {
        this.deployId = deployId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public Long getDeployerId() {
        return deployerId;
    }

    public void setDeployerId(Long deployerId) {
        this.deployerId = deployerId;
    }

    public String getPackagePath() {
        return packagePath;
    }

    public void setPackagePath(String packagePath) {
        this.packagePath = packagePath;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public String getAiSuggestion() {
        return aiSuggestion;
    }

    public void setAiSuggestion(String aiSuggestion) {
        this.aiSuggestion = aiSuggestion;
    }
 }