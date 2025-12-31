package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluate.entity.IndicatorWeight;
import com.evaluate.entity.IndicatorWeightScore;
import com.evaluate.mapper.IndicatorWeightScoreMapper;
import com.evaluate.service.IIndicatorWeightScoreService;
import com.evaluate.service.IIndicatorWeightService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 专家权重打分记录服务实现类
 *
 * @author System
 * @since 2024-11-06
 */
@Slf4j
@Service
public class IndicatorWeightScoreServiceImpl extends ServiceImpl<IndicatorWeightScoreMapper, IndicatorWeightScore>
        implements IIndicatorWeightScoreService {

    @Autowired
    private IIndicatorWeightService indicatorWeightService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveScores(List<IndicatorWeightScore> scores) {
        if (scores == null || scores.isEmpty()) {
            return false;
        }
        return saveBatch(scores);
    }

    @Override
    public List<IndicatorWeightScore> getScoresByConfigId(Long configId) {
        return baseMapper.selectByConfigId(configId);
    }

    @Override
    public List<IndicatorWeightScore> getScoresByConfigIdAndIndicatorCode(Long configId, String indicatorCode) {
        return baseMapper.selectByConfigIdAndIndicatorCode(configId, indicatorCode);
    }

    @Override
    public Map<String, Double> calculateAverageWeights(Long configId) {
        List<Map<String, Object>> results = baseMapper.selectAverageWeightsByConfigId(configId);
        Map<String, Double> averageWeights = new HashMap<>();

        for (Map<String, Object> result : results) {
            String indicatorCode = (String) result.get("indicatorCode");
            Double avgWeight = (Double) result.get("avgWeight");
            averageWeights.put(indicatorCode, avgWeight);
        }

        return averageWeights;
    }

    @Override
    public List<Map<String, Object>> getExpertsByConfigId(Long configId) {
        return baseMapper.selectExpertsByConfigId(configId);
    }

    @Override
    public Map<String, Object> getScoreStatistics(Long configId) {
        Map<String, Object> statistics = new HashMap<>();

        // 获取所有专家打分记录
        List<IndicatorWeightScore> scores = getScoresByConfigId(configId);

        // 获取该配置下的所有指标信息
        LambdaQueryWrapper<IndicatorWeight> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(IndicatorWeight::getConfigId, configId);
        List<IndicatorWeight> indicators = indicatorWeightService.list(queryWrapper);

        // 构建指标代码到指标信息的映射
        Map<String, IndicatorWeight> indicatorMap = indicators.stream()
                .collect(Collectors.toMap(IndicatorWeight::getIndicatorCode, ind -> ind));

        // 按指标代码分组
        Map<String, List<IndicatorWeightScore>> groupedByIndicator = scores.stream()
                .collect(Collectors.groupingBy(IndicatorWeightScore::getIndicatorCode));

        // 计算每个指标的统计信息
        List<Map<String, Object>> indicatorStats = new ArrayList<>();
        for (Map.Entry<String, List<IndicatorWeightScore>> entry : groupedByIndicator.entrySet()) {
            String indicatorCode = entry.getKey();
            List<IndicatorWeightScore> indicatorScores = entry.getValue();

            Map<String, Object> stat = new HashMap<>();
            stat.put("indicatorCode", indicatorCode);
            stat.put("scoreCount", indicatorScores.size());

            // 从indicator_weight表获取指标名称、级别、父ID等信息
            IndicatorWeight indicator = indicatorMap.get(indicatorCode);
            if (indicator != null) {
                stat.put("indicatorName", indicator.getIndicatorName());
                stat.put("indicatorLevel", indicator.getIndicatorLevel());
                stat.put("parentId", indicator.getParentId());
                stat.put("id", indicator.getId());
            }

            // 计算平均值
            double avgWeight = indicatorScores.stream()
                    .mapToDouble(IndicatorWeightScore::getWeight)
                    .average()
                    .orElse(0.0);
            stat.put("avgWeight", avgWeight);

            // 获取所有专家的打分
            List<Map<String, Object>> expertScores = indicatorScores.stream()
                    .map(score -> {
                        Map<String, Object> expertScore = new HashMap<>();
                        expertScore.put("expertName", score.getExpertName());
                        expertScore.put("expertPhone", score.getExpertPhone());
                        expertScore.put("weight", score.getWeight());
                        expertScore.put("createTime", score.getCreateTime());
                        return expertScore;
                    })
                    .collect(Collectors.toList());
            stat.put("expertScores", expertScores);

            indicatorStats.add(stat);
        }

        statistics.put("indicatorStats", indicatorStats);

        // 获取所有参与打分的专家列表
        List<Map<String, Object>> experts = getExpertsByConfigId(configId);
        statistics.put("experts", experts);

        // 总打分记录数
        statistics.put("totalScores", scores.size());

        return statistics;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applyAverageWeights(Long configId) {
        try {
            // 1. 计算平均权重
            Map<String, Double> averageWeights = calculateAverageWeights(configId);

            if (averageWeights.isEmpty()) {
                log.warn("配置ID {} 没有专家打分记录", configId);
                return false;
            }

            // 2. 获取该配置下的所有指标权重
            LambdaQueryWrapper<IndicatorWeight> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(IndicatorWeight::getConfigId, configId);
            List<IndicatorWeight> indicatorWeights = indicatorWeightService.list(queryWrapper);

            // 3. 更新权重值
            for (IndicatorWeight indicatorWeight : indicatorWeights) {
                String indicatorCode = indicatorWeight.getIndicatorCode();
                if (averageWeights.containsKey(indicatorCode)) {
                    indicatorWeight.setWeight(averageWeights.get(indicatorCode));
                }
            }

            // 4. 批量更新
            return indicatorWeightService.updateBatchById(indicatorWeights);
        } catch (Exception e) {
            log.error("应用平均权重失败", e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteExpertScores(Long configId, String expertName) {
        try {
            // 构建删除条件
            LambdaQueryWrapper<IndicatorWeightScore> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(IndicatorWeightScore::getConfigId, configId)
                    .eq(IndicatorWeightScore::getExpertName, expertName);

            // 执行删除
            boolean success = remove(queryWrapper);

            if (success) {
                log.info("成功删除配置 {} 中专家 {} 的打分记录", configId, expertName);
            } else {
                log.warn("未找到配置 {} 中专家 {} 的打分记录", configId, expertName);
            }

            return success;
        } catch (Exception e) {
            log.error("删除专家打分记录失败: configId={}, expertName={}", configId, expertName, e);
            throw e;
        }
    }
}
