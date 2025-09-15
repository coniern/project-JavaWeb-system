package com.example.projectmanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Swagger配置类
 * 用于生成API文档
 */
@Configuration
public class SwaggerConfig {
    
    /**
     * 创建API文档配置
     */
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                // 指定扫描的包路径
                .apis(RequestHandlerSelectors.basePackage("com.example.projectmanagement.controller"))
                // 指定路径规则
                .paths(PathSelectors.any())
                .build();
    }
    
    /**
     * 构建API文档的基本信息
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                // 标题
                .title("项目管理平台 API文档")
                // 描述
                .description("项目管理平台的RESTful API接口文档")
                // 版本
                .version("1.0.0")
                // 联系方式
                .contact(new Contact("项目管理平台团队", "http://localhost:8080/project-management", "admin@example.com"))
                // 许可证
                .license("Apache License 2.0")
                .licenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html")
                .build();
    }
}