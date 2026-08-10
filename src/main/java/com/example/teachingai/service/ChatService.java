package com.example.teachingai.service;

import com.example.teachingai.dto.ChatRequest;
import com.example.teachingai.dto.ChatResponse;
import com.example.teachingai.entity.ChatMessage;
import com.example.teachingai.mapper.ChatMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final RagService ragService;
    private final ChatMessageMapper chatMessageMapper;

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
}
