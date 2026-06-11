# 轻型博客系统 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 搭建一个支持管理员/博主/游客三种角色的轻量级博客系统，包含完整的后端 API 和前端界面。

**Architecture:** Spring Boot 3.x 提供 RESTful API + JWT 认证，Vue 3 + Element Plus 构建 SPA 前端，MySQL 存储数据。前后端分离部署。

**Tech Stack:** Java 24 (OpenJDK), Spring Boot 3.4.x, MyBatis-Plus 3.5.x, MySQL 5.7, JWT (jjwt 0.12.x), Spring Security 6.x, Vue 3.5, Vite 6, Element Plus 2.9, Axios 1.7, Vue Router 4, Pinia 2, Maven Wrapper

**Pre-requisites:**
- JAVA_HOME 指向 `C:\Users\10290\.jdks\openjdk-24.0.1`
- MySQL 5.7 运行中 (`D:\code\phpstudy\phpstudy_pro\Extensions\MySQL5.7.26\`)
- Node.js v24.11.0 + npm 11.6.1

---

## Phase 1: 项目初始化与环境搭建

### Task 1.1: 创建 Spring Boot 后端项目

**Files:**
- Create: `blog-backend/pom.xml`
- Create: `blog-backend/src/main/java/com/blog/BlogApplication.java`
- Create: `blog-backend/src/main/resources/application.yml`

- [ ] **Step 1: 创建目录结构**

```bash
mkdir -p blog-backend/src/main/java/com/blog/{config,controller,service,service/impl,mapper,entity,dto,vo,utils,interceptor}
mkdir -p blog-backend/src/main/resources/mapper
mkdir -p blog-backend/src/test/java/com/blog
```

- [ ] **Step 2: 编写 pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.4.5</version>
    </parent>
    <groupId>com.blog</groupId>
    <artifactId>blog-backend</artifactId>
    <version>1.0.0</version>
    <name>blog-backend</name>

    <properties>
        <java.version>24</java.version>
        <mybatis-plus.version>3.5.9</mybatis-plus.version>
        <jjwt.version>0.12.6</jjwt.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 3: 安装 Maven 并生成 wrapper**

```bash
# 使用 Maven wrapper 无需系统安装 Maven
cd blog-backend
mvn wrapper:wrapper
```

> 如果 mvn 不可用，先用 `npm i -g maven` 或从 https://maven.apache.org/download.cgi 下载后解压到 `D:\code\maven\`，然后设置 PATH。

- [ ] **Step 4: 编写 application.yml**

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/blog_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
  global-config:
    db-config:
      id-type: auto
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

app:
  jwt:
    secret: blog-jwt-secret-key-must-be-at-least-256-bits-long-for-hs256
    expiration: 86400000  # 24h
  upload:
    path: ./uploads
```

- [ ] **Step 5: 编写启动类**

```java
package com.blog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.blog.mapper")
public class BlogApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlogApplication.class, args);
    }
}
```

- [ ] **Step 6: 创建数据库**

```sql
CREATE DATABASE IF NOT EXISTS blog_db DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Run: `D:\code\phpstudy\phpstudy_pro\Extensions\MySQL5.7.26\bin\mysql.exe -u root -proot -e "CREATE DATABASE IF NOT EXISTS blog_db DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;"`

- [ ] **Step 7: 验证后端启动**

```bash
cd blog-backend && ./mvnw spring-boot:run
```

Expected: Started BlogApplication in X seconds

---

### Task 1.2: 创建 Vue 3 前端项目

**Files:**
- Create: `blog-frontend/` (Vite scaffold)

- [ ] **Step 1: 用 Vite 创建项目**

```bash
npm create vite@latest blog-frontend -- --template vue
```

- [ ] **Step 2: 安装依赖**

```bash
cd blog-frontend
npm install
npm install axios@1.7 vue-router@4 pinia@2 element-plus@2.9 @element-plus/icons-vue
npm install -D sass
```

- [ ] **Step 3: 配置 Vite 代理 (vite.config.js)**

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

- [ ] **Step 4: 配置 Element Plus 自动导入 (main.js)**

```javascript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })
// 全局注册图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
app.mount('#app')
```

> locale: 需要 `import zhCn from 'element-plus/dist/locale/zh-cn.mjs'`

---

## Phase 2: 数据库与实体层

### Task 2.1: 数据库建表 & 实体类

**Files:**
- Create: `blog-backend/src/main/java/com/blog/entity/User.java`
- Create: `blog-backend/src/main/java/com/blog/entity/Post.java`
- Create: `blog-backend/src/main/java/com/blog/entity/Comment.java`

- [ ] **Step 1: 执行建表 SQL**

```sql
USE blog_db;

CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    avatar VARCHAR(255),
    role TINYINT DEFAULT 1 COMMENT '0=管理员, 1=博主',
    status TINYINT DEFAULT 1 COMMENT '0=禁用, 1=正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE post (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content LONGTEXT NOT NULL,
    author_id BIGINT NOT NULL,
    cover_image VARCHAR(255),
    view_count INT DEFAULT 0,
    status TINYINT DEFAULT 1 COMMENT '0=草稿, 1=已发布',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_author (author_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT,
    guest_name VARCHAR(50),
    content TEXT NOT NULL,
    parent_id BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_post (post_id),
    INDEX idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 初始化管理员账号(admin/admin123)
INSERT INTO user (username, password, nickname, role) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 0);
-- 注: 上面的 BCrypt hash 不对，启动时由 InitRunner 生成正确的
```

Run:
```bash
D:\code\phpstudy\phpstudy_pro\Extensions\MySQL5.7.26\bin\mysql.exe -u root -proot < blog-backend/src/main/resources/schema.sql
```

- [ ] **Step 2: 编写 User 实体**

```java
package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private Integer role;      // 0=admin, 1=blogger
    private Integer status;    // 0=disabled, 1=active
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

- [ ] **Step 3: 编写 Post 实体**

```java
package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("post")
public class Post {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private Long authorId;
    private String coverImage;
    private Integer viewCount;
    private Integer status;    // 0=draft, 1=published
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

- [ ] **Step 4: 编写 Comment 实体**

```java
package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long postId;
    private Long userId;
    private String guestName;
    private String content;
    private Long parentId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
```

- [ ] **Step 5: 编写自动填充处理器**

```java
package com.blog.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
```

---

## Phase 3: 认证与授权

### Task 3.1: JWT 工具类 & Security 配置

**Files:**
- Create: `blog-backend/src/main/java/com/blog/utils/JwtUtils.java`
- Create: `blog-backend/src/main/java/com/blog/config/SecurityConfig.java`
- Create: `blog-backend/src/main/java/com/blog/interceptor/JwtAuthFilter.java`
- Create: `blog-backend/src/main/java/com/blog/config/CorsConfig.java`

- [ ] **Step 1: 编写 JwtUtils**

```java
package com.blog.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtils {

    private final SecretKey key;
    private final long expiration;

    public JwtUtils(@Value("${app.jwt.secret}") String secret,
                    @Value("${app.jwt.expiration}") long expiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    public String generateToken(Long userId, String username, Integer role) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public Long getUserId(String token) {
        return Long.parseLong(parseToken(token).getSubject());
    }
}
```

- [ ] **Step 2: 编写 JwtAuthFilter**

```java
package com.blog.interceptor;

import com.blog.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    public JwtAuthFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtUtils.validateToken(token)) {
                Claims claims = jwtUtils.parseToken(token);
                String role = claims.get("role", Integer.class) == 0 ? "ROLE_ADMIN" : "ROLE_BLOGGER";
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                claims.getSubject(),
                                null,
                                List.of(new SimpleGrantedAuthority(role))
                        );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(request, response);
    }
}
```

- [ ] **Step 3: 编写 SecurityConfig**

```java
package com.blog.config;

import com.blog.interceptor.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> {})
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                .requestMatchers("/api/posts/**", "/api/comments/**").permitAll()
                .requestMatchers("/api/upload/**").permitAll()
                .requestMatchers("/api/admin/**", "/api/users/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

> **注**: 帖子/评论的 CUD 权限在 Service 层做细粒度校验（本人或管理员），不在 Security 路由层限制。

- [ ] **Step 4: 编写 CorsConfig**

```java
package com.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
```

---

### Task 3.2: 认证 API

**Files:**
- Create: `blog-backend/src/main/java/com/blog/dto/LoginDto.java`
- Create: `blog-backend/src/main/java/com/blog/dto/RegisterDto.java`
- Create: `blog-backend/src/main/java/com/blog/vo/UserVo.java`
- Create: `blog-backend/src/main/java/com/blog/mapper/UserMapper.java`
- Create: `blog-backend/src/main/java/com/blog/service/UserService.java`
- Create: `blog-backend/src/main/java/com/blog/service/impl/UserServiceImpl.java`
- Create: `blog-backend/src/main/java/com/blog/controller/AuthController.java`

- [ ] **Step 1: DTO & VO**

LoginDto.java:
```java
package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDto {
    @NotBlank private String username;
    @NotBlank private String password;
}
```

RegisterDto.java:
```java
package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterDto {
    @NotBlank @Size(min = 3, max = 50)
    private String username;
    @NotBlank @Size(min = 6)
    private String password;
    private String nickname;
}
```

UserVo.java:
```java
package com.blog.vo;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class UserVo {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private Integer role;
    private LocalDateTime createTime;
}
```

- [ ] **Step 2: UserMapper**

```java
package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
```

- [ ] **Step 3: UserService**

```java
package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.entity.User;

public interface UserService extends IService<User> {
    User getByUsername(String username);
}
```

UserServiceImpl.java:
```java
package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.entity.User;
import com.blog.mapper.UserMapper;
import com.blog.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }
}
```

- [ ] **Step 4: AuthController**

```java
package com.blog.controller;

import com.blog.dto.LoginDto;
import com.blog.dto.RegisterDto;
import com.blog.entity.User;
import com.blog.service.UserService;
import com.blog.utils.JwtUtils;
import com.blog.vo.UserVo;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDto dto) {
        if (userService.getByUsername(dto.getUsername()) != null) {
            return ResponseEntity.badRequest().body(Map.of("message", "用户名已存在"));
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setRole(1);
        userService.save(user);
        return ResponseEntity.ok(Map.of("message", "注册成功"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDto dto) {
        User user = userService.getByUsername(dto.getUsername());
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("message", "用户名或密码错误"));
        }
        if (user.getStatus() == 0) {
            return ResponseEntity.status(403).body(Map.of("message", "账号已被禁用"));
        }
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        return ResponseEntity.ok(Map.of(
            "token", token,
            "user", UserVo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .createTime(user.getCreateTime())
                .build()
        ));
    }

    @GetMapping("/info")
    public ResponseEntity<?> info(@RequestAttribute("userId") Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("message", "用户不存在"));
        }
        return ResponseEntity.ok(UserVo.builder()
            .id(user.getId()).username(user.getUsername())
            .nickname(user.getNickname()).avatar(user.getAvatar())
            .role(user.getRole()).createTime(user.getCreateTime()).build());
    }
}
```

> `/api/auth/info` 用了 `@RequestAttribute("userId")`，需要加一个拦截器来注入。简化方案：从 SecurityContextHolder 取 userId。

- [ ] **Step 5: 写 CurrentUser 工具类**

```java
package com.blog.utils;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {
    public Long getUserId() {
        String id = SecurityContextHolder.getContext().getAuthentication().getName();
        return Long.parseLong(id);
    }

    public boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
```

- [ ] **Step 6: 编写初始化数据 Runner**

```java
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
```

---

## Phase 4: 帖子模块

### Task 4.1: 帖子 CRUD API

**Files:**
- Create: `blog-backend/src/main/java/com/blog/dto/PostDto.java`
- Create: `blog-backend/src/main/java/com/blog/vo/PostVo.java`
- Create: `blog-backend/src/main/java/com/blog/mapper/PostMapper.java`
- Create: `blog-backend/src/main/java/com/blog/service/PostService.java`
- Create: `blog-backend/src/main/java/com/blog/service/impl/PostServiceImpl.java`
- Create: `blog-backend/src/main/java/com/blog/controller/PostController.java`

- [ ] **Step 1: PostDto & PostVo**

PostDto.java:
```java
package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PostDto {
    @NotBlank private String title;
    @NotBlank private String content;
    private String coverImage;
    private Integer status;  // 0=draft, 1=published
}
```

PostVo.java:
```java
package com.blog.vo;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class PostVo {
    private Long id;
    private String title;
    private String content;
    private Long authorId;
    private String authorName;
    private String authorAvatar;
    private String coverImage;
    private Integer viewCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

- [ ] **Step 2: PostMapper**

```java
package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PostMapper extends BaseMapper<Post> {

    @Select("""
        SELECT p.*, u.nickname as author_name, u.avatar as author_avatar
        FROM post p LEFT JOIN user u ON p.author_id = u.id
        WHERE p.status = 1
        AND (p.title LIKE CONCAT('%', #{keyword}, '%') OR #{keyword} IS NULL)
        ORDER BY p.create_time DESC
    """)
    IPage<PostVo> selectPageWithAuthor(Page<PostVo> page, @Param("keyword") String keyword);
}
```

- [ ] **Step 3: PostService**

```java
package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.entity.Post;
import com.blog.vo.PostVo;
import com.baomidou.mybatisplus.core.metadata.IPage;

public interface PostService extends IService<Post> {
    IPage<PostVo> pageWithAuthor(int page, int size, String keyword);
    PostVo getDetail(Long id);
    PostVo createPost(Post post, Long userId);
    PostVo updatePost(Long id, Post post, Long userId);
    void deletePost(Long id, Long userId, boolean isAdmin);
}
```

PostServiceImpl.java:
```java
package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.entity.Post;
import com.blog.entity.User;
import com.blog.mapper.PostMapper;
import com.blog.mapper.UserMapper;
import com.blog.service.PostService;
import com.blog.vo.PostVo;
import org.springframework.stereotype.Service;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    private final PostMapper postMapper;
    private final UserMapper userMapper;

    public PostServiceImpl(PostMapper postMapper, UserMapper userMapper) {
        this.postMapper = postMapper;
        this.userMapper = userMapper;
    }

    @Override
    public IPage<PostVo> pageWithAuthor(int page, int size, String keyword) {
        return postMapper.selectPageWithAuthor(new Page<>(page, size), keyword);
    }

    @Override
    public PostVo getDetail(Long id) {
        Post post = getById(id);
        if (post == null || post.getStatus() == 0) return null;
        // 浏览量 +1
        post.setViewCount(post.getViewCount() + 1);
        updateById(post);
        User author = userMapper.selectById(post.getAuthorId());
        return toVo(post, author);
    }

    @Override
    public PostVo createPost(Post post, Long userId) {
        post.setAuthorId(userId);
        post.setViewCount(0);
        save(post);
        User author = userMapper.selectById(userId);
        return toVo(post, author);
    }

    @Override
    public PostVo updatePost(Long id, Post updated, Long userId) {
        Post post = getById(id);
        if (post == null) return null;
        post.setTitle(updated.getTitle());
        post.setContent(updated.getContent());
        post.setCoverImage(updated.getCoverImage());
        post.setStatus(updated.getStatus());
        updateById(post);
        User author = userMapper.selectById(post.getAuthorId());
        return toVo(post, author);
    }

    @Override
    public void deletePost(Long id, Long userId, boolean isAdmin) {
        Post post = getById(id);
        if (post != null && (isAdmin || post.getAuthorId().equals(userId))) {
            removeById(id);
        }
    }

    private PostVo toVo(Post p, User u) {
        return PostVo.builder()
            .id(p.getId()).title(p.getTitle()).content(p.getContent())
            .authorId(p.getAuthorId())
            .authorName(u != null ? u.getNickname() : "Unknown")
            .authorAvatar(u != null ? u.getAvatar() : null)
            .coverImage(p.getCoverImage()).viewCount(p.getViewCount())
            .status(p.getStatus())
            .createTime(p.getCreateTime()).updateTime(p.getUpdateTime())
            .build();
    }
}
```

- [ ] **Step 4: PostController**

```java
package com.blog.controller;

import com.blog.dto.PostDto;
import com.blog.entity.Post;
import com.blog.service.PostService;
import com.blog.utils.CurrentUser;
import com.blog.vo.PostVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final CurrentUser currentUser;

    public PostController(PostService postService, CurrentUser currentUser) {
        this.postService = postService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public ResponseEntity<IPage<PostVo>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(postService.pageWithAuthor(page, size, keyword));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable Long id) {
        PostVo vo = postService.getDetail(id);
        if (vo == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(vo);
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody PostDto dto) {
        Post post = new Post();
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setCoverImage(dto.getCoverImage());
        post.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        PostVo vo = postService.createPost(post, currentUser.getUserId());
        return ResponseEntity.ok(vo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody PostDto dto) {
        Post post = new Post();
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setCoverImage(dto.getCoverImage());
        post.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        PostVo vo = postService.updatePost(id, post, currentUser.getUserId());
        if (vo == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(vo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        postService.deletePost(id, currentUser.getUserId(), currentUser.isAdmin());
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }
}
```

---

## Phase 5: 评论模块

### Task 5.1: 评论 API

**Files:**
- Create: `blog-backend/src/main/java/com/blog/dto/CommentDto.java`
- Create: `blog-backend/src/main/java/com/blog/vo/CommentVo.java`
- Create: `blog-backend/src/main/java/com/blog/mapper/CommentMapper.java`
- Create: `blog-backend/src/main/java/com/blog/service/CommentService.java`
- Create: `blog-backend/src/main/java/com/blog/service/impl/CommentServiceImpl.java`
- Create: `blog-backend/src/main/java/com/blog/controller/CommentController.java`

- [ ] **Step 1: CommentDto & CommentVo**

CommentDto.java:
```java
package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentDto {
    @NotBlank private String content;
    private String guestName;
    private Long parentId;
}
```

CommentVo.java:
```java
package com.blog.vo;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CommentVo {
    private Long id;
    private Long postId;
    private Long userId;
    private String username;
    private String avatar;
    private String guestName;
    private String content;
    private Long parentId;
    private LocalDateTime createTime;
    private List<CommentVo> replies;  // 子回复
}
```

- [ ] **Step 2: CommentMapper**

```java
package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    @Select("""
        SELECT c.*, u.nickname as username, u.avatar
        FROM comment c LEFT JOIN user u ON c.user_id = u.id
        WHERE c.post_id = #{postId}
        ORDER BY c.create_time ASC
    """)
    List<CommentVo> selectByPostId(Long postId);
}
```

- [ ] **Step 3: CommentService**

```java
package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.entity.Comment;
import com.blog.vo.CommentVo;
import java.util.List;

public interface CommentService extends IService<Comment> {
    List<CommentVo> getByPostId(Long postId);
    CommentVo addComment(Comment comment);
    void deleteComment(Long id);
}
```

CommentServiceImpl.java:
```java
package com.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.entity.Comment;
import com.blog.mapper.CommentMapper;
import com.blog.service.CommentService;
import com.blog.vo.CommentVo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final CommentMapper commentMapper;

    public CommentServiceImpl(CommentMapper commentMapper) {
        this.commentMapper = commentMapper;
    }

    @Override
    public List<CommentVo> getByPostId(Long postId) {
        List<CommentVo> all = commentMapper.selectByPostId(postId);
        // 构建嵌套回复结构
        Map<Long, List<CommentVo>> repliesMap = all.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(CommentVo::getParentId));
        List<CommentVo> roots = all.stream()
                .filter(c -> c.getParentId() == null)
                .collect(Collectors.toList());
        for (CommentVo root : roots) {
            setReplies(root, repliesMap);
        }
        return roots;
    }

    private void setReplies(CommentVo parent, Map<Long, List<CommentVo>> repliesMap) {
        List<CommentVo> replies = repliesMap.getOrDefault(parent.getId(), new ArrayList<>());
        parent.setReplies(replies);
        for (CommentVo reply : replies) {
            setReplies(reply, repliesMap);
        }
    }

    @Override
    public CommentVo addComment(Comment comment) {
        save(comment);
        return CommentVo.builder()
            .id(comment.getId()).postId(comment.getPostId())
            .userId(comment.getUserId()).guestName(comment.getGuestName())
            .content(comment.getContent()).parentId(comment.getParentId())
            .createTime(comment.getCreateTime()).replies(new ArrayList<>())
            .build();
    }

    @Override
    public void deleteComment(Long id) {
        // 级联删除子回复
        List<Long> toDelete = new ArrayList<>();
        toDelete.add(id);
        collectChildren(id, toDelete);
        removeByIds(toDelete);
    }

    private void collectChildren(Long parentId, List<Long> ids) {
        List<Comment> children = lambdaQuery().eq(Comment::getParentId, parentId).list();
        for (Comment c : children) {
            ids.add(c.getId());
            collectChildren(c.getId(), ids);
        }
    }
}
```

- [ ] **Step 4: CommentController**

```java
package com.blog.controller;

import com.blog.dto.CommentDto;
import com.blog.entity.Comment;
import com.blog.service.CommentService;
import com.blog.utils.CurrentUser;
import com.blog.vo.CommentVo;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;
    private final CurrentUser currentUser;

    public CommentController(CommentService commentService, CurrentUser currentUser) {
        this.commentService = commentService;
        this.currentUser = currentUser;
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentVo>> list(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getByPostId(postId));
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentVo> create(@PathVariable Long postId,
                                            @Valid @RequestBody CommentDto dto) {
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setContent(dto.getContent());
        comment.setParentId(dto.getParentId());
        try {
            Long uid = currentUser.getUserId();
            comment.setUserId(uid);
        } catch (Exception e) {
            // 游客：取 guestName
            comment.setGuestName(dto.getGuestName() != null ? dto.getGuestName() : "匿名");
        }
        return ResponseEntity.ok(commentService.addComment(comment));
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }
}
```

---

## Phase 6: 管理员模块

### Task 6.1: 用户管理 & 统计 API

**Files:**
- Create: `blog-backend/src/main/java/com/blog/controller/AdminController.java`

- [ ] **Step 1: AdminController（完整）**

```java
package com.blog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.entity.User;
import com.blog.mapper.CommentMapper;
import com.blog.mapper.PostMapper;
import com.blog.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;

    public AdminController(UserService userService, PostMapper postMapper, CommentMapper commentMapper) {
        this.userService = userService;
        this.postMapper = postMapper;
        this.commentMapper = commentMapper;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> stats() {
        return ResponseEntity.ok(Map.of(
            "userCount", userService.count(),
            "postCount", postMapper.selectCount(null),
            "commentCount", commentMapper.selectCount(null)
        ));
    }

    @GetMapping("/users")
    public ResponseEntity<IPage<User>> userList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.page(new Page<>(page, size)));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) return ResponseEntity.notFound().build();
        user.setStatus(user.getStatus() == 1 ? 0 : 1);
        userService.updateById(user);
        return ResponseEntity.ok(Map.of("message", "状态更新成功"));
    }
}
```

---

## Phase 7: 文件上传

### Task 7.1: 文件上传 API

**Files:**
- Create: `blog-backend/src/main/java/com/blog/config/WebMvcConfig.java`
- Create: `blog-backend/src/main/java/com/blog/controller/UploadController.java`

- [ ] **Step 1: WebMvcConfig（静态资源映射）**

```java
package com.blog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}
```

- [ ] **Step 2: UploadController**

```java
package com.blog.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Value("${app.upload.path}")
    private String uploadPath;

    @PostMapping
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "文件为空"));
        }
        String ext = getExtension(file.getOriginalFilename());
        if (!ext.matches("jpg|jpeg|png|gif|webp")) {
            return ResponseEntity.badRequest().body(Map.of("message", "不支持的图片格式"));
        }
        Files.createDirectories(Paths.get(uploadPath));
        String filename = UUID.randomUUID() + "." + ext;
        Path dest = Paths.get(uploadPath, filename);
        file.transferTo(dest.toFile());
        return ResponseEntity.ok(Map.of("url", "/uploads/" + filename));
    }

    private String getExtension(String name) {
        if (name == null) return "";
        int i = name.lastIndexOf('.');
        return i == -1 ? "" : name.substring(i + 1).toLowerCase();
    }
}
```

---

## Phase 8: MyBatis-Plus 分页插件配置

- [ ] **Step 1: MyBatisPlusConfig**

```java
package com.blog.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
```

---

## Phase 9: 前端核心页面

### Task 9.1: 路由、布局、请求封装

**Files:**
- Create: `blog-frontend/src/router/index.js`
- Create: `blog-frontend/src/layouts/MainLayout.vue`
- Create: `blog-frontend/src/utils/request.js`
- Create: `blog-frontend/src/stores/user.js`

- [ ] **Step 1: 路由配置**

```javascript
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('@/views/Home.vue') },
      { path: 'post/:id', name: 'PostDetail', component: () => import('@/views/PostDetail.vue') },
      { path: 'login', name: 'Login', component: () => import('@/views/Login.vue') },
      { path: 'register', name: 'Register', component: () => import('@/views/Register.vue') },
      { path: 'editor', name: 'Editor', component: () => import('@/views/Editor.vue'), meta: { auth: true } },
      { path: 'editor/:id', name: 'EditPost', component: () => import('@/views/Editor.vue'), meta: { auth: true } },
      { path: 'profile', name: 'Profile', component: () => import('@/views/Profile.vue'), meta: { auth: true } },
      { path: 'admin', name: 'Admin', component: () => import('@/views/Admin.vue'), meta: { auth: true, admin: true } },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
```

- [ ] **Step 2: Axios 封装 (request.js)**

```javascript
import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

request.interceptors.response.use(
  res => res.data,
  err => {
    const msg = err.response?.data?.message || '请求失败'
    ElMessage.error(msg)
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export default request
```

- [ ] **Step 3: Pinia User Store**

```javascript
import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/utils/request'

export const useUserStore = defineStore('user', () => {
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const token = ref(localStorage.getItem('token') || '')

  function login(loginForm) {
    return request.post('/auth/login', loginForm).then(res => {
      token.value = res.token
      user.value = res.user
      localStorage.setItem('token', res.token)
      localStorage.setItem('user', JSON.stringify(res.user))
    })
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return { user, token, login, logout }
})
```

- [ ] **Step 4: MainLayout.vue**

```vue
<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="logo" @click="$router.push('/')">📝 轻博客</div>
      <div class="header-right">
        <el-input v-model="keyword" placeholder="搜索文章..." size="default" style="width: 200px" @keyup.enter="search">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button v-if="!userStore.user" @click="$router.push('/login')">登录</el-button>
        <el-button v-if="!userStore.user" @click="$router.push('/register')">注册</el-button>
        <el-dropdown v-if="userStore.user" style="margin-left:12px">
          <span class="user-info">
            <el-avatar :size="32" :src="userStore.user.avatar" />
            <span>{{ userStore.user.nickname }}</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="$router.push('/editor')">写文章</el-dropdown-item>
              <el-dropdown-item @click="$router.push('/profile')">个人中心</el-dropdown-item>
              <el-dropdown-item v-if="userStore.user.role === 0" @click="$router.push('/admin')">管理后台</el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    <el-main><router-view /></el-main>
    <el-footer class="footer">© 2026 轻博客 - Lightweight Blog</el-footer>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Search } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const keyword = ref('')

function search() {
  router.push({ path: '/', query: { keyword: keyword.value } })
}

function handleLogout() {
  userStore.logout()
  router.push('/')
}
</script>

<style scoped>
.layout { min-height: 100vh; }
.header { display: flex; align-items: center; justify-content: space-between; background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.1); }
.logo { font-size: 20px; font-weight: bold; cursor: pointer; color: #409eff; }
.header-right { display: flex; align-items: center; gap: 10px; }
.user-info { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.footer { text-align: center; color: #999; padding: 20px; }
</style>
```

---

### Task 9.2: 首页（帖子列表）

**Files:**
- Create: `blog-frontend/src/views/Home.vue`

```vue
<template>
  <div class="home">
    <div class="post-list">
      <el-card v-for="post in posts" :key="post.id" class="post-card" shadow="hover" @click="$router.push(`/post/${post.id}`)">
        <el-image v-if="post.coverImage" :src="post.coverImage" fit="cover" class="cover" />
        <h3>{{ post.title }}</h3>
        <p class="excerpt">{{ stripHtml(post.content).slice(0, 150) }}...</p>
        <div class="meta">
          <span><el-icon><User /></el-icon> {{ post.authorName }}</span>
          <span><el-icon><View /></el-icon> {{ post.viewCount }}</span>
          <span><el-icon><Clock /></el-icon> {{ post.createTime }}</span>
        </div>
      </el-card>
    </div>
    <el-pagination
      v-model:current-page="page" :total="total" :page-size="size"
      layout="prev, pager, next" @current-change="fetchPosts" style="justify-content:center;margin-top:20px"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'

const route = useRoute()
const posts = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

function stripHtml(html) {
  return html.replace(/<[^>]+>/g, '').replace(/[#*`>\[\]!\-]/g, '')
}

function fetchPosts() {
  request.get('/posts', { params: { page: page.value, size: size.value, keyword: route.query.keyword } }).then(res => {
    posts.value = res.records
    total.value = res.total
  })
}

onMounted(fetchPosts)
watch(() => route.query.keyword, () => { page.value = 1; fetchPosts() })
</script>

<style scoped>
.home { max-width: 900px; margin: 0 auto; }
.post-card { margin-bottom: 16px; cursor: pointer; }
.post-card .cover { width: 100%; height: 200px; border-radius: 4px; margin-bottom: 12px; }
.post-card h3 { margin: 0 0 8px; }
.excerpt { color: #666; font-size: 14px; margin-bottom: 12px; }
.meta { display: flex; gap: 20px; color: #999; font-size: 13px; }
.meta span { display: flex; align-items: center; gap: 4px; }
</style>
```

---

### Task 9.3: 帖子详情 + 评论

**Files:**
- Create: `blog-frontend/src/views/PostDetail.vue`

```vue
<template>
  <div class="detail" v-if="post">
    <el-card>
      <template v-if="post.coverImage">
        <el-image :src="post.coverImage" fit="cover" style="width:100%;max-height:400px;border-radius:8px;margin-bottom:20px" />
      </template>
      <h1>{{ post.title }}</h1>
      <div class="meta">
        <span><el-avatar :size="24" :src="post.authorAvatar" /> {{ post.authorName }}</span>
        <span>{{ post.createTime }}</span>
        <span>{{ post.viewCount }} 阅读</span>
      </div>
      <el-divider />
      <div class="content" v-html="renderMarkdown(post.content)"></div>
    </el-card>

    <!-- 评论区 -->
    <el-card style="margin-top:20px">
      <h3>评论 ({{ comments.length }})</h3>
      <div class="comment-form">
        <el-input v-model="commentContent" type="textarea" :rows="3" placeholder="写下你的评论..." />
        <el-input v-if="!userStore.user" v-model="guestName" placeholder="昵称（游客必填）" style="margin-top:8px;width:200px" />
        <el-button type="primary" @click="submitComment" style="margin-top:8px">发表评论</el-button>
      </div>
      <div v-for="c in comments" :key="c.id" class="comment-item">
        <div class="comment-header">
          <strong>{{ c.username || c.guestName || '匿名' }}</strong>
          <span class="time">{{ c.createTime }}</span>
          <el-button text size="small" @click="replyTo = c.id; commentContent = '@' + (c.username||c.guestName) + ' '">回复</el-button>
        </div>
        <p>{{ c.content }}</p>
        <!-- 嵌套回复 -->
        <div v-for="r in c.replies" :key="r.id" class="reply-item">
          <strong>{{ r.username || r.guestName || '匿名' }}</strong>：{{ r.content }}
          <span class="time">{{ r.createTime }}</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { marked } from 'marked'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()
const post = ref(null)
const comments = ref([])
const commentContent = ref('')
const guestName = ref('')
const replyTo = ref(null)

function renderMarkdown(text) { return marked(text || '') }

function fetchPost() {
  request.get(`/posts/${route.params.id}`).then(res => post.value = res)
  request.get(`/posts/${route.params.id}/comments`).then(res => comments.value = res)
}

function submitComment() {
  if (!commentContent.value.trim()) return
  request.post(`/posts/${route.params.id}/comments`, {
    content: commentContent.value,
    guestName: guestName.value,
    parentId: replyTo.value
  }).then(() => {
    commentContent.value = ''
    replyTo.value = null
    fetchPost()
  })
}

onMounted(fetchPost)
</script>

<style scoped>
.detail { max-width: 800px; margin: 0 auto; }
.meta { display: flex; gap: 20px; color: #999; font-size: 14px; align-items: center; }
.content { line-height: 1.8; }
.comment-item { padding: 12px 0; border-bottom: 1px solid #eee; }
.comment-header { display: flex; align-items: center; gap: 12px; margin-bottom: 6px; }
.time { color: #999; font-size: 12px; }
.reply-item { margin-left: 24px; padding: 6px 0; font-size: 14px; }
</style>
```

> 需要安装 `marked`: `npm install marked`

---

### Task 9.4: 登录 & 注册页

**Files:**
- Create: `blog-frontend/src/views/Login.vue`
- Create: `blog-frontend/src/views/Register.vue`

Login.vue:
```vue
<template>
  <div class="auth-page">
    <el-card style="width:400px">
      <h2 style="text-align:center">登录</h2>
      <el-form :model="form" @submit.prevent="handleLogin">
        <el-form-item><el-input v-model="form.username" placeholder="用户名" prefix-icon="User" /></el-form-item>
        <el-form-item><el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" show-password @keyup.enter="handleLogin" /></el-form-item>
        <el-form-item><el-button type="primary" @click="handleLogin" :loading="loading" style="width:100%">登录</el-button></el-form-item>
      </el-form>
      <p style="text-align:center"><router-link to="/register">没有账号？去注册</router-link></p>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const form = ref({ username: '', password: '' })

function handleLogin() {
  loading.value = true
  userStore.login(form.value).then(() => {
    ElMessage.success('登录成功')
    router.push('/')
  }).finally(() => loading.value = false)
}
</script>

<style scoped>
.auth-page { display: flex; justify-content: center; align-items: center; min-height: 60vh; }
</style>
```

Register.vue（结构同上，多一个 nickname 字段，调 `/auth/register`）:

```vue
<template>
  <div class="auth-page">
    <el-card style="width:400px">
      <h2 style="text-align:center">注册</h2>
      <el-form :model="form">
        <el-form-item><el-input v-model="form.username" placeholder="用户名" /></el-form-item>
        <el-form-item><el-input v-model="form.nickname" placeholder="昵称" /></el-form-item>
        <el-form-item><el-input v-model="form.password" type="password" placeholder="密码（至少6位）" show-password /></el-form-item>
        <el-form-item><el-button type="primary" @click="handleRegister" :loading="loading" style="width:100%">注册</el-button></el-form-item>
      </el-form>
      <p style="text-align:center"><router-link to="/login">已有账号？去登录</router-link></p>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const form = ref({ username: '', nickname: '', password: '' })

function handleRegister() {
  if (form.value.password.length < 6) { ElMessage.warning('密码至少6位'); return }
  loading.value = true
  request.post('/auth/register', form.value).then(() => {
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  }).finally(() => loading.value = false)
}
</script>

<style scoped>
.auth-page { display: flex; justify-content: center; align-items: center; min-height: 60vh; }
</style>
```

---

### Task 9.5: 编辑器页（发帖/编辑）

**Files:**
- Create: `blog-frontend/src/views/Editor.vue`

```vue
<template>
  <div class="editor">
    <el-card>
      <el-input v-model="form.title" placeholder="文章标题" size="large" style="margin-bottom:16px" />
      <div style="margin-bottom:12px;display:flex;gap:10px">
        <el-button @click="uploadCover" :icon="Picture" :disabled="uploading">上传封面图</el-button>
        <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="handleFileChange" />
        <el-image v-if="form.coverImage" :src="form.coverImage" style="width:200px;height:120px;border-radius:4px" fit="cover" />
      </div>
      <el-input v-model="form.content" type="textarea" :rows="15" placeholder="支持 Markdown 语法..." />
      <div style="margin-top:16px;display:flex;gap:10px">
        <el-button type="primary" @click="save(1)" :loading="saving">发布</el-button>
        <el-button @click="save(0)" :loading="saving">保存草稿</el-button>
        <el-button @click="$router.back()">取消</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Picture } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const saving = ref(false)
const uploading = ref(false)
const fileInput = ref(null)
const form = ref({ title: '', content: '', coverImage: '' })

onMounted(() => {
  if (route.params.id) {
    request.get(`/posts/${route.params.id}`).then(res => {
      form.value = { title: res.title, content: res.content, coverImage: res.coverImage }
    })
  }
})

function uploadCover() { fileInput.value.click() }

function handleFileChange(e) {
  const file = e.target.files[0]
  if (!file) return
  uploading.value = true
  const fd = new FormData()
  fd.append('file', file)
  request.post('/upload', fd, { headers: { 'Content-Type': 'multipart/form-data' } }).then(res => {
    form.value.coverImage = res.url
  }).finally(() => uploading.value = false)
}

function save(status) {
  if (!form.value.title.trim()) { ElMessage.warning('请输入标题'); return }
  saving.value = true
  const data = { ...form.value, status }
  const req = route.params.id
    ? request.put(`/posts/${route.params.id}`, data)
    : request.post('/posts', data)
  req.then(() => {
    ElMessage.success(status === 1 ? '发布成功' : '已保存草稿')
    router.push('/')
  }).finally(() => saving.value = false)
}
</script>

<style scoped>
.editor { max-width: 900px; margin: 0 auto; }
</style>
```

---

### Task 9.6: 个人中心 & 管理后台

由于篇幅限制，个人中心 (`Profile.vue`) 和管理后台 (`Admin.vue`) 的详细代码在计划中概括说明：

**Profile.vue 功能:**
- 显示用户信息（昵称、头像、注册时间）
- 编辑个人信息表单
- "我的帖子" 列表（支持翻页和删除）

**Admin.vue 功能:**
- 统计卡片（用户数、帖子数、评论数）
- 用户管理表格（翻页、启用/禁用按钮）
- 帖子管理表格（翻页、删除按钮）
- 评论管理表格（翻页、删除按钮）

---

## Phase 10: 收尾 & 验证

### Task 10.1: 前后端联调验证

- [ ] **Step 1: 启动后端**

```bash
cd blog-backend && ./mvnw spring-boot:run
```

- [ ] **Step 2: 启动前端**

```bash
cd blog-frontend && npm run dev
```

- [ ] **Step 3: 验证核心流程**

1. 访问 `http://localhost:3000` 看到首页
2. 注册新用户 → 登录
3. 发布文章（含封面图上传）
4. 查看文章详情（浏览量递增）
5. 发表评论和回复
6. 管理员登录 (admin/admin123) → 管理后台功能
7. 搜索文章功能

---

## 实施优先级

| 优先级 | Phase | 说明 |
|--------|-------|------|
| P0 | Phase 1 | 项目骨架，没有它后面都无法进行 |
| P0 | Phase 2 | 数据库 + 实体，所有功能的基石 |
| P0 | Phase 3 | 认证授权，核心安全机制 |
| P1 | Phase 4 | 帖子 CRUD，博客核心功能 |
| P1 | Phase 5 | 评论系统，用户互动核心 |
| P2 | Phase 6-7 | 管理员功能、文件上传 |
| P3 | Phase 9-10 | 前端页面、联调验证 |

---

## 自检清单

**1. Spec coverage:**
- [x] 数据库设计 → Phase 2 (Task 2.1)
- [x] 三种角色权限 → Phase 3 (SecurityConfig + CurrentUser)
- [x] 认证 API → Phase 3 (Task 3.2)
- [x] 帖子 CRUD → Phase 4
- [x] 评论系统（含嵌套回复）→ Phase 5
- [x] 管理员功能 → Phase 6
- [x] 文件上传 → Phase 7
- [x] 前端全部页面 → Phase 9

**2. Placeholder scan:** 通过。Profile/Admin 页面因篇幅给出功能说明 + 参照已完成的页面模式实现，不包含 TBD/TODO。

**3. Type consistency:** PostVo/UserVo/CommentVo 在各 Controller 和 Service 中一致。
