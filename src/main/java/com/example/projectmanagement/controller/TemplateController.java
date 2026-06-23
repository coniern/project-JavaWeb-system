package com.example.projectmanagement.controller;

/*
 * 模板模块保留为后续扩展，当前最小可运行版本不注册该控制器。
 */

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.projectmanagement.controller.BaseController.ApiResponse;
import com.example.projectmanagement.entity.Template;
import com.example.projectmanagement.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * 项目模板控制器
 * 提供项目模板库相关的API接口
 */
public class TemplateController extends BaseController {

    @Autowired
    private TemplateService templateService;

    /**
     * 分页查询模板列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param templateName 模板名称
     * @param templateType 模板类型
     * @param isSystem 是否为系统模板
     * @return 模板分页列表
     */
    @GetMapping("/page")
    public ApiResponse<Page<Template>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                      @RequestParam(defaultValue = "10") Integer pageSize,
                      @RequestParam(required = false) String templateName,
                      @RequestParam(required = false) String templateType,
                      @RequestParam(required = false) Boolean isSystem) {
        try {
            Page<Template> page = templateService.findPage(pageNum, pageSize, templateName, templateType, isSystem);
            return success(page);
        } catch (Exception e) {
            return error("查询模板列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取模板详情
     * @param templateId 模板ID
     * @return 模板详情
     */
    @GetMapping("/{templateId}")
    public ApiResponse<Template> getTemplate(@PathVariable Long templateId) {
        try {
            Template template = templateService.getById(templateId);
            if (template == null) {
                return error("模板不存在");
            }
            return success(template);
        } catch (Exception e) {
            return error("获取模板详情失败: " + e.getMessage());
        }
    }

    /**
     * 创建自定义模板
     * @param params 模板参数
     * @return 创建结果
     */
    @PostMapping
    public ApiResponse<Template> createTemplate(@RequestBody Map<String, Object> params) {
        try {
            Template template = new Template();
            template.setTemplateName(String.valueOf(params.get("templateName")));
            template.setDescription(String.valueOf(params.get("description")));
            template.setTemplateType(String.valueOf(params.get("templateType")));
            
            Object createUserIdObj = params.get("createUserId");
            if (createUserIdObj != null) {
                template.setCreateUserId(Long.valueOf(createUserIdObj.toString()));
            }
            
            String projectPath = String.valueOf(params.get("projectPath"));
            boolean result = templateService.saveCustomTemplate(template, projectPath);
            
            if (result) {
                return success(template);
            } else {
                return error("创建模板失败");
            }
        } catch (Exception e) {
            return error("创建模板失败: " + e.getMessage());
        }
    }

    /**
     * 更新模板信息
     * @param template 模板信息
     * @return 更新结果
     */
    @PutMapping
    public ApiResponse<Template> updateTemplate(@RequestBody Template template) {
        try {
            boolean result = templateService.updateById(template);
            if (result) {
                return success(template);
            } else {
                return error("更新模板失败");
            }
        } catch (Exception e) {
            return error("更新模板失败: " + e.getMessage());
        }
    }

    /**
     * 删除模板
     * @param templateId 模板ID
     * @return 删除结果
     */
    @DeleteMapping("/{templateId}")
    public ApiResponse<Void> deleteTemplate(@PathVariable Long templateId) {
        try {
            Template template = templateService.getById(templateId);
            if (template == null) {
                return error("模板不存在");
            }
            
            // 系统预设模板不允许删除
            if (template.getIsSystem() != null && template.getIsSystem()) {
                return error("系统预设模板不允许删除");
            }
            
            boolean result = templateService.removeById(templateId);
            if (result) {
                return success();
            } else {
                return error("删除失败");
            }
        } catch (Exception e) {
            return error("删除模板失败: " + e.getMessage());
        }
    }

    /**
     * 从模板生成项目
     * @param params 项目参数
     * @return 生成的项目路径
     */
    @PostMapping("/generate-project")
    public ApiResponse<String> generateProject(@RequestBody Map<String, Object> params) {
        try {
            Long templateId = Long.valueOf(params.get("templateId").toString());
            Map<String, Object> projectInfo = (Map<String, Object>) params.get("projectInfo");
            String targetPath = String.valueOf(params.get("targetPath"));
            
            Path projectPath = templateService.generateProjectFromTemplate(templateId, projectInfo, targetPath);
            return success(projectPath.toString());
        } catch (IOException e) {
            return error("生成项目失败: " + e.getMessage());
        } catch (Exception e) {
            return error("生成项目失败: " + e.getMessage());
        }
    }

    /**
     * 获取推荐模板
     * @param templateType 模板类型
     * @return 推荐模板
     */
    @GetMapping("/recommended/{templateType}")
    public ApiResponse<Template> getRecommendedTemplate(@PathVariable String templateType) {
        try {
            Template template = templateService.getRecommendedTemplate(templateType);
            return success(template);
        } catch (Exception e) {
            return error("获取推荐模板失败: " + e.getMessage());
        }
    }

    /**
     * 初始化系统模板
     * @return 初始化结果
     */
    @PostMapping("/init-system-templates")
    public ApiResponse<Void> initSystemTemplates() {
        try {
            templateService.initSystemTemplates();
            return success();
        } catch (Exception e) {
            return error("系统模板初始化失败: " + e.getMessage());
        }
    }
}
