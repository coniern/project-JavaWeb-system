package com.example.projectmanagement.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.projectmanagement.entity.Template;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * 项目模板服务接口
 * 提供项目模板的业务逻辑操作
 */
public interface TemplateService extends IService<Template> {

    /**
     * 分页查询模板列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param templateName 模板名称
     * @param templateType 模板类型
     * @param isSystem 是否为系统模板
     * @return 模板分页列表
     */
    Page<Template> findPage(Integer pageNum, Integer pageSize, String templateName, String templateType, Boolean isSystem);

    /**
     * 初始化系统预设模板
     * 当系统首次启动时，自动创建预设的项目模板
     */
    void initSystemTemplates();

    /**
     * 根据模板生成项目基础结构
     * @param templateId 模板ID
     * @param projectInfo 项目信息（包含项目名称、包名等）
     * @param targetPath 目标路径
     * @return 生成的项目路径
     * @throws IOException IO异常
     */
    Path generateProjectFromTemplate(Long templateId, Map<String, Object> projectInfo, String targetPath) throws IOException;

    /**
     * 保存自定义模板
     * @param template 模板信息
     * @param projectPath 项目路径（用于提取模板结构）
     * @return 保存结果
     */
    boolean saveCustomTemplate(Template template, String projectPath);

    /**
     * 根据模板类型获取推荐模板
     * @param templateType 模板类型
     * @return 推荐的模板
     */
    Template getRecommendedTemplate(String templateType);
}