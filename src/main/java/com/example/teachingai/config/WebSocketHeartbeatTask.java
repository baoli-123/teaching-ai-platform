package com.example.teachingai.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebSocketHeartbeatTask {

    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 20000)
    public void heartbeat() throws Exception {
        for (WebSocketSession session : ChatWebSocketHandler.SESSIONS) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(
                        objectMapper.writeValueAsString(Map.of("type", "ping"))
                ));
            } else {
                ChatWebSocketHandler.SESSIONS.remove(session);
            }
        }
    }
}
