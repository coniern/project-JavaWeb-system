package com.example.projectmanagement.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日志工具类
 * 提供统一的日志记录功能
 */
@Component
public class LogUtils {
    
    // 操作日志记录器
    private static final Logger operationLogger = LoggerFactory.getLogger("operationLog");
    // 异常日志记录器
    private static final Logger exceptionLogger = LoggerFactory.getLogger("exceptionLog");
    // 调试日志记录器
    private static final Logger debugLogger = LoggerFactory.getLogger("debugLog");
    
    // 日期时间格式化器
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 记录操作日志
     * @param module 模块名称
     * @param operation 操作描述
     * @param result 操作结果
     * @param remark 备注信息
     */
    public void logOperation(String module, String operation, boolean result, String remark) {
        // 获取当前登录用户
        String username = getCurrentUsername();
        // 构建日志消息
        String logMessage = String.format("[%s] [用户: %s] [模块: %s] [操作: %s] [结果: %s] [备注: %s]",
                formatter.format(LocalDateTime.now()),
                username,
                module,
                operation,
                result ? "成功" : "失败",
                remark != null ? remark : "无");
        // 记录日志
        operationLogger.info(logMessage);
    }
    
    /**
     * 记录异常日志
     * @param module 模块名称
     * @param operation 操作描述
     * @param e 异常对象
     */
    public void logException(String module, String operation, Exception e) {
        // 获取当前登录用户
        String username = getCurrentUsername();
        // 构建日志消息
        String logMessage = String.format("[%s] [用户: %s] [模块: %s] [操作: %s] [异常类型: %s] [异常信息: %s]",
                formatter.format(LocalDateTime.now()),
                username,
                module,
                operation,
                e.getClass().getName(),
                e.getMessage());
        // 记录日志
        exceptionLogger.error(logMessage, e);
    }
    
    /**
     * 记录调试日志
     * @param message 调试消息
     */
    public void logDebug(String message) {
        if (debugLogger.isDebugEnabled()) {
            debugLogger.debug("[{}] {}", formatter.format(LocalDateTime.now()), message);
        }
    }
    
    /**
     * 获取当前登录用户的用户名
     * @return 用户名，如果未登录则返回"anonymous"
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return userDetails.getUsername();
        }
        return "anonymous";
    }
    
    /**
     * 记录登录操作
     * @param username 用户名
     * @param success 是否成功
     * @param ip IP地址
     */
    public void logLogin(String username, boolean success, String ip) {
        String logMessage = String.format("[%s] [用户: %s] [操作: 登录] [结果: %s] [IP: %s]",
                formatter.format(LocalDateTime.now()),
                username,
                success ? "成功" : "失败",
                ip);
        operationLogger.info(logMessage);
    }
    
    /**
     * 记录退出操作
     * @param username 用户名
     * @param ip IP地址
     */
    public void logLogout(String username, String ip) {
        String logMessage = String.format("[%s] [用户: %s] [操作: 退出] [结果: 成功] [IP: %s]",
                formatter.format(LocalDateTime.now()),
                username,
                ip);
        operationLogger.info(logMessage);
    }
}