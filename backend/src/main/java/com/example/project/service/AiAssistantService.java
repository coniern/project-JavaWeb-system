package com.example.project.service;

import com.example.project.dto.AiAssistRequest;
import com.example.project.dto.AiAssistResponse;

public interface AiAssistantService {

    AiAssistResponse assist(AiAssistRequest request);
}
