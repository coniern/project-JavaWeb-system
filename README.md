# Project OS

> 单主线 Spring Boot + Vue 3 项目管理系统

当前仓库已经重构为一套干净的前后端工程：

- 后端只保留一个 Spring Boot 主应用
- 前端只保留一个 Vue 3 + Vite 管理后台
- 旧 JSP / Servlet / 重复 `backend/` 实现已移除

## 技术栈

### 后端

- Java 21
- Spring Boot 3
- Spring Security + JWT
- MyBatis-Plus
- H2 / MySQL
- Springdoc OpenAPI

### 前端

- Vue 3
- Vite
- Pinia
- Vue Router
- Element Plus
- ECharts
- Axios

## 核心能力

- 用户登录 / 注册 / JWT 鉴权
- 用户管理与角色展示
- 项目管理
- 任务管理
- AI 项目助手
- 仪表盘统计与洞察
- H2 本地开箱即用

## 目录结构

```text
project-JavaWeb-system/
├── src/
│   ├── main/
│   │   ├── java/com/example/projectmanagement/
│   │   │   ├── config
│   │   │   ├── controller
│   │   │   ├── dto
│   │   │   ├── entity
│   │   │   ├── filter
│   │   │   ├── handler
│   │   │   ├── mapper
│   │   │   ├── service
│   │   │   └── utils
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── schema.sql
│   │       └── data.sql
│   └── test/
│       └── java/com/example/projectmanagement/
├── frontend/
│   ├── src/
│   │   ├── api
│   │   ├── constants
│   │   ├── router
│   │   ├── stores
│   │   └── views
│   └── package.json
└── pom.xml
```

## 环境要求

- JDK 21+
- Maven 3.9+
- Node.js 18+

## 快速开始

### 1. 启动后端

默认使用 `h2` profile，本地不需要额外安装数据库。

```bash
mvn spring-boot:run
```

启动地址：

- API: `http://localhost:8080/api`
- Swagger: `http://localhost:8080/swagger-ui/index.html`

### 2. 启动前端

```bash
cd frontend
npm install
npm run dev
```

访问地址：

- `http://localhost:3000`

## 默认账号

- `admin / 123456`
- `dev / 123456`
- `tester / 123456`

## 数据库说明

默认 profile 为 `h2`，数据库文件位于：

- `./data/project-management`

如果你要切换 MySQL，可修改 [application.yml](/Users/mac/Desktop/毕业论文/project-JavaWeb-system/src/main/resources/application.yml) 中的 profile 和连接信息。

## AI 配置

默认配置支持两种模式：

- `ollama`
- `openai-compatible`

当前配置入口同样在 [application.yml](/Users/mac/Desktop/毕业论文/project-JavaWeb-system/src/main/resources/application.yml)：

- `ai.enabled`
- `ai.provider`
- `ai.base-url`
- `ai.model`
- `ai.api-key`
- `ai.use-fallback`

当远程模型不可用时，AI 助手会自动回退到本地规则生成建议。

## 主要接口

### 认证

- `POST /api/auth/login`
- `POST /api/auth/register`
- `GET /api/auth/me`

### 用户

- `GET /api/users`
- `POST /api/users`
- `PUT /api/users/{id}`
- `DELETE /api/users/{id}`

### 项目

- `GET /api/projects`
- `POST /api/projects`
- `GET /api/projects/{id}`
- `PUT /api/projects/{id}`
- `DELETE /api/projects/{id}`
- `GET /api/projects/dashboard`

### 任务

- `GET /api/tasks`
- `POST /api/tasks`
- `GET /api/tasks/{id}`
- `PUT /api/tasks/{id}`
- `DELETE /api/tasks/{id}`
- `GET /api/tasks/stats`

### AI

- `POST /api/ai/assist`
- `GET /api/ai/status`
- `GET /api/ai/dashboard-insight`

## 测试与构建

后端测试：

```bash
mvn test
```

后端打包：

```bash
mvn package
```

前端打包：

```bash
cd frontend
npm run build
```

## 当前版本说明

- 已统一为纯净 Spring Boot 主线，不再维护旧 JSP / Servlet 入口
- 前端布局和交互已重做为统一后台风格
- 任务模块和 AI 助手已迁入主后端，不再依赖旧 `backend/` 实现

## 作者

- 张雅
- GitHub: https://github.com/coniern/project-JavaWeb-system
