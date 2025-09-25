package com.evaluate.controller;

import com.evaluate.common.Result;
import com.evaluate.entity.AlgorithmConfig;
import com.evaluate.service.IAlgorithmConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 算法配置控制器
 * 
 * @author System
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/algorithm-config")
public class AlgorithmConfigController {

    @Autowired
    private IAlgorithmConfigService algorithmConfigService;

    @GetMapping
    public Result<List<AlgorithmConfig>> getAllAlgorithmConfigs() {
        try {
            List<AlgorithmConfig> list = algorithmConfigService.list();
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取算法配置列表失败", e);
            return Result.error("获取算法配置列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<AlgorithmConfig> getAlgorithmConfigById(@PathVariable Long id) {
        try {
            AlgorithmConfig config = algorithmConfigService.getById(id);
            return config != null ? Result.success(config) : Result.error("算法配置不存在");
        } catch (Exception e) {
            log.error("获取算法配置失败", e);
            return Result.error("获取算法配置失败: " + e.getMessage());
        }
    }

    @GetMapping("/default")
    public Result<AlgorithmConfig> getDefaultAlgorithmConfig() {
        try {
            AlgorithmConfig config = algorithmConfigService.getDefaultConfig();
            return config != null ? Result.success(config) : Result.error("默认算法配置不存在");
        } catch (Exception e) {
            log.error("获取默认算法配置失败", e);
            return Result.error("获取默认算法配置失败: " + e.getMessage());
        }
    }

    @PostMapping
    public Result<Boolean> createAlgorithmConfig(@RequestBody AlgorithmConfig algorithmConfig) {
        try {
            boolean result = algorithmConfigService.save(algorithmConfig);
            return result ? Result.success(true) : Result.error("创建算法配置失败");
        } catch (Exception e) {
            log.error("创建算法配置失败", e);
            return Result.error("创建算法配置失败: " + e.getMessage());
        }
    }

    @PutMapping
    public Result<Boolean> updateAlgorithmConfig(@RequestBody AlgorithmConfig algorithmConfig) {
        try {
            boolean result = algorithmConfigService.updateById(algorithmConfig);
            return result ? Result.success(true) : Result.error("更新算法配置失败");
        } catch (Exception e) {
            log.error("更新算法配置失败", e);
            return Result.error("更新算法配置失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteAlgorithmConfig(@PathVariable Long id) {
        try {
            boolean result = algorithmConfigService.removeById(id);
            return result ? Result.success(true) : Result.error("删除算法配置失败");
        } catch (Exception e) {
            log.error("删除算法配置失败", e);
            return Result.error("删除算法配置失败: " + e.getMessage());
        }
    }
}