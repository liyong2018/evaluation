package com.evaluate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evaluate.entity.IndicatorWeightScore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 专家权重打分记录Mapper
 *
 * @author System
 * @since 2024-11-06
 */
@Mapper
public interface IndicatorWeightScoreMapper extends BaseMapper<IndicatorWeightScore> {

    /**
     * 获取指定配置的所有专家打分记录（按专家分组）
     *
     * @param configId 配置ID
     * @return 打分记录列表
     */
    @Select("SELECT * FROM indicator_weight_score WHERE config_id = #{configId} ORDER BY create_time DESC")
    List<IndicatorWeightScore> selectByConfigId(@Param("configId") Long configId);

    /**
     * 获取指定配置和指标的所有专家打分记录
     *
     * @param configId      配置ID
     * @param indicatorCode 指标代码
     * @return 打分记录列表
     */
    @Select("SELECT * FROM indicator_weight_score WHERE config_id = #{configId} AND indicator_code = #{indicatorCode} ORDER BY create_time DESC")
    List<IndicatorWeightScore> selectByConfigIdAndIndicatorCode(@Param("configId") Long configId,
                                                                  @Param("indicatorCode") String indicatorCode);

    /**
     * 计算指定配置下每个指标的平均权重值
     *
     * @param configId 配置ID
     * @return 指标代码和平均权重的映射列表
     */
    @Select("SELECT indicator_code as indicatorCode, AVG(weight) as avgWeight, COUNT(*) as scoreCount " +
            "FROM indicator_weight_score " +
            "WHERE config_id = #{configId} " +
            "GROUP BY indicator_code")
    List<Map<String, Object>> selectAverageWeightsByConfigId(@Param("configId") Long configId);

    /**
     * 获取指定配置下的所有专家列表（去重）
     *
     * @param configId 配置ID
     * @return 专家列表
     */
    @Select("SELECT expert_name, expert_phone, MAX(create_time) as create_time " +
            "FROM indicator_weight_score " +
            "WHERE config_id = #{configId} " +
            "GROUP BY expert_name, expert_phone " +
            "ORDER BY create_time DESC")
    List<Map<String, Object>> selectExpertsByConfigId(@Param("configId") Long configId);
}
