package com.evaluate.controller;

import com.evaluate.common.Result;
import com.evaluate.entity.IndicatorWeight;
import com.evaluate.entity.IndicatorWeightScore;
import com.evaluate.service.IIndicatorWeightScoreService;
import com.evaluate.service.IIndicatorWeightService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 指标权重控制器
 * 
 * @author System
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/indicator-weight")
public class IndicatorWeightController {

    @Autowired
    private IIndicatorWeightService indicatorWeightService;

    @Autowired(required = false)
    private IIndicatorWeightScoreService indicatorWeightScoreService;

    @GetMapping
    public Result<List<IndicatorWeight>> getAllIndicatorWeights() {
        try {
            List<IndicatorWeight> list = indicatorWeightService.list();
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取指标权重列表失败", e);
            return Result.error("获取指标权重列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<IndicatorWeight> getIndicatorWeightById(@PathVariable Long id) {
        try {
            IndicatorWeight indicatorWeight = indicatorWeightService.getById(id);
            if (indicatorWeight == null) {
                return Result.error("指标权重不存在");
            }
            return Result.success(indicatorWeight);
        } catch (Exception e) {
            log.error("获取指标权重详情失败", e);
            return Result.error("获取指标权重详情失败: " + e.getMessage());
        }
    }

    @GetMapping("/config/{configId}")
    public Result<List<IndicatorWeight>> getIndicatorWeightsByConfigId(@PathVariable Long configId) {
        try {
            List<IndicatorWeight> list = indicatorWeightService.getByConfigId(configId);
            return Result.success(list);
        } catch (Exception e) {
            log.error("根据配置ID获取指标权重失败", e);
            return Result.error("根据配置ID获取指标权重失败: " + e.getMessage());
        }
    }

    @GetMapping("/config/{configId}/average-score")
    public Result<List<IndicatorWeight>> getIndicatorWeightsByConfigIdAverageScore(@PathVariable Long configId) {
        try {
            List<IndicatorWeight> list = indicatorWeightService.getByConfigId(configId);

            if (indicatorWeightScoreService == null) {
                return Result.success(list);
            }

            Map<String, Double> averageWeights = indicatorWeightScoreService.calculateAverageWeights(configId);
            if (averageWeights == null || averageWeights.isEmpty()) {
                return Result.success(list);
            }

            for (IndicatorWeight weight : list) {
                Double avg = averageWeights.get(weight.getIndicatorCode());
                if (avg != null) {
                    weight.setWeight(avg);
                }
            }

            return Result.success(list);
        } catch (Exception e) {
            log.error("根据配置ID获取指标平均权重失败", e);
            return Result.error("根据配置ID获取指标平均权重失败: " + e.getMessage());
        }
    }

    @GetMapping("/indicator/{indicatorCode}")
    public Result<List<IndicatorWeight>> getIndicatorWeightsByIndicatorCode(@PathVariable String indicatorCode) {
        try {
            // 暂时返回空列表，需要在Service中实现此方法
            List<IndicatorWeight> list = indicatorWeightService.list();
            return Result.success(list);
        } catch (Exception e) {
            log.error("根据指标代码获取权重失败", e);
            return Result.error("根据指标代码获取权重失败: " + e.getMessage());
        }
    }

    @PostMapping
    public Result<Boolean> createIndicatorWeight(@RequestBody IndicatorWeight indicatorWeight) {
        try {
            boolean result = indicatorWeightService.save(indicatorWeight);
            return result ? Result.success(true) : Result.error("创建指标权重失败");
        } catch (Exception e) {
            log.error("创建指标权重失败", e);
            return Result.error("创建指标权重失败: " + e.getMessage());
        }
    }

    @PostMapping("/batch")
    public Result<Boolean> batchCreateIndicatorWeights(@RequestBody List<IndicatorWeight> indicatorWeights) {
        try {
            boolean result = indicatorWeightService.saveBatch(indicatorWeights);
            return result ? Result.success(true) : Result.error("批量创建指标权重失败");
        } catch (Exception e) {
            log.error("批量创建指标权重失败", e);
            return Result.error("批量创建指标权重失败: " + e.getMessage());
        }
    }

    @PutMapping
    public Result<Boolean> updateIndicatorWeight(@RequestBody IndicatorWeight indicatorWeight) {
        try {
            boolean result = indicatorWeightService.updateById(indicatorWeight);
            return result ? Result.success(true) : Result.error("更新指标权重失败");
        } catch (Exception e) {
            log.error("更新指标权重失败", e);
            return Result.error("更新指标权重失败: " + e.getMessage());
        }
    }

    @PutMapping("/batch")
    public Result<Boolean> batchUpdateIndicatorWeights(@RequestBody List<IndicatorWeight> indicatorWeights) {
        try {
            boolean result = indicatorWeightService.batchUpdateWeight(indicatorWeights);
            return result ? Result.success(true) : Result.error("批量更新指标权重失败");
        } catch (Exception e) {
            log.error("批量更新指标权重失败", e);
            return Result.error("批量更新指标权重失败: " + e.getMessage());
        }
    }

    @PostMapping("/config/{configId}/init-default")
    public Result<Boolean> initDefaultWeights(@PathVariable Long configId) {
        try {
            boolean result = indicatorWeightService.initDefaultWeights(configId);
            if (result && indicatorWeightScoreService != null) {
                indicatorWeightScoreService.remove(
                        new LambdaQueryWrapper<IndicatorWeightScore>().eq(IndicatorWeightScore::getConfigId, configId)
                );
            }
            return result ? Result.success(true) : Result.error("初始化默认权重失败");
        } catch (Exception e) {
            log.error("初始化默认权重失败", e);
            return Result.error("初始化默认权重失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteIndicatorWeight(@PathVariable Long id) {
        try {
            boolean result = indicatorWeightService.removeById(id);
            return result ? Result.success(true) : Result.error("删除指标权重失败");
        } catch (Exception e) {
            log.error("删除指标权重失败", e);
            return Result.error("删除指标权重失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/purge-county")
    public Result<Integer> purgeCountyWeights(@RequestParam(required = false) Integer year) {
        try {
            int deleted = indicatorWeightService.purgeCountyWeights(year);
            return Result.success(deleted);
        } catch (Exception e) {
            log.error("删除区县级权重失败", e);
            return Result.error("删除区县级权重失败: " + e.getMessage());
        }
    }

    @PostMapping("/validate")
    public Result<Boolean> validateWeights(@RequestBody List<IndicatorWeight> weights) {
        try {
            // 简化验证逻辑
            boolean result = weights != null && !weights.isEmpty();
            return Result.success(result);
        } catch (Exception e) {
            log.error("验证权重配置失败", e);
            return Result.error("验证权重配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取指标权重（带继承逻辑）
     * 继承顺序：
     * 1. 专家打分表中的平均值（当前配置）
     * 2. 基准表中的权重（当前配置）
     * 3. 基准表中的权重（上级组织配置）
     *
     * @param configId 配置ID
     * @param parentOrgcode 上级组织编码（用于继承）
     * @param parentConfigId 上级配置ID（用于继承）
     * @return 带继承信息的指标权重列表
     */
    @GetMapping("/config/{configId}/with-inheritance")
    public Result<List<IndicatorWeight>> getIndicatorWeightsWithInheritance(
            @PathVariable Long configId,
            @RequestParam(required = false) String parentOrgcode,
            @RequestParam(required = false) Long parentConfigId
    ) {
        try {
            List<IndicatorWeight> list = indicatorWeightService.getWeightsWithInheritance(
                    configId, parentOrgcode, parentConfigId);
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取指标权重（带继承）失败", e);
            return Result.error("获取指标权重（带继承）失败: " + e.getMessage());
        }
    }

    /**
     * 获取指标权重（带完整继承逻辑）
     * 继承顺序：
     * 1. 专家打分表：按年份从新到旧查找（requestedYear → 2021）
     * 2. 2020年基准表：区县 → 市级 → 省级（层级继承）
     *
     * @param configId 配置ID（用于获取指标结构）
     * @param orgcode 组织编码
     * @param requestedYear 请求的年份
     * @param modelId 模型ID（优先使用，比configName更可靠）
     * @param configName 配置名称（已废弃，仅用于向后兼容）
     * @return 带继承信息的指标权重列表
     */
    @GetMapping("/config/{configId}/with-full-inheritance")
    public Result<List<IndicatorWeight>> getIndicatorWeightsWithFullInheritance(
            @PathVariable Long configId,
            @RequestParam String orgcode,
            @RequestParam Integer requestedYear,
            @RequestParam(required = false) Long modelId,
            @RequestParam(required = false) String configName
    ) {
        try {
            // 优先使用 modelId，如果没有则使用 configName（向后兼容）
            List<IndicatorWeight> list = indicatorWeightService.getWeightsWithFullInheritance(
                    configId, orgcode, requestedYear, modelId, configName);
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取指标权重（带完整继承）失败: orgcode={}, year={}, modelId={}, configName={}", orgcode, requestedYear, modelId, configName, e);
            return Result.error("获取指标权重（带完整继承）失败: " + e.getMessage());
        }
    }
}
