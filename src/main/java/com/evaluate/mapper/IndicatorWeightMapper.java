package com.evaluate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evaluate.entity.IndicatorWeight;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 指标权重Mapper接口
 * 
 * @author System
 * @since 2024-01-01
 */
@Mapper
public interface IndicatorWeightMapper extends BaseMapper<IndicatorWeight> {

    /**
     * 根据配置ID查询权重列表
     * 
     * @param configId 配置ID
     * @return 指标权重列表
     */
    List<IndicatorWeight> selectByConfigId(@Param("configId") Long configId);

    /**
     * 根据配置ID和指标级别查询权重列表
     * 
     * @param configId 配置ID
     * @param indicatorLevel 指标级别
     * @return 指标权重列表
     */
    List<IndicatorWeight> selectByConfigIdAndLevel(@Param("configId") Long configId, @Param("indicatorLevel") Integer indicatorLevel);

    /**
     * 根据父指标ID查询子指标权重列表
     * 
     * @param parentId 父指标ID
     * @return 指标权重列表
     */
    List<IndicatorWeight> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 根据配置ID和指标代码查询权重
     * 
     * @param configId 配置ID
     * @param indicatorCode 指标代码
     * @return 指标权重
     */
    IndicatorWeight selectByConfigIdAndCode(@Param("configId") Long configId, @Param("indicatorCode") String indicatorCode);

    /**
     * 批量插入指标权重
     * 
     * @param weightList 权重列表
     * @return 插入数量
     */
    int batchInsert(@Param("weightList") List<IndicatorWeight> weightList);

    /**
     * 根据配置ID删除所有权重
     * 
     * @param configId 配置ID
     * @return 删除数量
     */
    int deleteByConfigId(@Param("configId") Long configId);

    /**
     * 查询树形结构的权重数据
     * 
     * @param configId 配置ID
     * @return 树形权重列表
     */
    List<IndicatorWeight> selectTreeByConfigId(@Param("configId") Long configId);

    /**
     * 批量更新权重值
     * 
     * @param weightList 权重列表
     * @return 更新数量
     */
    int batchUpdateWeight(@Param("weightList") List<IndicatorWeight> weightList);
}