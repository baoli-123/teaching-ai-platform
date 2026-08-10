package com.example.teachingai.controller;

import com.example.teachingai.annotation.AuditLog;
import com.example.teachingai.annotation.PreventDuplicateSubmit;
import com.example.teachingai.annotation.RateLimit;
import com.example.teachingai.dto.ApiResponse;
import com.example.teachingai.dto.ExamSubmitRequest;
import com.example.teachingai.entity.ExamRecord;
import com.example.teachingai.entity.Question;
import com.example.teachingai.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @GetMapping("/courses/{courseId}/questions")
    public ApiResponse<List<Question>> questions(@PathVariable Long courseId) {
        return ApiResponse.ok(examService.listQuestions(courseId));
    }

    @PostMapping("/exams/submit")
    @RateLimit(key = "exam", limit = 10, windowSeconds = 60)
    @PreventDuplicateSubmit(key = "exam", expireSeconds = 10)
    @AuditLog("提交在线考试")
    public ApiResponse<Map<String, Object>> submit(@RequestBody ExamSubmitRequest request) {
        return ApiResponse.ok(examService.submit(request));
    }

    @GetMapping("/exams/records")
    public ApiResponse<List<ExamRecord>> records(@RequestParam(required = false) Long courseId) {
        return ApiResponse.ok(examService.listRecords(courseId));
    }
}
