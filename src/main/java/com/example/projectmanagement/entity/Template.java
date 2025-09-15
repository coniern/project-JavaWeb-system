package com.example.projectmanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目模板实体类
 * 用于存储项目模板的基本信息
 */
@Data
@TableName("template")
public class Template {

    /**
     * 模板ID
     */
    @TableId(type = IdType.AUTO)
    private Long templateId;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 模板类型
     * spring-boot: Spring Boot单体项目
     * ssm: SSM项目
     * microservice: 微服务项目
     */
    private String templateType;

    /**
     * 是否为系统预设模板
     */
    private Boolean isSystem;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    private Long createUserId;

    /**
     * 模板配置信息（JSON格式）
     * 包含项目结构、文件模板等配置
     */
    private String templateConfig;
}