package com.reader.config;

import com.reader.entity.User;
import com.reader.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 启动时初始化默认管理员账号 admin / admin123（仅当不存在时创建）。
 */
@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private final UserMapper userMapper;

    @Override
    public void run(String... args) {
        if (userMapper.findByUsername("admin") == null) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(ENCODER.encode("admin123"));
            admin.setNickname("管理员");
            admin.setRole(1);
            admin.setStatus(1);
            userMapper.insert(admin);
        }
    }
}
