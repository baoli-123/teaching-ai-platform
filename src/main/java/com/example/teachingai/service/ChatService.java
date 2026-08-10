package com.example.teachingai.service;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.example.teachingai.dto.ChatRequest;
import com.example.teachingai.dto.ChatResponse;
import com.example.teachingai.entity.ChatMessage;
import com.example.teachingai.mapper.ChatMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final RagService ragService;
    private final ChatMessageMapper chatMessageMapper;

    @SentinelResource(value = "chat", fallback = "chatFallback")
    public ChatResponse answer(ChatRequest request) {
        ChatResponse response = ragService.answer(request);
        ChatMessage message = new ChatMessage();
        message.setCourseId(request.getCourseId());
        message.setQuestion(request.getQuestion());
        message.setAnswer(response.getAnswer());
        message.setSources(String.join(", ", response.getSources()));
        chatMessageMapper.insert(message);
        return response;
    }

    public ChatResponse chatFallback(ChatRequest request, Throwable throwable) {
        return new ChatResponse("answer", "系统繁忙，请稍后重试。", List.of(), "sentinel");
    }
}
