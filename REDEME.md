# 轻型博客系统 - 完整开发需求

## 1. 项目概述
开发一个轻型博客系统，支持三种角色：管理员、博主、游客。系统需界面美观、功能实用、代码规范。

## 2. 技术栈
- **后端**：Spring Boot 3.x + MyBatis-Plus + MySQL 8.0 + JWT + Spring Security
- **前端**：Vue 3 + Vite + Element Plus + Axios + Vue Router + Pinia
- **数据库**：MySQL 8.0
- **构建工具**：Maven（后端）/ npm（前端）

## 3. 数据库设计（MySQL）

### 3.1 用户表 (user)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AI | 主键 |
| username | VARCHAR(50) UNIQUE NOT NULL | 用户名 |
| password | VARCHAR(255) NOT NULL | 加密密码(BCrypt) |
| nickname | VARCHAR(50) | 昵称 |
| avatar | VARCHAR(255) | 头像URL |
| role | TINYINT DEFAULT 1 | 0=管理员, 1=博主 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### 3.2 帖子表 (post)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AI | 主键 |
| title | VARCHAR(200) NOT NULL | 标题 |
| content | LONGTEXT NOT NULL | 内容（支持Markdown） |
| author_id | BIGINT FK | 作者ID |
| cover_image | VARCHAR(255) | 封面图 |
| view_count | INT DEFAULT 0 | 浏览量 |
| status | TINYINT DEFAULT 1 | 0=草稿, 1=已发布 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### 3.3 评论表 (comment)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AI | 主键 |
| post_id | BIGINT FK NOT NULL | 所属帖子 |
| user_id | BIGINT FK | 评论者ID（游客可为NULL）|
| guest_name | VARCHAR(50) | 游客昵称 |
| content | TEXT NOT NULL | 评论内容 |
| parent_id | BIGINT DEFAULT NULL | 回复的评论ID（支持楼中楼）|
| create_time | DATETIME | 创建时间 |

## 4. 角色与权限

### 4.1 游客（未登录）
- 浏览帖子列表和详情
- 发表评论（需填写昵称，无需注册）
- 查看评论列表

### 4.2 博主（需登录）
- 游客所有权限
- 发帖（标题、内容、封面图）
- 修改自己的帖子
- 删除自己的帖子
- 个人中心查看/编辑个人信息

### 4.3 管理员（需登录）
- 博主所有权限
- 删除任意帖子
- 删除任意评论
- 用户管理（查看用户列表，可禁用用户）
- 数据统计仪表盘（帖子数、用户数、评论数）

## 5. 后端 API 设计（RESTful）

### 认证模块
- POST /api/auth/register - 注册（博主）
- POST /api/auth/login - 登录，返回JWT
- GET /api/auth/info - 获取当前登录用户信息

### 帖子模块
- GET /api/posts - 帖子列表（分页，支持按标题搜索）
- GET /api/posts/{id} - 帖子详情（同时增加浏览量）
- POST /api/posts - 发帖（博主/管理员）
- PUT /api/posts/{id} - 修改帖子（仅自己或管理员）
- DELETE /api/posts/{id} - 删除帖子（仅自己或管理员）

### 评论模块
- GET /api/posts/{postId}/comments - 获取评论列表
- POST /api/posts/{postId}/comments - 发表评论（游客或登录用户）
- DELETE /api/comments/{id} - 删除评论（管理员）

### 用户模块（管理员）
- GET /api/users - 用户列表（分页）
- PUT /api/users/{id}/status - 启用/禁用用户
- GET /api/admin/stats - 统计数据

### 文件模块
- POST /api/upload - 上传图片（封面图、头像）

## 6. 前端页面设计

### 6.1 布局
- **顶部导航栏**：Logo、首页、搜索框、登录/注册/个人中心入口
- **主体内容区**：路由切换
- **底部**：版权信息

### 6.2 页面清单

| 页面 | 路径 | 说明 |
|------|------|------|
| 首页 | / | 帖子卡片列表，支持分页和搜索 |
| 帖子详情 | /post/:id | 内容展示 + 评论列表 + 评论框 |
| 登录 | /login | 登录表单 |
| 注册 | /register | 注册表单 |
| 发帖/编辑 | /editor | 富文本编辑器（可用Markdown编辑器组件）|
| 个人中心 | /profile | 个人信息 + 我的帖子列表 |
| 管理后台 | /admin | 仪表盘 + 用户管理 + 帖子管理 + 评论管理 |

### 6.3 UI 要求
- 使用 Element Plus 组件库
- 响应式设计，支持移动端适配
- 主题色建议：清新蓝/绿或深色模式可选
- 帖子卡片展示：封面图、标题、摘要、作者、发布时间、浏览量
- 评论区支持嵌套回复展示

## 7. 项目结构
blog-system/
├── blog-backend/          # Spring Boot 项目
│   ├── src/main/java/com/blog/
│   │   ├── config/        # 配置类（Security, JWT, Cors, MyBatisPlus）
│   │   ├── controller/    # 控制器
│   │   ├── service/       # 服务层
│   │   ├── mapper/        # MyBatis Mapper
│   │   ├── entity/        # 实体类
│   │   ├── dto/           # 数据传输对象
│   │   ├── vo/            # 视图对象
│   │   ├── utils/         # 工具类
│   │   └── interceptor/   # 拦截器
│   └── src/main/resources/
│       ├── mapper/        # XML映射文件
│       └── application.yml
│
└── blog-frontend/         # Vue 3 项目
├── src/
│   ├── api/           # API 请求封装
│   ├── assets/        # 静态资源
│   ├── components/    # 公共组件
│   ├── views/         # 页面视图
│   ├── router/        # 路由配置
│   ├── store/         # Pinia 状态管理
│   └── utils/         # 工具函数
└── package.json
