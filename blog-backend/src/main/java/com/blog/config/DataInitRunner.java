package com.blog.config;

import com.blog.entity.User;
import com.blog.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitRunner implements CommandLineRunner {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public DataInitRunner(UserService userService, PasswordEncoder passwordEncoder,
                          JdbcTemplate jdbcTemplate) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        // 自动建表（新表）和迁移（旧表加列）
        initSchema();

        // 初始化管理员账号
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

    private void initSchema() {
        try {
            // 新表：分类、标签、关联表、点赞表
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS category (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(50) NOT NULL UNIQUE,
                    create_time DATETIME
                )
            """);
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS tag (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(50) NOT NULL UNIQUE,
                    create_time DATETIME
                )
            """);
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS post_tag (
                    post_id BIGINT NOT NULL,
                    tag_id BIGINT NOT NULL,
                    PRIMARY KEY (post_id, tag_id)
                )
            """);
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS post_like (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    post_id BIGINT NOT NULL,
                    user_id BIGINT,
                    create_time DATETIME,
                    UNIQUE KEY uk_post_user (post_id, user_id)
                )
            """);

            // 迁移：为已存在的 post 表加 category_id
            try {
                jdbcTemplate.execute(
                    "ALTER TABLE post ADD COLUMN category_id BIGINT AFTER author_id"
                );
            } catch (Exception ignored) {
                // 列已存在则忽略
            }

            // 插入默认分类和标签（如不存在）
            jdbcTemplate.execute("""
                INSERT IGNORE INTO category (id, name) VALUES
                (1, '技术'), (2, '前端'), (3, '后端'), (4, '生活'), (5, '随笔')
            """);
            jdbcTemplate.execute("""
                INSERT IGNORE INTO tag (id, name) VALUES
                (1, 'Spring Boot'), (2, 'Java'), (3, 'Vue'), (4, 'JavaScript'), (5, '入门')
            """);

            System.out.println("[INIT] Schema initialized successfully");
        } catch (Exception e) {
            System.err.println("[INIT] Schema init error: " + e.getMessage());
        }
    }
}
