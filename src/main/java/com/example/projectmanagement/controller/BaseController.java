package com.example.projectmanagement.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

/**
 * 基础Controller类
 * 提供通用的Controller功能
 */
@RestController
public class BaseController {
    
    /**
     * 获取当前用户ID
     * @return 当前用户ID
     */
    protected Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            try {
                return Long.parseLong(authentication.getName());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    /**
     * 返回成功响应
     * @param data 响应数据
     * @return 响应对象
     */
    protected <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }
    
    /**
     * 返回成功响应（无数据）
     * @return 响应对象
     */
    protected ApiResponse<Void> success() {
        return new ApiResponse<>(200, "success", null);
    }
    
    /**
     * 返回错误响应
     * @param code 错误代码
     * @param message 错误信息
     * @return 响应对象
     */
    protected <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
    
    /**
     * 返回错误响应（默认错误代码）
     * @param message 错误信息
     * @return 响应对象
     */
    protected <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(400, message, null);
    }
    
    /**
     * API响应封装类
     */
    public static class ApiResponse<T> {
        private int code;
        private String message;
        private T data;
        
        public ApiResponse(int code, String message, T data) {
            this.code = code;
            this.message = message;
            this.data = data;
        }
        
        public int getCode() {
            return code;
        }
        
        public void setCode(int code) {
            this.code = code;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public T getData() {
            return data;
        }
        
        public void setData(T data) {
            this.data = data;
        }
    }
}