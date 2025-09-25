package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluate.entity.IndicatorWeight;
import com.evaluate.mapper.IndicatorWeightMapper;
import com.evaluate.service.IIndicatorWeightService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 指标权重服务实现类
 * 
 * @author System
 * @since 2024-01-01
 */
@Slf4j
@Service
public class IndicatorWeightServiceImpl extends ServiceImpl<IndicatorWeightMapper, IndicatorWeight> implements IIndicatorWeightService {

    @Override
    public List<IndicatorWeight> getByConfigId(Long configId) {
        QueryWrapper<IndicatorWeight> wrapper = new QueryWrapper<>();
        wrapper.eq("config_id", configId)
               .orderByAsc("indicator_level", "sort_order");
        return list(wrapper);
    }

    @Override
    public List<IndicatorWeight> getByConfigIdAndLevel(Long configId, Integer indicatorLevel) {
        QueryWrapper<IndicatorWeight> wrapper = new QueryWrapper<>();
        wrapper.eq("config_id", configId)
               .eq("indicator_level", indicatorLevel)
               .orderByAsc("sort_order");
        return list(wrapper);
    }

    @Override
    public List<IndicatorWeight> getByParentId(Long parentId) {
        QueryWrapper<IndicatorWeight> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", parentId)
               .orderByAsc("sort_order");
        return list(wrapper);
    }

    @Override
    public IndicatorWeight getByConfigIdAndCode(Long configId, String indicatorCode) {
        QueryWrapper<IndicatorWeight> wrapper = new QueryWrapper<>();
        wrapper.eq("config_id", configId)
               .eq("indicator_code", indicatorCode);
        return getOne(wrapper);
    }

    @Override
    public List<IndicatorWeight> getTreeByConfigId(Long configId) {
        QueryWrapper<IndicatorWeight> wrapper = new QueryWrapper<>();
        wrapper.eq("config_id", configId)
               .orderByAsc("indicator_level", "sort_order");
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchSave(List<IndicatorWeight> weightList) {
        if (weightList == null || weightList.isEmpty()) {
            return false;
        }
        
        // 验证数据
        for (IndicatorWeight weight : weightList) {
            if (!validateIndicatorWeight(weight)) {
                log.error("指标权重验证失败: {}", weight);
                return false;
            }
        }
        
        return saveBatch(weightList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateWeight(List<IndicatorWeight> weightList) {
        if (weightList == null || weightList.isEmpty()) {
            return false;
        }
        
        return updateBatchById(weightList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByConfigId(Long configId) {
        QueryWrapper<IndicatorWeight> wrapper = new QueryWrapper<>();
        wrapper.eq("config_id", configId);
        return remove(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean copyWeightsByConfigId(Long sourceConfigId, Long targetConfigId) {
        List<IndicatorWeight> sourceWeights = getByConfigId(sourceConfigId);
        if (sourceWeights.isEmpty()) {
            return true; // 源配置没有权重数据，复制成功
        }
        
        List<IndicatorWeight> targetWeights = new ArrayList<>();
        for (IndicatorWeight source : sourceWeights) {
            IndicatorWeight target = new IndicatorWeight();
            target.setConfigId(targetConfigId);
            target.setIndicatorCode(source.getIndicatorCode());
            target.setIndicatorName(source.getIndicatorName());
            target.setIndicatorLevel(source.getIndicatorLevel());
            target.setWeight(source.getWeight());
            target.setParentId(source.getParentId());
            target.setSortOrder(source.getSortOrder());
            target.setCreateTime(LocalDateTime.now());
            
            targetWeights.add(target);
        }
        
        return batchSave(targetWeights);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean initDefaultWeights(Long configId) {
        // 删除现有权重配置
        deleteByConfigId(configId);
        
        List<IndicatorWeight> defaultWeights = createDefaultWeights(configId);
        return batchSave(defaultWeights);
    }

    @Override
    public boolean validateWeightIntegrity(Long configId) {
        List<IndicatorWeight> weights = getByConfigId(configId);
        
        // 检查一级指标权重总和是否为1
        List<IndicatorWeight> primaryWeights = weights.stream()
            .filter(w -> w.getIndicatorLevel() == 1)
            .collect(Collectors.toList());
        
        double primarySum = primaryWeights.stream()
            .mapToDouble(IndicatorWeight::getWeight)
            .sum();
        
        if (Math.abs(primarySum - 1.0) > 0.001) {
            log.warn("一级指标权重总和不为1: {}", primarySum);
            return false;
        }
        
        // 检查每个一级指标下的二级指标权重总和是否为1
        for (IndicatorWeight primary : primaryWeights) {
            List<IndicatorWeight> secondaryWeights = weights.stream()
                .filter(w -> w.getIndicatorLevel() == 2 && Objects.equals(w.getParentId(), primary.getId()))
                .collect(Collectors.toList());
            
            if (!secondaryWeights.isEmpty()) {
                double secondarySum = secondaryWeights.stream()
                    .mapToDouble(IndicatorWeight::getWeight)
                    .sum();
                
                if (Math.abs(secondarySum - 1.0) > 0.001) {
                    log.warn("二级指标权重总和不为1, 一级指标: {}, 权重总和: {}", primary.getIndicatorName(), secondarySum);
                    return false;
                }
            }
        }
        
        return true;
    }

    @Override
    public Map<String, Object> getWeightStatistics(Long configId) {
        List<IndicatorWeight> weights = getByConfigId(configId);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalCount", weights.size());
        statistics.put("primaryCount", weights.stream().filter(w -> w.getIndicatorLevel() == 1).count());
        statistics.put("secondaryCount", weights.stream().filter(w -> w.getIndicatorLevel() == 2).count());
        statistics.put("isValid", validateWeightIntegrity(configId));
        
        return statistics;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateIndicatorWeight(IndicatorWeight indicatorWeight) {
        if (!validateIndicatorWeight(indicatorWeight)) {
            log.error("指标权重验证失败: {}", indicatorWeight);
            return false;
        }
        
        return updateById(indicatorWeight);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addIndicatorWeight(IndicatorWeight indicatorWeight) {
        if (!validateIndicatorWeight(indicatorWeight)) {
            log.error("指标权重验证失败: {}", indicatorWeight);
            return false;
        }
        
        // 检查指标代码是否重复
        IndicatorWeight existing = getByConfigIdAndCode(indicatorWeight.getConfigId(), indicatorWeight.getIndicatorCode());
        if (existing != null) {
            log.error("指标代码已存在: {}", indicatorWeight.getIndicatorCode());
            return false;
        }
        
        indicatorWeight.setCreateTime(LocalDateTime.now());
        
        return save(indicatorWeight);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteIndicatorWeight(Long id) {
        return removeById(id);
    }

    @Override
    public boolean validateIndicatorWeight(IndicatorWeight indicatorWeight) {
        if (indicatorWeight == null) {
            return false;
        }
        
        // 验证必填字段
        if (indicatorWeight.getConfigId() == null ||
            !StringUtils.hasText(indicatorWeight.getIndicatorCode()) ||
            !StringUtils.hasText(indicatorWeight.getIndicatorName()) ||
            indicatorWeight.getIndicatorLevel() == null ||
            indicatorWeight.getWeight() == null) {
            return false;
        }
        
        // 验证指标级别
        if (indicatorWeight.getIndicatorLevel() != 1 && indicatorWeight.getIndicatorLevel() != 2) {
            return false;
        }
        
        // 验证权重值范围
        if (indicatorWeight.getWeight() < 0 || indicatorWeight.getWeight() > 1) {
            return false;
        }
        
        // 验证二级指标必须有父指标
        if (indicatorWeight.getIndicatorLevel() == 2 && indicatorWeight.getParentId() == null) {
            return false;
        }
        
        return true;
    }

    /**
     * 创建默认权重配置
     */
    private List<IndicatorWeight> createDefaultWeights(Long configId) {
        List<IndicatorWeight> weights = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        // 创建一级指标
        IndicatorWeight primary1 = new IndicatorWeight();
        primary1.setConfigId(configId);
        primary1.setIndicatorCode("A");
        primary1.setIndicatorName("防灾减灾能力");
        primary1.setIndicatorLevel(1);
        primary1.setWeight(0.4);
        primary1.setSortOrder(1);
        primary1.setCreateTime(now);
        weights.add(primary1);
        
        IndicatorWeight primary2 = new IndicatorWeight();
        primary2.setConfigId(configId);
        primary2.setIndicatorCode("B");
        primary2.setIndicatorName("应急响应能力");
        primary2.setIndicatorLevel(1);
        primary2.setWeight(0.35);
        primary2.setSortOrder(2);
        primary2.setCreateTime(now);
        weights.add(primary2);
        
        IndicatorWeight primary3 = new IndicatorWeight();
        primary3.setConfigId(configId);
        primary3.setIndicatorCode("C");
        primary3.setIndicatorName("恢复重建能力");
        primary3.setIndicatorLevel(1);
        primary3.setWeight(0.25);
        primary3.setSortOrder(3);
        primary3.setCreateTime(now);
        weights.add(primary3);
        
        return weights;
    }
}