package com.example.projectmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.projectmanagement.entity.FlowExecution;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 流程执行Mapper接口
 * 用于流程执行相关的数据库操作
 */
@Mapper
public interface FlowExecutionMapper extends BaseMapper<FlowExecution> {

    /**
     * 分页查询流程执行记录
     * @param page 分页参数
     * @param projectId 项目ID
     * @param flowId 流程ID
     * @param status 执行状态
     * @return 流程执行记录分页列表
     */
    Page<FlowExecution> findPage(Page<FlowExecution> page, 
                               @Param("projectId") Long projectId, 
                               @Param("flowId") Long flowId, 
                               @Param("status") String status);

    /**
     * 根据项目ID查询流程执行记录
     * @param projectId 项目ID
     * @return 流程执行记录列表
     */
    List<FlowExecution> findByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据流程ID查询流程执行记录
     * @param flowId 流程ID
     * @return 流程执行记录列表
     */
    List<FlowExecution> findByFlowId(@Param("flowId") Long flowId);
}