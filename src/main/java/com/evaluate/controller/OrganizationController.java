package com.evaluate.controller;

import com.evaluate.common.Result;
import com.evaluate.entity.Organization;
import com.evaluate.service.IOrganizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 组织机构控制器
 *
 * @author System
 * @since 2025-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/organization")
public class OrganizationController {

    @Autowired
    private IOrganizationService organizationService;

    /**
     * 分页查询组织机构列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getOrganizationList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) Long parentId) {
        log.info("查询组织机构列表，页码: {}, 每页大小: {}, 机构编码: {}, 机构名称: {}, 级别: {}, 父级ID: {}",
                page, size, code, name, level, parentId);
        try {
            Map<String, Object> result = organizationService.getOrganizationList(page, size, code, name, level, parentId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询组织机构列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取组织机构
     */
    @GetMapping("/{id}")
    public Result<Organization> getById(@PathVariable Long id) {
        log.info("根据ID获取组织机构，ID: {}", id);
        try {
            Organization organization = organizationService.getById(id);
            if (organization != null) {
                return Result.success(organization);
            } else {
                return Result.error("组织机构不存在");
            }
        } catch (Exception e) {
            log.error("根据ID获取组织机构失败，ID: {}", id, e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据编码获取组织机构
     */
    @GetMapping("/code/{code}")
    public Result<Organization> getByCode(@PathVariable String code) {
        log.info("根据编码获取组织机构，编码: {}", code);
        try {
            Organization organization = organizationService.getByCode(code);
            if (organization != null) {
                return Result.success(organization);
            } else {
                return Result.error("组织机构不存在");
            }
        } catch (Exception e) {
            log.error("根据编码获取组织机构失败，编码: {}", code, e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取组织机构树形结构
     */
    @GetMapping("/tree")
    public Result<List<Map<String, Object>>> getOrganizationTree(
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) Integer maxLevel) {
        log.info("获取组织机构树形结构，父级ID: {}, 最大层级: {}", parentId, maxLevel);
        try {
            List<Map<String, Object>> tree = organizationService.getOrganizationTree(parentId, maxLevel);
            return Result.success(tree);
        } catch (Exception e) {
            log.error("获取组织机构树形结构失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据父级ID获取子级组织机构
     */
    @GetMapping("/children/{parentId}")
    public Result<List<Organization>> getChildrenByParentId(@PathVariable Long parentId) {
        log.info("根据父级ID获取子级组织机构，父级ID: {}", parentId);
        try {
            List<Organization> children = organizationService.getChildrenByParentId(parentId);
            return Result.success(children);
        } catch (Exception e) {
            log.error("根据父级ID获取子级组织机构失败，父级ID: {}", parentId, e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 搜索组织机构
     */
    @GetMapping("/search")
    public Result<List<Organization>> searchOrganization(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer level) {
        log.info("搜索组织机构，关键词: {}, 级别: {}", keyword, level);
        try {
            List<Organization> result = organizationService.searchOrganization(keyword, level);
            return Result.success(result);
        } catch (Exception e) {
            log.error("搜索组织机构失败", e);
            return Result.error("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 获取省级组织机构列表
     */
    @GetMapping("/provinces")
    public Result<List<Organization>> getProvinces() {
        log.info("获取省级组织机构列表");
        try {
            List<Organization> provinces = organizationService.getOrganizationsByLevel(1);
            return Result.success(provinces);
        } catch (Exception e) {
            log.error("获取省级组织机构列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取市级组织机构列表
     */
    @GetMapping("/cities")
    public Result<List<Organization>> getCities(@RequestParam(required = false) String provinceCode) {
        log.info("获取市级组织机构列表，省编码: {}", provinceCode);
        try {
            List<Organization> cities = organizationService.getCitiesByProvinceCode(provinceCode);
            return Result.success(cities);
        } catch (Exception e) {
            log.error("获取市级组织机构列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取县级组织机构列表
     */
    @GetMapping("/counties")
    public Result<List<Organization>> getCounties(@RequestParam(required = false) String cityCode) {
        log.info("获取县级组织机构列表，市编码: {}", cityCode);
        try {
            List<Organization> counties = organizationService.getCountiesByCityCode(cityCode);
            return Result.success(counties);
        } catch (Exception e) {
            log.error("获取县级组织机构列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取乡镇级组织机构列表
     */
    @GetMapping("/townships")
    public Result<List<Organization>> getTownships(@RequestParam(required = false) String countyCode) {
        log.info("获取乡镇级组织机构列表，县编码: {}", countyCode);
        try {
            List<Organization> townships = organizationService.getTownshipsByCountyCode(countyCode);
            return Result.success(townships);
        } catch (Exception e) {
            log.error("获取乡镇级组织机构列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取社区级组织机构列表
     */
    @GetMapping("/communities")
    public Result<List<Organization>> getCommunities(@RequestParam(required = false) String townshipCode) {
        log.info("获取社区级组织机构列表，乡镇编码: {}", townshipCode);
        try {
            List<Organization> communities = organizationService.getCommunitiesByTownshipCode(townshipCode);
            return Result.success(communities);
        } catch (Exception e) {
            log.error("获取社区级组织机构列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }
}
