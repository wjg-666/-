-- 插入用户数据 (密码是 BCrypt 加密后的 admin123)
INSERT IGNORE INTO user (username, password, nickname, role, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '系统管理员', 0, 1),
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '测试用户', 1, 1);

-- 插入分类
INSERT IGNORE INTO category (id, name) VALUES
(1, '技术'), (2, '前端'), (3, '后端'), (4, '生活'), (5, '随笔');

-- 插入标签
INSERT IGNORE INTO tag (id, name) VALUES
(1, 'Spring Boot'), (2, 'Java'), (3, 'Vue'), (4, 'JavaScript'), (5, '入门');

-- 插入文章
INSERT IGNORE INTO post (title, content, author_id, category_id, view_count, status) VALUES
('欢迎来到博客系统', '这是博客系统的第一篇文章，欢迎大家访问！', 1, 1, 100, 1),
('Spring Boot 入门教程', '本文介绍Spring Boot的基本使用方法...', 2, 3, 50, 1),
('我的第一篇随笔', '今天天气很好，出门散散步...', 2, 5, 30, 1);

-- 文章-标签关联
INSERT IGNORE INTO post_tag (post_id, tag_id) VALUES
(1, 3), (1, 4),
(2, 1), (2, 2), (2, 5);

-- 插入评论
INSERT IGNORE INTO comment (content, post_id, user_id) VALUES
('写得很好！', 1, 2),
('期待更多文章', 2, 1);
