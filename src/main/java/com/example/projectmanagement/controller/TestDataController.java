package com.example.projectmanagement.controller;

import com.example.projectmanagement.controller.BaseController.ApiResponse;
import com.example.projectmanagement.entity.Project;
import com.example.projectmanagement.entity.User;
import com.example.projectmanagement.service.ProjectService;
import com.example.projectmanagement.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test-data")
public class TestDataController extends BaseController {

    private final UserService userService;
    private final ProjectService projectService;
    private final BCryptPasswordEncoder passwordEncoder;

    public TestDataController(UserService userService, ProjectService projectService, BCryptPasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.projectService = projectService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/seed")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> seedTestData(@RequestParam(defaultValue = "3") Integer userCount,
                                                         @RequestParam(defaultValue = "5") Integer projectCount) {
        int createdUsers = 0;
        int createdProjects = 0;

        for (int i = 0; i < userCount; i++) {
            String username = "demo_user_" + System.currentTimeMillis() + "_" + i;
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode("admin123"));
            user.setNickname("演示用户" + (i + 1));
            user.setEmail(username + "@example.com");
            user.setPhone("1350000" + String.format("%04d", i));
            user.setStatus(1);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            if (userService.save(user)) {
                createdUsers++;
            }
        }

        List<Long> leaderIds = userService.list().stream().map(User::getId).toList();
        List<String> techStacks = List.of("Spring Boot", "SSM", "Spring Cloud");
        List<String> statuses = List.of("planning", "developing", "testing", "deployed");
        for (int i = 0; i < projectCount; i++) {
            Project project = new Project();
            project.setName("测试项目-" + System.currentTimeMillis() + "-" + (i + 1));
            project.setDescription("用于演示项目列表、筛选、AI 分析和管理功能的测试数据");
            project.setTechStack(techStacks.get(i % techStacks.size()));
            project.setStatus(statuses.get(i % statuses.size()));
            project.setLeaderId(leaderIds.get(i % leaderIds.size()));
            project.setProgress((i * 17 + 28) % 100);
            if (projectService.createProject(project)) {
                createdProjects++;
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("createdUsers", createdUsers);
        result.put("createdProjects", createdProjects);
        result.put("defaultPassword", "admin123");
        return success(result);
    }
}
