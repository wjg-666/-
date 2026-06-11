package com.blog.config;

import com.blog.entity.User;
import com.blog.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitRunner implements CommandLineRunner {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public DataInitRunner(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userService.getByUsername("admin") == null) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNickname("系统管理员");
            admin.setRole(0);
            userService.save(admin);
            System.out.println("[INIT] Admin account created: admin / admin123");
        }
    }
}
