package com.example.projectmanagement.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
@RestController
@RequestMapping("/api/templates")
public class TemplateController {

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
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                      @RequestParam(defaultValue = "10") Integer pageSize,
                      @RequestParam(required = false) String templateName,
                      @RequestParam(required = false) String templateType,
                      @RequestParam(required = false) Boolean isSystem) {
        try {
            Page<Template> page = templateService.findPage(pageNum, pageSize, templateName, templateType, isSystem);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error("查询模板列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取模板详情
     * @param templateId 模板ID
     * @return 模板详情
     */
    @GetMapping("/{templateId}")
    public Result getTemplate(@PathVariable Long templateId) {
        try {
            Template template = templateService.getById(templateId);
            if (template == null) {
                return Result.error("模板不存在");
            }
            return Result.success(template);
        } catch (Exception e) {
            return Result.error("获取模板详情失败: " + e.getMessage());
        }
    }

    /**
     * 创建自定义模板
     * @param params 模板参数
     * @return 创建结果
     */
    @PostMapping
    public Result createTemplate(@RequestBody Map<String, Object> params) {
        try {
            Template template = new Template();
            template.setTemplateName((String) params.get("templateName"));
            template.setDescription((String) params.get("description"));
            template.setTemplateType((String) params.get("templateType"));
            template.setCreateUserId(((Number) params.get("createUserId")).longValue());
            
            String projectPath = (String) params.get("projectPath");
            boolean result = templateService.saveCustomTemplate(template, projectPath);
            
            if (result) {
                return Result.success(template);
            } else {
                return Result.error("创建模板失败");
            }
        } catch (Exception e) {
            return Result.error("创建模板失败: " + e.getMessage());
        }
    }

    /**
     * 更新模板信息
     * @param template 模板信息
     * @return 更新结果
     */
    @PutMapping
    public Result updateTemplate(@RequestBody Template template) {
        try {
            boolean result = templateService.updateById(template);
            if (result) {
                return Result.success(template);
            } else {
                return Result.error("更新模板失败");
            }
        } catch (Exception e) {
            return Result.error("更新模板失败: " + e.getMessage());
        }
    }

    /**
     * 删除模板
     * @param templateId 模板ID
     * @return 删除结果
     */
    @DeleteMapping("/{templateId}")
    public Result deleteTemplate(@PathVariable Long templateId) {
        try {
            Template template = templateService.getById(templateId);
            if (template == null) {
                return Result.error("模板不存在");
            }
            
            // 系统预设模板不允许删除
            if (template.getIsSystem() != null && template.getIsSystem()) {
                return Result.error("系统预设模板不允许删除");
            }
            
            boolean result = templateService.removeById(templateId);
            if (result) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            return Result.error("删除模板失败: " + e.getMessage());
        }
    }

    /**
     * 从模板生成项目
     * @param params 项目参数
     * @return 生成的项目路径
     */
    @PostMapping("/generate-project")
    public Result generateProject(@RequestBody Map<String, Object> params) {
        try {
            Long templateId = ((Number) params.get("templateId")).longValue();
            Map<String, Object> projectInfo = (Map<String, Object>) params.get("projectInfo");
            String targetPath = (String) params.get("targetPath");
            
            Path projectPath = templateService.generateProjectFromTemplate(templateId, projectInfo, targetPath);
            return Result.success(projectPath.toString());
        } catch (IOException e) {
            return Result.error("生成项目失败: " + e.getMessage());
        } catch (Exception e) {
            return Result.error("生成项目失败: " + e.getMessage());
        }
    }

    /**
     * 获取推荐模板
     * @param templateType 模板类型
     * @return 推荐模板
     */
    @GetMapping("/recommended/{templateType}")
    public Result getRecommendedTemplate(@PathVariable String templateType) {
        try {
            Template template = templateService.getRecommendedTemplate(templateType);
            return Result.success(template);
        } catch (Exception e) {
            return Result.error("获取推荐模板失败: " + e.getMessage());
        }
    }

    /**
     * 初始化系统模板
     * @return 初始化结果
     */
    @PostMapping("/init-system-templates")
    public Result initSystemTemplates() {
        try {
            templateService.initSystemTemplates();
            return Result.success("系统模板初始化成功");
        } catch (Exception e) {
            return Result.error("系统模板初始化失败: " + e.getMessage());
        }
    }

    /**
     * 自定义结果类，用于统一API响应格式
     */
    public static class Result {
        private int code;
        private String message;
        private Object data;

        public Result(int code, String message, Object data) {
            this.code = code;
            this.message = message;
            this.data = data;
        }

        public static Result success(Object data) {
            return new Result(200, "success", data);
        }

        public static Result error(String message) {
            return new Result(500, message, null);
        }

        // getter and setter
        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }
    }
}