package com.example.teachingai.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.teachingai.entity.LearningProgress;
import com.example.teachingai.mapper.LearningProgressMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ProgressSyncService {

    private final LearningProgressMapper progressMapper;
    private final Map<String, LearningProgress> buffer = new ConcurrentHashMap<>();

    public void record(String username, Long courseId, Long resourceId, Integer progress) {
        String key = username + ":" + courseId + ":" + resourceId;
        LearningProgress item = new LearningProgress();
        item.setUsername(username);
        item.setCourseId(courseId);
        item.setResourceId(resourceId);
        item.setProgress(progress);
        item.setUpdatedAt(LocalDateTime.now());
        buffer.put(key, item);
    }

    public List<LearningProgress> listByUser(String username) {
        return progressMapper.selectList(
                Wrappers.<LearningProgress>lambdaQuery()
                        .eq(LearningProgress::getUsername, username)
                        .orderByDesc(LearningProgress::getUpdatedAt)
        );
    }

    @Scheduled(fixedDelay = 300000, initialDelay = 15000)
    public void flushToDatabase() {
        buffer.forEach((key, item) -> {
            LearningProgress existing = progressMapper.selectOne(
                    Wrappers.<LearningProgress>lambdaQuery()
                            .eq(LearningProgress::getUsername, item.getUsername())
                            .eq(LearningProgress::getCourseId, item.getCourseId())
                            .eq(LearningProgress::getResourceId, item.getResourceId())
            );
            if (existing == null) {
                progressMapper.insert(item);
            } else {
                existing.setProgress(item.getProgress());
                existing.setUpdatedAt(item.getUpdatedAt());
                progressMapper.updateById(existing);
            }
            buffer.remove(key);
        });
    }
}
