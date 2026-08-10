package com.example.teachingai.controller;

import com.example.teachingai.annotation.AuditLog;
import com.example.teachingai.dto.ApiResponse;
import com.example.teachingai.entity.LearningProgress;
import com.example.teachingai.security.UserPrincipal;
import com.example.teachingai.service.ProgressSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressSyncService progressSyncService;

    @PostMapping
    @AuditLog("记录学习进度")
    public ApiResponse<String> record(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Object> body
    ) {
        progressSyncService.record(
                principal.getUsername(),
                Long.valueOf(String.valueOf(body.get("courseId"))),
                Long.valueOf(String.valueOf(body.get("resourceId"))),
                Integer.valueOf(String.valueOf(body.get("progress")))
        );
        return ApiResponse.ok("recorded");
    }

    @GetMapping
    public ApiResponse<List<LearningProgress>> list(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(progressSyncService.listByUser(principal.getUsername()));
    }
}
