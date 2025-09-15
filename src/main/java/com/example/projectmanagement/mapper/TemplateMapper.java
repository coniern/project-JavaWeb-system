package com.example.projectmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.projectmanagement.entity.Template;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 项目模板Mapper接口
 * 提供项目模板的数据库访问操作
 */
@Mapper
public interface TemplateMapper extends BaseMapper<Template> {

    /**
     * 分页查询模板列表
     * @param page 分页参数
     * @param templateName 模板名称（模糊查询）
     * @param templateType 模板类型
     * @param isSystem 是否为系统模板
     * @return 模板分页列表
     */
    Page<Template> findPage(@Param("page") Page<Template> page,
                           @Param("templateName") String templateName,
                           @Param("templateType") String templateType,
                           @Param("isSystem") Boolean isSystem);

    /**
     * 根据模板类型查询模板列表
     * @param templateType 模板类型
     * @return 模板列表
     */
    List<Template> findByType(@Param("templateType") String templateType);

    /**
     * 查询系统预设模板
     * @return 系统模板列表
     */
    List<Template> findSystemTemplates();

    /**
     * 查询用户自定义模板
     * @param createUserId 创建用户ID
     * @return 用户自定义模板列表
     */
    List<Template> findUserTemplates(@Param("createUserId") Long createUserId);
}