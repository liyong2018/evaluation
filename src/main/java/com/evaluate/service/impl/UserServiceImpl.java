package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.evaluate.entity.Role;
import com.evaluate.entity.User;
import com.evaluate.mapper.RoleMapper;
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
    private RoleMapper roleMapper;

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
    public User validateUser(String username, String password) {
        User user = userMapper.selectUserByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
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

        // 创建新用户
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setStatus(1); // 默认启用
        
        int rows = userMapper.insert(newUser);
        
        // 分配默认角色 (普通用户)
        if (rows > 0) {
            // 假设 ROLE_USER 是默认角色，先查询出来
            Role defaultRole = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getRoleCode, "ROLE_USER"));
            if (defaultRole != null) {
                userRoleMapper.insert(newUser.getId(), defaultRole.getId());
            }
            return true;
        }
        
        return false;
    }
}
