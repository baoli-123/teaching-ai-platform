package com.example.teachingai.service;

import com.example.teachingai.entity.ExamRecord;
import com.example.teachingai.mapper.ChatMessageMapper;
import com.example.teachingai.mapper.CourseMapper;
import com.example.teachingai.mapper.ExamRecordMapper;
import com.example.teachingai.mapper.QuestionMapper;
import com.example.teachingai.mapper.ResourceItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CourseMapper courseMapper;
    private final ResourceItemMapper resourceItemMapper;
    private final QuestionMapper questionMapper;
    private final ExamRecordMapper examRecordMapper;
    private final ChatMessageMapper chatMessageMapper;

    public Map<String, Object> stats() {
        List<ExamRecord> records = examRecordMapper.selectList(null);
        double avgScore = records.stream()
                .filter(record -> record.getTotal() != null && record.getTotal() > 0)
                .mapToDouble(record -> record.getScore() * 100.0 / record.getTotal())
                .average()
                .orElse(0);

        Map<String, Object> result = new HashMap<>();
        result.put("courseCount", courseMapper.selectCount(null));
        result.put("resourceCount", resourceItemMapper.selectCount(null));
        result.put("questionCount", questionMapper.selectCount(null));
        result.put("examCount", examRecordMapper.selectCount(null));
        result.put("chatCount", chatMessageMapper.selectCount(null));
        result.put("avgScore", Math.round(avgScore * 10) / 10.0);
        return result;
    }
}
