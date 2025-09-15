package com.example.projectmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.projectmanagement.entity.EnvironmentConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 环境配置Mapper接口
 * 用于环境配置相关的数据库操作
 */
@Mapper
public interface EnvironmentConfigMapper extends BaseMapper<EnvironmentConfig> {
    
    /**
     * 根据项目ID查询环境配置列表
     * @param projectId 项目ID
     * @return 环境配置列表
     */
    List<EnvironmentConfig> findByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据项目ID和环境类型查询环境配置
     * @param projectId 项目ID
     * @param envType 环境类型
     * @return 环境配置信息
     */
    EnvironmentConfig findByProjectIdAndEnvType(@Param("projectId") Long projectId, 
                                              @Param("envType") String envType);

    /**
     * 根据环境类型查询启用的环境配置
     * @param envType 环境类型
     * @return 环境配置列表
     */
    List<EnvironmentConfig> findEnabledByEnvType(@Param("envType") String envType);
}