package com.evaluate.service.impl;

import com.evaluate.entity.User;
import com.evaluate.service.IUserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 * 从配置文件读取和写入用户数据
 *
 * @author System
 * @since 2025-01-01
 */
@Service
public class UserServiceImpl implements IUserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final String USERS_FILE_PATH = "src/main/resources/users.json";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private List<User> cachedUsers = null;

    @Override
    public List<User> getAllUsers() {
        if (cachedUsers == null) {
            loadUsersFromFile();
        }
        return new ArrayList<>(cachedUsers);
    }

    @Override
    public User validateUser(String username, String password) {
        List<User> users = getAllUsers();
        for (User user : users) {
            if (user.getUsername() != null && user.getUsername().equals(username)
                    && user.getPassword() != null && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public boolean isUserExists(String username) {
        List<User> users = getAllUsers();
        return users.stream()
                .anyMatch(user -> user.getUsername() != null && user.getUsername().equals(username));
    }

    @Override
    public boolean registerUser(String username, String password) {
        // 检查用户名是否已存在
        if (isUserExists(username)) {
            log.warn("用户名已存在: {}", username);
            return false;
        }

        // 创建新用户
        User newUser = new User(username, password, false);
        cachedUsers.add(newUser);

        // 保存到文件
        return saveUsersToFile();
    }

    /**
     * 从文件加载用户列表
     */
    private void loadUsersFromFile() {
        File file = new File(USERS_FILE_PATH);
        if (!file.exists()) {
            log.warn("用户配置文件不存在: {}, 使用默认用户列表", USERS_FILE_PATH);
            cachedUsers = createDefaultUsers();
            return;
        }

        try {
            cachedUsers = objectMapper.readValue(file, new TypeReference<List<User>>() {});
            log.info("成功加载 {} 个用户", cachedUsers.size());
        } catch (IOException e) {
            log.error("读取用户配置文件失败: {}", e.getMessage(), e);
            cachedUsers = createDefaultUsers();
        }
    }

    /**
     * 保存用户列表到文件
     */
    private boolean saveUsersToFile() {
        try {
            File file = new File(USERS_FILE_PATH);
            // 确保目录存在
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            // 格式化输出 JSON
            String json = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(cachedUsers);

            Files.write(file.toPath(), json.getBytes(), StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            log.info("成功保存 {} 个用户到配置文件", cachedUsers.size());
            return true;
        } catch (IOException e) {
            log.error("保存用户配置文件失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 创建默认用户列表（当配置文件不存在时使用）
     */
    private List<User> createDefaultUsers() {
        List<User> users = new ArrayList<>();
        users.add(new User("admin", "admin@123", true));
        users.add(new User("林老师", "123456", false));
        users.add(new User("荣老师", "123456", false));
        users.add(new User("何春梅", "123456", false));
        return users;
    }
}
