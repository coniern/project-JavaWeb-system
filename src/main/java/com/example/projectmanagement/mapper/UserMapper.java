package com.example.projectmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.projectmanagement.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户Mapper接口
 * 用于用户相关的数据库操作
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    User findByUsername(@Param("username") String username);

    /**
     * 根据项目ID查询项目成员
     * @param projectId 项目ID
     * @return 成员列表
     */
    @Select("SELECT u.* FROM sys_user u JOIN project_member pm ON u.id = pm.user_id WHERE pm.project_id = #{projectId}")
    List<User> findByProjectId(@Param("projectId") Long projectId);

    /**
     * 查询用户的角色列表
     * @param userId 用户ID
     * @return 角色列表
     */
    @Select("SELECT r.code FROM sys_role r JOIN sys_user_role ur ON r.id = ur.role_id WHERE ur.user_id = #{userId}")
    List<String> findRolesByUserId(@Param("userId") Long userId);

    @Select("SELECT id FROM sys_role WHERE code = #{roleCode}")
    Long findRoleIdByCode(@Param("roleCode") String roleCode);

    @Insert("INSERT INTO sys_user_role (user_id, role_id) VALUES (#{userId}, #{roleId})")
    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int deleteUserRoles(@Param("userId") Long userId);
}
