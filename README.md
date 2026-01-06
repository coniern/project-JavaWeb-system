# 项目管理平台

## 项目简介

项目管理平台是一个现代化的Web应用程序，旨在简化项目管理流程，提高团队协作效率，实现快速部署和交付。

## 技术栈

### 前端
- HTML5 + CSS3 + JavaScript
- Tailwind CSS (v3)
- Font Awesome (v4.7.0)
- Chart.js (v4.4.8)

### 后端
- Java 17
- Spring Boot 3.2.1
- Spring Web

## 项目结构

```
project-JavaWeb-system/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── example/
│                   └── projectmanagement/
│                       ├── SimpleServerApplication.java  # 简化版服务器应用程序
│                       └── controller/
│                           └── SimpleApiController.java  # 简单的REST API控制器
├── web/
│   ├── index.html      # 首页
│   ├── login.html      # 登录页
│   ├── register.html    # 注册页
│   └── WEB-INF/
│       └── web.xml
├── pom.xml             # Maven配置文件
└── README.md           # 项目说明文件
```

## 运行项目

### 前端
前端页面是纯静态的HTML/CSS/JavaScript文件，可以直接在浏览器中打开运行。

1. 打开 `web/index.html` 文件，即可访问首页。
2. 打开 `web/login.html` 文件，即可访问登录页。
3. 打开 `web/register.html` 文件，即可访问注册页。

### 后端
后端服务器使用Spring Boot构建，提供了简单的REST API接口。

1. 确保已安装Java 17和Maven。
2. 在项目根目录下运行以下命令启动服务器：
   ```bash
   mvn spring-boot:run
   ```
3. 服务器将在 `http://localhost:8080` 上运行。

## API接口

### 认证接口
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册

### 项目接口
- `GET /api/projects` - 获取项目列表

## 测试账号

- 用户名: `admin`
- 密码: `admin`

## 注意事项

1. 由于Spring Boot配置问题，后端服务器可能无法正常启动。如果遇到 `FactoryBeanObjectType` 错误，请尝试以下解决方案：
   - 检查依赖项版本是否兼容
   - 检查配置文件是否正确
   - 尝试使用不同版本的Spring Boot

2. 前端页面已经配置为使用模拟数据，即使后端服务器无法启动，也可以正常显示页面内容。

3. 前端页面使用了CDN加载Tailwind CSS和Font Awesome，因此需要互联网连接才能正常显示样式和图标。

## 功能特性

- 现代化的响应式设计
- 流畅的动画效果和过渡
- 完整的用户认证流程
- 项目管理功能
- 数据可视化图表
- 移动端适配

## 未来规划

- 添加更多项目管理功能
- 实现完整的用户授权系统
- 增加数据持久化存储
- 优化性能和用户体验
- 添加更多数据可视化图表