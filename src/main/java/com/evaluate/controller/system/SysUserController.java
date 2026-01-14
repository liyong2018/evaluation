package com.evaluate.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evaluate.common.Result;
import com.evaluate.entity.User;
import com.evaluate.service.IUserService;
import com.evaluate.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.evaluate.mapper.UserOrganizationMapper;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * 系统用户管理
 */
@RestController
@RequestMapping("/api/sys/user")
public class SysUserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserOrganizationMapper userOrganizationMapper;

    @Autowired
    private com.evaluate.mapper.UserRoleMapper userRoleMapper;

    @Autowired
    private com.evaluate.mapper.RoleMapper roleMapper;

    /**
     * 分页查询用户
     */
    @GetMapping("/list")
    public Result<Page<User>> list(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer size,
                                   @RequestParam(required = false) String username) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like(User::getUsername, username);
        }
        Page<User> result = userMapper.selectPage(pageParam, wrapper);
        result.getRecords().forEach(u -> {
            u.setPassword(null);
            u.setRoles(roleMapper.selectRolesByUserId(u.getId()));
        });
        return Result.success(result);
    }

    /**
     * 新增用户
     */
    @PostMapping
    public Result<Long> add(@RequestBody User user) {
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userMapper.insert(user);
        return Result.success(user.getId());
    }

    /**
     * 修改用户
     */
    @PutMapping
    public Result<Boolean> update(@RequestBody User user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null); // 不修改密码
        }
        userMapper.updateById(user);
        return Result.success(true);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        userMapper.deleteById(id);
        return Result.success(true);
    }

    @GetMapping("/{userId}/organizations")
    public Result<List<Long>> getUserOrganizations(@PathVariable Long userId) {
        return Result.success(userOrganizationMapper.selectOrgIdsByUserId(userId));
    }

    @PostMapping("/{userId}/organizations")
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> assignOrganizations(@PathVariable Long userId, @RequestBody List<Long> orgIds) {
        userOrganizationMapper.deleteByUserId(userId);
        if (orgIds != null && !orgIds.isEmpty()) {
            for (Long orgId : orgIds) {
                userOrganizationMapper.insertUserOrg(userId, orgId);
            }
        }
        return Result.success(true);
    }

    @GetMapping("/{userId}/roles")
    public Result<List<Long>> getUserRoles(@PathVariable Long userId) {
        return Result.success(userRoleMapper.selectRoleIdsByUserId(userId));
    }

    @PostMapping("/{userId}/roles")
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> assignRoles(@PathVariable Long userId, @RequestBody List<Long> roleIds) {
        userRoleMapper.deleteByUserId(userId);
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                userRoleMapper.insert(userId, roleId);
            }
        }
        return Result.success(true);
    }
}
