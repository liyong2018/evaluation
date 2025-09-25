package com.evaluate.service;

import com.evaluate.entity.Region;

import java.util.List;

/**
 * 地区组织机构服务接口
 */
public interface RegionService {
    
    /**
     * 获取所有地区树形结构
     */
    List<Region> getRegionTree();
    
    /**
     * 根据父级ID获取子级地区
     */
    List<Region> getRegionsByParentId(Long parentId);
    
    /**
     * 根据级别获取地区列表
     */
    List<Region> getRegionsByLevel(Integer level);
    
    /**
     * 根据地区代码获取地区信息
     */
    Region getRegionByCode(String code);
    
    /**
     * 根据地区ID列表获取地区信息
     */
    List<Region> getRegionsByIds(List<Long> ids);
    
    /**
     * 获取所有启用的地区
     */
    List<Region> getAllEnabledRegions();
}