package com.example.projectmanagement.service;

import com.example.projectmanagement.entity.Project;

import java.util.List;
import java.util.Map;

public interface AiInsightService {

    String summarizeProject(Project project);

    String analyzeProjectRisk(Project project);

    String generateDashboardInsight(List<Project> projects);

    String completeCode(String language, String prompt, String codeContext);

    Map<String, Object> getAiStatus();
}
