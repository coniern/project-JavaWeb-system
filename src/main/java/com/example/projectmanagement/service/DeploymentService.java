package com.example.projectmanagement.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.projectmanagement.entity.Deployment;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 部署服务接口
 * 提供部署相关的业务逻辑
 */
public interface DeploymentService extends IService<Deployment> {
    
    /**
     * 分页查询部署历史
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param projectId 项目ID
     * @param env 环境类型
     * @param status 部署状态
     * @return 部署历史分页列表
     */
    Page<Deployment> findPage(Integer pageNum, Integer pageSize, Long projectId, String env, String status);
    
    /**
     * 创建部署记录
     * @param deployment 部署记录
     * @return 创建结果
     */
    boolean createDeployment(Deployment deployment);
    
    /**
     * 更新部署状态
     * @param deployId 部署ID
     * @param status 部署状态
     * @return 更新结果
     */
    boolean updateDeploymentStatus(Long deployId, String status);
    
    /**
     * 添加部署日志
     * @param deployId 部署ID
     * @param log 日志内容
     * @return 更新结果
     */
    boolean addDeploymentLog(Long deployId, String log);
    
    /**
     * 根据项目ID查询部署历史
     * @param projectId 项目ID
     * @return 部署历史列表
     */
    List<Deployment> findByProjectId(Long projectId);
    
    /**
     * 获取项目在指定环境的最新部署记录
     * @param projectId 项目ID
     * @param env 环境类型
     * @return 最新的部署记录
     */
    Deployment findLatestByProjectIdAndEnv(Long projectId, String env);
    
    /**
     * 执行部署操作
     * @param projectId 项目ID
     * @param env 环境类型
     * @param version 版本号
     * @param file 部署包文件
     * @param deployerId 部署人ID
     * @return 部署ID
     */
    Long executeDeployment(Long projectId, String env, String version, MultipartFile file, Long deployerId);
    
    /**
     * 分析部署失败原因并生成AI建议
     * @param deploymentId 部署ID
     * @return 分析结果
     */
    boolean analyzeDeploymentFailure(Long deploymentId);
    
    /**
     * 获取常见错误类型及解决方案
     * @return 常见错误解决方案列表
     */
    Map<String, String> getCommonErrors();
}