package com.example.projectmanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 项目实体类
 * 用于存储项目的基础信息
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("project")
public class Project implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "project_id", type = IdType.AUTO)
    private Long projectId;

    // 项目名称
    @NotBlank(message = "项目名称不能为空")
    private String name;

    // 项目描述
    private String description;

    // 技术栈
    @NotBlank(message = "技术栈不能为空")
    private String techStack;

    // 项目状态：planning(规划中), developing(开发中), testing(测试中), deployed(已部署)
    private String status;

    // 项目开始时间
    private LocalDateTime startTime;

    // 项目结束时间
    private LocalDateTime endTime;

    // 负责人ID
    @NotNull(message = "负责人ID不能为空")
    private Long leaderId;

    // 创建时间
    private LocalDateTime createTime;

    // 更新时间
    private LocalDateTime updateTime;

    // 完成进度（百分比）
    private Integer progress;
}