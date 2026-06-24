package com.example.projectmanagement.controller;

import com.example.projectmanagement.entity.User;
import com.example.projectmanagement.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController extends BaseController {

    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserController(UserService userService, BCryptPasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> getCurrentUser() {
        User user = currentUserEntity();
        if (user == null) {
            return error("用户未登录");
        }
        return success(toView(user));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER', 'TESTER')")
    public ApiResponse<List<Map<String, Object>>> getUserList() {
        List<Map<String, Object>> users = userService.list().stream().map(this::toView).toList();
        return success(users);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> createUser(@RequestBody CreateUserRequest requestBody) {
        if (requestBody.getUsername() == null || requestBody.getUsername().isBlank()) {
            return error("用户名不能为空");
        }
        if (requestBody.getPassword() == null || requestBody.getPassword().length() < 6) {
            return error("密码长度不能少于6位");
        }
        User user = new User();
        user.setUsername(requestBody.getUsername().trim());
        user.setPassword(requestBody.getPassword());
        user.setNickname(requestBody.getNickname());
        user.setEmail(requestBody.getEmail());
        user.setPhone(requestBody.getPhone());
        user.setStatus(requestBody.getStatus() == null ? 1 : requestBody.getStatus());

        boolean created = userService.createUser(user, requestBody.getRoleCode());
        if (!created) {
            return error("创建用户失败，用户名可能已存在或角色无效");
        }
        return success(toView(userService.getById(user.getId())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    public ApiResponse<Map<String, Object>> getUserById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return error("用户不存在");
        }
        return success(toView(user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    public ApiResponse<Boolean> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest requestBody) {
        User existingUser = userService.getById(id);
        if (existingUser == null) {
            return error("用户不存在");
        }

        User currentUser = currentUserEntity();
        boolean admin = hasAdminRole();
        if (!admin && (currentUser == null || !id.equals(currentUser.getId()))) {
            return error("没有权限修改此用户信息");
        }

        User updateUser = new User();
        updateUser.setId(id);
        updateUser.setNickname(requestBody.getNickname());
        updateUser.setEmail(requestBody.getEmail());
        updateUser.setPhone(requestBody.getPhone());
        if (admin && requestBody.getStatus() != null) {
            updateUser.setStatus(requestBody.getStatus());
        }
        if (requestBody.getPassword() != null && !requestBody.getPassword().isBlank()) {
            if (requestBody.getPassword().length() < 6) {
                return error("密码长度不能少于6位");
            }
            updateUser.setPassword(passwordEncoder.encode(requestBody.getPassword()));
        }

        boolean updated = userService.updateUser(updateUser);
        if (!updated) {
            return error("更新用户信息失败");
        }
        if (admin && requestBody.getRoleCode() != null && !requestBody.getRoleCode().isBlank()) {
            boolean roleUpdated = userService.updateRole(id, requestBody.getRoleCode());
            if (!roleUpdated) {
                return error("用户信息已更新，但角色更新失败");
            }
        }
        return success(true);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        if (id.equals(getCurrentUserId())) {
            return error("不能删除当前登录账号");
        }
        if (!userService.deleteUser(id)) {
            return error("删除用户失败");
        }
        return success();
    }

    @PutMapping("/{id}/profile")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    public ApiResponse<Boolean> updateProfile(@PathVariable Long id, @RequestBody UpdateProfileRequest requestBody) {
        User existingUser = userService.getById(id);
        if (existingUser == null) {
            return error("用户不存在");
        }
        User user = new User();
        user.setId(id);
        user.setNickname(requestBody.getNickname());
        user.setEmail(requestBody.getEmail());
        user.setPhone(requestBody.getPhone());
        user.setUpdateTime(LocalDateTime.now());
        return success(userService.updateUser(user));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Boolean> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            return error("状态值无效，只能是0（禁用）或1（启用）");
        }
        User existingUser = userService.getById(id);
        if (existingUser == null) {
            return error("用户不存在");
        }
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        return success(userService.updateUser(user));
    }

    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Boolean> resetUserPassword(@PathVariable Long id, @RequestParam String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            return error("密码长度不能少于6位");
        }
        if (userService.getById(id) == null) {
            return error("用户不存在");
        }
        User user = new User();
        user.setId(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        return success(userService.updateUser(user));
    }

    @PutMapping("/change-password")
    public ApiResponse<Boolean> changePassword(@RequestParam String oldPassword, @RequestParam String newPassword) {
        User currentUser = currentUserEntity();
        if (currentUser == null) {
            return error("用户未登录");
        }
        if (!passwordEncoder.matches(oldPassword, currentUser.getPassword())) {
            return error("旧密码不正确");
        }
        if (newPassword == null || newPassword.length() < 6) {
            return error("新密码长度不能少于6位");
        }
        User user = new User();
        user.setId(currentUser.getId());
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        return success(userService.updateUser(user));
    }

    private User currentUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            return null;
        }
        return userService.findByUsername(userDetails.getUsername());
    }

    private boolean hasAdminRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    private Map<String, Object> toView(User user) {
        User latest = userService.getById(user.getId());
        User source = latest == null ? user : latest;
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", source.getId());
        view.put("username", source.getUsername());
        view.put("nickname", source.getNickname() == null ? "" : source.getNickname());
        view.put("email", source.getEmail() == null ? "" : source.getEmail());
        view.put("phone", source.getPhone() == null ? "" : source.getPhone());
        view.put("status", source.getStatus() == null ? 1 : source.getStatus());
        view.put("roleCodes", userService.findRolesByUserId(source.getId()));
        view.put("createTime", source.getCreateTime() == null ? "" : source.getCreateTime().toString().replace('T', ' '));
        return view;
    }

    static class CreateUserRequest {
        private String username;
        private String password;
        private String nickname;
        private String email;
        private String phone;
        private Integer status;
        private String roleCode;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
        public String getRoleCode() { return roleCode; }
        public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
    }

    static class UpdateUserRequest {
        private String nickname;
        private String email;
        private String phone;
        private Integer status;
        private String password;
        private String roleCode;

        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getRoleCode() { return roleCode; }
        public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
    }

    static class UpdateProfileRequest {
        private String nickname;
        private String email;
        private String phone;

        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }
}
