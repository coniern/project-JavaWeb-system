package com.example.project.controller;

import com.example.project.common.Result;
import com.example.project.dto.AiAssistRequest;
import com.example.project.dto.AiAssistResponse;
import com.example.project.service.AiAssistantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin
public class AiController {

    @Autowired
    private AiAssistantService aiAssistantService;

    @PostMapping("/assist")
    public Result<AiAssistResponse> assist(@RequestBody AiAssistRequest request) {
        try {
            return Result.success(aiAssistantService.assist(request));
        } catch (IllegalArgumentException exception) {
            return Result.error(400, exception.getMessage());
        }
    }
}
