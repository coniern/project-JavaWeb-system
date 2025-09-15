package com.example.projectmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.projectmanagement.entity.CustomDeployFlow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 自定义部署流程Mapper接口
 * 用于自定义部署流程相关的数据库操作
 */
@Mapper
public interface CustomDeployFlowMapper extends BaseMapper<CustomDeployFlow> {

    /**
     * 分页查询自定义部署流程
     * @param page 分页参数
     * @param projectId 项目ID
     * @param flowName 流程名称（模糊查询）
     * @return 自定义部署流程列表
     */
    Page<CustomDeployFlow> findPage(Page<CustomDeployFlow> page, 
                                   @Param("projectId") Long projectId, 
                                   @Param("flowName") String flowName);

    /**
     * 根据项目ID查询自定义部署流程
     * @param projectId 项目ID
     * @return 自定义部署流程列表
     */
    List<CustomDeployFlow> findByProjectId(@Param("projectId") Long projectId);

    /**
     * 查询项目的默认部署流程
     * @param projectId 项目ID
     * @return 默认部署流程
     */
    CustomDeployFlow findDefaultFlowByProjectId(@Param("projectId") Long projectId);
}