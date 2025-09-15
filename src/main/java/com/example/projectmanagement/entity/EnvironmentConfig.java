package com.example.projectmanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 环境配置实体类
 * 用于存储不同环境的部署配置信息
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("environment_config")
public class EnvironmentConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    // 项目ID
    private Long projectId;

    // 环境类型：development(开发环境), testing(测试环境), production(生产环境)
    private String envType;

    // 服务器IP地址
    private String serverIp;

    // SSH端口
    private Integer sshPort;

    // SSH用户名
    private String sshUsername;

    // SSH密码（加密存储）
    private String sshPassword;

    // SSH密钥路径
    private String sshKeyPath;

    // 部署路径
    private String deployPath;

    // 服务端口
    private Integer servicePort;

    // JVM参数
    private String jvmArgs;

    // 创建时间
    private LocalDateTime createTime;

    // 更新时间
    private LocalDateTime updateTime;

    // 是否启用
    private Boolean enabled;
}