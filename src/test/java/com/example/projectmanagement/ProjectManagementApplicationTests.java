package com.example.projectmanagement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectManagementApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldLoginWithDefaultAdmin() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.username").value("admin"))
                .andExpect(jsonPath("$.data.user.roleCodes[0]").value("ADMIN"));
    }

    @Test
    void shouldReturnProjectListForAuthorizedUser() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(get("/api/projects")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].name").isNotEmpty());
    }

    @Test
    void shouldReturnAiSuggestions() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(post("/api/ai/assist")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "论文演示项目重构",
                                  "description": "统一仓库结构并优化前端交互",
                                  "goal": "在答辩前完成可运行版本",
                                  "teamSize": 3,
                                  "deadline": "2026-07-15"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.summary").isNotEmpty())
                .andExpect(jsonPath("$.data.taskSuggestions[0].title").isNotEmpty());
    }

    @Test
    void shouldCreateTaskThroughUnifiedApi() throws Exception {
        String token = loginAndGetToken();
        String uniqueTitle = "regression-task-" + System.currentTimeMillis();

        MvcResult result = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "projectId": 1,
                                  "title": "%s",
                                  "description": "验证统一任务接口可用",
                                  "status": "todo",
                                  "priority": 2,
                                  "assigneeId": 2,
                                  "dueDate": "2026-07-20"
                                }
                                """.formatted(uniqueTitle)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(root.at("/data/title").asText()).isEqualTo(uniqueTitle);
    }

    private String loginAndGetToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.at("/data/token").asText();
    }
}
