package com.evaluate.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evaluate.common.Result;
import com.evaluate.entity.Role;
import com.evaluate.mapper.RoleMapper;
import com.evaluate.mapper.RoleOrganizationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统角色管理
 */
@RestController
@RequestMapping("/api/sys/role")
public class SysRoleController {

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private RoleOrganizationMapper roleOrganizationMapper;

    @GetMapping("/list")
    public Result<Page<Role>> list(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer size,
                                   @RequestParam(required = false) String roleName) {
        Page<Role> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        if (roleName != null && !roleName.isEmpty()) {
            wrapper.like(Role::getRoleName, roleName);
        }
        return Result.success(roleMapper.selectPage(pageParam, wrapper));
    }

    @PostMapping
    public Result<Boolean> add(@RequestBody Role role) {
        roleMapper.insert(role);
        return Result.success(true);
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody Role role) {
        roleMapper.updateById(role);
        return Result.success(true);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        roleMapper.deleteById(id);
        return Result.success(true);
    }

    @GetMapping("/{roleId}/organizations")
    public Result<List<Long>> getRoleOrganizations(@PathVariable Long roleId) {
        return Result.success(roleOrganizationMapper.selectOrgIdsByRoleId(roleId));
    }

    @PostMapping("/{roleId}/organizations")
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> assignOrganizations(@PathVariable Long roleId, @RequestBody List<Long> orgIds) {
        roleOrganizationMapper.deleteByRoleId(roleId);
        if (orgIds != null && !orgIds.isEmpty()) {
            for (Long orgId : orgIds) {
                roleOrganizationMapper.insertRoleOrg(roleId, orgId);
            }
        }
        return Result.success(true);
    }
}
