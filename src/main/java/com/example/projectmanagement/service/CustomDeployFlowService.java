package com.example.projectmanagement.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.projectmanagement.entity.CustomDeployFlow;

import java.util.List;

/**
 * 自定义部署流程服务接口
 * 提供自定义部署流程相关的业务逻辑
 */
public interface CustomDeployFlowService extends IService<CustomDeployFlow> {

    /**
     * 分页查询自定义部署流程
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param projectId 项目ID
     * @param flowName 流程名称
     * @return 自定义部署流程分页列表
     */
    Page<CustomDeployFlow> findPage(Integer pageNum, Integer pageSize, Long projectId, String flowName);

    /**
     * 根据项目ID查询自定义部署流程
     * @param projectId 项目ID
     * @return 自定义部署流程列表
     */
    List<CustomDeployFlow> findByProjectId(Long projectId);

    /**
     * 查询项目的默认部署流程
     * @param projectId 项目ID
     * @return 默认部署流程
     */
    CustomDeployFlow findDefaultFlowByProjectId(Long projectId);

    /**
     * 设置项目的默认部署流程
     * @param projectId 项目ID
     * @param flowId 流程ID
     * @return 设置结果
     */
    boolean setDefaultFlow(Long projectId, Long flowId);

    /**
     * 执行自定义部署流程
     * @param flowId 流程ID
     * @param projectId 项目ID
     * @param env 环境类型
     * @param version 版本号
     * @param deployerId 部署人ID
     * @return 部署ID
     */
    Long executeCustomFlow(Long flowId, Long projectId, String env, String version, Long deployerId);

    /**
     * 继续执行被暂停的部署流程
     * @param flowExecutionId 流程执行ID
     * @return 继续执行结果
     */
    boolean resumeFlowExecution(Long flowExecutionId);

    /**
     * 暂停部署流程
     * @param flowExecutionId 流程执行ID
     * @return 暂停结果
     */
    boolean pauseFlowExecution(Long flowExecutionId);
}