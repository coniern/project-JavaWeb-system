package com.example.projectmanagement.controller;

import com.example.projectmanagement.controller.BaseController.ApiResponse;
import com.example.projectmanagement.entity.User;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 简单的REST API控制器
 * 提供基本的认证和项目管理功能
 */
@RestController
@RequestMapping("/api")
public class SimpleApiController extends BaseController {

    // 模拟用户数据
    private static final Map<String, User> users = new HashMap<>();
    
    // 模拟项目数据
    private static final List<Map<String, Object>> projects = new ArrayList<>();
    
    static {
        // 初始化模拟用户
        User admin = new User();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setPassword("admin");
        admin.setNickname("管理员");
        users.put("admin", admin);
        
        // 初始化模拟项目
        Map<String, Object> project1 = new HashMap<>();
        project1.put("id", 1);
        project1.put("name", "电商平台项目");
        project1.put("description", "电商系统开发");
        project1.put("status", "进行中");
        project1.put("createdAt", "2026-01-01");
        project1.put("manager", "张三");
        projects.add(project1);
        
        Map<String, Object> project2 = new HashMap<>();
        project2.put("id", 2);
        project2.put("name", "企业官网重构");
        project2.put("description", "前端开发");
        project2.put("status", "已完成");
        project2.put("createdAt", "2025-12-15");
        project2.put("manager", "李四");
        projects.add(project2);
        
        Map<String, Object> project3 = new HashMap<>();
        project3.put("id", 3);
        project3.put("name", "移动应用后端");
        project3.put("description", "后端开发");
        project3.put("status", "待审核");
        project3.put("createdAt", "2026-01-03");
        project3.put("manager", "王五");
        projects.add(project3);
    }

    /**
     * 登录接口
     */
    @PostMapping("/auth/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        
        User user = users.get(username);
        if (user != null && user.getPassword().equals(password)) {
            // 模拟生成token
            String token = "mock-jwt-token-" + System.currentTimeMillis();
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", user);
            
            return success(response);
        } else {
            return error("用户名或密码错误");
        }
    }

    /**
     * 注册接口
     */
    @PostMapping("/auth/register")
    public ApiResponse<Boolean> register(@RequestBody User user) {
        if (users.containsKey(user.getUsername())) {
            return error("用户名已存在");
        }
        
        user.setId((long) (users.size() + 1));
        users.put(user.getUsername(), user);
        
        return success(true);
    }

    /**
     * 获取项目列表
     */
    @GetMapping("/projects")
    public ApiResponse<List<Map<String, Object>>> getProjects() {
        return success(projects);
    }

    /**
     * 获取项目详情
     */
    @GetMapping("/projects/{id}")
    public ApiResponse<Map<String, Object>> getProject(@PathVariable Integer id) {
        for (Map<String, Object> project : projects) {
            if (project.get("id").equals(id)) {
                return success(project);
            }
        }
        return error("项目不存在");
    }

    /**
     * 创建项目
     */
    @PostMapping("/projects")
    public ApiResponse<Map<String, Object>> createProject(@RequestBody Map<String, Object> project) {
        project.put("id", projects.size() + 1);
        project.put("createdAt", "2026-01-06");
        projects.add(project);
        return success(project);
    }

    /**
     * 更新项目
     */
    @PutMapping("/projects/{id}")
    public ApiResponse<Map<String, Object>> updateProject(@PathVariable Integer id, @RequestBody Map<String, Object> updatedProject) {
        for (int i = 0; i < projects.size(); i++) {
            if (projects.get(i).get("id").equals(id)) {
                projects.get(i).putAll(updatedProject);
                return success(projects.get(i));
            }
        }
        return error("项目不存在");
    }

    /**
     * 删除项目
     */
    @DeleteMapping("/projects/{id}")
    public ApiResponse<Boolean> deleteProject(@PathVariable Integer id) {
        for (int i = 0; i < projects.size(); i++) {
            if (projects.get(i).get("id").equals(id)) {
                projects.remove(i);
                return success(true);
            }
        }
        return error("项目不存在");
    }
}