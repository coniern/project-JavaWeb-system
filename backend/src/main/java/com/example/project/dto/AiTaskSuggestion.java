package com.example.project.dto;

public class AiTaskSuggestion {

    private String title;
    private String description;
    private Integer priority;

    public AiTaskSuggestion() {
    }

    public AiTaskSuggestion(String title, String description, Integer priority) {
        this.title = title;
        this.description = description;
        this.priority = priority;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
