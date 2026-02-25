package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluate.entity.EvaluationModel;
import com.evaluate.entity.IndicatorWeight;
import com.evaluate.entity.IndicatorWeightScore;
import com.evaluate.entity.WeightConfig;
import com.evaluate.entity.Organization;
import com.evaluate.mapper.EvaluationModelMapper;
import com.evaluate.mapper.WeightConfigMapper;
import com.evaluate.service.IIndicatorWeightScoreService;
import com.evaluate.service.IWeightConfigService;
import com.evaluate.service.IIndicatorWeightService;
import com.evaluate.service.IOrganizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 权重配置服务实现类
 *
 * @author System
 * @since 2024-01-01
 */
@Slf4j
@Service
public class WeightConfigServiceImpl extends ServiceImpl<WeightConfigMapper, WeightConfig> implements IWeightConfigService {

    private static final int BASELINE_YEAR = 2020;

    @Autowired
    private IIndicatorWeightService indicatorWeightService;

    @Autowired
    private IIndicatorWeightScoreService indicatorWeightScoreService;

    @Autowired
    private EvaluationModelMapper evaluationModelMapper;

    @Autowired
    private IOrganizationService organizationService;

    @Override
    public WeightConfig getDefaultConfig() {
        // 返回第一个配置作为默认配置
        QueryWrapper<WeightConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByAsc("id");
        queryWrapper.last("LIMIT 1");
        return getOne(queryWrapper);
    }

    @Override
    public WeightConfig getByConfigName(String configName) {
        QueryWrapper<WeightConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("config_name", configName);
        queryWrapper.eq("is_deleted", 0);
        return getOne(queryWrapper);
    }

    @Override
    public List<WeightConfig> getEnabledConfigs() {
        // 由于没有status字段，返回所有未删除的配置
        QueryWrapper<WeightConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", 0);
        return list(queryWrapper);
    }

    @Override
    public List<WeightConfig> getByCreator(String creator) {
        // creator字段不存在，返回所有配置
        return list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setDefaultConfig(Long id) {
        // 默认配置功能暂不支持，直接返回true
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createWeightConfig(WeightConfig weightConfig) {
        if (!validateWeightConfig(weightConfig)) {
            log.error("权重配置验证失败: {}", weightConfig);
            return false;
        }
        
        // 检查配置名称是否重复
        WeightConfig existing = getByConfigName(weightConfig.getConfigName());
        if (existing != null) {
            log.error("权重配置名称已存在: {}", weightConfig.getConfigName());
            return false;
        }
        
        weightConfig.setCreateTime(LocalDateTime.now());
        
        return save(weightConfig);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateWeightConfig(WeightConfig weightConfig) {
        if (!validateWeightConfig(weightConfig)) {
            log.error("权重配置验证失败: {}", weightConfig);
            return false;
        }
        
        // 检查配置名称是否重复（排除自己）
        WeightConfig existing = getByConfigName(weightConfig.getConfigName());
        if (existing != null && !existing.getId().equals(weightConfig.getId())) {
            log.error("权重配置名称已存在: {}", weightConfig.getConfigName());
            return false;
        }
        
        // 更新时间字段已移除
        
        return updateById(weightConfig);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteWeightConfigAndRelated(Long id) {
        // 删除相关的指标权重数据
        indicatorWeightService.deleteByConfigId(id);
        
        // 删除权重配置
        return removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean copyWeightConfig(Long sourceId, String newConfigName, String creator) {
        WeightConfig sourceConfig = getById(sourceId);
        if (sourceConfig == null) {
            log.error("源权重配置不存在: {}", sourceId);
            return false;
        }
        
        // 检查新配置名称是否重复
        WeightConfig existing = getByConfigName(newConfigName);
        if (existing != null) {
            log.error("权重配置名称已存在: {}", newConfigName);
            return false;
        }
        
        // 创建新的权重配置
        WeightConfig newConfig = new WeightConfig();
        newConfig.setConfigName(newConfigName);
        newConfig.setDescription(sourceConfig.getDescription() + "(复制)");
        newConfig.setOrgcode(sourceConfig.getOrgcode());
        newConfig.setDataSource(sourceConfig.getDataSource());
        newConfig.setYear(sourceConfig.getYear());
        newConfig.setCreateTime(LocalDateTime.now());
        
        if (!save(newConfig)) {
            return false;
        }
        
        // 复制指标权重数据
        return indicatorWeightService.copyWeightsByConfigId(sourceId, newConfig.getId());
    }

    @Override
    public boolean validateWeightConfig(WeightConfig weightConfig) {
        if (weightConfig == null) {
            return false;
        }
        
        // 验证必填字段
        if (!StringUtils.hasText(weightConfig.getConfigName())) {
            return false;
        }
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        // status字段不存在于weight_config表，直接返回true
        return true;
    }

    @Override
    public List<WeightConfig> getByOrgcode(String orgcode) {
        if (!StringUtils.hasText(orgcode)) {
            return list();
        }
        QueryWrapper<WeightConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("orgcode", orgcode);
        queryWrapper.eq("is_deleted", 0);
        return list(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<WeightConfig> getOrCreateModelYearConfigs(String orgcode, Integer year) {
        if (!StringUtils.hasText(orgcode) || year == null) {
            return new ArrayList<>();
        }

        String trimmedOrgcode = orgcode.trim();
        List<WeightConfig> effective = getEffectiveModelYearConfigs(trimmedOrgcode, year);
        Map<Long, String> modelIdToName = resolveDefaultModelNames();
        if (effective.size() >= modelIdToName.size()) {
            return effective;
        }

        String createOrgcode = resolveCreateOrgcode(trimmedOrgcode);

        List<String> modelNames = new ArrayList<>(modelIdToName.values());

        QueryWrapper<WeightConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("orgcode", createOrgcode);
        queryWrapper.in("config_name", modelNames);
        queryWrapper.eq("is_deleted", 0);
        queryWrapper.and(w -> w.eq("year", year).or().isNull("year").apply("YEAR(create_time) = {0}", year));

        List<WeightConfig> existing = list(queryWrapper);
        Map<String, WeightConfig> existingByName = new HashMap<>();
        for (WeightConfig cfg : existing) {
            if (StringUtils.hasText(cfg.getConfigName())) {
                existingByName.put(cfg.getConfigName(), cfg);
            }
        }

        List<WeightConfig> result = new ArrayList<>();
        for (Map.Entry<Long, String> entry : modelIdToName.entrySet()) {
            Long modelId = entry.getKey();
            String modelName = entry.getValue();
            String desiredDataSource = resolveDataSource(modelId);
            WeightConfig cfg = existingByName.get(modelName);
            if (cfg == null) {
                WeightConfig newCfg = new WeightConfig();
                newCfg.setOrgcode(createOrgcode);
                newCfg.setConfigName(modelName);
                newCfg.setDescription(modelName + "权重配置");
                newCfg.setDataSource(desiredDataSource);
                newCfg.setYear(year);
                newCfg.setCreateTime(LocalDateTime.of(year, 1, 1, 0, 0));

                boolean saved = save(newCfg);
                if (!saved || newCfg.getId() == null) {
                    log.error("创建默认权重配置失败: orgcode={}, year={}, modelName={}", trimmedOrgcode, year, modelName);
                    continue;
                }

                indicatorWeightService.initDefaultWeights(newCfg.getId());
                cfg = newCfg;
            } else {
                if (cfg.getYear() == null) {
                    WeightConfig patch = new WeightConfig();
                    patch.setId(cfg.getId());
                    patch.setYear(year);
                    updateById(patch);
                    cfg.setYear(year);
                }
                if (!Objects.equals(desiredDataSource, cfg.getDataSource())) {
                    WeightConfig patch = new WeightConfig();
                    patch.setId(cfg.getId());
                    patch.setDataSource(desiredDataSource);
                    updateById(patch);
                    cfg.setDataSource(desiredDataSource);
                }
                if (indicatorWeightService.getByConfigId(cfg.getId()).isEmpty()) {
                    indicatorWeightService.initDefaultWeights(cfg.getId());
                }
            }
            if (modelId != null && modelId.equals(11L)) {
                indicatorWeightService.ensureComprehensiveCountyWeights(cfg.getId(), cfg.getOrgcode(), year);
            }
            result.add(cfg);
        }

        if (!effective.isEmpty()) {
            Map<String, WeightConfig> byName = new HashMap<>();
            for (WeightConfig cfg : result) {
                if (cfg != null && StringUtils.hasText(cfg.getConfigName())) {
                    byName.put(cfg.getConfigName().trim(), cfg);
                }
            }
            for (WeightConfig cfg : effective) {
                if (cfg != null && StringUtils.hasText(cfg.getConfigName())) {
                    byName.putIfAbsent(cfg.getConfigName().trim(), cfg);
                }
            }
            List<WeightConfig> ordered = new ArrayList<>();
            Long[] orderedModelIds = new Long[]{3L, 4L, 8L, 11L};
            for (Long modelId : orderedModelIds) {
                String modelName = modelIdToName.get(modelId);
                if (!StringUtils.hasText(modelName)) {
                    continue;
                }
                WeightConfig cfg = byName.get(modelName.trim());
                if (cfg != null) {
                    ordered.add(cfg);
                }
            }
            return ordered;
        }

        return result;
    }

    private String resolveDataSource(Long modelId) {
        if (modelId == null) {
            return null;
        }
        if (modelId.equals(3L)) {
            return "township";
        }
        if (modelId.equals(4L) || modelId.equals(8L) || modelId.equals(11L)) {
            return "community";
        }
        return null;
    }

    @Override
    public List<WeightConfig> getEffectiveModelYearConfigs(String orgcode, Integer year) {
        return getEffectiveModelYearConfigsInternal(orgcode, year, true);
    }

    private List<WeightConfig> getEffectiveModelYearConfigsInternal(String orgcode, Integer year, boolean allowBaselineFallback) {
        if (!StringUtils.hasText(orgcode) || year == null) {
            return new ArrayList<>();
        }

        String trimmedOrgcode = orgcode.trim();
        List<String> orgcodeCandidates = resolveOrgcodeCandidates(trimmedOrgcode);
        if (orgcodeCandidates.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, String> modelIdToName = resolveDefaultModelNames();
        Map<Long, String> legacyNameByModelId = resolveLegacyModelNames();

        List<WeightConfig> candidates = queryCandidatesByYear(orgcodeCandidates, year);
        List<WeightConfig> result = buildEffectiveConfigsFromCandidates(
                orgcodeCandidates,
                candidates,
                modelIdToName,
                legacyNameByModelId,
                year,
                year,
                null
        );

        if (shouldFallbackToBaseline(year, allowBaselineFallback, result)) {
            List<WeightConfig> baselineCandidates = queryBaselineCandidates(orgcodeCandidates);
            List<WeightConfig> baseline = buildEffectiveConfigsFromCandidates(
                    orgcodeCandidates,
                    baselineCandidates,
                    modelIdToName,
                    legacyNameByModelId,
                    BASELINE_YEAR,
                    BASELINE_YEAR,
                    BASELINE_YEAR
            );
            return baseline;
        }

        return result;
    }

    private List<WeightConfig> queryCandidatesByYear(List<String> orgcodeCandidates, Integer year) {
        if (orgcodeCandidates == null || orgcodeCandidates.isEmpty() || year == null) {
            return new ArrayList<>();
        }
        QueryWrapper<WeightConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("orgcode", orgcodeCandidates);
        queryWrapper.eq("is_deleted", 0);
        queryWrapper.and(w -> w.eq("year", year).or().isNull("year").apply("YEAR(create_time) = {0}", year));
        queryWrapper.orderByDesc("create_time");
        return list(queryWrapper);
    }

    private List<WeightConfig> queryBaselineCandidates(List<String> orgcodeCandidates) {
        if (orgcodeCandidates == null || orgcodeCandidates.isEmpty()) {
            return new ArrayList<>();
        }

        QueryWrapper<WeightConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("orgcode", orgcodeCandidates);
        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByDesc("create_time");

        List<WeightConfig> all = list(queryWrapper);
        if (all == null || all.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, Integer> minYearByOrg = new HashMap<>();
        for (WeightConfig cfg : all) {
            if (cfg == null || !StringUtils.hasText(cfg.getOrgcode()) || cfg.getYear() == null) {
                continue;
            }
            String key = cfg.getOrgcode().trim();
            Integer current = minYearByOrg.get(key);
            if (current == null || cfg.getYear() < current) {
                minYearByOrg.put(key, cfg.getYear());
            }
        }

        List<WeightConfig> baseline = new ArrayList<>();
        for (WeightConfig cfg : all) {
            if (cfg == null || !StringUtils.hasText(cfg.getOrgcode())) {
                continue;
            }
            String org = cfg.getOrgcode().trim();
            Integer minYear = minYearByOrg.get(org);
            Integer cfgYear = cfg.getYear();
            if (cfgYear != null && cfgYear.equals(BASELINE_YEAR)) {
                baseline.add(cfg);
                continue;
            }
            if (cfgYear == null) {
                baseline.add(cfg);
                continue;
            }
            if (minYear != null && cfgYear.equals(minYear)) {
                baseline.add(cfg);
            }
        }
        return baseline;
    }

    private boolean shouldFallbackToBaseline(Integer year, boolean allowBaselineFallback, List<WeightConfig> result) {
        if (!allowBaselineFallback) {
            return false;
        }
        if (year == null) {
            return false;
        }
        if (year <= BASELINE_YEAR) {
            return false;
        }
        if (year < 2023) {
            return false;
        }
        return result == null || result.isEmpty();
    }

    private List<WeightConfig> buildEffectiveConfigsFromCandidates(
            List<String> orgcodeCandidates,
            List<WeightConfig> candidates,
            Map<Long, String> modelIdToName,
            Map<Long, String> legacyNameByModelId,
            Integer requestedYear,
            Integer fallbackYearForNull,
            Integer forcedYear
    ) {
        List<WeightConfig> result = new ArrayList<>();
        Long[] orderedModelIds = new Long[]{3L, 4L, 8L, 11L};
        for (Long modelId : orderedModelIds) {
            String modelName = modelIdToName.get(modelId);
            String legacyName = legacyNameByModelId.get(modelId);
            WeightConfig best = null;
            for (String candidateOrg : orgcodeCandidates) {
                WeightConfig bestAtLevel = pickBestConfigByOrgcode(candidates, candidateOrg, modelId, modelName, legacyName);
                if (bestAtLevel != null) {
                    best = bestAtLevel;
                    break;
                }
            }

            if (best != null) {
                // 获取实际数据的年份（配置的原始年份）
                Integer actualDataYear = best.getYear();
                Integer effectiveYear = forcedYear != null ? forcedYear : resolveConfigYear(best, fallbackYearForNull);
                if (modelId != null && modelId.equals(11L)) {
                    indicatorWeightService.ensureComprehensiveCountyWeights(best.getId(), best.getOrgcode(), effectiveYear);
                }
                WeightConfig view = new WeightConfig();
                view.setId(best.getId());
                view.setOrgcode(best.getOrgcode());
                view.setConfigName(modelName);
                view.setDescription(best.getDescription());
                view.setDataSource(best.getDataSource());
                view.setYear(effectiveYear);
                view.setCreateTime(best.getCreateTime());
                view.setUpdateTime(best.getUpdateTime());
                view.setIsDeleted(best.getIsDeleted());

                // 设置实际数据来源信息
                view.setActualOrgcode(best.getOrgcode());
                Organization actualOrg = organizationService.getByCode(best.getOrgcode());
                view.setActualOrgName(actualOrg != null ? actualOrg.getName() : best.getOrgcode());
                // 设置实际数据年份
                view.setActualYear(actualDataYear);

                result.add(view);
            }
        }
        return result;
    }

    private Integer resolveConfigYear(WeightConfig cfg, Integer fallbackYearForNull) {
        if (cfg == null) {
            return fallbackYearForNull;
        }
        if (cfg.getYear() != null) {
            return cfg.getYear();
        }
        return fallbackYearForNull;
    }

    private List<String> resolveOrgcodeCandidates(String orgcode) {
        if (!StringUtils.hasText(orgcode)) {
            return new ArrayList<>();
        }
        String trimmed = orgcode.trim();
        List<String> list = new ArrayList<>();

        if (trimmed.length() >= 6) {
            list.add(trimmed.substring(0, 6));
        }
        if (trimmed.length() >= 4) {
            String city = trimmed.substring(0, 4);
            if (!list.contains(city)) {
                list.add(city);
            }
        }
        if (trimmed.length() >= 2) {
            String province = trimmed.substring(0, 2);
            if (!list.contains(province)) {
                list.add(province);
            }
        }
        if (list.isEmpty()) {
            list.add(trimmed);
        }

        return list;
    }

    private String resolveCreateOrgcode(String orgcode) {
        if (!StringUtils.hasText(orgcode)) {
            return orgcode;
        }
        String trimmed = orgcode.trim();
        if (trimmed.length() >= 4) {
            return trimmed.substring(0, 4);
        }
        if (trimmed.length() >= 2) {
            return trimmed.substring(0, 2);
        }
        return trimmed;
    }

    private WeightConfig pickBestConfigByOrgcode(List<WeightConfig> candidates, String orgcode, Long modelId, String modelName, String legacyName) {
        if (candidates == null || candidates.isEmpty() || !StringUtils.hasText(orgcode)) {
            return null;
        }

        WeightConfig best = null;
        long bestScoreCnt = -1;
        int bestNameScore = -1;

        for (WeightConfig cfg : candidates) {
            if (cfg == null || cfg.getId() == null || !StringUtils.hasText(cfg.getOrgcode()) || !StringUtils.hasText(cfg.getConfigName())) {
                continue;
            }
            if (!orgcode.trim().equals(cfg.getOrgcode().trim())) {
                continue;
            }
            String name = cfg.getConfigName().trim();
            int nameScore = scoreNameMatch(modelId, name, modelName, legacyName);
            if (nameScore <= 0) {
                continue;
            }

            long weightCnt = indicatorWeightService.count(new QueryWrapper<IndicatorWeight>().eq("config_id", cfg.getId()));
            if (weightCnt <= 0) {
                continue;
            }

            long scoreCnt = indicatorWeightScoreService.count(
                    new QueryWrapper<IndicatorWeightScore>().eq("config_id", cfg.getId())
            );

            if (best == null
                    || nameScore > bestNameScore
                    || (nameScore == bestNameScore && scoreCnt > bestScoreCnt)
                    || (nameScore == bestNameScore && scoreCnt == bestScoreCnt && cfg.getCreateTime() != null
                    && (best.getCreateTime() == null || cfg.getCreateTime().isAfter(best.getCreateTime())))) {
                best = cfg;
                bestScoreCnt = scoreCnt;
                bestNameScore = nameScore;
            }
        }

        return best;
    }

    private int scoreNameMatch(Long modelId, String name, String modelName, String legacyName) {
        if (!StringUtils.hasText(name)) {
            return 0;
        }
        String n = name.trim();
        if (StringUtils.hasText(modelName) && n.equals(modelName.trim())) {
            return 3;
        }
        if (StringUtils.hasText(legacyName) && n.equals(legacyName.trim())) {
            return 3;
        }

        String upper = n.toUpperCase();
        boolean containsTown = upper.contains("乡镇") || upper.contains("街道");
        boolean containsCommunity = upper.contains("社区");
        boolean containsVillage = upper.contains("行政村") || upper.contains("社区单元") || upper.contains("乡镇单元");
        boolean containsComprehensive = upper.contains("综合");

        if (modelId == null) {
            return 0;
        }
        if (modelId.equals(3L)) {
            return (containsTown && !containsCommunity) ? 2 : 0;
        }
        if (modelId.equals(4L)) {
            return (containsCommunity && (upper.contains("行政村") || upper.contains("社区单元"))) ? 2 : 0;
        }
        if (modelId.equals(8L)) {
            return (containsCommunity && upper.contains("乡镇")) ? 2 : 0;
        }
        if (modelId.equals(11L)) {
            return containsComprehensive ? 2 : 0;
        }
        return 0;
    }

    private Map<Long, String> resolveDefaultModelNames() {
        Map<Long, String> map = new HashMap<>();

        Long[] modelIds = new Long[]{3L, 4L, 8L, 11L};
        for (Long modelId : modelIds) {
            EvaluationModel model = evaluationModelMapper.selectById(modelId);
            if (model != null && StringUtils.hasText(model.getModelName())) {
                map.put(modelId, model.getModelName().trim());
            }
        }

        if (!map.containsKey(3L)) map.put(3L, "乡镇减灾能力评估模型");
        if (!map.containsKey(4L)) map.put(4L, "社区-行政村能力评估模型");
        if (!map.containsKey(8L)) map.put(8L, "社区-乡镇能力评估模型");
        if (!map.containsKey(11L)) map.put(11L, "综合减灾能力评估模型");

        Map<Long, String> ordered = new HashMap<>();
        ordered.put(3L, map.get(3L));
        ordered.put(4L, map.get(4L));
        ordered.put(8L, map.get(8L));
        ordered.put(11L, map.get(11L));
        return ordered;
    }

    private Map<Long, String> resolveLegacyModelNames() {
        Map<Long, String> map = new HashMap<>();
        map.put(3L, "乡镇街道权重配置");
        map.put(4L, "社区-社区单元权重配置");
        map.put(8L, "社区-乡镇单元权重配置");
        map.put(11L, "综合模型权重配置");
        return map;
    }
}
