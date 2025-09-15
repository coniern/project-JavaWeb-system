package com.example.projectmanagement.controller;

import com.example.projectmanagement.controller.BaseController.ApiResponse;
import com.example.projectmanagement.entity.User;
import com.example.projectmanagement.service.UserService;
import com.example.projectmanagement.utils.LogUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户管理控制器
 * 处理用户相关的HTTP请求
 */
@RestController
@RequestMapping("/api/users")
public class UserController extends BaseController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
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
     * 获取当前登录用户信息
     */
    @GetMapping("/me")
    public ApiResponse<User> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
                return error("用户未登录");
            }
            
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            if (user == null) {
                logUtils.logDebug("当前用户获取信息失败：用户不存在");
                return error("用户不存在");
            }
            
            // 清除密码信息
            user.setPassword(null);
            logUtils.logDebug("用户获取个人信息: " + userDetails.getUsername());
            return success(user);
        } catch (Exception e) {
            logUtils.logException("用户管理", "获取当前用户信息", e);
            return error("获取用户信息失败");
        }
    }
    
    /**
     * 获取用户列表
     * 仅管理员可以访问
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<List<User>> getUserList() {
        try {
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            List<User> users = userService.list();
            // 清除所有用户的密码信息
            users.forEach(user -> user.setPassword(null));
            logUtils.logOperation("用户管理", "获取用户列表", true, "当前用户: " + currentUsername + ", 列表大小: " + users.size());
            return success(users);
        } catch (Exception e) {
            logUtils.logException("用户管理", "获取用户列表", e);
            return error("获取用户列表失败");
        }
    }
    
    /**
     * 获取用户详情
     * 仅管理员或用户本人可以访问
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or #id == principal.id")
    public ApiResponse<User> getUserById(@PathVariable Long id) {
        try {
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getById(id);
            if (user == null) {
                logUtils.logOperation("用户管理", "获取用户详情", false, "当前用户: " + currentUsername + ", 用户ID: " + id + ", 原因: 用户不存在");
                return error("用户不存在");
            }
            
            // 清除密码信息
            user.setPassword(null);
            logUtils.logOperation("用户管理", "获取用户详情", true, "当前用户: " + currentUsername + ", 用户ID: " + id + ", 用户名: " + user.getUsername());
            return success(user);
        } catch (Exception e) {
            logUtils.logException("用户管理", "获取用户详情", e);
            return error("获取用户详情失败");
        }
    }
    
    /**
     * 更新用户信息
     * 仅管理员或用户本人可以访问
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or #id == principal.id")
    public ApiResponse<Boolean> updateUser(@PathVariable Long id, @Valid @RequestBody User user) {
        try {
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            // 检查用户是否存在
            User existingUser = userService.getById(id);
            if (existingUser == null) {
                logUtils.logOperation("用户管理", "更新用户信息", false, "当前用户: " + currentUsername + ", 用户ID: " + id + ", 原因: 用户不存在");
                return error("用户不存在");
            }
            
            // 记录更新前的信息摘要
            String originalInfo = "用户ID: " + id + ", 用户名: " + existingUser.getUsername();
            
            // 设置用户ID
            user.setId(id);
            
            // 如果不是管理员，不允许修改其他用户的角色和状态
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails currentUser = (UserDetails) authentication.getPrincipal();
                User currentUserEntity = userService.findByUsername(currentUser.getUsername());
                if (!currentUserEntity.getId().equals(id)) {
                    // 非管理员不能修改其他用户
                    logUtils.logOperation("用户管理", "更新用户信息", false, "当前用户: " + currentUsername + ", " + originalInfo + ", 原因: 没有权限");
                    return error("没有权限修改此用户信息");
                }
            }
            
            // 更新用户信息
            boolean result = userService.updateUser(user);
            if (!result) {
                logUtils.logOperation("用户管理", "更新用户信息", false, "当前用户: " + currentUsername + ", " + originalInfo + ", 原因: 更新失败");
                return error("更新用户信息失败");
            }
            
            logUtils.logOperation("用户管理", "更新用户信息", true, "当前用户: " + currentUsername + ", " + originalInfo + ", IP: " + getClientIp());
            return success(true);
        } catch (Exception e) {
            logUtils.logException("用户管理", "更新用户信息", e);
            return error("更新用户信息失败");
        }
    }
    
    /**
     * 修改用户状态（启用/禁用）
     * 仅管理员可以访问
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<Boolean> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            // 检查用户是否存在
            User existingUser = userService.getById(id);
            if (existingUser == null) {
                logUtils.logOperation("用户管理", "修改用户状态", false, "当前用户: " + currentUsername + ", 用户ID: " + id + ", 原因: 用户不存在");
                return error("用户不存在");
            }
            
            // 验证状态值
            if (status != 0 && status != 1) {
                logUtils.logOperation("用户管理", "修改用户状态", false, "当前用户: " + currentUsername + ", 用户ID: " + id + ", 原因: 状态值无效");
                return error("状态值无效，只能是0（禁用）或1（启用）");
            }
            
            // 记录状态变更
            String oldStatus = existingUser.getStatus() == 1 ? "启用" : "禁用";
            String newStatus = status == 1 ? "启用" : "禁用";
            
            // 更新用户状态
            User user = new User();
            user.setId(id);
            user.setStatus(status);
            user.setUpdateTime(LocalDateTime.now());
            
            boolean result = userService.updateUser(user);
            if (!result) {
                logUtils.logOperation("用户管理", "修改用户状态", false, "当前用户: " + currentUsername + ", 用户ID: " + id + ", 用户名: " + existingUser.getUsername() + ", 原状态: " + oldStatus + ", 新状态: " + newStatus);
                return error("更新用户状态失败");
            }
            
            logUtils.logOperation("用户管理", "修改用户状态", true, "当前用户: " + currentUsername + ", 用户ID: " + id + ", 用户名: " + existingUser.getUsername() + ", 原状态: " + oldStatus + ", 新状态: " + newStatus + ", IP: " + getClientIp());
            return success(true);
        } catch (Exception e) {
            logUtils.logException("用户管理", "修改用户状态", e);
            return error("更新用户状态失败");
        }
    }
    
    /**
     * 重置用户密码
     * 仅管理员可以访问
     */
    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<Boolean> resetUserPassword(@PathVariable Long id, @RequestParam String newPassword) {
        try {
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            // 检查用户是否存在
            User existingUser = userService.getById(id);
            if (existingUser == null) {
                logUtils.logOperation("用户管理", "重置密码", false, "当前用户: " + currentUsername + ", 用户ID: " + id + ", 原因: 用户不存在");
                return error("用户不存在");
            }
            
            // 验证密码长度
            if (newPassword == null || newPassword.length() < 6) {
                logUtils.logOperation("用户管理", "重置密码", false, "当前用户: " + currentUsername + ", 用户ID: " + id + ", 用户名: " + existingUser.getUsername() + ", 原因: 密码长度不足");
                return error("密码长度不能少于6位");
            }
            
            // 更新用户密码
            User user = new User();
            user.setId(id);
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setUpdateTime(LocalDateTime.now());
            
            boolean result = userService.updateUser(user);
            if (!result) {
                logUtils.logOperation("用户管理", "重置密码", false, "当前用户: " + currentUsername + ", 用户ID: " + id + ", 用户名: " + existingUser.getUsername());
                return error("重置密码失败");
            }
            
            logUtils.logOperation("用户管理", "重置密码", true, "当前用户: " + currentUsername + ", 用户ID: " + id + ", 用户名: " + existingUser.getUsername() + ", IP: " + getClientIp());
            return success(true);
        } catch (Exception e) {
            logUtils.logException("用户管理", "重置密码", e);
            return error("重置密码失败");
        }
    }
    
    /**
     * 修改自己的密码
     */
    @PutMapping("/change-password")
    public ApiResponse<Boolean> changePassword(@RequestParam String oldPassword, 
                                              @RequestParam String newPassword) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
                return error("用户未登录");
            }
            
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            User user = userService.findByUsername(username);
            
            // 验证旧密码
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                logUtils.logOperation("用户管理", "修改个人密码", false, "当前用户: " + username + ", 原因: 旧密码错误");
                return error("旧密码不正确");
            }
            
            // 验证新密码长度
            if (newPassword == null || newPassword.length() < 6) {
                logUtils.logOperation("用户管理", "修改个人密码", false, "当前用户: " + username + ", 原因: 新密码长度不足");
                return error("新密码长度不能少于6位");
            }
            
            // 更新密码
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setUpdateTime(LocalDateTime.now());
            
            boolean result = userService.updateUser(user);
            if (!result) {
                logUtils.logOperation("用户管理", "修改个人密码", false, "当前用户: " + username + ", 原因: 更新失败");
                return error("修改密码失败");
            }
            
            logUtils.logOperation("用户管理", "修改个人密码", true, "当前用户: " + username + ", IP: " + getClientIp());
            return success(true);
        } catch (Exception e) {
            logUtils.logException("用户管理", "修改个人密码", e);
            return error("修改密码失败");
        }
    }
}