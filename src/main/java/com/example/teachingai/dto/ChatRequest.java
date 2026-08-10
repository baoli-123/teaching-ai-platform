package com.example.teachingai.dto;

import lombok.Data;

@Data
public class ChatRequest {
    private String question;
    private Long courseId;
}
