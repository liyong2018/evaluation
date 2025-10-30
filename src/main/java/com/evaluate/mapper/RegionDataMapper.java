package com.evaluate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evaluate.entity.RegionData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RegionDataMapper extends BaseMapper<RegionData> {

    /**
     * 根据父级编码获取子级地区列表
     * @param parentCode 父级编码
     * @return 地区列表
     */
    @Select("SELECT * FROM region_data WHERE parent_code = #{parentCode} AND status = 1 ORDER BY sort_order ASC, id ASC")
    List<RegionData> getByParentCode(@Param("parentCode") String parentCode);

    /**
     * 根据级别获取地区列表
     * @param level 级别
     * @return 地区列表
     */
    @Select("SELECT * FROM region_data WHERE level = #{level} AND status = 1 ORDER BY sort_order ASC, id ASC")
    List<RegionData> getByLevel(@Param("level") Integer level);

    /**
     * 获取所有省份（级别为1的地区）
     * @return 省份列表
     */
    @Select("SELECT * FROM region_data WHERE level = 1 AND status = 1 ORDER BY sort_order ASC, id ASC")
    List<RegionData> getProvinces();

    /**
     * 根据编码获取地区信息
     * @param code 地区编码
     * @return 地区信息
     */
    @Select("SELECT * FROM region_data WHERE code = #{code} AND status = 1")
    RegionData getByCode(@Param("code") String code);
}