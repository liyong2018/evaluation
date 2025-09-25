package com.evaluate.controller;

import com.evaluate.common.Result;
import com.evaluate.entity.WeightConfig;
import com.evaluate.service.IWeightConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权重配置控制器
 * 
 * @author System
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/weight-config")
public class WeightConfigController {

    @Autowired
    private IWeightConfigService weightConfigService;

    @GetMapping
    public Result<List<WeightConfig>> getAllWeightConfigs() {
        try {
            List<WeightConfig> list = weightConfigService.list();
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取权重配置列表失败", e);
            return Result.error("获取权重配置列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<WeightConfig> getWeightConfigById(@PathVariable Long id) {
        try {
            WeightConfig weightConfig = weightConfigService.getById(id);
            if (weightConfig == null) {
                return Result.error("权重配置不存在");
            }
            return Result.success(weightConfig);
        } catch (Exception e) {
            log.error("获取权重配置详情失败", e);
            return Result.error("获取权重配置详情失败: " + e.getMessage());
        }
    }

    @GetMapping("/name/{configName}")
    public Result<WeightConfig> getWeightConfigByName(@PathVariable String configName) {
        try {
            WeightConfig weightConfig = weightConfigService.getByConfigName(configName);
            if (weightConfig == null) {
                return Result.error("权重配置不存在");
            }
            return Result.success(weightConfig);
        } catch (Exception e) {
            log.error("根据名称获取权重配置失败", e);
            return Result.error("根据名称获取权重配置失败: " + e.getMessage());
        }
    }

    @GetMapping("/active")
    public Result<List<WeightConfig>> getActiveWeightConfigs() {
        try {
            List<WeightConfig> list = weightConfigService.getEnabledConfigs();
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取激活的权重配置失败", e);
            return Result.error("获取激活的权重配置失败: " + e.getMessage());
        }
    }

    @PostMapping
    public Result<Boolean> createWeightConfig(@RequestBody WeightConfig weightConfig) {
        try {
            boolean result = weightConfigService.save(weightConfig);
            return result ? Result.success(true) : Result.error("创建权重配置失败");
        } catch (Exception e) {
            log.error("创建权重配置失败", e);
            return Result.error("创建权重配置失败: " + e.getMessage());
        }
    }

    @PutMapping
    public Result<Boolean> updateWeightConfig(@RequestBody WeightConfig weightConfig) {
        try {
            boolean result = weightConfigService.updateById(weightConfig);
            return result ? Result.success(true) : Result.error("更新权重配置失败");
        } catch (Exception e) {
            log.error("更新权重配置失败", e);
            return Result.error("更新权重配置失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteWeightConfig(@PathVariable Long id) {
        try {
            boolean result = weightConfigService.removeById(id);
            return result ? Result.success(true) : Result.error("删除权重配置失败");
        } catch (Exception e) {
            log.error("删除权重配置失败", e);
            return Result.error("删除权重配置失败: " + e.getMessage());
        }
    }

    @PostMapping("/activate/{id}")
    public Result<Boolean> activateWeightConfig(@PathVariable Long id) {
        try {
            boolean result = weightConfigService.updateStatus(id, 1);
            return result ? Result.success(true) : Result.error("激活权重配置失败");
        } catch (Exception e) {
            log.error("激活权重配置失败", e);
            return Result.error("激活权重配置失败: " + e.getMessage());
        }
    }

    @PostMapping("/deactivate/{id}")
    public Result<Boolean> deactivateWeightConfig(@PathVariable Long id) {
        try {
            boolean result = weightConfigService.updateStatus(id, 0);
            return result ? Result.success(true) : Result.error("停用权重配置失败");
        } catch (Exception e) {
            log.error("停用权重配置失败", e);
            return Result.error("停用权重配置失败: " + e.getMessage());
        }
    }

    @PostMapping("/copy/{id}")
    public Result<Boolean> copyWeightConfig(@PathVariable Long id, @RequestParam String newConfigName) {
        try {
            boolean result = weightConfigService.copyWeightConfig(id, newConfigName, "system");
            return result ? Result.success(true) : Result.error("复制权重配置失败");
        } catch (Exception e) {
            log.error("复制权重配置失败", e);
            return Result.error("复制权重配置失败: " + e.getMessage());
        }
    }

    @PostMapping("/validate")
    public Result<Boolean> validateWeightConfig(@RequestBody WeightConfig weightConfig) {
        try {
            boolean result = weightConfigService.validateWeightConfig(weightConfig);
            return Result.success(result);
        } catch (Exception e) {
            log.error("验证权重配置失败", e);
            return Result.error("验证权重配置失败: " + e.getMessage());
        }
    }
}