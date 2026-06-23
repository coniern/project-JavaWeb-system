package com.example.projectmanagement.service.impl;

/*
 * 模板服务保留为后续扩展，当前最小可运行版本不注册该 Service。
 */

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.projectmanagement.entity.Template;
import com.example.projectmanagement.mapper.TemplateMapper;
import com.example.projectmanagement.service.TemplateService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 项目模板服务实现类
 * 实现项目模板的具体业务逻辑
 */
public class TemplateServiceImpl extends ServiceImpl<TemplateMapper, Template> implements TemplateService {

    private static final Logger log = LoggerFactory.getLogger(TemplateServiceImpl.class);

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
            throw new IOException("模板不存在: " + templateId);
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
        
        // 创建.gitignore文件
        createGitignore(projectPath.resolve(".gitignore"));
    }

    /**
     * 生成SSM项目
     */
    private void generateSsmProject(Path projectPath, Map<String, Object> projectInfo) throws IOException {
        String projectName = (String) projectInfo.getOrDefault("projectName", "ssm-project");
        String packageName = (String) projectInfo.getOrDefault("packageName", "com.example.project");
        
        // 创建src/main/java目录
        Path javaSrcPath = projectPath.resolve("src/main/java").resolve(packageName.replace('.', '/'));
        Files.createDirectories(javaSrcPath);
        
        // 创建src/main/resources目录
        Path resourcesPath = projectPath.resolve("src/main/resources");
        Files.createDirectories(resourcesPath);
        
        // 创建webapp/WEB-INF目录
        Path webappPath = projectPath.resolve("src/main/webapp/WEB-INF");
        Files.createDirectories(webappPath);
        
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
                String relativePath = Paths.get(projectPath).relativize(file).toString();
                
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

    /**
     * 创建.gitignore文件
     */
    private void createGitignore(Path filePath) throws IOException {
        String content = "# Logs\n" +
                "logs\n" +
                "*.log\n" +
                "npm-debug.log*\n" +
                "yarn-debug.log*\n" +
                "yarn-error.log*\n" +
                "pnpm-debug.log*\n" +
                "lerna-debug.log*\n\n" +
                "node_modules\n" +
                "dist\n" +
                "dist-ssr\n" +
                "*.local\n\n" +
                "# Editor directories and files\n" +
                ".vscode/*\n" +
                "!.vscode/extensions.json\n" +
                ".idea\n" +
                ".DS_Store\n" +
                "*.suo\n" +
                "*.ntvs*\n" +
                "*.njsproj\n" +
                "*.sln\n" +
                "*.sw?\n\n" +
                "# Maven\n" +
                "target/\n\n" +
                "# Gradle\n" +
                "build/\n" +
                ".gradle/\n\n" +
                "# OS generated files\n" +
                "Thumbs.db\n" +
                ".DS_Store\n\n" +
                "# Environment variables\n" +
                ".env\n" +
                ".env.local\n" +
                ".env.development.local\n" +
                ".env.test.local\n" +
                ".env.production.local";
        Files.writeString(filePath, content);
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
