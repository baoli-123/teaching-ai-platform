package com.example.teachingai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("learning_progress")
public class LearningProgress {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private Long courseId;
    private Long resourceId;
    private Integer progress;
    private LocalDateTime updatedAt;
}
