package com.example.teachingai.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.teachingai.entity.Course;
import com.example.teachingai.entity.ResourceItem;
import com.example.teachingai.mapper.CourseMapper;
import com.example.teachingai.mapper.ResourceItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseMapper courseMapper;
    private final ResourceItemMapper resourceItemMapper;

    public List<Course> listCourses() {
        return courseMapper.selectList(null);
    }

    public List<ResourceItem> listResources(Long courseId) {
        return resourceItemMapper.selectList(
                Wrappers.<ResourceItem>lambdaQuery()
                        .eq(courseId != null, ResourceItem::getCourseId, courseId)
                        .orderByAsc(ResourceItem::getId)
        );
    }
}
