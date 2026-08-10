package com.example.teachingai.security;

import com.example.teachingai.entity.AppUser;
import com.example.teachingai.mapper.AppUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DemoUserInitializer implements CommandLineRunner {

    private final AppUserMapper appUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (appUserMapper.selectCount(null) > 0) {
            return;
        }
        createUser("admin", "admin123", "管理员", "ADMIN");
        createUser("teacher", "teacher123", "李老师", "TEACHER");
        createUser("student", "student123", "学生用户", "STUDENT");
    }

    private void createUser(String username, String rawPassword, String displayName, String role) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setDisplayName(displayName);
        user.setRole(role);
        user.setEnabled(true);
        appUserMapper.insert(user);
    }
}
