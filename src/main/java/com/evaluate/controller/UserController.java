package com.evaluate.controller;

import com.evaluate.common.Result;
import com.evaluate.entity.User;
import com.evaluate.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户认证控制器
 *
 * @author System
 * @since 2025-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private IUserService userService;

    /**
     * 获取所有用户列表（用于登录时的用户名检查）
     */
    @GetMapping("/list")
    public Result<List<User>> getUserList() {
        try {
            List<User> users = userService.getAllUsers();
            // 不返回密码信息
            users.forEach(user -> user.setPassword(null));
            return Result.success(users);
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            return Result.error("获取用户列表失败: " + e.getMessage());
        }
    }

    /**
     * 用户登录验证
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");

            if (username == null || username.trim().isEmpty()) {
                return Result.error("用户名不能为空");
            }
            if (password == null || password.trim().isEmpty()) {
                return Result.error("密码不能为空");
            }

            User user = userService.validateUser(username, password);
            if (user == null) {
                return Result.error("用户名或密码错误");
            }

            // 构造返回数据（不包含密码）
            Map<String, Object> result = new HashMap<>();
            result.put("username", user.getUsername());
            result.put("isAdmin", user.getIsAdmin() != null ? user.getIsAdmin() : false);

            return Result.success(result);
        } catch (Exception e) {
            log.error("登录验证失败", e);
            return Result.error("登录验证失败: " + e.getMessage());
        }
    }

    /**
     * 检查用户名是否已存在
     */
    @GetMapping("/exists/{username}")
    public Result<Boolean> checkUserExists(@PathVariable String username) {
        try {
            boolean exists = userService.isUserExists(username);
            return Result.success(exists);
        } catch (Exception e) {
            log.error("检查用户名是否存在失败", e);
            return Result.error("检查用户名是否存在失败: " + e.getMessage());
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Boolean> register(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");

            if (username == null || username.trim().isEmpty()) {
                return Result.error("用户名不能为空");
            }
            if (username.length() < 2 || username.length() > 20) {
                return Result.error("用户名长度必须在 2 到 20 个字符之间");
            }
            if (password == null || password.trim().isEmpty()) {
                return Result.error("密码不能为空");
            }
            if (password.length() < 6) {
                return Result.error("密码长度不能少于 6 个字符");
            }

            boolean success = userService.registerUser(username, password);
            if (success) {
                return Result.success(true);
            } else {
                return Result.error("用户名已存在");
            }
        } catch (Exception e) {
            log.error("用户注册失败", e);
            return Result.error("用户注册失败: " + e.getMessage());
        }
    }
}
