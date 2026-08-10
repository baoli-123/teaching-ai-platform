package com.example.teachingai.config;

import com.example.teachingai.dto.ChatRequest;
import com.example.teachingai.dto.ChatResponse;
import com.example.teachingai.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    public static final Set<WebSocketSession> SESSIONS = ConcurrentHashMap.newKeySet();

    private final ChatService chatService;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        SESSIONS.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        SESSIONS.remove(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        ChatRequest request = objectMapper.readValue(message.getPayload(), ChatRequest.class);
        session.sendMessage(new TextMessage(
                objectMapper.writeValueAsString(Map.of("type", "status", "message", "正在检索知识库..."))
        ));
        ChatResponse response = chatService.answer(request);
        List<String> chunks = splitAnswer(response.getAnswer());
        for (String chunk : chunks) {
            session.sendMessage(new TextMessage(
                    objectMapper.writeValueAsString(Map.of("type", "chunk", "content", chunk))
            ));
        }
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    private List<String> splitAnswer(String answer) {
        List<String> chunks = new ArrayList<>();
        String[] lines = answer.split("(?<=[。！？\n])");
        for (String line : lines) {
            if (!line.isBlank()) {
                chunks.add(line.trim());
            }
        }
        return chunks;
    }
}
