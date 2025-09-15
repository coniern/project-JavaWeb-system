package com.example.projectmanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 部署流程执行实体类
 * 用于跟踪自定义部署流程的执行状态
 */
@Data
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
}