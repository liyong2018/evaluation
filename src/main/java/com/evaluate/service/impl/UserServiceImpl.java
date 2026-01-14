package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.evaluate.entity.User;
import com.evaluate.mapper.UserMapper;
import com.evaluate.mapper.UserRoleMapper;
import com.evaluate.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户服务实现类
 * 数据库实现
 */
@Service
public class UserServiceImpl implements IUserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    @Lazy // 防止循环依赖，因为SecurityConfig可能依赖UserService(虽然这里是UserDetailsServiceImpl，但小心为上)
    private PasswordEncoder passwordEncoder;

    @Override
    public List<User> getAllUsers() {
        return userMapper.selectList(null);
    }

    @Override
    public User validateCredentials(String username, String password) {
        User user = userMapper.selectUserByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    @Override
    public boolean hasRoles(Long userId) {
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        return roleIds != null && !roleIds.isEmpty();
    }

    @Override
    public User validateUser(String username, String password) {
        User user = validateCredentials(username, password);
        if (user != null) {
            // 检查用户是否有角色，没有角色的用户不能登录
            if (!hasRoles(user.getId())) {
                log.warn("用户 {} 没有分配任何角色，禁止登录", username);
                return null;
            }
            return user;
        }
        return null;
    }

    @Override
    public boolean isUserExists(String username) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        return count != null && count > 0;
    }

    @Override
    @Transactional
    public boolean registerUser(String username, String password) {
        // 检查用户名是否已存在
        if (isUserExists(username)) {
            log.warn("用户名已存在: {}", username);
            return false;
        }

        // 创建新用户（不授予任何角色）
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setStatus(1); // 默认启用

        int rows = userMapper.insert(newUser);

        return rows > 0;
    }
}
