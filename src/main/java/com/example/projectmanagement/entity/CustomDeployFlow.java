package com.example.projectmanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 自定义部署流程实体类
 * 用于存储用户自定义的部署流程
 */
@Data
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
}