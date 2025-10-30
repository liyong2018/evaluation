package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluate.entity.AlgorithmConfig;
import com.evaluate.mapper.AlgorithmConfigMapper;
import com.evaluate.service.IAlgorithmConfigService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 算法配置服务实现类
 * 
 * @author System
 * @since 2024-01-01
 */
@Slf4j
@Service
public class AlgorithmConfigServiceImpl extends ServiceImpl<AlgorithmConfigMapper, AlgorithmConfig> implements IAlgorithmConfigService {

    private static final Logger log = LoggerFactory.getLogger(AlgorithmConfigServiceImpl.class);

    @Override
    public AlgorithmConfig getDefaultConfig() {
        // 返回第一个启用的配置作为默认配置
        QueryWrapper<AlgorithmConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);
        queryWrapper.orderByAsc("id");
        queryWrapper.last("LIMIT 1");
        return getOne(queryWrapper);
    }

    @Override
    public AlgorithmConfig getByConfigName(String configName) {
        QueryWrapper<AlgorithmConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("config_name", configName);
        queryWrapper.eq("status", 1);
        return getOne(queryWrapper);
    }

    @Override
    public List<AlgorithmConfig> getEnabledConfigs() {
        QueryWrapper<AlgorithmConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);
        queryWrapper.orderByAsc("id");
        return list(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setDefaultConfig(Long id) {
        // 这里可以实现设置默认配置的逻辑
        // 目前简单返回true，实际可以添加默认标记字段
        AlgorithmConfig config = getById(id);
        if (config != null) {
            config.setStatus(1);
            return updateById(config);
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        log.info("更新算法配置状态 - ID: {}, 状态: {}", id, status);
        AlgorithmConfig config = getById(id);
        if (config != null) {
            config.setStatus(status);
            return updateById(config);
        }
        return false;
    }
}