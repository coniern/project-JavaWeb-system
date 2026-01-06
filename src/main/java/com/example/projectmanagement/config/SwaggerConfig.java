package com.example.projectmanagement.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger配置类
 * 用于生成API文档
 */
@Configuration
public class SwaggerConfig {
    
    /**
     * 创建OpenAPI配置
     */
    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .info(apiInfo());
    }
    
    /**
     * 构建API文档的基本信息
     */
    private Info apiInfo() {
        return new Info()
                // 标题
                .title("项目管理平台 API文档")
                // 描述
                .description("项目管理平台的RESTful API接口文档")
                // 版本
                .version("1.0.0")
                // 联系方式
                .contact(new Contact()
                        .name("项目管理平台团队")
                        .url("http://localhost:8080/project-management")
                        .email("admin@example.com"))
                // 许可证
                .license(new License()
                        .name("Apache License 2.0")
                        .url("http://www.apache.org/licenses/LICENSE-2.0.html"));
    }
}