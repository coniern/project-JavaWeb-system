package com.example.projectmanagement.handler;

import com.example.projectmanagement.controller.BaseController.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * JWT认证入口点
 * 处理认证失败的情况
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, 
                         AuthenticationException authException) throws IOException, ServletException {
        // 设置响应状态码为401 Unauthorized
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // 设置响应内容类型为JSON
        response.setContentType("application/json;charset=utf-8");
        // 创建API响应对象
        ApiResponse<String> apiResponse = new ApiResponse<>(401, "认证失败，请重新登录", null);
        // 将响应对象转换为JSON字符串
        String json = new ObjectMapper().writeValueAsString(apiResponse);
        // 输出响应
        PrintWriter out = response.getWriter();
        out.write(json);
        out.flush();
        out.close();
    }
}