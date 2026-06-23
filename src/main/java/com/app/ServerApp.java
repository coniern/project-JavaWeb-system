package com.app;

/*
 * 历史 Mock 入口，保留源码供参考。
 * 当前项目的正式启动类为 com.example.projectmanagement.ProjectManagementApplication。
 */

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 项目管理平台应用程序
 * 提供完整的项目管理、用户认证和部署管理功能
 */
@SpringBootApplication
public class ServerApp {

    public static void main(String[] args) {
        SpringApplication.run(ServerApp.class, args);
    }

    /**
     * 健康检查控制器
     */
    @RestController
    public static class HealthController {
        @GetMapping("/health")
        public String health() {
            return "OK";
        }
    }

    /**
     * API控制器
     * 提供完整的项目管理、用户认证和部署管理API端点
     */
    @RestController
    @RequestMapping("/api")
    public static class ApiController {

        // 模拟用户数据
        private final Map<String, User> users = new ConcurrentHashMap<>();

        // 模拟项目数据
        private final Map<Long, Project> projects = new ConcurrentHashMap<>();

        // 模拟部署数据
        private final Map<Long, Deployment> deployments = new ConcurrentHashMap<>();

        // 计数器
        private long projectIdCounter = 1;
        private long deploymentIdCounter = 1;

        /**
         * 用户认证相关端点
         */
        @PostMapping("/auth/login")
        public ApiResponse<String> login(@RequestBody LoginRequest request) {
            // 模拟登录验证
            if ("admin".equals(request.getUsername()) && "admin123".equals(request.getPassword())) {
                return new ApiResponse<>(200, "success", "mock-jwt-token");
            }
            return new ApiResponse<>(401, "用户名或密码错误", null);
        }

        @PostMapping("/auth/register")
        public ApiResponse<Boolean> register(@RequestBody RegisterRequest request) {
            // 模拟注册
            if (users.containsKey(request.getUsername())) {
                return new ApiResponse<>(400, "用户名已存在", false);
            }
            User user = new User(request.getUsername(), request.getPassword(), request.getEmail());
            users.put(request.getUsername(), user);
            return new ApiResponse<>(200, "success", true);
        }

        /**
         * 项目管理相关端点
         */
        @GetMapping("/projects")
        public ApiResponse<List<Project>> getProjects() {
            return new ApiResponse<>(200, "success", new ArrayList<>(projects.values()));
        }

        @PostMapping("/projects")
        public ApiResponse<Project> createProject(@RequestBody ProjectRequest request) {
            Project project = new Project(projectIdCounter++, request.getName(), request.getDescription(), "created");
            projects.put(project.getId(), project);
            return new ApiResponse<>(200, "success", project);
        }

        @GetMapping("/projects/{id}")
        public ApiResponse<Project> getProject(@PathVariable Long id) {
            Project project = projects.get(id);
            if (project == null) {
                return new ApiResponse<>(404, "项目不存在", null);
            }
            return new ApiResponse<>(200, "success", project);
        }

        @PutMapping("/projects/{id}")
        public ApiResponse<Project> updateProject(@PathVariable Long id, @RequestBody ProjectRequest request) {
            Project project = projects.get(id);
            if (project == null) {
                return new ApiResponse<>(404, "项目不存在", null);
            }
            project.setName(request.getName());
            project.setDescription(request.getDescription());
            return new ApiResponse<>(200, "success", project);
        }

        @DeleteMapping("/projects/{id}")
        public ApiResponse<Boolean> deleteProject(@PathVariable Long id) {
            boolean removed = projects.remove(id) != null;
            if (!removed) {
                return new ApiResponse<>(404, "项目不存在", false);
            }
            return new ApiResponse<>(200, "success", true);
        }

        /**
         * 部署管理相关端点
         */
        @PostMapping("/deployments")
        public ApiResponse<Deployment> createDeployment(@RequestBody DeploymentRequest request) {
            Deployment deployment = new Deployment(deploymentIdCounter++, request.getProjectId(), request.getEnvironment(), "pending");
            deployments.put(deployment.getId(), deployment);
            return new ApiResponse<>(200, "success", deployment);
        }

        @GetMapping("/deployments")
        public ApiResponse<List<Deployment>> getDeployments() {
            return new ApiResponse<>(200, "success", new ArrayList<>(deployments.values()));
        }

        @GetMapping("/deployments/{id}")
        public ApiResponse<Deployment> getDeployment(@PathVariable Long id) {
            Deployment deployment = deployments.get(id);
            if (deployment == null) {
                return new ApiResponse<>(404, "部署不存在", null);
            }
            return new ApiResponse<>(200, "success", deployment);
        }

        @PutMapping("/deployments/{id}/status")
        public ApiResponse<Deployment> updateDeploymentStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest request) {
            Deployment deployment = deployments.get(id);
            if (deployment == null) {
                return new ApiResponse<>(404, "部署不存在", null);
            }
            deployment.setStatus(request.getStatus());
            return new ApiResponse<>(200, "success", deployment);
        }

        /**
         * 部署统计相关端点
         */
        @GetMapping("/deployments/statistics")
        public ApiResponse<DeploymentStatistics> getDeploymentStatistics() {
            long total = deployments.size();
            long successful = deployments.values().stream().filter(d -> "success".equals(d.getStatus())).count();
            long failed = deployments.values().stream().filter(d -> "failed".equals(d.getStatus())).count();

            DeploymentStatistics stats = new DeploymentStatistics();
            stats.setTotalDeployments(total);
            stats.setSuccessfulDeployments(successful);
            stats.setFailedDeployments(failed);
            stats.setSuccessRate(total > 0 ? (successful * 100.0 / total) : 0.0);

            return new ApiResponse<>(200, "success", stats);
        }

        /**
         * 请求和响应模型
         */
        public static class LoginRequest {
            private String username;
            private String password;

            // Getters and Setters
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
        }

        public static class RegisterRequest {
            private String username;
            private String password;
            private String email;

            // Getters and Setters
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

            public String getEmail() {
                return email;
            }

            public void setEmail(String email) {
                this.email = email;
            }
        }

        public static class ProjectRequest {
            private String name;
            private String description;

            // Getters and Setters
            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getDescription() {
                return description;
            }

            public void setDescription(String description) {
                this.description = description;
            }
        }

        public static class DeploymentRequest {
            private Long projectId;
            private String environment;

            // Getters and Setters
            public Long getProjectId() {
                return projectId;
            }

            public void setProjectId(Long projectId) {
                this.projectId = projectId;
            }

            public String getEnvironment() {
                return environment;
            }

            public void setEnvironment(String environment) {
                this.environment = environment;
            }
        }

        public static class StatusUpdateRequest {
            private String status;

            // Getters and Setters
            public String getStatus() {
                return status;
            }

            public void setStatus(String status) {
                this.status = status;
            }
        }

        /**
         * 数据模型
         */
        public static class User {
            private String username;
            private String password;
            private String email;

            public User(String username, String password, String email) {
                this.username = username;
                this.password = password;
                this.email = email;
            }

            // Getters and Setters
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

            public String getEmail() {
                return email;
            }

            public void setEmail(String email) {
                this.email = email;
            }
        }

        public static class Project {
            private Long id;
            private String name;
            private String description;
            private String status;

            public Project(Long id, String name, String description, String status) {
                this.id = id;
                this.name = name;
                this.description = description;
                this.status = status;
            }

            // Getters and Setters
            public Long getId() {
                return id;
            }

            public void setId(Long id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getDescription() {
                return description;
            }

            public void setDescription(String description) {
                this.description = description;
            }

            public String getStatus() {
                return status;
            }

            public void setStatus(String status) {
                this.status = status;
            }
        }

        public static class Deployment {
            private Long id;
            private Long projectId;
            private String environment;
            private String status;

            public Deployment(Long id, Long projectId, String environment, String status) {
                this.id = id;
                this.projectId = projectId;
                this.environment = environment;
                this.status = status;
            }

            // Getters and Setters
            public Long getId() {
                return id;
            }

            public void setId(Long id) {
                this.id = id;
            }

            public Long getProjectId() {
                return projectId;
            }

            public void setProjectId(Long projectId) {
                this.projectId = projectId;
            }

            public String getEnvironment() {
                return environment;
            }

            public void setEnvironment(String environment) {
                this.environment = environment;
            }

            public String getStatus() {
                return status;
            }

            public void setStatus(String status) {
                this.status = status;
            }
        }

        public static class DeploymentStatistics {
            private long totalDeployments;
            private long successfulDeployments;
            private long failedDeployments;
            private double successRate;

            // Getters and Setters
            public long getTotalDeployments() {
                return totalDeployments;
            }

            public void setTotalDeployments(long totalDeployments) {
                this.totalDeployments = totalDeployments;
            }

            public long getSuccessfulDeployments() {
                return successfulDeployments;
            }

            public void setSuccessfulDeployments(long successfulDeployments) {
                this.successfulDeployments = successfulDeployments;
            }

            public long getFailedDeployments() {
                return failedDeployments;
            }

            public void setFailedDeployments(long failedDeployments) {
                this.failedDeployments = failedDeployments;
            }

            public double getSuccessRate() {
                return successRate;
            }

            public void setSuccessRate(double successRate) {
                this.successRate = successRate;
            }
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

            // Getters and Setters
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
}
