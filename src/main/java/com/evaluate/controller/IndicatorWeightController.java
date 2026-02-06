package com.evaluate.controller;

import com.evaluate.common.Result;
import com.evaluate.entity.IndicatorWeight;
import com.evaluate.service.IIndicatorWeightScoreService;
import com.evaluate.service.IIndicatorWeightService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
}
