package com.evaluate.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.evaluate.common.Result;
import com.evaluate.entity.WeightConfig;
import com.evaluate.service.IWeightConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
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

    private static final int BASELINE_YEAR = 2020;

    @Autowired
    private IWeightConfigService weightConfigService;

    @GetMapping
    public Result<List<WeightConfig>> getAllWeightConfigs(
            @RequestParam(required = false) String orgcode,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Boolean ensureDefaults
    ) {
        try {
            if (StringUtils.hasText(orgcode) && year != null) {
                if (Boolean.TRUE.equals(ensureDefaults)) {
                    List<WeightConfig> list = weightConfigService.getOrCreateModelYearConfigs(orgcode.trim(), year);
                    return Result.success(list);
                }
                List<WeightConfig> list = weightConfigService.getEffectiveModelYearConfigs(orgcode.trim(), year);
                return Result.success(list);
            }

            QueryWrapper<WeightConfig> queryWrapper = new QueryWrapper<>();
            if (StringUtils.hasText(orgcode)) {
                queryWrapper.eq("orgcode", orgcode.trim());
            }
            if (year != null) {
                queryWrapper.and(w -> w.eq("year", year).or().isNull("year").apply("YEAR(create_time) = {0}", year));
            }
            queryWrapper.orderByDesc("create_time");

            List<WeightConfig> list = weightConfigService.list(queryWrapper);
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取权重配置列表失败", e);
            return Result.error("获取权重配置列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/by-org/{orgcode}")
    public Result<List<WeightConfig>> getWeightConfigsByOrgcode(@PathVariable String orgcode) {
        try {
            List<WeightConfig> list = weightConfigService.getByOrgcode(orgcode);
            return Result.success(list);
        } catch (Exception e) {
            log.error("根据组织机构获取权重配置失败", e);
            return Result.error("根据组织机构获取权重配置失败: " + e.getMessage());
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
    public Result<WeightConfig> getWeightConfigByName(
            @PathVariable String configName,
            @RequestParam(required = false) Integer year
    ) {
        try {
            QueryWrapper<WeightConfig> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("config_name", configName);
            queryWrapper.eq("is_deleted", 0);
            if (year != null) {
                queryWrapper.and(w -> w.eq("year", year).or().isNull("year").apply("YEAR(create_time) = {0}", year));
            }
            queryWrapper.orderByDesc("create_time");
            queryWrapper.last("LIMIT 1");

            WeightConfig weightConfig = weightConfigService.getOne(queryWrapper, false);
            if (weightConfig == null && year != null && year >= 2023) {
                QueryWrapper<WeightConfig> baselineQuery = new QueryWrapper<>();
                baselineQuery.eq("config_name", configName);
                baselineQuery.eq("is_deleted", 0);
                baselineQuery.and(w -> w.eq("year", BASELINE_YEAR).or().isNull("year"));
                baselineQuery.orderByDesc("create_time");
                baselineQuery.last("LIMIT 1");
                weightConfig = weightConfigService.getOne(baselineQuery, false);
            }
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
    public Result<WeightConfig> createWeightConfig(
            @RequestBody WeightConfig weightConfig,
            @RequestParam(required = false) Integer year
    ) {
        try {
            if (weightConfig != null && weightConfig.getYear() == null && year != null) {
                weightConfig.setYear(year);
            }
            if (weightConfig != null && weightConfig.getCreateTime() == null && year != null) {
                weightConfig.setCreateTime(LocalDateTime.of(year, 1, 1, 0, 0));
            }
            boolean result = weightConfigService.save(weightConfig);
            return result ? Result.success(weightConfig) : Result.error("创建权重配置失败");
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
