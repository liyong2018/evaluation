package com.evaluate.controller.system;

import com.evaluate.common.Result;
import com.evaluate.entity.Menu;
import com.evaluate.mapper.MenuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统菜单管理
 */
@RestController
@RequestMapping("/api/sys/menu")
public class SysMenuController {

    @Autowired
    private MenuMapper menuMapper;

    @GetMapping("/list")
    public Result<List<Menu>> list() {
        return Result.success(menuMapper.selectList(null));
    }

    @PostMapping
    public Result<Boolean> add(@RequestBody Menu menu) {
        menuMapper.insert(menu);
        return Result.success(true);
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody Menu menu) {
        menuMapper.updateById(menu);
        return Result.success(true);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        menuMapper.deleteById(id);
        return Result.success(true);
    }
}
