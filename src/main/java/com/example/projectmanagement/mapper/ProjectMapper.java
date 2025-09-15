package com.example.projectmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.projectmanagement.entity.Project;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 项目Mapper接口
 * 用于项目相关的数据库操作
 */
@Mapper
public interface ProjectMapper extends BaseMapper<Project> {
    
    /**
     * 分页查询项目列表
     * @param page 分页参数
     * @param status 项目状态
     * @param keyword 搜索关键词
     * @return 项目列表
     */
    Page<Project> findPage(Page<Project> page, 
                          @Param("status") String status, 
                          @Param("keyword") String keyword);

    /**
     * 根据负责人ID查询项目列表
     * @param leaderId 负责人ID
     * @return 项目列表
     */
    List<Project> findByLeaderId(@Param("leaderId") Long leaderId);

    /**
     * 根据技术栈查询项目列表
     * @param techStack 技术栈
     * @return 项目列表
     */
    List<Project> findByTechStack(@Param("techStack") String techStack);
}