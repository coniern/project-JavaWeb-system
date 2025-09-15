package com.example.projectmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.projectmanagement.entity.ProjectMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 项目成员Mapper接口
 * 用于项目成员相关的数据库操作
 */
@Mapper
public interface ProjectMemberMapper extends BaseMapper<ProjectMember> {
    
    /**
     * 根据项目ID查询项目成员列表
     * @param projectId 项目ID
     * @return 项目成员列表
     */
    List<ProjectMember> findByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据用户ID查询项目成员列表
     * @param userId 用户ID
     * @return 项目成员列表
     */
    List<ProjectMember> findByUserId(@Param("userId") Long userId);

    /**
     * 根据项目ID和用户ID查询项目成员
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return 项目成员信息
     */
    ProjectMember findByProjectIdAndUserId(@Param("projectId") Long projectId, 
                                          @Param("userId") Long userId);

    /**
     * 根据项目ID删除所有项目成员
     * @param projectId 项目ID
     * @return 受影响的行数
     */
    int deleteByProjectId(@Param("projectId") Long projectId);
}