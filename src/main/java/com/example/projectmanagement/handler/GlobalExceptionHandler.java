package com.example.projectmanagement.handler;

import com.example.projectmanagement.controller.BaseController.ApiResponse;
import com.example.projectmanagement.utils.LogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 统一处理应用程序中的异常情况
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @Autowired
    private LogUtils logUtils;
    
    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        return new ResponseEntity<>(new ApiResponse<>(400, "参数验证失败", errors), HttpStatus.BAD_REQUEST);
    }
    
    /**
     * 处理权限不足异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(AccessDeniedException ex) {
        return new ResponseEntity<>(new ApiResponse<>(403, "权限不足，无法访问该资源", null), HttpStatus.FORBIDDEN);
    }
    
    /**
     * 处理认证失败异常
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<?>> handleBadCredentialsException(BadCredentialsException ex) {
        return new ResponseEntity<>(new ApiResponse<>(401, "认证失败，用户名或密码错误", null), HttpStatus.UNAUTHORIZED);
    }
    
    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return new ResponseEntity<>(new ApiResponse<>(400, ex.getMessage(), null), HttpStatus.BAD_REQUEST);
    }
    
    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<?>> handleNullPointerException(NullPointerException ex) {
        logUtils.logException("空指针异常", "系统错误", ex);
        return new ResponseEntity<>(new ApiResponse<>(500, "系统内部错误，请稍后重试", null), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * 处理SQL异常
     */
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ApiResponse<?>> handleSQLException(SQLException ex) {
        logUtils.logException("数据库异常", "SQL错误", ex);
        return new ResponseEntity<>(new ApiResponse<>(500, "数据库操作失败，请检查数据库连接和SQL语句", null), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * 处理IO异常
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiResponse<?>> handleIOException(IOException ex) {
        logUtils.logException("IO异常", "文件操作错误", ex);
        return new ResponseEntity<>(new ApiResponse<>(500, "文件操作失败，请检查文件路径和权限", null), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * 处理404异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        return new ResponseEntity<>(new ApiResponse<>(404, "请求的资源不存在", null), HttpStatus.NOT_FOUND);
    }
    
    /**
     * 处理其他所有异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGlobalException(Exception ex, WebRequest request) {
        // 记录异常日志
        logUtils.logException("全局异常", "系统错误", ex);
        
        // 根据异常类型返回更具体的错误信息
        String errorMessage = "系统内部错误，请联系管理员";
        if (ex instanceof RuntimeException) {
            errorMessage = ex.getMessage() != null ? ex.getMessage() : errorMessage;
        }
        
        return new ResponseEntity<>(new ApiResponse<>(500, errorMessage, null), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}