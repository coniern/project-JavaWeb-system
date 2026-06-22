package com.example.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目管理系统 - 启动类
 * @author 张雅
 */
@SpringBootApplication
public class ProjectManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectManagementApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("🚀 项目管理系统后端服务启动成功！");
        System.out.println("📍 服务地址：http://localhost:8080");
        System.out.println("🔗 接口前缀：http://localhost:8080/api");
        System.out.println("========================================\n");
    }
}
