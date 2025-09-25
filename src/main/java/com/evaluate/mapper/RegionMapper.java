package com.evaluate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evaluate.entity.Region;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 地区组织机构Mapper接口
 */
@Mapper
public interface RegionMapper extends BaseMapper<Region> {
    
    /**
     * 根据父级ID查询子级地区
     */
    @Select("SELECT * FROM region WHERE parent_id = #{parentId} AND status = 1 ORDER BY sort ASC")
    List<Region> selectByParentId(Long parentId);
    
    /**
     * 根据级别查询地区
     */
    @Select("SELECT * FROM region WHERE level = #{level} AND status = 1 ORDER BY sort ASC")
    List<Region> selectByLevel(Integer level);
    
    /**
     * 查询所有启用的地区
     */
    @Select("SELECT * FROM region WHERE status = 1 ORDER BY level ASC, sort ASC")
    List<Region> selectAllEnabled();
    
    /**
     * 根据地区代码查询
     */
    @Select("SELECT * FROM region WHERE code = #{code} AND status = 1")
    Region selectByCode(String code);
}