package com.example.projectmanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 部署历史实体类
 * 用于记录项目的部署历史信息
 */
@Data
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
 }