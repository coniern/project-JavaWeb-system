package com.example.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.project.entity.Project;
import com.example.project.entity.Task;
import com.example.project.entity.User;
import com.example.project.mapper.ProjectMapper;
import com.example.project.mapper.TaskMapper;
import com.example.project.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectManagementApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Test
    void shouldLoginWithDefaultAdmin() throws Exception {
        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("token-1"))
                .andExpect(jsonPath("$.data.user.username").value("admin"));
    }

    @Test
    void shouldReturnProjectList() throws Exception {
        mockMvc.perform(get("/api/project/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(greaterThanOrEqualTo(1)));
    }

    @Test
    void shouldCompleteBusinessWorkflow() throws Exception {
        String username = "workflow_user";
        String projectName = "工作流验证项目";
        String taskTitle = "工作流验证任务";

        mockMvc.perform(post("/api/user/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "123456",
                                  "nickname": "流程用户",
                                  "email": "workflow@example.com",
                                  "status": 1
                                }
                                """.formatted(username)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        Long userId = findUserId(username);

        mockMvc.perform(put("/api/user/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": %d,
                                  "nickname": "流程用户已更新",
                                  "email": "workflow-updated@example.com",
                                  "status": 1
                                }
                                """.formatted(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/api/project/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "description": "用于验证项目 CRUD 流程",
                                  "status": "规划中",
                                  "ownerId": %d,
                                  "progress": 15,
                                  "startDate": "2026-06-22",
                                  "endDate": "2026-07-15"
                                }
                                """.formatted(projectName, userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        Long projectId = findProjectId(projectName);

        mockMvc.perform(put("/api/project/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": %d,
                                  "name": "%s",
                                  "description": "用于验证项目更新流程",
                                  "status": "进行中",
                                  "ownerId": %d,
                                  "progress": 55,
                                  "startDate": "2026-06-22",
                                  "endDate": "2026-07-20"
                                }
                                """.formatted(projectId, projectName, userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/api/task/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "projectId": %d,
                                  "assigneeId": %d,
                                  "title": "%s",
                                  "description": "用于验证任务 CRUD 流程",
                                  "status": "待开始",
                                  "priority": 2,
                                  "dueDate": "2026-07-01"
                                }
                                """.formatted(projectId, userId, taskTitle)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        Long taskId = findTaskId(taskTitle);

        mockMvc.perform(put("/api/task/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": %d,
                                  "projectId": %d,
                                  "assigneeId": %d,
                                  "title": "%s",
                                  "description": "用于验证任务更新流程",
                                  "status": "进行中",
                                  "priority": 1,
                                  "dueDate": "2026-07-02"
                                }
                                """.formatted(taskId, projectId, userId, taskTitle)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/project/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/task/stats/" + projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(delete("/api/task/delete/" + taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(delete("/api/project/delete/" + projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(delete("/api/user/delete/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldReturnAiSuggestions() throws Exception {
        mockMvc.perform(post("/api/ai/assist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "AI 验证项目",
                                  "description": "验证 AI 建议接口是否可用",
                                  "goal": "快速形成任务拆解",
                                  "teamSize": 3,
                                  "deadline": "2026-07-30"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.summary").isNotEmpty())
                .andExpect(jsonPath("$.data.taskSuggestions.length()").value(greaterThanOrEqualTo(1)));
    }

    private Long findUserId(String username) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .eq(User::getDeleted, 0));
        if (user != null) {
            return user.getId();
        }
        throw new IllegalStateException("未找到用户: " + username);
    }

    private Long findProjectId(String projectName) {
        Project project = projectMapper.selectOne(new LambdaQueryWrapper<Project>()
                .eq(Project::getName, projectName)
                .eq(Project::getDeleted, 0));
        if (project != null) {
            return project.getId();
        }
        throw new IllegalStateException("未找到项目: " + projectName);
    }

    private Long findTaskId(String taskTitle) {
        Task task = taskMapper.selectOne(new LambdaQueryWrapper<Task>()
                .eq(Task::getTitle, taskTitle)
                .eq(Task::getDeleted, 0));
        if (task != null) {
            return task.getId();
        }
        throw new IllegalStateException("未找到任务: " + taskTitle);
    }
}
