package com.example.projectmanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 自定义部署流程实体类
 * 用于存储用户自定义的部署流程
 */
@EqualsAndHashCode(callSuper = false)
@TableName("custom_deploy_flow")
public class CustomDeployFlow implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "flow_id", type = IdType.AUTO)
    private Long flowId;

    // 流程名称
    private String flowName;

    // 关联的项目ID
    private Long projectId;

    // 流程配置（JSON格式）
    private JsonNode flowConfig;

    // 是否为默认流程
    private Boolean isDefault;

    // 创建时间
    private LocalDateTime createTime;

    // 更新时间
    private LocalDateTime updateTime;

    // 创建人ID
    private Long creatorId;

    // Getters and Setters
    public Long getFlowId() {
        return flowId;
    }

    public void setFlowId(Long flowId) {
        this.flowId = flowId;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public JsonNode getFlowConfig() {
        return flowConfig;
    }

    public void setFlowConfig(JsonNode flowConfig) {
        this.flowConfig = flowConfig;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }
}