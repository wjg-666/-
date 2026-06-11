-- ========================================
-- 轻型博客系统数据库初始化脚本
-- 数据库: blog_db
-- ========================================

-- 设置 MySQL 模式（兼容性问题）
SET SESSION sql_mode = 'NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION';

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS blog_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE blog_db;

-- ========================================
-- 用户表
-- ========================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(Bcrypt加密)',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `role` TINYINT DEFAULT 1 COMMENT '角色: 0=管理员, 1=博主',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0=禁用, 1=正常',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ========================================
-- 帖子表
-- ========================================
DROP TABLE IF EXISTS `post`;
CREATE TABLE `post` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `content` LONGTEXT NOT NULL COMMENT '内容(Markdown格式)',
    `author_id` BIGINT NOT NULL COMMENT '作者ID',
    `cover_image` VARCHAR(255) DEFAULT NULL COMMENT '封面图URL',
    `view_count` INT DEFAULT 0 COMMENT '浏览量',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0=草稿, 1=已发布',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_author_id` (`author_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子表';

-- ========================================
-- 评论表
-- ========================================
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `post_id` BIGINT NOT NULL COMMENT '所属帖子ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '评论用户ID(游客为NULL)',
    `guest_name` VARCHAR(50) DEFAULT NULL COMMENT '游客昵称',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `parent_id` BIGINT DEFAULT NULL COMMENT '回复的评论ID(支持楼中楼)',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- ========================================
-- 初始化测试数据
-- ========================================

-- 插入管理员账号 (密码: admin123)
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 0, 1),
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试博主', 1, 1);

-- 插入示例帖子
INSERT INTO `post` (`title`, `content`, `author_id`, `cover_image`, `view_count`, `status`) VALUES
('欢迎使用轻型博客系统', '# 欢迎来到博客系统\n\n这是一个使用 Spring Boot + Vue 3 构建的轻型博客系统。\n\n## 主要功能\n\n- 📝 发布和管理文章\n- 💬 评论和回复\n- 👤 用户权限管理\n- 📊 管理后台统计\n\n## 技术栈\n\n- **后端**: Spring Boot 3.x + MyBatis-Plus + MySQL\n- **前端**: Vue 3 + Vite + Element Plus\n- **认证**: JWT + Spring Security\n\n快来体验吧！', 1, NULL, 128, 1),
('Spring Boot 入门教程', '# Spring Boot 快速入门\n\nSpring Boot 是目前最流行的 Java 微服务框架之一。\n\n## 创建第一个项目\n\n1. 访问 [Spring Initializr](https://start.spring.io/)\n2. 选择依赖：Web, Thymeleaf\n3. 下载并导入 IDE\n\n## 示例代码\n\n```java\n@SpringBootApplication\npublic class Application {\n    public static void main(String[] args) {\n        SpringApplication.run(Application.class, args);\n    }\n}\n```\n\n## 下一步\n\n- 学习 Spring Data JPA\n- 整合 MyBatis\n- 实现 RESTful API', 1, NULL, 256, 1),
('Vue 3 组合式 API 指南', '# Vue 3 组合式 API\n\nVue 3 引入了全新的组合式 API，让代码组织更加灵活。\n\n## 基本用法\n\n```javascript\nimport { ref, computed, onMounted } from ''vue''\n\nexport default {\n  setup() {\n    const count = ref(0)\n    const double = computed(() => count.value * 2)\n    \n    onMounted(() => {\n      console.log(''Component mounted'')\n    })\n    \n    return { count, double }\n  }\n}\n```\n\n## 优势\n\n- 更好的逻辑复用\n- 更灵活的代码组织\n- 更好的 TypeScript 支持', 2, NULL, 189, 1);

-- 插入示例评论
INSERT INTO `comment` (`post_id`, `user_id`, `guest_name`, `content`, `parent_id`) VALUES
(1, NULL, '游客小明', '这个系统看起来很棒！', NULL),
(1, 2, NULL, '感谢分享，已经注册体验了', 1),
(2, NULL, 'Java学习者', '讲解得很清晰，收藏了！', NULL),
(3, NULL, '前端新手', '组合式 API 确实比 Options API 更好用', NULL);

-- ========================================
-- 完成提示
-- ========================================
SELECT '数据库初始化完成！' AS 'Status';
SELECT COUNT(*) AS '用户数' FROM user;
SELECT COUNT(*) AS '帖子数' FROM post;
SELECT COUNT(*) AS '评论数' FROM comment;