package com.example.projectmanagement.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.projectmanagement.entity.Template;
import com.example.projectmanagement.mapper.TemplateMapper;
import com.example.projectmanagement.service.TemplateService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 项目模板服务实现类
 * 实现项目模板的具体业务逻辑
 */
@Service
public class TemplateServiceImpl extends ServiceImpl<TemplateMapper, Template> implements TemplateService {

    @Autowired
    private TemplateMapper templateMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Page<Template> findPage(Integer pageNum, Integer pageSize, String templateName, String templateType, Boolean isSystem) {
        Page<Template> page = new Page<>(pageNum, pageSize);
        return templateMapper.findPage(page, templateName, templateType, isSystem);
    }

    @Override
    public void initSystemTemplates() {
        // 检查是否已初始化过系统模板
        List<Template> systemTemplates = templateMapper.findSystemTemplates();
        if (!systemTemplates.isEmpty()) {
            return;
        }

        // 创建Spring Boot单体项目模板
        createSpringBootTemplate();
        
        // 创建SSM项目模板
        createSsmTemplate();
    }

    @Override
    public Path generateProjectFromTemplate(Long templateId, Map<String, Object> projectInfo, String targetPath) throws IOException {
        // 获取模板信息
        Template template = getById(templateId);
        if (template == null) {
            throw new FileNotFoundException("模板不存在: " + templateId);
        }

        // 解析模板配置
        JsonNode templateConfig = objectMapper.readTree(template.getTemplateConfig());
        
        // 创建目标项目目录
        String projectName = (String) projectInfo.getOrDefault("projectName", "new-project");
        Path projectPath = Paths.get(targetPath, projectName);
        Files.createDirectories(projectPath);

        // 根据模板类型生成对应的项目结构
        if ("spring-boot".equals(template.getTemplateType())) {
            generateSpringBootProject(projectPath, projectInfo);
        } else if ("ssm".equals(template.getTemplateType())) {
            generateSsmProject(projectPath, projectInfo);
        } else {
            // 通用模板生成逻辑
            generateProjectFromConfig(projectPath, templateConfig, projectInfo);
        }

        return projectPath;
    }

    @Override
    public boolean saveCustomTemplate(Template template, String projectPath) {
        try {
            // 分析项目结构，生成模板配置
            ObjectNode templateConfig = analyzeProjectStructure(projectPath);
            template.setTemplateConfig(templateConfig.toString());
            template.setIsSystem(false);
            template.setCreateTime(LocalDateTime.now());
            template.setUpdateTime(LocalDateTime.now());
            
            return save(template);
        } catch (Exception e) {
            log.error("保存自定义模板失败", e);
            return false;
        }
    }

    @Override
    public Template getRecommendedTemplate(String templateType) {
        // 获取指定类型的模板列表
        List<Template> templates = templateMapper.findByType(templateType);
        if (templates.isEmpty()) {
            return null;
        }
        
        // 返回第一个系统预设模板或用户模板
        return templates.stream()
                .filter(t -> t.getIsSystem() != null && t.getIsSystem())
                .findFirst()
                .orElse(templates.get(0));
    }

    /**
     * 创建Spring Boot单体项目模板
     */
    private void createSpringBootTemplate() {
        Template springBootTemplate = new Template();
        springBootTemplate.setTemplateName("Spring Boot单体项目");
        springBootTemplate.setDescription("标准的Spring Boot单体项目模板，包含Web、测试等基础依赖");
        springBootTemplate.setTemplateType("spring-boot");
        springBootTemplate.setIsSystem(true);
        springBootTemplate.setCreateTime(LocalDateTime.now());
        springBootTemplate.setUpdateTime(LocalDateTime.now());
        
        // 构建模板配置JSON
        ObjectNode templateConfig = objectMapper.createObjectNode();
        
        // 添加文件结构配置
        ArrayNode files = objectMapper.createArrayNode();
        
        // pom.xml配置
        ObjectNode pomXml = objectMapper.createObjectNode();
        pomXml.put("path", "pom.xml");
        pomXml.put("type", "xml");
        files.add(pomXml);
        
        // application.yml配置
        ObjectNode applicationYml = objectMapper.createObjectNode();
        applicationYml.put("path", "src/main/resources/application.yml");
        applicationYml.put("type", "yaml");
        files.add(applicationYml);
        
        // 主启动类配置
        ObjectNode mainClass = objectMapper.createObjectNode();
        mainClass.put("path", "src/main/java/");
        mainClass.put("type", "java");
        mainClass.put("name", "Application.java");
        files.add(mainClass);
        
        templateConfig.set("files", files);
        springBootTemplate.setTemplateConfig(templateConfig.toString());
        
        save(springBootTemplate);
    }

    /**
     * 创建SSM项目模板
     */
    private void createSsmTemplate() {
        Template ssmTemplate = new Template();
        ssmTemplate.setTemplateName("SSM项目");
        ssmTemplate.setDescription("标准的Spring + SpringMVC + MyBatis项目模板");
        ssmTemplate.setTemplateType("ssm");
        ssmTemplate.setIsSystem(true);
        ssmTemplate.setCreateTime(LocalDateTime.now());
        ssmTemplate.setUpdateTime(LocalDateTime.now());
        
        // 构建模板配置JSON
        ObjectNode templateConfig = objectMapper.createObjectNode();
        
        // 添加文件结构配置
        ArrayNode files = objectMapper.createArrayNode();
        
        // pom.xml配置
        ObjectNode pomXml = objectMapper.createObjectNode();
        pomXml.put("path", "pom.xml");
        pomXml.put("type", "xml");
        files.add(pomXml);
        
        // mybatis-config.xml配置
        ObjectNode mybatisXml = objectMapper.createObjectNode();
        mybatisXml.put("path", "src/main/resources/mybatis-config.xml");
        mybatisXml.put("type", "xml");
        files.add(mybatisXml);
        
        // spring-mvc.xml配置
        ObjectNode springMvcXml = objectMapper.createObjectNode();
        springMvcXml.put("path", "src/main/resources/spring-mvc.xml");
        springMvcXml.put("type", "xml");
        files.add(springMvcXml);
        
        // applicationContext.xml配置
        ObjectNode applicationContextXml = objectMapper.createObjectNode();
        applicationContextXml.put("path", "src/main/resources/applicationContext.xml");
        applicationContextXml.put("type", "xml");
        files.add(applicationContextXml);
        
        templateConfig.set("files", files);
        ssmTemplate.setTemplateConfig(templateConfig.toString());
        
        save(ssmTemplate);
    }

    /**
     * 生成Spring Boot项目
     */
    private void generateSpringBootProject(Path projectPath, Map<String, Object> projectInfo) throws IOException {
        String projectName = (String) projectInfo.getOrDefault("projectName", "spring-boot-project");
        String packageName = (String) projectInfo.getOrDefault("packageName", "com.example.project");
        String description = (String) projectInfo.getOrDefault("description", "Spring Boot Project");
        String port = (String) projectInfo.getOrDefault("port", "8080");
        
        // 创建src/main/java目录
        Path javaSrcPath = projectPath.resolve("src/main/java").resolve(packageName.replace('.', '/'));
        Files.createDirectories(javaSrcPath);
        
        // 创建src/main/resources目录
        Path resourcesPath = projectPath.resolve("src/main/resources");
        Files.createDirectories(resourcesPath);
        
        // 创建pom.xml
        createPomXml(projectPath.resolve("pom.xml"), projectName, description, packageName);
        
        // 创建application.yml
        createApplicationYml(resourcesPath.resolve("application.yml"), port);
        
        // 创建主启动类
        String mainClassName = capitalizeFirstLetter(projectName) + "Application";
        createSpringBootApplicationClass(javaSrcPath.resolve(mainClassName + ".java"), packageName, mainClassName);
        
        // 创建.gitignore文件
        createGitignore(projectPath.resolve(".gitignore"));
    }

    /**
     * 生成SSM项目
     */
    private void generateSsmProject(Path projectPath, Map<String, Object> projectInfo) throws IOException {
        String projectName = (String) projectInfo.getOrDefault("projectName", "ssm-project");
        String packageName = (String) projectInfo.getOrDefault("packageName", "com.example.project");
        String description = (String) projectInfo.getOrDefault("description", "SSM Project");
        
        // 创建src/main/java目录
        Path javaSrcPath = projectPath.resolve("src/main/java").resolve(packageName.replace('.', '/'));
        Files.createDirectories(javaSrcPath);
        
        // 创建src/main/resources目录
        Path resourcesPath = projectPath.resolve("src/main/resources");
        Files.createDirectories(resourcesPath);
        
        // 创建webapp/WEB-INF目录
        Path webappPath = projectPath.resolve("src/main/webapp/WEB-INF");
        Files.createDirectories(webappPath);
        
        // 创建pom.xml
        createSsmPomXml(projectPath.resolve("pom.xml"), projectName, description, packageName);
        
        // 创建mybatis-config.xml
        createMybatisConfigXml(resourcesPath.resolve("mybatis-config.xml"));
        
        // 创建spring-mvc.xml
        createSpringMvcXml(webappPath.resolve("spring-mvc.xml"), packageName);
        
        // 创建applicationContext.xml
        createApplicationContextXml(webappPath.resolve("applicationContext.xml"), packageName);
        
        // 创建web.xml
        createWebXml(webappPath.resolve("web.xml"));
        
        // 创建.gitignore文件
        createGitignore(projectPath.resolve(".gitignore"));
    }

    /**
     * 从模板配置生成项目
     */
    private void generateProjectFromConfig(Path projectPath, JsonNode templateConfig, Map<String, Object> projectInfo) throws IOException {
        // 通用项目生成逻辑，可以根据模板配置生成不同结构的项目
        // 这里仅作为示例
    }

    /**
     * 分析项目结构，生成模板配置
     */
    private ObjectNode analyzeProjectStructure(String projectPath) throws IOException {
        ObjectNode templateConfig = objectMapper.createObjectNode();
        ArrayNode files = objectMapper.createArrayNode();
        
        // 遍历项目目录，收集文件信息
        Files.walkFileTree(Paths.get(projectPath), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                // 跳过不需要的文件
                String fileName = file.getFileName().toString();
                if (fileName.startsWith(".") || fileName.endsWith(".log") || fileName.endsWith(".tmp")) {
                    return FileVisitResult.CONTINUE;
                }
                
                // 获取相对路径
                String relativePath = projectPath.relativize(file).toString();
                
                ObjectNode fileNode = objectMapper.createObjectNode();
                fileNode.put("path", relativePath);
                
                // 设置文件类型
                String extension = getFileExtension(fileName);
                fileNode.put("type", extension);
                
                files.add(fileNode);
                return FileVisitResult.CONTINUE;
            }
        });
        
        templateConfig.set("files", files);
        return templateConfig;
    }

    // 以下是各种文件创建的辅助方法
    
    private void createPomXml(Path filePath, String projectName, String description, String packageName) throws IOException {
        String pomContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n    xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n    <modelVersion>4.0.0</modelVersion>\n    <parent>\n        <groupId>org.springframework.boot</groupId>\n        <artifactId>spring-boot-starter-parent</artifactId>\n        <version>2.7.5</version>\n        <relativePath/> <!-- lookup parent from repository -->\n    </parent>\n    <groupId>" + packageName + "</groupId>\n    <artifactId>" + projectName + "</artifactId>\n    <version>0.0.1-SNAPSHOT</version>\n    <name>" + projectName + "</name>\n    <description>" + description + "</description>\n    <properties>\n        <java.version>1.8</java.version>\n    </properties>\n    <dependencies>\n        <dependency>\n            <groupId>org.springframework.boot</groupId>\n            <artifactId>spring-boot-starter-web</artifactId>\n        </dependency>\n\n        <dependency>\n            <groupId>org.springframework.boot</groupId>\n            <artifactId>spring-boot-starter-test</artifactId>\n            <scope>test</scope>\n        </dependency>\n    </dependencies>\n\n    <build>\n        <plugins>\n            <plugin>\n                <groupId>org.springframework.boot</groupId>\n                <artifactId>spring-boot-maven-plugin</artifactId>\n            </plugin>\n        </plugins>\n    </build>\n\n</project>";
        Files.writeString(filePath, pomContent);
    }

    private void createApplicationYml(Path filePath, String port) throws IOException {
        String ymlContent = "server:\n  port: " + port + "\n\nspring:\n  application:\n    name: spring-boot-project\n\n# 数据库配置示例\n# spring:\n#  datasource:\n#    url: jdbc:mysql://localhost:3306/testdb?useSSL=false&serverTimezone=UTC\n#    username: root\n#    password: password\n#    driver-class-name: com.mysql.cj.jdbc.Driver\n\n# MyBatis配置示例\n# mybatis:\n#  mapper-locations: classpath:mapper/*.xml\n#  type-aliases-package: " + ("com.example.project") + ".entity";\n        Files.writeString(filePath, ymlContent);
    }

    private void createSpringBootApplicationClass(Path filePath, String packageName, String className) throws IOException {
        String classContent = "package " + packageName + ";\n\nimport org.springframework.boot.SpringApplication;\nimport org.springframework.boot.autoconfigure.SpringBootApplication;\n\n@SpringBootApplication\npublic class " + className + " {\n\n    public static void main(String[] args) {\n        SpringApplication.run(" + className + ".class, args);\n    }\n\n}";
        Files.writeString(filePath, classContent);
    }

    private void createSsmPomXml(Path filePath, String projectName, String description, String packageName) throws IOException {
        String pomContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n    xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n    <modelVersion>4.0.0</modelVersion>\n    <groupId>" + packageName + "</groupId>\n    <artifactId>" + projectName + "</artifactId>\n    <version>0.0.1-SNAPSHOT</version>\n    <packaging>war</packaging>\n    <name>" + projectName + "</name>\n    <description>" + description + "</description>\n    <properties>\n        <java.version>1.8</java.version>\n        <spring.version>5.3.23</spring.version>\n        <mybatis.version>3.5.11</mybatis.version>\n        <mybatis-spring.version>2.0.7</mybatis-spring.version>\n    </properties>\n    <dependencies>\n        <!-- Spring -->\n        <dependency>\n            <groupId>org.springframework</groupId>\n            <artifactId>spring-context</artifactId>\n            <version>${spring.version}</version>\n        </dependency>\n        <dependency>\n            <groupId>org.springframework</groupId>\n            <artifactId>spring-web</artifactId>\n            <version>${spring.version}</version>\n        </dependency>\n        <dependency>\n            <groupId>org.springframework</groupId>\n            <artifactId>spring-webmvc</artifactId>\n            <version>${spring.version}</version>\n        </dependency>\n        <dependency>\n            <groupId>org.springframework</groupId>\n            <artifactId>spring-jdbc</artifactId>\n            <version>${spring.version}</version>\n        </dependency>\n\n        <!-- MyBatis -->\n        <dependency>\n            <groupId>org.mybatis</groupId>\n            <artifactId>mybatis</artifactId>\n            <version>${mybatis.version}</version>\n        </dependency>\n        <dependency>\n            <groupId>org.mybatis</groupId>\n            <artifactId>mybatis-spring</artifactId>\n            <version>${mybatis-spring.version}</version>\n        </dependency>\n\n        <!-- 数据库驱动 -->\n        <dependency>\n            <groupId>mysql</groupId>\n            <artifactId>mysql-connector-java</artifactId>\n            <version>8.0.31</version>\n        </dependency>\n\n        <!-- 其他依赖 -->\n        <dependency>\n            <groupId>javax.servlet</groupId>\n            <artifactId>javax.servlet-api</artifactId>\n            <version>4.0.1</version>\n            <scope>provided</scope>\n        </dependency>\n        <dependency>\n            <groupId>com.fasterxml.jackson.core</groupId>\n            <artifactId>jackson-databind</artifactId>\n            <version>2.13.4.2</version>\n        </dependency>\n    </dependencies>\n\n    <build>\n        <plugins>\n            <plugin>\n                <groupId>org.apache.maven.plugins</groupId>\n                <artifactId>maven-compiler-plugin</artifactId>\n                <version>3.8.1</version>\n                <configuration>\n                    <source>1.8</source>\n                    <target>1.8</target>\n                </configuration>\n            </plugin>\n            <plugin>\n                <groupId>org.apache.maven.plugins</groupId>\n                <artifactId>maven-war-plugin</artifactId>\n                <version>3.3.2</version>\n                <configuration>\n                    <failOnMissingWebXml>false</failOnMissingWebXml>\n                </configuration>\n            </plugin>\n        </plugins>\n    </build>\n\n</project>";\n        Files.writeString(filePath, pomContent);
    }

    private void createMybatisConfigXml(Path filePath) throws IOException {
        String content = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>
<!DOCTYPE configuration\n        PUBLIC \"-//mybatis.org//DTD Config 3.0//EN\"\n        \"http://mybatis.org/dtd/mybatis-3-config.dtd\">\n<configuration>\n    <settings>\n        <!-- 开启驼峰命名转换 -->\n        <setting name=\"mapUnderscoreToCamelCase\" value=\"true\"/>\n        <!-- 开启日志 -->\n        <setting name=\"logImpl\" value=\"STDOUT_LOGGING\"/>\n    </settings>\n    \n    <!-- 类型别名配置 -->\n    <typeAliases>\n        <package name=\"com.example.project.entity\"/>\n    </typeAliases>\n    \n    <!-- 映射器配置 -->\n    <mappers>\n        <package name=\"com.example.project.mapper\"/>\n    </mappers>\n</configuration>";\n        Files.writeString(filePath, content);
    }

    private void createSpringMvcXml(Path filePath, String packageName) throws IOException {
        String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<beans xmlns=\"http://www.springframework.org/schema/beans\"\n       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n       xmlns:context=\"http://www.springframework.org/schema/context\"\n       xmlns:mvc=\"http://www.springframework.org/schema/mvc\"\n       xsi:schemaLocation=\"http://www.springframework.org/schema/beans\n        http://www.springframework.org/schema/beans/spring-beans.xsd\n        http://www.springframework.org/schema/context\n        http://www.springframework.org/schema/context/spring-context.xsd\n        http://www.springframework.org/schema/mvc\n        http://www.springframework.org/schema/mvc/spring-mvc.xsd\">\n\n    <!-- 扫描控制器 -->\n    <context:component-scan base-package=\"" + packageName + ".controller\"/>\n    \n    <!-- 开启MVC注解驱动 -->\n    <mvc:annotation-driven/>\n    \n    <!-- 静态资源处理 -->\n    <mvc:default-servlet-handler/>\n    \n    <!-- 视图解析器 -->\n    <bean class=\"org.springframework.web.servlet.view.InternalResourceViewResolver\">\n        <property name=\"prefix\" value=\"/WEB-INF/views/\"/>\n        <property name=\"suffix\" value=\".jsp\"/>\n    </bean>\n</beans>";\n        Files.writeString(filePath, content);
    }

    private void createApplicationContextXml(Path filePath, String packageName) throws IOException {
        String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<beans xmlns=\"http://www.springframework.org/schema/beans\"\n       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n       xmlns:context=\"http://www.springframework.org/schema/context\"\n       xsi:schemaLocation=\"http://www.springframework.org/schema/beans\n        http://www.springframework.org/schema/beans/spring-beans.xsd\n        http://www.springframework.org/schema/context\n        http://www.springframework.org/schema/context/spring-context.xsd\">\n\n    <!-- 扫描Service和Dao -->\n    <context:component-scan base-package=\"" + packageName + ".service\", \"" + packageName + ".mapper\"/>\n    \n    <!-- 加载属性文件 -->\n    <context:property-placeholder location=\"classpath:jdbc.properties\"/>\n    \n    <!-- 配置数据源 -->\n    <bean id=\"dataSource\" class=\"org.springframework.jdbc.datasource.DriverManagerDataSource\">\n        <property name=\"driverClassName\" value=\"${jdbc.driver}\"/>\n        <property name=\"url\" value=\"${jdbc.url}\"/>\n        <property name=\"username\" value=\"${jdbc.username}\"/>\n        <property name=\"password\" value=\"${jdbc.password}\"/>\n    </bean>\n    \n    <!-- 配置SqlSessionFactory -->\n    <bean id=\"sqlSessionFactory\" class=\"org.mybatis.spring.SqlSessionFactoryBean\">\n        <property name=\"dataSource\" ref=\"dataSource\"/>\n        <property name=\"configLocation\" value=\"classpath:mybatis-config.xml\"/>\n        <property name=\"mapperLocations\" value=\"classpath:mapper/*.xml\"/>\n    </bean>\n    \n    <!-- 配置Mapper扫描器 -->\n    <bean class=\"org.mybatis.spring.mapper.MapperScannerConfigurer\">\n        <property name=\"basePackage\" value=\"" + packageName + ".mapper\"/>\n        <property name=\"sqlSessionFactoryBeanName\" value=\"sqlSessionFactory\"/>\n    </bean>\n</beans>";\n        Files.writeString(filePath, content);
    }

    private void createWebXml(Path filePath) throws IOException {
        String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<web-app xmlns=\"http://xmlns.jcp.org/xml/ns/javaee\"\n         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n         xsi:schemaLocation=\"http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd\"\n         version=\"4.0\">\n\n    <!-- Spring配置文件 -->\n    <context-param>\n        <param-name>contextConfigLocation</param-name>\n        <param-value>/WEB-INF/applicationContext.xml</param-value>\n    </context-param>\n\n    <!-- Spring监听器 -->\n    <listener>\n        <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>\n    </listener>\n\n    <!-- SpringMVC前端控制器 -->\n    <servlet>\n        <servlet-name>dispatcher</servlet-name>\n        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>\n        <init-param>\n            <param-name>contextConfigLocation</param-name>\n            <param-value>/WEB-INF/spring-mvc.xml</param-value>\n        </init-param>\n        <load-on-startup>1</load-on-startup>\n    </servlet>\n\n    <!-- Servlet映射 -->\n    <servlet-mapping>\n        <servlet-name>dispatcher</servlet-name>\n        <url-pattern>/</url-pattern>\n    </servlet-mapping>\n\n    <!-- 字符编码过滤器 -->\n    <filter>\n        <filter-name>encodingFilter</filter-name>\n        <filter-class>org.springframework.web.filter.CharacterEncodingFilter</filter-class>\n        <init-param>\n            <param-name>encoding</param-name>\n            <param-value>UTF-8</param-value>\n        </init-param>\n        <init-param>\n            <param-name>forceEncoding</param-name>\n            <param-value>true</param-value>\n        </init-param>\n    </filter>\n    <filter-mapping>\n        <filter-name>encodingFilter</filter-name>\n        <url-pattern>/*</url-pattern>\n    </filter-mapping>\n</web-app>";\n        Files.writeString(filePath, content);
    }

    private void createGitignore(Path filePath) throws IOException {
        String content = "# Logs\nlogs\n*.log\nnpm-debug.log*\nyarn-debug.log*\nyarn-error.log*\npnpm-debug.log*\nlerna-debug.log*\n\nnode_modules\ndist\ndist-ssr\n*.local\n\n# Editor directories and files\n.vscode/*\n!.vscode/extensions.json\n.idea\n.DS_Store\n*.suo\n*.ntvs*\n*.njsproj\n*.sln\n*.sw?\n\n# Maven\ntarget/\n\n# Gradle\nbuild/\n.gradle/\n\n# OS generated files\nThumbs.db\n.DS_Store\n\n# Environment variables\n.env\n.env.local\n.env.development.local\n.env.test.local\n.env.production.local";\n        Files.writeString(filePath, content);
    }

    // 工具方法
    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(dotIndex + 1) : "txt";
    }

    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}