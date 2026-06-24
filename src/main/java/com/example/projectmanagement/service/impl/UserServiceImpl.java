package com.example.projectmanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.projectmanagement.entity.Role;
import com.example.projectmanagement.entity.User;
import com.example.projectmanagement.mapper.UserMapper;
import com.example.projectmanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户服务实现类
 * 实现用户相关的业务逻辑
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService, UserDetailsService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }
    
    @Override
    public List<User> findByProjectId(Long projectId) {
        return userMapper.findByProjectId(projectId);
    }
    
    @Override
    public boolean register(User user) {
        return createUser(user, "DEVELOPER");
    }

    @Override
    @Transactional
    public boolean createUser(User user, String roleCode) {
        if (user == null || user.getUsername() == null || user.getUsername().isBlank() || user.getPassword() == null || user.getPassword().isBlank()) {
            return false;
        }
        if (findByUsername(user.getUsername()) != null) {
            return false;
        }

        String finalRoleCode = roleCode == null || roleCode.isBlank() ? "DEVELOPER" : roleCode.trim();
        Long roleId = userMapper.findRoleIdByCode(finalRoleCode);
        if (roleId == null) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(user.getStatus() == null ? 1 : user.getStatus());
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        boolean saved = save(user);
        if (!saved || user.getId() == null) {
            return false;
        }
        return userMapper.insertUserRole(user.getId(), roleId) > 0;
    }
    
    @Override
    public boolean updateUser(User user) {
        user.setUpdateTime(LocalDateTime.now());
        return updateById(user);
    }

    @Override
    @Transactional
    public boolean updateRole(Long userId, String roleCode) {
        if (userId == null || roleCode == null || roleCode.isBlank()) {
            return false;
        }
        Long roleId = userMapper.findRoleIdByCode(roleCode.trim());
        if (roleId == null) {
            return false;
        }
        userMapper.deleteUserRoles(userId);
        return userMapper.insertUserRole(userId, roleId) > 0;
    }

    @Override
    @Transactional
    public boolean deleteUser(Long userId) {
        if (userId == null) {
            return false;
        }
        userMapper.deleteUserRoles(userId);
        return removeById(userId);
    }
    
    @Override
    public List<String> findRolesByUserId(Long userId) {
        return userMapper.findRolesByUserId(userId);
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户名不存在");
        }
        
        List<String> roleCodes = findRolesByUserId(user.getId());
        List<Role> roles = new ArrayList<>();
        for (String roleCode : roleCodes) {
            Role role = new Role();
            role.setCode("ROLE_" + roleCode);
            role.setName(roleCode);
            roles.add(role);
        }
        user.setRoles(roles);
        
        return user;
    }
}
