package com.example.teachingai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.example.teachingai.mapper")
@EnableScheduling
public class TeachingAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TeachingAiApplication.class, args);
    }
}
