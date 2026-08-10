package com.example.teachingai.controller;

import com.example.teachingai.dto.ApiResponse;
import com.example.teachingai.entity.Course;
import com.example.teachingai.entity.ResourceItem;
import com.example.teachingai.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping("/courses")
    public ApiResponse<List<Course>> courses() {
        return ApiResponse.ok(courseService.listCourses());
    }

    @GetMapping("/courses/{courseId}/resources")
    public ApiResponse<List<ResourceItem>> resources(@PathVariable Long courseId) {
        return ApiResponse.ok(courseService.listResources(courseId));
    }
}
