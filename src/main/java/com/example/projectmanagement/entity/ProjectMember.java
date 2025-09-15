package com.example.projectmanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 项目成员实体类
 * 用于存储项目和用户之间的关联关系
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("project_member")
public class ProjectMember implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    // 项目ID
    private Long projectId;

    // 用户ID
    private Long userId;

    // 角色：ADMIN(管理员), DEVELOPER(开发者), TESTER(测试人员)
    private String role;

    // 创建时间
    private LocalDateTime createTime;

    // 更新时间
    private LocalDateTime updateTime;
}