package com.evaluate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evaluate.entity.AlgorithmConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 算法配置Mapper接口
 * 
 * @author System
 * @since 2024-01-01
 */
@Mapper
public interface AlgorithmConfigMapper extends BaseMapper<AlgorithmConfig> {

    /**
     * 查询默认算法配置
     * 
     * @return 默认算法配置
     */
    AlgorithmConfig selectDefaultConfig();

    /**
     * 根据算法名称查询
     * 
     * @param algorithmName 算法名称
     * @return 算法配置
     */
    AlgorithmConfig selectByAlgorithmName(@Param("algorithmName") String algorithmName);

    /**
     * 查询启用状态的算法列表
     * 
     * @return 算法配置列表
     */
    List<AlgorithmConfig> selectEnabledConfigs();

    /**
     * 根据创建人查询算法列表
     * 
     * @param creator 创建人
     * @return 算法配置列表
     */
    List<AlgorithmConfig> selectByCreator(@Param("creator") String creator);

    /**
     * 更新默认算法状态
     * 
     * @param id 算法ID
     * @param isDefault 是否默认
     * @return 更新数量
     */
    int updateDefaultStatus(@Param("id") Long id, @Param("isDefault") Integer isDefault);

    /**
     * 清除所有默认算法状态
     * 
     * @return 更新数量
     */
    int clearAllDefaultStatus();

    /**
     * 根据版本查询算法配置
     * 
     * @param algorithmVersion 算法版本
     * @return 算法配置列表
     */
    List<AlgorithmConfig> selectByVersion(@Param("algorithmVersion") String algorithmVersion);
}