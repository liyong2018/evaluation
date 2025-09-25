package com.evaluate.controller;

import com.evaluate.common.Result;
import com.evaluate.entity.Region;
import com.evaluate.service.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 地区组织机构控制器
 */
@RestController
@RequestMapping("/api/region")
@CrossOrigin(origins = "*")
public class RegionController {
    
    @Autowired
    private RegionService regionService;
    
    /**
     * 获取地区树形结构
     */
    @GetMapping("/tree")
    public Result<List<Region>> getRegionTree() {
        try {
            List<Region> regionTree = regionService.getRegionTree();
            return Result.success(regionTree);
        } catch (Exception e) {
            return Result.error("获取地区树形结构失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据父级ID获取子级地区
     */
    @GetMapping("/children/{parentId}")
    public Result<List<Region>> getRegionsByParentId(@PathVariable Long parentId) {
        try {
            List<Region> regions = regionService.getRegionsByParentId(parentId);
            return Result.success(regions);
        } catch (Exception e) {
            return Result.error("获取子级地区失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据级别获取地区列表
     */
    @GetMapping("/level/{level}")
    public Result<List<Region>> getRegionsByLevel(@PathVariable Integer level) {
        try {
            List<Region> regions = regionService.getRegionsByLevel(level);
            return Result.success(regions);
        } catch (Exception e) {
            return Result.error("获取指定级别地区失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据地区代码获取地区信息
     */
    @GetMapping("/code/{code}")
    public Result<Region> getRegionByCode(@PathVariable String code) {
        try {
            Region region = regionService.getRegionByCode(code);
            return Result.success(region);
        } catch (Exception e) {
            return Result.error("获取地区信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据地区ID列表获取地区信息
     */
    @PostMapping("/batch")
    public Result<List<Region>> getRegionsByIds(@RequestBody List<Long> ids) {
        try {
            List<Region> regions = regionService.getRegionsByIds(ids);
            return Result.success(regions);
        } catch (Exception e) {
            return Result.error("批量获取地区信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有启用的地区
     */
    @GetMapping("/all")
    public Result<List<Region>> getAllEnabledRegions() {
        try {
            List<Region> regions = regionService.getAllEnabledRegions();
            return Result.success(regions);
        } catch (Exception e) {
            return Result.error("获取所有地区失败: " + e.getMessage());
        }
    }
}