package com.evaluate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.evaluate.entity.IndicatorWeightScore;

import java.util.List;
import java.util.Map;

/**
 * 专家权重打分记录服务接口
 *
 * @author System
 * @since 2024-11-06
 */
public interface IIndicatorWeightScoreService extends IService<IndicatorWeightScore> {

    /**
     * 保存专家打分记录（批量）
     *
     * @param scores 打分记录列表
     * @return 是否保存成功
     */
    boolean saveScores(List<IndicatorWeightScore> scores);

    /**
     * 获取指定配置的所有专家打分记录
     *
     * @param configId 配置ID
     * @return 打分记录列表
     */
    List<IndicatorWeightScore> getScoresByConfigId(Long configId);

    /**
     * 获取指定配置和指标的所有专家打分记录
     *
     * @param configId      配置ID
     * @param indicatorCode 指标代码
     * @return 打分记录列表
     */
    List<IndicatorWeightScore> getScoresByConfigIdAndIndicatorCode(Long configId, String indicatorCode);

    /**
     * 计算指定配置下每个指标的平均权重值
     *
     * @param configId 配置ID
     * @return 指标代码和平均权重的映射
     */
    Map<String, Double> calculateAverageWeights(Long configId);

    /**
     * 获取指定配置下的所有专家列表（去重）
     *
     * @param configId 配置ID
     * @return 专家列表
     */
    List<Map<String, Object>> getExpertsByConfigId(Long configId);

    /**
     * 获取指定配置的打分统计信息
     * 包括：每个指标的平均分、打分人数、所有专家的打分记录
     *
     * @param configId 配置ID
     * @return 统计信息
     */
    Map<String, Object> getScoreStatistics(Long configId);

    /**
     * 将平均权重应用到正式的 indicator_weight 表
     *
     * @param configId 配置ID
     * @return 是否应用成功
     */
    boolean applyAverageWeights(Long configId);

    /**
     * 删除指定专家的打分记录
     *
     * @param configId   配置ID
     * @param expertName 专家姓名
     * @return 是否删除成功
     */
    boolean deleteExpertScores(Long configId, String expertName);
}
