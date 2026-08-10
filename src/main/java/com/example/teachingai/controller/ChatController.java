package com.example.teachingai.controller;

import com.example.teachingai.annotation.AuditLog;
import com.example.teachingai.annotation.RateLimit;
import com.example.teachingai.dto.ApiResponse;
import com.example.teachingai.dto.ChatRequest;
import com.example.teachingai.dto.ChatResponse;
import com.example.teachingai.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/chat")
    @RateLimit(key = "chat", limit = 20, windowSeconds = 60)
    @AuditLog("AI 智能问答")
    public ApiResponse<ChatResponse> chat(@RequestBody ChatRequest request) {
        return ApiResponse.ok(chatService.answer(request));
    }
}
