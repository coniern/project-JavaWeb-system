package com.example.projectmanagement.handler;

import com.example.projectmanagement.controller.BaseController.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * JWT权限不足处理器
 * 处理权限不足的情况
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
    
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, 
                      AccessDeniedException accessDeniedException) throws IOException, ServletException {
        // 设置响应状态码为403 Forbidden
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        // 设置响应内容类型为JSON
        response.setContentType("application/json;charset=utf-8");
        // 创建API响应对象
        ApiResponse<String> apiResponse = new ApiResponse<>(403, "权限不足，无法访问此资源", null);
        // 将响应对象转换为JSON字符串
        String json = new ObjectMapper().writeValueAsString(apiResponse);
        // 输出响应
        PrintWriter out = response.getWriter();
        out.write(json);
        out.flush();
        out.close();
    }
}