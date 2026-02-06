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
        if (configId == null) {
            return false;
        }

        deleteByConfigId(configId);

        LocalDateTime now = LocalDateTime.now();

        List<IndicatorWeight> primaryWeights = new ArrayList<>();

        IndicatorWeight p1 = new IndicatorWeight();
        p1.setConfigId(configId);
        p1.setIndicatorCode("L1_DISASTER_MANAGEMENT");
        p1.setIndicatorName("灾害管理能力");
        p1.setIndicatorLevel(1);
        p1.setWeight(0.33);
        p1.setSortOrder(1);
        p1.setCreateTime(now);
        primaryWeights.add(p1);

        IndicatorWeight p2 = new IndicatorWeight();
        p2.setConfigId(configId);
        p2.setIndicatorCode("L1_DISASTER_PREPAREDNESS");
        p2.setIndicatorName("灾害备灾能力");
        p2.setIndicatorLevel(1);
        p2.setWeight(0.32);
        p2.setSortOrder(2);
        p2.setCreateTime(now);
        primaryWeights.add(p2);

        IndicatorWeight p3 = new IndicatorWeight();
        p3.setConfigId(configId);
        p3.setIndicatorCode("L1_SELF_RESCUE_TRANSFER");
        p3.setIndicatorName("自救转移能力");
        p3.setIndicatorLevel(1);
        p3.setWeight(0.35);
        p3.setSortOrder(3);
        p3.setCreateTime(now);
        primaryWeights.add(p3);

        boolean primarySaved = saveBatch(primaryWeights);
        if (!primarySaved) {
            return false;
        }

        Map<String, Long> parentIdMap = new HashMap<>();
        parentIdMap.put(p1.getIndicatorCode(), p1.getId());
        parentIdMap.put(p2.getIndicatorCode(), p2.getId());
        parentIdMap.put(p3.getIndicatorCode(), p3.getId());

        List<IndicatorWeight> secondaryWeights = new ArrayList<>();

        secondaryWeights.add(buildSecondary(configId, "L2_MANAGEMENT_CAPABILITY", "队伍管理能力", parentIdMap.get("L1_DISASTER_MANAGEMENT"), 0.37, 1, now));
        secondaryWeights.add(buildSecondary(configId, "L2_RISK_ASSESSMENT", "风险评估能力", parentIdMap.get("L1_DISASTER_MANAGEMENT"), 0.31, 2, now));
        secondaryWeights.add(buildSecondary(configId, "L2_FUNDING", "财政投入能力", parentIdMap.get("L1_DISASTER_MANAGEMENT"), 0.32, 3, now));

        secondaryWeights.add(buildSecondary(configId, "L2_MATERIAL", "物资储备能力", parentIdMap.get("L1_DISASTER_PREPAREDNESS"), 0.51, 4, now));
        secondaryWeights.add(buildSecondary(configId, "L2_MEDICAL", "医疗保障能力", parentIdMap.get("L1_DISASTER_PREPAREDNESS"), 0.49, 5, now));

        secondaryWeights.add(buildSecondary(configId, "L2_SELF_RESCUE", "自救互救能力", parentIdMap.get("L1_SELF_RESCUE_TRANSFER"), 0.33, 6, now));
        secondaryWeights.add(buildSecondary(configId, "L2_PUBLIC_AVOIDANCE", "公众避险能力", parentIdMap.get("L1_SELF_RESCUE_TRANSFER"), 0.33, 7, now));
        secondaryWeights.add(buildSecondary(configId, "L2_RELOCATION", "转移安置能力", parentIdMap.get("L1_SELF_RESCUE_TRANSFER"), 0.34, 8, now));

        return saveBatch(secondaryWeights);
    }

    private IndicatorWeight buildSecondary(Long configId, String code, String name, Long parentId, Double weight, Integer sortOrder, LocalDateTime now) {
        IndicatorWeight w = new IndicatorWeight();
        w.setConfigId(configId);
        w.setIndicatorCode(code);
        w.setIndicatorName(name);
        w.setIndicatorLevel(2);
        w.setWeight(weight == null ? 0.0 : weight);
        w.setParentId(parentId);
        w.setSortOrder(sortOrder);
        w.setCreateTime(now);
        return w;
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

    @Override
    public List<IndicatorWeight> getSecondaryWeights(Long weightConfigId) {
        return getByConfigIdAndLevel(weightConfigId, 2);
    }

    @Override
    public List<IndicatorWeight> getPrimaryWeights(Long weightConfigId) {
        return getByConfigIdAndLevel(weightConfigId, 1);
    }

    @Override
    public List<IndicatorWeight> getChildWeights(Long weightConfigId, Long parentId) {
        QueryWrapper<IndicatorWeight> wrapper = new QueryWrapper<>();
        wrapper.eq("config_id", weightConfigId)
               .eq("parent_id", parentId)
               .orderByAsc("sort_order");
        return list(wrapper);
    }

}
