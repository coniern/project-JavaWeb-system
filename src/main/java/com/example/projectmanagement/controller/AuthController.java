package com.example.projectmanagement.controller;

import com.example.projectmanagement.controller.BaseController.ApiResponse;
import com.example.projectmanagement.entity.User;
import com.example.projectmanagement.service.UserService;
import com.example.projectmanagement.utils.JwtUtils;
import com.example.projectmanagement.utils.LogUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证Controller
 * 处理用户登录、注册等认证相关的HTTP请求
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController extends BaseController {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private LogUtils logUtils;
    
    @Autowired
    private HttpServletRequest request;
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIp() {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理的情况下，第一个IP为客户端真实IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // 创建认证令牌
            UsernamePasswordAuthenticationToken authenticationToken = 
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());
            
            // 进行认证
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 生成JWT令牌
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = jwtUtils.generateToken(userDetails);
            
            // 查询用户信息
            User user = userService.findByUsername(loginRequest.getUsername());
            if (user != null) {
                user.setPassword(null);
            }
            
            // 构建响应数据
            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("user", user);
            
            // 记录登录成功日志
            logUtils.logLogin(loginRequest.getUsername(), true, getClientIp());
            logUtils.logOperation("认证模块", "用户登录", true, "用户名: " + loginRequest.getUsername());
            
            return success(response);
        } catch (Exception e) {
            // 记录登录失败日志
            logUtils.logLogin(loginRequest.getUsername(), false, getClientIp());
            logUtils.logException("认证模块", "用户登录", e);
            return error("用户名或密码错误");
        }
    }
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ApiResponse<Boolean> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // 检查用户名是否已存在
            User existingUser = userService.findByUsername(registerRequest.getUsername());
            if (existingUser != null) {
                logUtils.logOperation("认证模块", "用户注册", false, "用户名已存在: " + registerRequest.getUsername());
                return error("用户名已存在");
            }
            
            // 创建用户对象
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setPassword(registerRequest.getPassword());
            user.setNickname(registerRequest.getNickname());
            user.setEmail(registerRequest.getEmail());
            user.setPhone(registerRequest.getPhone());
            user.setStatus(1); // 默认启用
            
            // 注册用户
            boolean result = userService.register(user);
            if (!result) {
                logUtils.logOperation("认证模块", "用户注册", false, "注册失败: " + registerRequest.getUsername());
                return error("注册失败，请重试");
            }
            
            logUtils.logOperation("认证模块", "用户注册", true, "用户名: " + registerRequest.getUsername() + ", IP: " + getClientIp());
            return success(true);
        } catch (Exception e) {
            logUtils.logException("认证模块", "用户注册", e);
            return error("注册过程中发生错误，请重试");
        }
    }
    
    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/me")
    public ApiResponse<User> getCurrentUser(Authentication authentication) {
        try {
            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
                return error("用户未登录");
            }
            
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            if (user != null) {
                user.setPassword(null);
            }
            
            logUtils.logDebug("用户获取个人信息: " + userDetails.getUsername());
            return success(user);
        } catch (Exception e) {
            logUtils.logException("认证模块", "获取用户信息", e);
            return error("获取用户信息失败");
        }
    }
    
    // 登录请求参数类
    static class LoginRequest {
        private String username;
        private String password;
        
        // getter和setter
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    // 注册请求参数类
    static class RegisterRequest {
        private String username;
        private String password;
        private String nickname;
        private String email;
        private String phone;
        
        // getter和setter
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }
}
