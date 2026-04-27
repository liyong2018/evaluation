package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluate.entity.IndicatorWeight;
import com.evaluate.entity.WeightConfig;
import com.evaluate.mapper.IndicatorWeightMapper;
import com.evaluate.mapper.WeightConfigMapper;
import com.evaluate.service.IIndicatorWeightService;
import com.evaluate.service.IIndicatorWeightScoreService;
import com.evaluate.service.IWeightConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    private static final int BASELINE_YEAR = 2020;
    private static final Map<Long, List<String>> LEGACY_CONFIG_NAMES_BY_MODEL_ID = createLegacyConfigNamesByModelId();

    @Autowired
    private WeightConfigMapper weightConfigMapper;

    @Lazy
    @Autowired(required = false)
    private IIndicatorWeightScoreService indicatorWeightScoreService;

    @Lazy
    @Autowired(required = false)
    private IWeightConfigService weightConfigService;

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
        if (configId == null) {
            return false;
        }

        remove(new QueryWrapper<IndicatorWeight>()
                .eq("config_id", configId)
                .eq("indicator_level", 2));

        remove(new QueryWrapper<IndicatorWeight>()
                .eq("config_id", configId)
                .eq("indicator_level", 1));

        return true;
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

        WeightConfig cfg = weightConfigMapper.selectById(configId);
        if (cfg != null && StringUtils.hasText(cfg.getConfigName()) && cfg.getConfigName().contains("综合")) {
            return initDefaultComprehensiveWeights(configId);
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

    private boolean initDefaultComprehensiveWeights(Long configId) {
        deleteByConfigId(configId);

        LocalDateTime now = LocalDateTime.now();

        IndicatorWeight town = new IndicatorWeight();
        town.setConfigId(configId);
        town.setIndicatorCode("L1_TOWNSHIP");
        town.setIndicatorName("乡镇");
        town.setIndicatorLevel(1);
        town.setWeight(0.53);
        town.setSortOrder(1);
        town.setCreateTime(now);

        IndicatorWeight community = new IndicatorWeight();
        community.setConfigId(configId);
        community.setIndicatorCode("L1_COMMUNITY");
        community.setIndicatorName("社区");
        community.setIndicatorLevel(1);
        community.setWeight(0.47);
        community.setSortOrder(2);
        community.setCreateTime(now);

        boolean primarySaved = saveBatch(Arrays.asList(town, community));
        if (!primarySaved) {
            return false;
        }

        List<IndicatorWeight> secondaryWeights = new ArrayList<>();
        secondaryWeights.add(buildSecondary(configId, "L2_TOWNSHIP_DISASTER_MANAGEMENT", "灾害管理能力", town.getId(), 0.33, 1, now));
        secondaryWeights.add(buildSecondary(configId, "L2_TOWNSHIP_DISASTER_PREPAREDNESS", "灾害备灾能力", town.getId(), 0.32, 2, now));
        secondaryWeights.add(buildSecondary(configId, "L2_TOWNSHIP_SELF_RESCUE_TRANSFER", "自救转移能力", town.getId(), 0.35, 3, now));

        secondaryWeights.add(buildSecondary(configId, "L2_COMMUNITY_DISASTER_MANAGEMENT", "灾害管理能力", community.getId(), 0.32, 4, now));
        secondaryWeights.add(buildSecondary(configId, "L2_COMMUNITY_DISASTER_PREPAREDNESS", "灾害备灾能力", community.getId(), 0.31, 5, now));
        secondaryWeights.add(buildSecondary(configId, "L2_COMMUNITY_SELF_RESCUE_TRANSFER", "自救转移能力", community.getId(), 0.37, 6, now));

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
    @Transactional(rollbackFor = Exception.class)
    public boolean ensureComprehensiveCountyWeights(Long configId, String orgcode, Integer year) {
        if (configId == null || !StringUtils.hasText(orgcode) || year == null) {
            return false;
        }

        String trimmed = orgcode.trim();

        IndicatorWeight town = getByConfigIdAndCode(configId, "L1_TOWNSHIP");
        IndicatorWeight community = getByConfigIdAndCode(configId, "L1_COMMUNITY");
        boolean needsInit = town == null || community == null;
        if (!needsInit) {
            needsInit = getByConfigIdAndCode(configId, "L2_TOWNSHIP_DISASTER_MANAGEMENT") == null
                    || getByConfigIdAndCode(configId, "L2_TOWNSHIP_DISASTER_PREPAREDNESS") == null
                    || getByConfigIdAndCode(configId, "L2_TOWNSHIP_SELF_RESCUE_TRANSFER") == null
                    || getByConfigIdAndCode(configId, "L2_COMMUNITY_DISASTER_MANAGEMENT") == null
                    || getByConfigIdAndCode(configId, "L2_COMMUNITY_DISASTER_PREPAREDNESS") == null
                    || getByConfigIdAndCode(configId, "L2_COMMUNITY_SELF_RESCUE_TRANSFER") == null;
        }
        if (needsInit) {
            if (!initDefaultComprehensiveWeights(configId)) {
                return false;
            }
            town = getByConfigIdAndCode(configId, "L1_TOWNSHIP");
            community = getByConfigIdAndCode(configId, "L1_COMMUNITY");
            if (town == null || community == null) {
                return false;
            }
        }

        String cityCode = null;
        if (trimmed.length() >= 4) {
            cityCode = trimmed.substring(0, 4);
        }
        Long cityTownshipConfigId = findConfigIdByModelId(cityCode, year, 3L);
        Long cityCommunityConfigId = findConfigIdByModelId(cityCode, year, 8L);

        double townshipPrimaryBase = sumWeights(cityTownshipConfigId,
                "L1_DISASTER_MANAGEMENT", "L1_DISASTER_PREPAREDNESS", "L1_SELF_RESCUE_TRANSFER");
        double communityPrimaryBase = sumWeights(cityCommunityConfigId,
                "L1_DISASTER_MANAGEMENT", "L1_DISASTER_PREPAREDNESS", "L1_SELF_RESCUE_TRANSFER");
        if (townshipPrimaryBase > 0 && communityPrimaryBase > 0) {
            double[] primary = roundTwoToOne(townshipPrimaryBase, communityPrimaryBase);
            town.setWeight(primary[0]);
            community.setWeight(primary[1]);
            updateById(town);
            updateById(community);
        }

        double[] townshipSecondary = resolveThreeWeights(
                cityTownshipConfigId,
                new String[]{"L1_DISASTER_MANAGEMENT", "L1_DISASTER_PREPAREDNESS", "L1_SELF_RESCUE_TRANSFER"},
                new String[]{"L2_TOWNSHIP_DISASTER_MANAGEMENT", "L2_TOWNSHIP_DISASTER_PREPAREDNESS", "L2_TOWNSHIP_SELF_RESCUE_TRANSFER"},
                configId
        );
        if (townshipSecondary != null) {
            updateSecondaryWeight(configId, "L2_TOWNSHIP_DISASTER_MANAGEMENT", town.getId(), townshipSecondary[0]);
            updateSecondaryWeight(configId, "L2_TOWNSHIP_DISASTER_PREPAREDNESS", town.getId(), townshipSecondary[1]);
            updateSecondaryWeight(configId, "L2_TOWNSHIP_SELF_RESCUE_TRANSFER", town.getId(), townshipSecondary[2]);
        }

        double[] communitySecondary = resolveThreeWeights(
                cityCommunityConfigId,
                new String[]{"L1_DISASTER_MANAGEMENT", "L1_DISASTER_PREPAREDNESS", "L1_SELF_RESCUE_TRANSFER"},
                new String[]{"L2_COMMUNITY_DISASTER_MANAGEMENT", "L2_COMMUNITY_DISASTER_PREPAREDNESS", "L2_COMMUNITY_SELF_RESCUE_TRANSFER"},
                configId
        );
        if (communitySecondary != null) {
            updateSecondaryWeight(configId, "L2_COMMUNITY_DISASTER_MANAGEMENT", community.getId(), communitySecondary[0]);
            updateSecondaryWeight(configId, "L2_COMMUNITY_DISASTER_PREPAREDNESS", community.getId(), communitySecondary[1]);
            updateSecondaryWeight(configId, "L2_COMMUNITY_SELF_RESCUE_TRANSFER", community.getId(), communitySecondary[2]);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int purgeCountyWeights(Integer year) {
        StringBuilder sub = new StringBuilder();
        sub.append("SELECT id FROM weight_config WHERE is_deleted = 0 AND LENGTH(orgcode) = 6");
        if (year != null) {
            sub.append(" AND (year = ").append(year)
               .append(" OR (year IS NULL AND YEAR(create_time) = ").append(year).append("))");
        }
        String configSub = sub.toString();

        QueryWrapper<IndicatorWeight> deleteChildren = new QueryWrapper<>();
        deleteChildren.inSql("config_id", configSub);
        deleteChildren.isNotNull("parent_id");
        int childDeleted = baseMapper.delete(deleteChildren);

        QueryWrapper<IndicatorWeight> deleteParents = new QueryWrapper<>();
        deleteParents.inSql("config_id", configSub);
        deleteParents.isNull("parent_id");
        int parentDeleted = baseMapper.delete(deleteParents);

        return childDeleted + parentDeleted;
    }

    private Long findConfigIdByModelId(String orgcode, Integer year, Long modelId) {
        if (!StringUtils.hasText(orgcode) || year == null || modelId == null) {
            return null;
        }
        Long cfgId = findConfigIdExactYearByModelId(orgcode, year, modelId);
        if (cfgId != null) {
            return cfgId;
        }
        if (year >= 2023 && year > BASELINE_YEAR) {
            cfgId = findConfigIdExactYearByModelId(orgcode, BASELINE_YEAR, modelId);
            if (cfgId != null) {
                return cfgId;
            }
        }
        return findConfigIdYearNullByModelId(orgcode, modelId);
    }

    private Long findConfigIdExactYearByModelId(String orgcode, Integer year, Long modelId) {
        if (!StringUtils.hasText(orgcode) || year == null || modelId == null) {
            return null;
        }
        QueryWrapper<WeightConfig> qw = new QueryWrapper<>();
        qw.eq("orgcode", orgcode.trim());
        qw.eq("model_id", modelId);
        qw.eq("is_deleted", 0);
        qw.and(w -> w.eq("year", year).or().isNull("year").apply("YEAR(create_time) = {0}", year));
        qw.orderByDesc("create_time").orderByDesc("id");
        qw.last("LIMIT 1");
        WeightConfig cfg = weightConfigMapper.selectOne(qw);
        return cfg == null ? null : cfg.getId();
    }

    private Long findConfigIdYearNullByModelId(String orgcode, Long modelId) {
        if (!StringUtils.hasText(orgcode) || modelId == null) {
            return null;
        }
        QueryWrapper<WeightConfig> qw = new QueryWrapper<>();
        qw.eq("orgcode", orgcode.trim());
        qw.eq("model_id", modelId);
        qw.eq("is_deleted", 0);
        qw.isNull("year");
        qw.orderByDesc("create_time").orderByDesc("id");
        qw.last("LIMIT 1");
        WeightConfig cfg = weightConfigMapper.selectOne(qw);
        return cfg == null ? null : cfg.getId();
    }

    private Double getWeight(Long configId, String indicatorCode) {
        if (configId == null || !StringUtils.hasText(indicatorCode)) {
            return null;
        }
        IndicatorWeight w = getByConfigIdAndCode(configId, indicatorCode.trim());
        if (w == null || w.getWeight() == null) {
            return null;
        }
        return w.getWeight();
    }

    private double sumWeights(Long configId, String... indicatorCodes) {
        if (configId == null || indicatorCodes == null || indicatorCodes.length == 0) {
            return 0.0;
        }
        double sum = 0.0;
        for (String indicatorCode : indicatorCodes) {
            Double value = getWeight(configId, indicatorCode);
            if (value != null && value > 0) {
                sum += value;
            }
        }
        return sum;
    }

    private double[] resolveThreeWeights(Long sourceConfigId, String[] sourceCodes, String[] currentCodes, Long currentConfigId) {
        if (sourceCodes == null || currentCodes == null || sourceCodes.length != 3 || currentCodes.length != 3) {
            return null;
        }
        double[] values = new double[3];
        for (int i = 0; i < 3; i++) {
            Double sourceWeight = getWeight(sourceConfigId, sourceCodes[i]);
            if (sourceWeight != null && sourceWeight > 0) {
                values[i] = sourceWeight;
                continue;
            }
            Double currentWeight = getWeight(currentConfigId, currentCodes[i]);
            values[i] = currentWeight == null ? 0.0 : currentWeight;
        }
        double sum = values[0] + values[1] + values[2];
        if (sum <= 0) {
            return null;
        }
        return roundThreeToOne(values[0], values[1], values[2]);
    }

    private void updateSecondaryWeight(Long configId, String code, Long parentId, double weight) {
        if (configId == null || !StringUtils.hasText(code) || parentId == null) {
            return;
        }
        IndicatorWeight w = getByConfigIdAndCode(configId, code.trim());
        if (w == null) {
            return;
        }
        w.setParentId(parentId);
        w.setWeight(round2(weight));
        updateById(w);
    }

    private double round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private double[] roundTwoToOne(double w1, double w2) {
        double a = round2(w1);
        double b = round2(w2);
        double diff = round2(1.0 - (a + b));
        b = round2(b + diff);
        return new double[]{a, b};
    }

    private double[] roundThreeToOne(double w1, double w2, double w3) {
        double a = round2(w1);
        double b = round2(w2);
        double c = round2(w3);
        double diff = round2(1.0 - (a + b + c));
        c = round2(c + diff);
        return new double[]{a, b, c};
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

    @Override
    public List<IndicatorWeight> getWeightsWithInheritance(Long configId, String parentOrgcode, Long parentConfigId) {
        if (configId == null) {
            return new ArrayList<>();
        }

        // 获取基准权重
        List<IndicatorWeight> baselineWeights = getByConfigId(configId);

        // 如果有专家打分服务，获取专家平均权重
        Map<String, Double> expertWeights = new HashMap<>();
        try {
            if (indicatorWeightScoreService != null) {
                Map<String, Double> averages = indicatorWeightScoreService.calculateAverageWeights(configId);
                if (averages != null) {
                    expertWeights = averages;
                }
            }
        } catch (Exception e) {
            log.warn("获取专家平均权重失败: configId={}", configId, e);
        }

        // 准备继承权重映射（从父级配置）
        Map<String, IndicatorWeight> parentWeightMap = new HashMap<>();
        if (parentConfigId != null) {
            try {
                List<IndicatorWeight> parentWeights = getByConfigId(parentConfigId);
                for (IndicatorWeight pw : parentWeights) {
                    if (pw.getIndicatorCode() != null) {
                        parentWeightMap.put(pw.getIndicatorCode(), pw);
                    }
                }
            } catch (Exception e) {
                log.warn("获取父级权重失败: parentConfigId={}", parentConfigId, e);
            }
        }

        // 构建结果列表，应用继承逻辑
        List<IndicatorWeight> result = new ArrayList<>();
        for (IndicatorWeight baseline : baselineWeights) {
            IndicatorWeight weight = new IndicatorWeight();
            weight.setId(baseline.getId());
            weight.setConfigId(baseline.getConfigId());
            weight.setIndicatorCode(baseline.getIndicatorCode());
            weight.setIndicatorName(baseline.getIndicatorName());
            weight.setIndicatorLevel(baseline.getIndicatorLevel());
            weight.setParentId(baseline.getParentId());
            weight.setSortOrder(baseline.getSortOrder());
            weight.setCreateTime(baseline.getCreateTime());

            // 继承优先级：
            // 1. 专家打分平均值
            // 2. 基准表权重
            // 3. 父级配置权重
            String code = baseline.getIndicatorCode();
            Double finalWeight = null;
            String dataSource = "baseline"; // 数据来源标识

            if (expertWeights.containsKey(code)) {
                finalWeight = expertWeights.get(code);
                dataSource = "expert";
            } else if (baseline.getWeight() != null) {
                finalWeight = baseline.getWeight();
                dataSource = "baseline";
            } else if (parentWeightMap.containsKey(code) && parentWeightMap.get(code).getWeight() != null) {
                finalWeight = parentWeightMap.get(code).getWeight();
                dataSource = "parent";
            }

            weight.setWeight(finalWeight != null ? finalWeight : 0.0);
            result.add(weight);
        }

        return result;
    }

    @Override
    public Map<String, Double> findExpertScoresByOrgcodeAndYear(String orgcode, Integer requestedYear, Long modelId, String configName) {
        if (indicatorWeightScoreService == null || !StringUtils.hasText(orgcode) || requestedYear == null) {
            return new HashMap<>();
        }

        // 优先使用 modelId 查找
        if (modelId != null) {
            for (int year = requestedYear; year >= 2021; year--) {
                QueryWrapper<WeightConfig> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("orgcode", orgcode.trim());
                queryWrapper.eq("model_id", modelId);
                queryWrapper.eq("is_deleted", 0);
                queryWrapper.eq("year", year);
                queryWrapper.last("LIMIT 1");

                WeightConfig config = weightConfigMapper.selectOne(queryWrapper);
                if (config != null && config.getId() != null) {
                    Map<String, Double> averages = indicatorWeightScoreService.calculateAverageWeights(config.getId());
                    if (averages != null && !averages.isEmpty()) {
                        log.info("找到专家打分数据（通过modelId）: orgcode={}, year={}, modelId={}", orgcode, year, modelId);
                        return averages;
                    }
                }
            }
        }

        // 备用：使用 configName 查找（向后兼容）
        if (StringUtils.hasText(configName)) {
            for (int year = requestedYear; year >= 2021; year--) {
                QueryWrapper<WeightConfig> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("orgcode", orgcode.trim());
                queryWrapper.eq("config_name", configName.trim());
                queryWrapper.eq("is_deleted", 0);
                queryWrapper.eq("year", year);
                queryWrapper.last("LIMIT 1");

                WeightConfig config = weightConfigMapper.selectOne(queryWrapper);
                if (config != null && config.getId() != null) {
                    Map<String, Double> averages = indicatorWeightScoreService.calculateAverageWeights(config.getId());
                    if (averages != null && !averages.isEmpty()) {
                        log.info("找到专家打分数据（通过configName）: orgcode={}, year={}, configName={}", orgcode, year, configName);
                        return averages;
                    }
                }
            }
        }

        log.debug("未找到专家打分数据: orgcode={}, requestedYear={}, modelId={}, configName={}", orgcode, requestedYear, modelId, configName);
        return new HashMap<>();
    }

    @Override
    public Map<String, IndicatorWeight> findBaselineWeightsByOrgcode(String orgcode, Long modelId, String configName) {
        if (!StringUtils.hasText(orgcode)) {
            return new HashMap<>();
        }

        // 层级查找：区县 → 市级 → 省级
        List<String> orgcodeCandidates = new ArrayList<>();
        String trimmed = orgcode.trim();

        // 市级（4位）- 先添加市级，因为基准数据主要是市级
        if (trimmed.length() >= 4) {
            orgcodeCandidates.add(trimmed.substring(0, 4));
        }
        // 省级（2位）
        if (trimmed.length() >= 2) {
            orgcodeCandidates.add(trimmed.substring(0, 2));
        }
        // 区县级（6位）- 只有在区县有自己数据时才使用
        if (trimmed.length() >= 6) {
            orgcodeCandidates.add(trimmed.substring(0, 6));
        }

        List<String> configNameCandidates = resolveConfigNameCandidates(modelId, configName);

        // 优先使用 modelId 查找，但需要在候选中挑出指标更完整且名称更匹配的配置
        if (modelId != null) {
            for (String candidateOrgcode : orgcodeCandidates) {
                QueryWrapper<WeightConfig> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("orgcode", candidateOrgcode);
                queryWrapper.eq("model_id", modelId);
                queryWrapper.eq("is_deleted", 0);
                queryWrapper.eq("year", BASELINE_YEAR);
                queryWrapper.orderByDesc("create_time");

                List<WeightConfig> configs = weightConfigMapper.selectList(queryWrapper);
                WeightConfig bestConfig = pickBestBaselineConfig(configs, configNameCandidates);
                if (bestConfig != null && bestConfig.getId() != null) {
                    Map<String, IndicatorWeight> weightMap = toWeightMap(getByConfigId(bestConfig.getId()));
                    if (!weightMap.isEmpty()) {
                        log.info("找到基准表数据（通过modelId）: orgcode={}, modelId={}, configName={}, weightCount={}",
                                orgcode, modelId, bestConfig.getConfigName(), weightMap.size());
                        return weightMap;
                    }
                }
            }
        }

        // 备用：使用 configName 查找（向后兼容，支持新旧模型名）
        for (String candidateName : configNameCandidates) {
            for (String candidateOrgcode : orgcodeCandidates) {
                QueryWrapper<WeightConfig> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("orgcode", candidateOrgcode);
                queryWrapper.eq("config_name", candidateName);
                queryWrapper.eq("is_deleted", 0);
                queryWrapper.eq("year", BASELINE_YEAR);
                queryWrapper.last("LIMIT 1");

                WeightConfig config = weightConfigMapper.selectOne(queryWrapper);
                if (config != null && config.getId() != null) {
                    List<IndicatorWeight> weights = getByConfigId(config.getId());
                    if (!weights.isEmpty()) {
                        Map<String, IndicatorWeight> weightMap = toWeightMap(weights);
                        log.info("找到基准表数据（通过configName）: orgcode={}, configName={}, weightCount={}",
                                orgcode, candidateName, weightMap.size());
                        return weightMap;
                    }
                }
            }
        }

        log.debug("未找到基准表数据: orgcode={}, modelId={}, configName={}", orgcode, modelId, configName);
        return new HashMap<>();
    }

    @Override
    public List<IndicatorWeight> getWeightsWithFullInheritance(
            Long configId, String orgcode, Integer requestedYear, Long modelId, String configName) {

        if (configId == null) {
            return new ArrayList<>();
        }

        // 获取当前配置的指标结构
        List<IndicatorWeight> templateWeights = getByConfigId(configId);

        Map<String, IndicatorWeight> baselineWeights = findBaselineWeightsByOrgcode(orgcode, modelId, configName);
        Map<String, IndicatorWeight> mergedTemplates = new LinkedHashMap<>();
        for (IndicatorWeight template : sortWeights(templateWeights)) {
            if (template == null || !StringUtils.hasText(template.getIndicatorCode())) {
                continue;
            }
            mergedTemplates.put(template.getIndicatorCode().trim(), copyTemplateWeight(template));
        }

        // 如果当前配置没有指标结构，尝试从基准数据中获取模板
        if (mergedTemplates.isEmpty() && !baselineWeights.isEmpty()) {
            log.warn("配置ID {} 没有指标结构，尝试从基准数据获取模板", configId);
            for (IndicatorWeight baseline : sortWeights(new ArrayList<>(baselineWeights.values()))) {
                if (baseline == null || !StringUtils.hasText(baseline.getIndicatorCode())) {
                    continue;
                }
                mergedTemplates.put(baseline.getIndicatorCode().trim(), copyTemplateWeight(baseline));
            }
        }

        // 当前模板缺失的指标代码，补齐自2020基准表，确保执行链路能拿到完整权重
        if (!baselineWeights.isEmpty()) {
            for (IndicatorWeight baseline : sortWeights(new ArrayList<>(baselineWeights.values()))) {
                if (baseline == null || !StringUtils.hasText(baseline.getIndicatorCode())) {
                    continue;
                }
                String code = baseline.getIndicatorCode().trim();
                mergedTemplates.putIfAbsent(code, copyTemplateWeight(baseline));
            }
        }

        if (mergedTemplates.isEmpty()) {
            log.warn("未找到指标结构: configId={}, orgcode={}, modelId={}, configName={}", configId, orgcode, modelId, configName);
            return new ArrayList<>();
        }

        // 1. 首先查找专家打分数据（按年份从新到旧）
        Map<String, Double> expertWeights = findExpertScoresByOrgcodeAndYear(orgcode, requestedYear, modelId, configName);

        // 3. 构建结果，应用继承逻辑
        List<IndicatorWeight> result = new ArrayList<>();
        for (IndicatorWeight template : mergedTemplates.values()) {
            IndicatorWeight weight = copyTemplateWeight(template);

            String code = template.getIndicatorCode();
            Double finalWeight = null;

            // 优先级：专家打分 > 基准表
            if (expertWeights.containsKey(code)) {
                finalWeight = expertWeights.get(code);
            } else if (baselineWeights.containsKey(code) && baselineWeights.get(code).getWeight() != null) {
                finalWeight = baselineWeights.get(code).getWeight();
            }

            weight.setWeight(finalWeight != null ? finalWeight : 0.0);
            result.add(weight);
        }

        return result;
    }

    private static Map<Long, List<String>> createLegacyConfigNamesByModelId() {
        Map<Long, List<String>> mapping = new HashMap<>();
        mapping.put(4L, Arrays.asList(
                "社区-行政村能力评估模型",
                "区县-社区（行政村）减灾能力（社区单元）评估"
        ));
        mapping.put(8L, Arrays.asList(
                "社区-乡镇能力评估模型",
                "区县-社区（行政村）减灾能力（乡镇单元）评估"
        ));
        mapping.put(11L, Arrays.asList(
                "综合减灾能力评估模型",
                "区县综合减灾能力评估"
        ));
        return mapping;
    }

    private List<String> resolveConfigNameCandidates(Long modelId, String configName) {
        LinkedHashSet<String> names = new LinkedHashSet<>();
        if (StringUtils.hasText(configName)) {
            names.add(configName.trim());
        }
        if (modelId != null) {
            List<String> legacyNames = LEGACY_CONFIG_NAMES_BY_MODEL_ID.get(modelId);
            if (legacyNames != null) {
                for (String legacyName : legacyNames) {
                    if (StringUtils.hasText(legacyName)) {
                        names.add(legacyName.trim());
                    }
                }
            }
        }
        return new ArrayList<>(names);
    }

    private Map<String, IndicatorWeight> toWeightMap(List<IndicatorWeight> weights) {
        Map<String, IndicatorWeight> weightMap = new HashMap<>();
        if (weights == null || weights.isEmpty()) {
            return weightMap;
        }
        for (IndicatorWeight weight : weights) {
            if (weight == null || !StringUtils.hasText(weight.getIndicatorCode())) {
                continue;
            }
            weightMap.put(weight.getIndicatorCode().trim(), weight);
        }
        return weightMap;
    }

    private List<IndicatorWeight> sortWeights(List<IndicatorWeight> weights) {
        if (weights == null || weights.isEmpty()) {
            return new ArrayList<>();
        }
        List<IndicatorWeight> sorted = new ArrayList<>(weights);
        sorted.sort(Comparator
                .comparing((IndicatorWeight item) -> item.getIndicatorLevel() == null ? Integer.MAX_VALUE : item.getIndicatorLevel())
                .thenComparing(item -> item.getSortOrder() == null ? Integer.MAX_VALUE : item.getSortOrder())
                .thenComparing(item -> item.getId() == null ? Long.MAX_VALUE : item.getId()));
        return sorted;
    }

    private IndicatorWeight copyTemplateWeight(IndicatorWeight source) {
        IndicatorWeight target = new IndicatorWeight();
        target.setId(source.getId());
        target.setConfigId(source.getConfigId());
        target.setIndicatorCode(source.getIndicatorCode());
        target.setIndicatorName(source.getIndicatorName());
        target.setIndicatorLevel(source.getIndicatorLevel());
        target.setParentId(source.getParentId());
        target.setSortOrder(source.getSortOrder());
        target.setCreateTime(source.getCreateTime());
        target.setWeight(source.getWeight());
        return target;
    }

    private WeightConfig pickBestBaselineConfig(List<WeightConfig> configs, List<String> configNameCandidates) {
        if (configs == null || configs.isEmpty()) {
            return null;
        }

        WeightConfig best = null;
        int bestNameScore = -1;
        int bestWeightCount = -1;

        for (WeightConfig config : configs) {
            if (config == null || config.getId() == null) {
                continue;
            }

            int nameScore = 0;
            if (StringUtils.hasText(config.getConfigName()) && configNameCandidates != null) {
                String name = config.getConfigName().trim();
                for (String candidate : configNameCandidates) {
                    if (StringUtils.hasText(candidate) && name.equals(candidate.trim())) {
                        nameScore = 2;
                        break;
                    }
                    if (StringUtils.hasText(candidate) && name.contains(candidate.trim())) {
                        nameScore = Math.max(nameScore, 1);
                    }
                }
            }

            int weightCount = getByConfigId(config.getId()).size();
            if (best == null
                    || nameScore > bestNameScore
                    || (nameScore == bestNameScore && weightCount > bestWeightCount)
                    || (nameScore == bestNameScore && weightCount == bestWeightCount
                    && config.getCreateTime() != null
                    && (best.getCreateTime() == null || config.getCreateTime().isAfter(best.getCreateTime())))) {
                best = config;
                bestNameScore = nameScore;
                bestWeightCount = weightCount;
            }
        }

        return best;
    }

}
