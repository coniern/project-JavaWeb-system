package com.example.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.project.common.Result;
import com.example.project.entity.User;
import com.example.project.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String username = params.getOrDefault("username", "").trim();
        String password = params.getOrDefault("password", "").trim();

        if (username.isEmpty() || password.isEmpty()) {
            return Result.error(400, "用户名和密码不能为空");
        }

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .eq(User::getPassword, password)
                .eq(User::getDeleted, 0));

        if (user == null) {
            return Result.error("用户名或密码错误");
        }

        if (!Integer.valueOf(1).equals(user.getStatus())) {
            return Result.error(403, "用户已被禁用");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("token", "token-" + user.getId());
        data.put("user", user);

        return Result.success(data);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody User user) {
        return createInternal(user, false);
    }

    /**
     * 后台创建用户
     */
    @PostMapping("/create")
    public Result<String> create(@RequestBody User user) {
        return createInternal(user, true);
    }

    private Result<String> createInternal(User user, boolean allowCustomStatus) {
        if (user == null) {
            return Result.error(400, "请求参数不能为空");
        }
        String username = user.getUsername() == null ? "" : user.getUsername().trim();
        String password = user.getPassword() == null ? "" : user.getPassword().trim();
        String nickname = user.getNickname() == null ? "" : user.getNickname().trim();

        if (username.isEmpty()) {
            return Result.error(400, "用户名不能为空");
        }
        if (password.isEmpty()) {
            return Result.error(400, "密码不能为空");
        }
        if (nickname.isEmpty()) {
            nickname = username;
        }

        User existUser = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .eq(User::getDeleted, 0));

        if (existUser != null) {
            return Result.error("用户名已存在");
        }

        user.setUsername(username);
        user.setPassword(password);
        user.setNickname(nickname);
        user.setStatus(allowCustomStatus && user.getStatus() != null ? user.getStatus() : 1);
        user.setDeleted(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);

        return Result.success(allowCustomStatus ? "创建成功" : "注册成功");
    }

    /**
     * 获取用户列表
     */
    @GetMapping("/list")
    public Result<List<User>> list() {
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getDeleted, 0));
        return Result.success(users);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<User> userInfo(@RequestParam String token) {
        if (token == null || !token.startsWith("token-")) {
            return Result.error(400, "无效 token");
        }
        Long userId = Long.parseLong(token.replace("token-", ""));
        User user = userMapper.selectById(userId);
        if (user == null || Integer.valueOf(1).equals(user.getDeleted())) {
            return Result.error(404, "用户不存在");
        }
        return Result.success(user);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/update")
    public Result<String> update(@RequestBody User user) {
        if (user == null || user.getId() == null) {
            return Result.error(400, "用户ID不能为空");
        }
        User existingUser = userMapper.selectById(user.getId());
        if (existingUser == null || Integer.valueOf(1).equals(existingUser.getDeleted())) {
            return Result.error(404, "用户不存在");
        }
        if (user.getNickname() == null || user.getNickname().trim().isEmpty()) {
            return Result.error(400, "昵称不能为空");
        }
        if (user.getStatus() == null) {
            user.setStatus(existingUser.getStatus());
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            user.setPassword(existingUser.getPassword());
        }
        user.setUsername(existingUser.getUsername());
        user.setDeleted(existingUser.getDeleted());
        userMapper.updateById(user);
        return Result.success("更新成功");
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null || Integer.valueOf(1).equals(user.getDeleted())) {
            return Result.error(404, "用户不存在");
        }
        userMapper.deleteById(id);
        return Result.success("删除成功");
    }
}
