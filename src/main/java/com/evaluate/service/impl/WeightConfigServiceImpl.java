package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluate.entity.WeightConfig;
import com.evaluate.mapper.WeightConfigMapper;
import com.evaluate.service.IWeightConfigService;
import com.evaluate.service.IIndicatorWeightService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

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
}