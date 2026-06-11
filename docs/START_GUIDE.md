# 🚀 轻型博客系统 - 启动指南

## 📋 前置要求

- JDK 17+ (本项目使用 JDK 24)
- MySQL 8.0+
- Node.js 16+
- Maven 3.6+

---

## 第一步：初始化数据库

### 1.1 打开 MySQL

确保 MySQL 服务已启动（使用 MySQL Workbench、Navicat 或命令行）

### 1.2 执行初始化脚本

```bash
# 方式一：命令行执行
mysql -u root -p < docs/sql/init.sql

# 方式二：复制 docs/sql/init.sql 内容到 MySQL 客户端执行
```

### 1.3 验证数据库

执行后应该看到：
- 用户数：2
- 帖子数：3
- 评论数：4

### 1.4 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 博主 | testuser | admin123 |

> ⚠️ 如果使用 DataInitRunner 自动初始化，只需知道 admin/admin123 即可

---

## 第二步：启动后端

### 2.1 进入后端目录

```bash
cd blog-backend
```

### 2.2 修改数据库配置（如需要）

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/blog_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: your_username    # 修改为你的用户名
    password: your_password    # 修改为你的密码
```

### 2.3 启动后端

```bash
# 方式一：Maven 命令
mvn spring-boot:run

# 方式二：IDE 启动
# 用 IntelliJ IDEA / Eclipse 打开项目，运行 BlogApplication.java
```

### 2.4 验证后端启动

访问 http://localhost:8080/api/posts 应返回帖子列表 JSON

---

## 第三步：启动前端

### 3.1 进入前端目录

```bash
cd blog-frontend
```

### 3.2 安装依赖

```bash
npm install
```

### 3.3 启动前端

```bash
npm run dev
```

### 3.4 访问系统

打开浏览器访问：**http://localhost:3000**

---

## 🔧 常见问题

### Q1: 后端启动报错 "Unable to obtain connection"

**原因**：MySQL 未启动或数据库配置错误

**解决**：
1. 确认 MySQL 服务已启动
2. 检查 application.yml 中的数据库连接信息
3. 确认 blog_db 数据库已创建

### Q2: 前端无法调用后端 API

**原因**：跨域问题

**解决**：
- vite.config.js 已配置代理，确保后端在 8080 端口运行

### Q3: 登录失败 "用户名或密码错误"

**原因**：
1. 数据库中没有用户数据
2. 密码加密格式不正确

**解决**：
1. 执行初始化脚本 `docs/sql/init.sql`
2. 或等待 DataInitRunner 自动创建 admin 账号

### Q4: 端口被占用

**解决**：
- 后端端口修改：application.yml 中的 `server.port`
- 前端端口修改：vite.config.js 中的 `server.port`

---

## 📁 项目结构

```
blog/
├── blog-backend/          # Spring Boot 后端
│   ├── src/main/java/com/blog/
│   │   ├── config/       # 配置类
│   │   ├── controller/   # API 控制器
│   │   ├── service/      # 业务逻辑
│   │   ├── mapper/       # 数据库映射
│   │   ├── entity/        # 实体类
│   │   └── utils/         # 工具类
│   └── src/main/resources/
│       ├── application.yml
│       └── mapper/        # XML 映射文件
│
├── blog-frontend/         # Vue 3 前端
│   ├── src/
│   │   ├── api/          # API 调用
│   │   ├── views/        # 页面组件
│   │   ├── router/       # 路由配置
│   │   ├── stores/       # 状态管理
│   │   └── utils/        # 工具函数
│   └── package.json
│
└── docs/
    └── sql/
        └── init.sql      # 数据库初始化脚本
```

---

## ✅ 启动完成清单

- [ ] MySQL 服务运行中
- [ ] blog_db 数据库已创建
- [ ] 测试账号可登录
- [ ] 后端 http://localhost:8080/api/posts 正常
- [ ] 前端 http://localhost:3000 正常访问