package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluate.entity.EvaluationModel;
import com.evaluate.entity.IndicatorWeightScore;
import com.evaluate.entity.WeightConfig;
import com.evaluate.mapper.EvaluationModelMapper;
import com.evaluate.mapper.WeightConfigMapper;
import com.evaluate.service.IIndicatorWeightScoreService;
import com.evaluate.service.IWeightConfigService;
import com.evaluate.service.IIndicatorWeightService;
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

/**
 * 权重配置服务实现类
 * 
 * @author System
 * @since 2024-01-01
 */
@Slf4j
@Service
public class WeightConfigServiceImpl extends ServiceImpl<WeightConfigMapper, WeightConfig> implements IWeightConfigService {

    @Autowired
    private IIndicatorWeightService indicatorWeightService;

    @Autowired
    private IIndicatorWeightScoreService indicatorWeightScoreService;

    @Autowired
    private EvaluationModelMapper evaluationModelMapper;

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
        String normalizedOrgcode = normalizeOrgcodeToCounty(trimmedOrgcode);

        Map<Long, String> modelIdToName = resolveDefaultModelNames();
        List<String> modelNames = new ArrayList<>(modelIdToName.values());

        QueryWrapper<WeightConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("orgcode", normalizedOrgcode);
        queryWrapper.apply("YEAR(create_time) = {0}", year);
        queryWrapper.in("config_name", modelNames);
        queryWrapper.eq("is_deleted", 0);

        List<WeightConfig> existing = list(queryWrapper);
        Map<String, WeightConfig> existingByName = new HashMap<>();
        for (WeightConfig cfg : existing) {
            if (StringUtils.hasText(cfg.getConfigName())) {
                existingByName.put(cfg.getConfigName(), cfg);
            }
        }

        List<WeightConfig> result = new ArrayList<>();
        for (Map.Entry<Long, String> entry : modelIdToName.entrySet()) {
            String modelName = entry.getValue();
            WeightConfig cfg = existingByName.get(modelName);
            if (cfg == null) {
                WeightConfig newCfg = new WeightConfig();
                newCfg.setOrgcode(normalizedOrgcode);
                newCfg.setConfigName(modelName);
                newCfg.setDescription(modelName + "权重配置");
                newCfg.setCreateTime(LocalDateTime.of(year, 1, 1, 0, 0));

                boolean saved = save(newCfg);
                if (!saved || newCfg.getId() == null) {
                    log.error("创建默认权重配置失败: orgcode={}, year={}, modelName={}", trimmedOrgcode, year, modelName);
                    continue;
                }

                indicatorWeightService.initDefaultWeights(newCfg.getId());
                cfg = newCfg;
            } else {
                if (indicatorWeightService.getByConfigId(cfg.getId()).isEmpty()) {
                    indicatorWeightService.initDefaultWeights(cfg.getId());
                }
            }
            result.add(cfg);
        }

        return result;
    }

    @Override
    public List<WeightConfig> getEffectiveModelYearConfigs(String orgcode, Integer year) {
        if (!StringUtils.hasText(orgcode) || year == null) {
            return new ArrayList<>();
        }

        String trimmedOrgcode = orgcode.trim();
        String normalizedOrgcode = normalizeOrgcodeToCounty(trimmedOrgcode);

        Map<Long, String> modelIdToName = resolveDefaultModelNames();
        Map<Long, String> legacyNameByModelId = resolveLegacyModelNames();

        QueryWrapper<WeightConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("orgcode", normalizedOrgcode);
        queryWrapper.apply("YEAR(create_time) = {0}", year);
        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByDesc("create_time");

        List<WeightConfig> candidates = list(queryWrapper);

        List<WeightConfig> result = new ArrayList<>();
        Long[] orderedModelIds = new Long[]{3L, 4L, 8L, 11L};
        for (Long modelId : orderedModelIds) {
            String modelName = modelIdToName.get(modelId);
            String legacyName = legacyNameByModelId.get(modelId);
            WeightConfig best = null;
            long bestScoreCnt = -1;
            int bestNameScore = -1;

            for (WeightConfig cfg : candidates) {
                if (cfg == null || !StringUtils.hasText(cfg.getConfigName())) {
                    continue;
                }
                String name = cfg.getConfigName().trim();
                int nameScore = scoreNameMatch(modelId, name, modelName, legacyName);
                if (nameScore <= 0) {
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

            if (best != null) {
                WeightConfig view = new WeightConfig();
                view.setId(best.getId());
                view.setOrgcode(best.getOrgcode());
                view.setConfigName(modelName);
                view.setDescription(best.getDescription());
                view.setCreateTime(best.getCreateTime());
                view.setUpdateTime(best.getUpdateTime());
                view.setIsDeleted(best.getIsDeleted());
                result.add(view);
            }
        }

        return result;
    }

    private String normalizeOrgcodeToCounty(String orgcode) {
        if (!StringUtils.hasText(orgcode)) {
            return orgcode;
        }
        String trimmed = orgcode.trim();
        if (trimmed.length() >= 6) {
            return trimmed.substring(0, 6);
        }
        return trimmed;
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
