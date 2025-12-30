package com.evaluate.controller;

import com.evaluate.common.Result;
import com.evaluate.entity.IndicatorWeightScore;
import com.evaluate.service.IIndicatorWeightScoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 专家权重打分记录控制器
 *
 * @author System
 * @since 2024-11-06
 */
@Slf4j
@RestController
@RequestMapping("/api/indicator-weight-score")
@CrossOrigin
public class IndicatorWeightScoreController {

    @Autowired(required = false)
    private IIndicatorWeightScoreService indicatorWeightScoreService;

    /**
     * 保存专家打分记录（批量）
     *
     * @param scores 打分记录列表
     * @return 操作结果
     */
    @PostMapping("/batch")
    public Result<Boolean> saveScores(@RequestBody List<IndicatorWeightScore> scores) {
        log.info("保存专家打分记录，数量: {}", scores.size());
        try {
            if (indicatorWeightScoreService == null) {
                log.warn("专家权重打分服务未注入，无法保存打分记录");
                return Result.error("服务暂时不可用，请稍后重试");
            }
            boolean success = indicatorWeightScoreService.saveScores(scores);
            return success ? Result.success(true) : Result.error("保存失败");
        } catch (Exception e) {
            log.error("保存专家打分记录失败", e);
            return Result.error("保存失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定配置的所有专家打分记录
     *
     * @param configId 配置ID
     * @return 打分记录列表
     */
    @GetMapping("/config/{configId}")
    public Result<List<IndicatorWeightScore>> getScoresByConfigId(@PathVariable Long configId) {
        log.info("获取配置 {} 的专家打分记录", configId);
        try {
            if (indicatorWeightScoreService == null) {
                log.warn("专家权重打分服务未注入，无法获取打分记录");
                return Result.error("服务暂时不可用，请稍后重试");
            }
            List<IndicatorWeightScore> scores = indicatorWeightScoreService.getScoresByConfigId(configId);
            return Result.success(scores);
        } catch (Exception e) {
            log.error("获取专家打分记录失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定配置和指标的所有专家打分记录
     *
     * @param configId      配置ID
     * @param indicatorCode 指标代码
     * @return 打分记录列表
     */
    @GetMapping("/config/{configId}/indicator/{indicatorCode}")
    public Result<List<IndicatorWeightScore>> getScoresByConfigIdAndIndicatorCode(
            @PathVariable Long configId,
            @PathVariable String indicatorCode) {
        log.info("获取配置 {} 指标 {} 的专家打分记录", configId, indicatorCode);
        try {
            List<IndicatorWeightScore> scores = indicatorWeightScoreService
                    .getScoresByConfigIdAndIndicatorCode(configId, indicatorCode);
            return Result.success(scores);
        } catch (Exception e) {
            log.error("获取专家打分记录失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 计算指定配置下每个指标的平均权重值
     *
     * @param configId 配置ID
     * @return 指标代码和平均权重的映射
     */
    @GetMapping("/config/{configId}/average")
    public Result<Map<String, Double>> calculateAverageWeights(@PathVariable Long configId) {
        log.info("计算配置 {} 的平均权重", configId);
        try {
            if (indicatorWeightScoreService == null) {
                log.warn("专家权重打分服务未注入，无法计算平均权重");
                return Result.error("服务暂时不可用，请稍后重试");
            }
            Map<String, Double> averageWeights = indicatorWeightScoreService.calculateAverageWeights(configId);
            return Result.success(averageWeights);
        } catch (Exception e) {
            log.error("计算平均权重失败", e);
            return Result.error("计算失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定配置下的所有专家列表（去重）
     *
     * @param configId 配置ID
     * @return 专家列表
     */
    @GetMapping("/config/{configId}/experts")
    public Result<List<Map<String, Object>>> getExpertsByConfigId(@PathVariable Long configId) {
        log.info("获取配置 {} 的专家列表", configId);
        try {
            if (indicatorWeightScoreService == null) {
                log.warn("专家权重打分服务未注入，无法获取专家列表");
                return Result.error("服务暂时不可用，请稍后重试");
            }
            List<Map<String, Object>> experts = indicatorWeightScoreService.getExpertsByConfigId(configId);
            return Result.success(experts);
        } catch (Exception e) {
            log.error("获取专家列表失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定配置的打分统计信息
     *
     * @param configId 配置ID
     * @return 统计信息
     */
    @GetMapping("/config/{configId}/statistics")
    public Result<Map<String, Object>> getScoreStatistics(@PathVariable Long configId) {
        log.info("获取配置 {} 的打分统计信息", configId);
        try {
            if (indicatorWeightScoreService == null) {
                log.warn("专家权重打分服务未注入，无法获取统计信息");
                return Result.error("服务暂时不可用，请稍后重试");
            }
            Map<String, Object> statistics = indicatorWeightScoreService.getScoreStatistics(configId);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取统计信息失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 将平均权重应用到正式的 indicator_weight 表
     *
     * @param configId 配置ID
     * @return 操作结果
     */
    @PostMapping("/config/{configId}/apply-average")
    public Result<Boolean> applyAverageWeights(@PathVariable Long configId) {
        log.info("应用配置 {} 的平均权重", configId);
        try {
            if (indicatorWeightScoreService == null) {
                log.warn("专家权重打分服务未注入，无法应用平均权重");
                return Result.error("服务暂时不可用，请稍后重试");
            }
            boolean success = indicatorWeightScoreService.applyAverageWeights(configId);
            return success ? Result.success(true) : Result.error("应用失败");
        } catch (Exception e) {
            log.error("应用平均权重失败", e);
            return Result.error("应用失败: " + e.getMessage());
        }
    }
}
