package com.example.projectmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.projectmanagement.entity.Deployment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部署Mapper接口
 * 用于部署历史相关的数据库操作
 */
@Mapper
public interface DeploymentMapper extends BaseMapper<Deployment> {
    
    /**
     * 分页查询部署历史
     * @param page 分页参数
     * @param projectId 项目ID
     * @param env 环境类型
     * @param status 部署状态
     * @return 部署历史列表
     */
    Page<Deployment> findPage(Page<Deployment> page, 
                             @Param("projectId") Long projectId, 
                             @Param("env") String env, 
                             @Param("status") String status);

    /**
     * 根据项目ID和环境类型查询最新的部署记录
     * @param projectId 项目ID
     * @param env 环境类型
     * @return 最新的部署记录
     */
    Deployment findLatestByProjectIdAndEnv(@Param("projectId") Long projectId, 
                                          @Param("env") String env);

    /**
     * 根据项目ID查询部署历史
     * @param projectId 项目ID
     * @return 部署历史列表
     */
    List<Deployment> findByProjectId(@Param("projectId") Long projectId);
}