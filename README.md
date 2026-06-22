# 项目管理系统

> 基于 Spring Boot 3 + Vue 3 的前后端分离项目管理系统

## 技术栈

### 后端
- Java 17
- Spring Boot 3.2.1
- MyBatis Plus 3.5.5
- MySQL 8.x

### 前端
- Vue 3.4
- Vite 5
- Element Plus 2.5
- Vue Router 4
- Pinia 2
- Axios 1.6
- ECharts 5.4

## 功能特性

- ✅ 用户认证（登录/注册）
- ✅ 用户后台创建/编辑/删除
- ✅ 项目管理（增删改查）
- ✅ 任务管理（增删改查）
- ✅ AI 项目助手（摘要、风险、里程碑、任务建议）
- ✅ 数据可视化仪表盘
- ✅ 响应式设计

## 环境要求

- JDK 17
- Maven 3.9+
- Node.js 18+
- MySQL 8.x

## 快速开始

### 1. 初始化数据库

先创建数据库：

```sql
CREATE DATABASE project_management CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

后端默认会在启动时自动执行：

- `backend/src/main/resources/schema.sql`
- `backend/src/main/resources/data.sql`

默认连接信息在 [backend/src/main/resources/application.yml](/Users/mac/javaweb-optimized/backend/src/main/resources/application.yml)：

- 数据库：`project_management`
- 用户名：`root`
- 密码：`123456`

也可以通过环境变量覆盖：

```bash
export DB_URL='jdbc:mysql://localhost:3306/project_management?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true'
export DB_USERNAME='root'
export DB_PASSWORD='123456'
```

### 后端启动

```bash
cd backend
mvn clean package
mvn spring-boot:run
```

启动后接口地址：`http://localhost:8080/api`

默认账号：`admin` / `123456`

如果本地暂时没有 MySQL，可用开发配置快速启动：

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

`dev` profile 使用内存 H2 数据库，便于本地联调和演示。

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

访问：`http://localhost:3000`

## 运行验证

启动后可以验证以下主流程：

- 登录 `admin / 123456`
- 在“用户管理”中新增、编辑、删除用户
- 在“项目管理”中新增、编辑、删除项目
- 在“任务管理”中新增、编辑、删除任务
- 在“AI 助手”中生成项目建议，并导入为任务
- 在“仪表盘”查看项目统计和最近项目列表

## 项目结构

```text
javaweb-optimized/
├── backend/                    # 后端项目
│   ├── src/main/java/
│   │   └── com/example/project/
│   │       ├── controller/     # 控制器层
│   │       ├── mapper/         # 数据访问层
│   │       ├── entity/         # 实体类
│   │       ├── config/         # 配置类
│   │       └── common/         # 通用类
│   └── src/main/resources/
│       ├── application.yml     # 配置文件
│       ├── schema.sql          # 建表脚本
│       └── data.sql            # 初始化数据
│
└── frontend/                   # 前端项目
    └── src/
        ├── api/                # API 接口
        ├── views/              # 页面组件
        ├── router/             # 路由配置
        └── main.js             # 入口文件
```

## API 接口

### 用户接口
- `POST /api/user/login` - 用户登录
- `POST /api/user/register` - 用户注册
- `POST /api/user/create` - 后台创建用户
- `GET /api/user/list` - 获取用户列表
- `GET /api/user/info` - 获取当前用户
- `PUT /api/user/update` - 更新用户
- `DELETE /api/user/delete/{id}` - 删除用户

### 项目接口
- `GET /api/project/list` - 获取项目列表
- `GET /api/project/{id}` - 获取项目详情
- `POST /api/project/create` - 创建项目
- `PUT /api/project/update` - 更新项目
- `DELETE /api/project/delete/{id}` - 删除项目
- `GET /api/project/stats` - 获取项目统计

### 任务接口
- `GET /api/task/list` - 获取任务列表
- `GET /api/task/{id}` - 获取任务详情
- `POST /api/task/create` - 创建任务
- `PUT /api/task/update` - 更新任务
- `DELETE /api/task/delete/{id}` - 删除任务
- `GET /api/task/stats/{projectId}` - 获取任务统计

### AI 接口
- `POST /api/ai/assist` - 生成项目摘要、风险、里程碑和任务建议

## 说明

- 根目录 `pom.xml` 已统一为聚合入口，实际后端代码位于 `backend/`。
- 根目录旧 `src/`、`web/` 为历史遗留内容，不再作为当前版本运行入口。
- 当前版本以本地开发和演示可用为目标，密码仍为明文存储。
- 若配置 `AI_API_URL`、`AI_API_KEY`、`AI_MODEL`，`AI 助手`会优先调用远程模型；未配置时自动回退为本地规则生成建议。

## 开发者

- **张雅**
- 邮箱：3229134261@qq.com
- 掘金：https://juejin.cn/user/2021328701106188
- GitHub: https://github.com/coniern/project-JavaWeb-system

## License

MIT
