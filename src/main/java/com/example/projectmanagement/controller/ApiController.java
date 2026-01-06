package com.example.projectmanagement.controller;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 完整的REST API控制器
 * 提供前端所需的所有API接口
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    // 模拟用户数据
    private static final Map<String, User> users = new HashMap<>();
    
    // 模拟项目数据
    private static final List<Map<String, Object>> projects = new ArrayList<>();
    
    // 模拟部署数据
    private static final List<Map<String, Object>> deployments = new ArrayList<>();
    
    static {
        // 初始化模拟用户
        User admin = new User();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setPassword("admin");
        admin.setNickname("管理员");
        admin.setEmail("admin@example.com");
        admin.setPhone("13800138000");
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
        
        // 初始化模拟部署数据
        Map<String, Object> deployment1 = new HashMap<>();
        deployment1.put("id", 1);
        deployment1.put("projectId", 1);
        deployment1.put("projectName", "电商平台项目");
        deployment1.put("status", "成功");
        deployment1.put("deployTime", "2026-01-05 10:30:00");
        deployment1.put("deployUser", "张三");
        deployment1.put("duration", 120); // 秒
        deployments.add(deployment1);
        
        Map<String, Object> deployment2 = new HashMap<>();
        deployment2.put("id", 2);
        deployment2.put("projectId", 2);
        deployment2.put("projectName", "企业官网重构");
        deployment2.put("status", "成功");
        deployment2.put("deployTime", "2025-12-20 14:15:00");
        deployment2.put("deployUser", "李四");
        deployment2.put("duration", 90); // 秒
        deployments.add(deployment2);
    }

    /**
     * 登录接口
     */
    @PostMapping("/auth/login")
    public Map<String, Object> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        
        User user = users.get(username);
        Map<String, Object> response = new HashMap<>();
        
        if (user != null && user.getPassword().equals(password)) {
            // 登录成功
            response.put("code", 200);
            response.put("message", "success");
            
            Map<String, Object> data = new HashMap<>();
            data.put("token", "mock-jwt-token-" + System.currentTimeMillis());
            data.put("user", user);
            
            response.put("data", data);
        } else {
            // 登录失败
            response.put("code", 400);
            response.put("message", "用户名或密码错误");
            response.put("data", null);
        }
        
        return response;
    }

    /**
     * 注册接口
     */
    @PostMapping("/auth/register")
    public Map<String, Object> register(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();
        
        if (users.containsKey(user.getUsername())) {
            response.put("code", 400);
            response.put("message", "用户名已存在");
            response.put("data", false);
        } else {
            user.setId((long) (users.size() + 1));
            users.put(user.getUsername(), user);
            
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", true);
        }
        
        return response;
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/auth/me")
    public Map<String, Object> getCurrentUser() {
        Map<String, Object> response = new HashMap<>();
        
        // 返回管理员用户信息
        User admin = users.get("admin");
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", admin);
        
        return response;
    }

    /**
     * 获取项目列表
     */
    @GetMapping("/projects")
    public Map<String, Object> getProjects() {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", projects);
        return response;
    }

    /**
     * 获取项目详情
     */
    @GetMapping("/projects/{id}")
    public Map<String, Object> getProject(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        
        for (Map<String, Object> project : projects) {
            if (project.get("id").equals(id)) {
                response.put("code", 200);
                response.put("message", "success");
                response.put("data", project);
                return response;
            }
        }
        
        response.put("code", 404);
        response.put("message", "项目不存在");
        response.put("data", null);
        return response;
    }

    /**
     * 创建项目
     */
    @PostMapping("/projects")
    public Map<String, Object> createProject(@RequestBody Map<String, Object> project) {
        Map<String, Object> response = new HashMap<>();
        
        project.put("id", projects.size() + 1);
        project.put("createdAt", "2026-01-06");
        projects.add(project);
        
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", project);
        return response;
    }

    /**
     * 更新项目
     */
    @PutMapping("/projects/{id}")
    public Map<String, Object> updateProject(@PathVariable Integer id, @RequestBody Map<String, Object> updatedProject) {
        Map<String, Object> response = new HashMap<>();
        
        for (int i = 0; i < projects.size(); i++) {
            if (projects.get(i).get("id").equals(id)) {
                projects.get(i).putAll(updatedProject);
                response.put("code", 200);
                response.put("message", "success");
                response.put("data", projects.get(i));
                return response;
            }
        }
        
        response.put("code", 404);
        response.put("message", "项目不存在");
        response.put("data", null);
        return response;
    }

    /**
     * 删除项目
     */
    @DeleteMapping("/projects/{id}")
    public Map<String, Object> deleteProject(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        
        for (int i = 0; i < projects.size(); i++) {
            if (projects.get(i).get("id").equals(id)) {
                projects.remove(i);
                response.put("code", 200);
                response.put("message", "success");
                response.put("data", true);
                return response;
            }
        }
        
        response.put("code", 404);
        response.put("message", "项目不存在");
        response.put("data", false);
        return response;
    }

    /**
     * 获取部署列表
     */
    @GetMapping("/deployments")
    public Map<String, Object> getDeployments() {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", deployments);
        return response;
    }

    /**
     * 创建部署
     */
    @PostMapping("/deployments")
    public Map<String, Object> createDeployment(@RequestBody Map<String, Object> deployment) {
        Map<String, Object> response = new HashMap<>();
        
        deployment.put("id", deployments.size() + 1);
        deployment.put("deployTime", "2026-01-06 " + java.time.LocalTime.now().toString());
        deployment.put("status", "成功");
        deployment.put("duration", 100); // 模拟部署时间
        deployments.add(deployment);
        
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", deployment);
        return response;
    }

    /**
     * 获取部署统计数据
     */
    @GetMapping("/deployments/stats")
    public Map<String, Object> getDeploymentStats() {
        Map<String, Object> response = new HashMap<>();
        
        // 模拟部署统计数据
        Map<String, Object> stats = new HashMap<>();
        
        // 部署成功率
        List<Map<String, Object>> successRate = new ArrayList<>();
        successRate.add(Map.of("month", "1月", "rate", 92));
        successRate.add(Map.of("month", "2月", "rate", 95));
        successRate.add(Map.of("month", "3月", "rate", 94));
        successRate.add(Map.of("month", "4月", "rate", 96));
        successRate.add(Map.of("month", "5月", "rate", 98));
        successRate.add(Map.of("month", "6月", "rate", 99));
        stats.put("successRate", successRate);
        
        // 部署时间趋势
        List<Map<String, Object>> timeTrend = new ArrayList<>();
        timeTrend.add(Map.of("month", "1月", "time", 15));
        timeTrend.add(Map.of("month", "2月", "time", 12));
        timeTrend.add(Map.of("month", "3月", "time", 10));
        timeTrend.add(Map.of("month", "4月", "time", 8));
        timeTrend.add(Map.of("month", "5月", "time", 6));
        timeTrend.add(Map.of("month", "6月", "time", 5));
        stats.put("timeTrend", timeTrend);
        
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", stats);
        return response;
    }

    /**
     * 用户类
     */
    public static class User {
        private Long id;
        private String username;
        private String password;
        private String nickname;
        private String email;
        private String phone;

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }
}