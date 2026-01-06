package com.example.projectmanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 部署流程执行实体类
 * 用于跟踪自定义部署流程的执行状态
 */
@EqualsAndHashCode(callSuper = false)
@TableName("flow_execution")
public class FlowExecution implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "execution_id", type = IdType.AUTO)
    private Long executionId;

    // 流程ID
    private Long flowId;

    // 项目ID
    private Long projectId;

    // 环境类型
    private String env;

    // 版本号
    private String version;

    // 执行状态：running(运行中), paused(已暂停), completed(已完成), failed(失败)
    private String status;

    // 当前执行步骤索引
    private Integer currentStepIndex;

    // 执行日志
    private String executionLog;

    // 开始时间
    private LocalDateTime startTime;

    // 结束时间
    private LocalDateTime endTime;

    // 部署人ID
    private Long deployerId;

    // 关联的部署记录ID
    private Long deploymentId;

    // Getters and Setters
    public Long getExecutionId() {
        return executionId;
    }

    public void setExecutionId(Long executionId) {
        this.executionId = executionId;
    }

    public Long getFlowId() {
        return flowId;
    }

    public void setFlowId(Long flowId) {
        this.flowId = flowId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCurrentStepIndex() {
        return currentStepIndex;
    }

    public void setCurrentStepIndex(Integer currentStepIndex) {
        this.currentStepIndex = currentStepIndex;
    }

    public String getExecutionLog() {
        return executionLog;
    }

    public void setExecutionLog(String executionLog) {
        this.executionLog = executionLog;
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

    public Long getDeployerId() {
        return deployerId;
    }

    public void setDeployerId(Long deployerId) {
        this.deployerId = deployerId;
    }

    public Long getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(Long deploymentId) {
        this.deploymentId = deploymentId;
    }
}