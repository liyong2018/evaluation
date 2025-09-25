package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.evaluate.entity.Region;
import com.evaluate.mapper.RegionMapper;
import com.evaluate.service.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 地区组织机构服务实现类
 */
@Service
public class RegionServiceImpl implements RegionService {
    
    @Autowired
    private RegionMapper regionMapper;
    
    @Override
    public List<Region> getRegionTree() {
        // 获取所有启用的地区
        List<Region> allRegions = regionMapper.selectAllEnabled();
        
        // 构建树形结构
        return buildRegionTree(allRegions, null);
    }
    
    @Override
    public List<Region> getRegionsByParentId(Long parentId) {
        return regionMapper.selectByParentId(parentId);
    }
    
    @Override
    public List<Region> getRegionsByLevel(Integer level) {
        return regionMapper.selectByLevel(level);
    }
    
    @Override
    public Region getRegionByCode(String code) {
        return regionMapper.selectByCode(code);
    }
    
    @Override
    public List<Region> getRegionsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        
        QueryWrapper<Region> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", ids)
                   .eq("status", 1)
                   .orderByAsc("level", "sort");
        
        return regionMapper.selectList(queryWrapper);
    }
    
    @Override
    public List<Region> getAllEnabledRegions() {
        return regionMapper.selectAllEnabled();
    }
    
    /**
     * 构建地区树形结构
     */
    private List<Region> buildRegionTree(List<Region> allRegions, Long parentId) {
        List<Region> result = new ArrayList<>();
        
        // 按父级ID分组
        Map<Long, List<Region>> regionMap = allRegions.stream()
                .collect(Collectors.groupingBy(region -> 
                    region.getParentId() == null ? 0L : region.getParentId()));
        
        // 获取指定父级的子节点
        Long targetParentId = parentId == null ? 0L : parentId;
        List<Region> children = regionMap.get(targetParentId);
        
        if (children != null) {
            for (Region region : children) {
                // 递归构建子树
                List<Region> subChildren = buildRegionTree(allRegions, region.getId());
                region.setChildren(subChildren);
                result.add(region);
            }
        }
        
        return result;
    }
}