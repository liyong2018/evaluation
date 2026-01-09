package com.evaluate.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.evaluate.entity.Role;
import com.evaluate.entity.User;
import com.evaluate.mapper.RoleMapper;
import com.evaluate.mapper.UserMapper;
import com.evaluate.mapper.UserRoleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdminUserInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserInitializer.class);

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${INIT_ADMIN_USERNAME:admin}")
    private String adminUsername;

    @Value("${INIT_ADMIN_PASSWORD:123456}")
    private String adminPassword;

    public AdminUserInitializer(
            UserMapper userMapper,
            RoleMapper roleMapper,
            UserRoleMapper userRoleMapper,
            PasswordEncoder passwordEncoder
    ) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            if (adminUsername == null || adminUsername.trim().isEmpty()) {
                return;
            }
            if (adminPassword == null || adminPassword.trim().isEmpty()) {
                return;
            }

            Role adminRole = roleMapper.selectOne(
                    new LambdaQueryWrapper<Role>().eq(Role::getRoleCode, "ROLE_ADMIN").last("LIMIT 1")
            );
            if (adminRole == null) {
                Role r = new Role();
                r.setRoleName("管理员");
                r.setRoleCode("ROLE_ADMIN");
                r.setDescription("系统管理员");
                roleMapper.insert(r);
                adminRole = r;
            }

            User user = userMapper.selectUserByUsername(adminUsername);
            if (user == null) {
                User u = new User();
                u.setUsername(adminUsername);
                u.setPassword(passwordEncoder.encode(adminPassword));
                u.setNickname("管理员");
                u.setStatus(1);
                userMapper.insert(u);
                user = u;
            } else {
                boolean needUpdate = user.getPassword() == null || !passwordEncoder.matches(adminPassword, user.getPassword());
                if (needUpdate) {
                    String encoded = passwordEncoder.encode(adminPassword);
                    userMapper.update(
                            null,
                            new LambdaUpdateWrapper<User>()
                                    .eq(User::getUsername, adminUsername)
                                    .set(User::getPassword, encoded)
                                    .set(User::getStatus, 1)
                    );
                    user = userMapper.selectUserByUsername(adminUsername);
                }
            }

            if (user != null && user.getId() != null && adminRole.getId() != null) {
                Long adminRoleId = adminRole.getId();
                List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(user.getId());
                boolean hasRole = roleIds != null && roleIds.stream().anyMatch(id -> id != null && id.equals(adminRoleId));
                if (!hasRole) {
                    userRoleMapper.insert(user.getId(), adminRoleId);
                }
            }
        } catch (Exception e) {
            log.error("初始化管理员账号失败", e);
        }
    }
}
