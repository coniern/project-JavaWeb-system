package com.example.projectmanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.projectmanagement.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

/**
 * 用户服务接口
 * 提供用户相关的业务逻辑
 */
public interface UserService extends IService<User>, UserDetailsService {
    
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    User findByUsername(String username);

    /**
     * 根据项目ID查询项目成员
     * @param projectId 项目ID
     * @return 成员列表
     */
    List<User> findByProjectId(Long projectId);

    /**
     * 注册新用户
     * @param user 用户信息
     * @return 注册结果
     */
    boolean register(User user);

    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 更新结果
     */
    boolean updateUser(User user);

    /**
     * 查询用户的角色列表
     * @param userId 用户ID
     * @return 角色列表
     */
    List<String> findRolesByUserId(Long userId);
}