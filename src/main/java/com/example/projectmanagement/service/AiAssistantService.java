package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.AiAssistRequest;
import com.example.projectmanagement.dto.AiAssistResponse;

public interface AiAssistantService {

    AiAssistResponse assist(AiAssistRequest request);
}
