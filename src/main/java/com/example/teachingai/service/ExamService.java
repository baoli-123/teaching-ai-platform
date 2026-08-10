package com.example.teachingai.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.teachingai.dto.ExamSubmitRequest;
import com.example.teachingai.entity.ExamRecord;
import com.example.teachingai.entity.Question;
import com.example.teachingai.mapper.ExamRecordMapper;
import com.example.teachingai.mapper.QuestionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExamService {

    private final QuestionMapper questionMapper;
    private final ExamRecordMapper examRecordMapper;

    public List<Question> listQuestions(Long courseId) {
        return questionMapper.selectList(
                Wrappers.<Question>lambdaQuery()
                        .eq(courseId != null, Question::getCourseId, courseId)
                        .orderByAsc(Question::getId)
        );
    }

    public Map<String, Object> submit(ExamSubmitRequest request) {
        List<Question> questions = listQuestions(request.getCourseId());
        int correct = 0;
        List<String> details = new ArrayList<>();

        for (Question question : questions) {
            String userAnswer = request.getAnswers() == null ? null
                    : request.getAnswers().get(String.valueOf(question.getId()));
            boolean isCorrect = question.getAnswer() != null && question.getAnswer().equalsIgnoreCase(userAnswer);
            if (isCorrect) {
                correct++;
            }
            details.add(
                    "第" + question.getId() + "题：" + (isCorrect ? "正确" : "错误")
                            + "，正确答案：" + question.getAnswer()
                            + "，解析：" + question.getAnalysis()
            );
        }

        ExamRecord record = new ExamRecord();
        record.setStudentName(request.getStudentName() == null ? "匿名学员" : request.getStudentName());
        record.setCourseId(request.getCourseId());
        record.setScore(correct);
        record.setTotal(questions.size());
        record.setDetail(String.join("；", details).substring(0, Math.min(2000, String.join("；", details).length())));
        examRecordMapper.insert(record);

        Map<String, Object> result = new HashMap<>();
        result.put("score", correct);
        result.put("total", questions.size());
        result.put("correct", correct);
        result.put("detail", details);
        result.put("recordId", record.getId());
        return result;
    }

    public List<ExamRecord> listRecords(Long courseId) {
        return examRecordMapper.selectList(
                Wrappers.<ExamRecord>lambdaQuery()
                        .eq(courseId != null, ExamRecord::getCourseId, courseId)
                        .orderByDesc(ExamRecord::getCreatedAt)
        );
    }
}
