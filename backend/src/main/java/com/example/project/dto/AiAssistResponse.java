package com.example.project.dto;

import java.util.ArrayList;
import java.util.List;

public class AiAssistResponse {

    private String summary;
    private List<String> riskTips = new ArrayList<>();
    private List<String> milestoneSuggestions = new ArrayList<>();
    private List<AiTaskSuggestion> taskSuggestions = new ArrayList<>();
    private String provider;
    private boolean usedFallback;

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getRiskTips() {
        return riskTips;
    }

    public void setRiskTips(List<String> riskTips) {
        this.riskTips = riskTips;
    }

    public List<String> getMilestoneSuggestions() {
        return milestoneSuggestions;
    }

    public void setMilestoneSuggestions(List<String> milestoneSuggestions) {
        this.milestoneSuggestions = milestoneSuggestions;
    }

    public List<AiTaskSuggestion> getTaskSuggestions() {
        return taskSuggestions;
    }

    public void setTaskSuggestions(List<AiTaskSuggestion> taskSuggestions) {
        this.taskSuggestions = taskSuggestions;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public boolean isUsedFallback() {
        return usedFallback;
    }

    public void setUsedFallback(boolean usedFallback) {
        this.usedFallback = usedFallback;
    }
}
