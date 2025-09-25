package com.evaluate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.evaluate.entity.AlgorithmConfig;

import java.util.List;

/**
 * 算法配置服务接口
 * 
 * @author System
 * @since 2024-01-01
 */
public interface IAlgorithmConfigService extends IService<AlgorithmConfig> {

    /**
     * 获取默认算法配置
     * 
     * @return 默认算法配置
     */
    AlgorithmConfig getDefaultConfig();

    /**
     * 根据配置名称获取算法配置
     * 
     * @param configName 配置名称
     * @return 算法配置
     */
    AlgorithmConfig getByConfigName(String configName);

    /**
     * 获取启用的算法配置列表
     * 
     * @return 启用的算法配置列表
     */
    List<AlgorithmConfig> getEnabledConfigs();

    /**
     * 设置默认配置
     * 
     * @param id 配置ID
     * @return 是否成功
     */
    boolean setDefaultConfig(Long id);

    /**
     * 启用/禁用配置
     * 
     * @param id 配置ID
     * @param status 状态(1-启用,0-禁用)
     * @return 是否成功
     */
    boolean updateStatus(Long id, Integer status);
}