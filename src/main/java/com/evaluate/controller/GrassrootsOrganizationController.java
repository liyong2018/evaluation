package com.evaluate.controller;

import com.evaluate.common.Result;
import com.evaluate.entity.GrassrootsOrganization;
import com.evaluate.service.IGrassrootsOrganizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基层组织机构控制器（乡镇和社区）
 */
@Slf4j
@RestController
@RequestMapping("/api/grassroots-organization")
public class GrassrootsOrganizationController {

    @Autowired
    private IGrassrootsOrganizationService grassrootsOrganizationService;

    /**
     * 分页查询基层组织机构列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getGrassrootsOrganizationList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long countyId,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) Integer year) {
        log.info("查询基层组织机构列表，页码: {}, 每页大小: {}, 区县ID: {}, 机构编码: {}, 机构名称: {}, 级别: {}, 父级ID: {}, 年份: {}",
                page, size, countyId, code, name, level, parentId, year);
        try {
            Map<String, Object> result = grassrootsOrganizationService.getGrassrootsOrganizationList(
                    page, size, countyId, code, name, level, parentId, year);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询基层组织机构列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据区县ID获取乡镇列表
     */
    @GetMapping("/townships/by-county-id/{countyId}")
    public Result<List<GrassrootsOrganization>> getTownshipsByCountyId(
            @PathVariable Long countyId,
            @RequestParam(required = false) Integer year) {
        log.info("根据区县ID获取乡镇列表，区县ID: {}, 年份: {}", countyId, year);
        try {
            List<GrassrootsOrganization> townships = grassrootsOrganizationService.getTownshipsByCountyId(countyId, year);
            return Result.success(townships);
        } catch (Exception e) {
            log.error("根据区县ID获取乡镇列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据区县代码获取乡镇列表
     */
    @GetMapping("/townships/by-county-code")
    public Result<List<GrassrootsOrganization>> getTownshipsByCountyCode(
            @RequestParam String countyCode,
            @RequestParam(required = false) Integer year) {
        log.info("根据区县代码获取乡镇列表，区县代码: {}, 年份: {}", countyCode, year);
        try {
            List<GrassrootsOrganization> townships = grassrootsOrganizationService.getTownshipsByCountyCode(countyCode, year);
            return Result.success(townships);
        } catch (Exception e) {
            log.error("根据区县代码获取乡镇列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据乡镇ID获取社区列表
     */
    @GetMapping("/communities/by-township-id/{townshipId}")
    public Result<List<GrassrootsOrganization>> getCommunitiesByTownshipId(
            @PathVariable Long townshipId,
            @RequestParam(required = false) Integer year) {
        log.info("根据乡镇ID获取社区列表，乡镇ID: {}, 年份: {}", townshipId, year);
        try {
            List<GrassrootsOrganization> communities = grassrootsOrganizationService.getCommunitiesByTownshipId(townshipId, year);
            log.info("查询结果: 找到 {} 个社区", communities == null ? 0 : communities.size());
            return Result.success(communities);
        } catch (Exception e) {
            log.error("根据乡镇ID获取社区列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 调试接口：查看乡镇下的所有社区（不受年份限制）
     */
    @GetMapping("/debug/communities-by-township-id/{townshipId}")
    public Result<Map<String, Object>> debugGetCommunitiesByTownshipId(@PathVariable Long townshipId) {
        try {
            Map<String, Object> result = new HashMap<>();
            List<GrassrootsOrganization> allCommunities = grassrootsOrganizationService.debugGetCommunitiesByTownshipId(townshipId);
            result.put("townshipId", townshipId);
            result.put("communities", allCommunities);
            result.put("count", allCommunities == null ? 0 : allCommunities.size());
            return Result.success(result);
        } catch (Exception e) {
            log.error("调试查询失败", e);
            return Result.error("调试查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据乡镇代码获取社区列表
     */
    @GetMapping("/communities/by-township-code")
    public Result<List<GrassrootsOrganization>> getCommunitiesByTownshipCode(
            @RequestParam String townshipCode,
            @RequestParam(required = false) Integer year) {
        log.info("根据乡镇代码获取社区列表，乡镇代码: {}, 年份: {}", townshipCode, year);
        try {
            List<GrassrootsOrganization> communities = grassrootsOrganizationService.getCommunitiesByTownshipCode(townshipCode, year);
            return Result.success(communities);
        } catch (Exception e) {
            log.error("根据乡镇代码获取社区列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据区县ID获取树形结构
     */
    @GetMapping("/tree/by-county-id/{countyId}")
    public Result<List<Map<String, Object>>> getTreeByCountyId(
            @PathVariable Long countyId,
            @RequestParam(required = false) Integer year) {
        log.info("根据区县ID获取树形结构，区县ID: {}, 年份: {}", countyId, year);
        try {
            List<Map<String, Object>> tree = grassrootsOrganizationService.getTreeByCountyId(countyId, year);
            return Result.success(tree);
        } catch (Exception e) {
            log.error("根据区县ID获取树形结构失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据区县代码获取树形结构
     */
    @GetMapping("/tree/by-county-code")
    public Result<List<Map<String, Object>>> getTreeByCountyCode(
            @RequestParam String countyCode,
            @RequestParam(required = false) Integer year) {
        log.info("根据区县代码获取树形结构，区县代码: {}, 年份: {}", countyCode, year);
        try {
            List<Map<String, Object>> tree = grassrootsOrganizationService.getTreeByCountyCode(countyCode, year);
            return Result.success(tree);
        } catch (Exception e) {
            log.error("根据区县代码获取树形结构失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据编码获取基层组织机构
     */
    @GetMapping("/code/{code}")
    public Result<GrassrootsOrganization> getByCode(
            @PathVariable String code,
            @RequestParam(required = false) Integer year) {
        log.info("根据编码获取基层组织机构，编码: {}, 年份: {}", code, year);
        try {
            GrassrootsOrganization organization = grassrootsOrganizationService.getByCode(code, year);
            if (organization != null) {
                return Result.success(organization);
            } else {
                return Result.error("基层组织机构不存在");
            }
        } catch (Exception e) {
            log.error("根据编码获取基层组织机构失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 搜索基层组织机构
     */
    @GetMapping("/search")
    public Result<List<GrassrootsOrganization>> searchGrassrootsOrganizations(
            @RequestParam(required = false) Long countyId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) Integer year) {
        log.info("搜索基层组织机构，区县ID: {}, 关键词: {}, 级别: {}, 年份: {}", countyId, keyword, level, year);
        try {
            List<GrassrootsOrganization> result = grassrootsOrganizationService.searchGrassrootsOrganizations(
                    countyId, keyword, level, year);
            return Result.success(result);
        } catch (Exception e) {
            log.error("搜索基层组织机构失败", e);
            return Result.error("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 创建基层组织机构
     */
    @PostMapping
    public Result<Boolean> create(@RequestBody GrassrootsOrganization organization) {
        log.info("创建基层组织机构，编码: {}, 名称: {}", organization.getCode(), organization.getName());
        try {
            boolean success = grassrootsOrganizationService.createGrassrootsOrganization(organization);
            return success ? Result.success(true) : Result.error("创建失败");
        } catch (Exception e) {
            log.error("创建基层组织机构失败", e);
            return Result.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新基层组织机构
     */
    @PutMapping
    public Result<Boolean> update(@RequestBody GrassrootsOrganization organization) {
        log.info("更新基层组织机构，ID: {}", organization.getId());
        try {
            boolean success = grassrootsOrganizationService.updateGrassrootsOrganization(organization);
            return success ? Result.success(true) : Result.error("更新失败");
        } catch (Exception e) {
            log.error("更新基层组织机构失败", e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除基层组织机构（支持按年份删除）
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(
            @PathVariable Long id,
            @RequestParam(required = false) Integer year) {
        log.info("删除基层组织机构，ID: {}, 年份: {}", id, year);
        try {
            boolean success = grassrootsOrganizationService.deleteGrassrootsOrganization(id, year);
            return success ? Result.success(true) : Result.error("删除失败");
        } catch (Exception e) {
            log.error("删除基层组织机构失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除基层组织机构
     */
    @DeleteMapping("/batch")
    public Result<Boolean> batchDelete(@RequestBody List<Long> ids) {
        log.info("批量删除基层组织机构，IDs: {}", ids);
        try {
            boolean success = grassrootsOrganizationService.batchDeleteGrassrootsOrganizations(ids);
            return success ? Result.success(true) : Result.error("批量删除失败");
        } catch (Exception e) {
            log.error("批量删除基层组织机构失败", e);
            return Result.error("批量删除失败: " + e.getMessage());
        }
    }

    /**
     * 从上一年复制年度配置
     */
    @PostMapping("/copy-from-previous-year")
    public Result<Map<String, Object>> copyFromPreviousYear(@RequestParam("targetYear") Integer targetYear) {
        try {
            int count = grassrootsOrganizationService.copyFromPreviousYear(targetYear);
            Map<String, Object> data = new HashMap<>();
            data.put("targetYear", targetYear);
            data.put("sourceYear", targetYear != null ? targetYear - 1 : null);
            data.put("count", count);
            return Result.success(data);
        } catch (Exception e) {
            log.error("从上一年复制年度配置失败: targetYear={}", targetYear, e);
            return Result.error("复制失败: " + e.getMessage());
        }
    }

    /**
     * 删除区县的年度数据
     */
    @DeleteMapping("/year-data/{countyId}")
    public Result<Map<String, Object>> deleteYearDataByCountyId(
            @PathVariable Long countyId,
            @RequestParam("year") Integer year) {
        log.info("删除区县的年度数据，区县ID: {}, 年份: {}", countyId, year);
        try {
            Map<String, Object> result = grassrootsOrganizationService.deleteYearDataByCountyId(countyId, year);
            return Result.success(result);
        } catch (Exception e) {
            log.error("删除区县的年度数据失败", e);
            return Result.error("删除年度数据失败: " + e.getMessage());
        }
    }

    /**
     * 修复社区数据的parent_id（根据township_name匹配）
     */
    @PostMapping("/fix-parent-ids/{year}")
    public Result<Map<String, Object>> fixParentIds(@PathVariable Integer year) {
        try {
            log.info("开始修复{}年社区数据的parent_id", year);
            Map<String, Object> result = grassrootsOrganizationService.fixCommunityParentIds(year);
            return Result.success(result);
        } catch (Exception e) {
            log.error("修复parent_id失败", e);
            return Result.error("修复失败: " + e.getMessage());
        }
    }
}
