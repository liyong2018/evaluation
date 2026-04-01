package com.evaluate.service;

import com.evaluate.entity.User;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

/**
 * 用户服务接口
 *
 * @author System
 * @since 2025-01-01
 */
public interface IUserService {

    /**
     * 获取所有用户列表
     *
     * @return 用户列表
     */
    List<User> getAllUsers();

    /**
     * 根据用户名验证用户密码（不检查角色）
     *
     * @param username 用户名
     * @param password 密码
     * @return 验证通过返回用户对象，否则返回 null
     */
    User validateCredentials(String username, String password);

    /**
     * 检查用户是否有角色
     *
     * @param userId 用户ID
     * @return 有角色返回 true，否则返回 false
     */
    boolean hasRoles(Long userId);

    /**
     * 根据用户名验证用户密码（包含角色检查）
     *
     * @param username 用户名
     * @param password 密码
     * @return 验证通过返回用户对象，否则返回 null
     */
    User validateUser(String username, String password);

    /**
     * 检查用户名是否已存在
     *
     * @param username 用户名
     * @return 存在返回 true，否则返回 false
     */
    boolean isUserExists(String username);

    /**
     * 注册新用户
     *
     * @param username 用户名
     * @param password 密码
     * @return 注册成功返回 true，用户名已存在返回 false
     */
    boolean registerUser(String username, String password);
}
