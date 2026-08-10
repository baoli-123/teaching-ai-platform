package com.example.teachingai.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ExamSubmitRequest {
    private String studentName;
    private Long courseId;
    private Map<String, String> answers;
}
