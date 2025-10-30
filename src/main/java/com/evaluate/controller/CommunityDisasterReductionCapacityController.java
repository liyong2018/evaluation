package com.evaluate.controller;

import com.evaluate.common.Result;
import com.evaluate.entity.CommunityDisasterReductionCapacity;
import com.evaluate.service.ICommunityDisasterReductionCapacityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 社区行政村减灾能力控制器
 *
 * @author System
 * @since 2025-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/community-capacity")
public class CommunityDisasterReductionCapacityController {

    @Autowired
    private ICommunityDisasterReductionCapacityService communityDisasterReductionCapacityService;

    /**
     * 导入社区行政村减灾能力数据
     */
    @PostMapping("/import")
    public Result<Map<String, Object>> importCommunityCapacityData(
            @RequestParam("file") MultipartFile file) {
        log.info("开始导入社区行政村减灾能力数据，文件名: {}", file.getOriginalFilename());
        try {
            Map<String, Object> result = communityDisasterReductionCapacityService.importCommunityCapacityData(file);
            return Result.success(result);
        } catch (Exception e) {
            log.error("导入社区行政村减灾能力数据失败", e);
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询社区行政村减灾能力数据
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getCommunityCapacityList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String regionCode,
            @RequestParam(required = false) String communityName) {
        log.info("查询社区行政村减灾能力数据列表，页码: {}, 每页大小: {}, 行政区代码: {}, 社区名称: {}",
                page, size, regionCode, communityName);
        try {
            Map<String, Object> result = communityDisasterReductionCapacityService.getCommunityCapacityList(
                    page, size, regionCode, communityName);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询社区行政村减灾能力数据列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 搜索社区行政村减灾能力数据
     */
    @GetMapping("/search")
    public Result<List<CommunityDisasterReductionCapacity>> searchCommunityCapacity(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String regionCode,
            @RequestParam(required = false) String communityName) {
        log.info("搜索社区行政村减灾能力数据，关键词: {}, 行政区代码: {}, 社区名称: {}",
                keyword, regionCode, communityName);
        try {
            List<CommunityDisasterReductionCapacity> result = communityDisasterReductionCapacityService.searchCommunityCapacity(
                    keyword, regionCode, communityName);
            return Result.success(result);
        } catch (Exception e) {
            log.error("搜索社区行政村减灾能力数据失败", e);
            return Result.error("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取社区行政村减灾能力数据
     */
    @GetMapping("/{id}")
    public Result<CommunityDisasterReductionCapacity> getById(@PathVariable Long id) {
        log.info("根据ID获取社区行政村减灾能力数据，ID: {}", id);
        try {
            CommunityDisasterReductionCapacity data = communityDisasterReductionCapacityService.getById(id);
            if (data != null) {
                return Result.success(data);
            } else {
                return Result.error("数据不存在");
            }
        } catch (Exception e) {
            log.error("根据ID获取社区行政村减灾能力数据失败，ID: {}", id, e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 更新社区行政村减灾能力数据
     */
    @PutMapping("/{id}")
    public Result<Boolean> updateById(@PathVariable Long id, @RequestBody CommunityDisasterReductionCapacity data) {
        log.info("更新社区行政村减灾能力数据，ID: {}", id);
        try {
            data.setId(id);
            boolean result = communityDisasterReductionCapacityService.updateById(data);
            if (result) {
                return Result.success(true);
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            log.error("更新社区行政村减灾能力数据失败，ID: {}", id, e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除社区行政村减灾能力数据
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteById(@PathVariable Long id) {
        log.info("删除社区行政村减灾能力数据，ID: {}", id);
        try {
            boolean result = communityDisasterReductionCapacityService.deleteById(id);
            if (result) {
                return Result.success(true);
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除社区行政村减灾能力数据失败，ID: {}", id, e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除社区行政村减灾能力数据
     */
    @DeleteMapping("/batch")
    public Result<Boolean> deleteByIds(@RequestBody List<Long> ids) {
        log.info("批量删除社区行政村减灾能力数据，IDs: {}", ids);
        try {
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要删除的数据");
            }
            boolean result = communityDisasterReductionCapacityService.deleteByIds(ids);
            if (result) {
                return Result.success(true);
            } else {
                return Result.error("批量删除失败");
            }
        } catch (Exception e) {
            log.error("批量删除社区行政村减灾能力数据失败，IDs: {}", ids, e);
            return Result.error("批量删除失败: " + e.getMessage());
        }
    }

    /**
     * 下载社区行政村减灾能力数据导入模板
     */
    @GetMapping("/template")
    public Result<String> downloadTemplate() {
        log.info("下载社区行政村减灾能力数据导入模板");
        try {
            // 这里可以返回模板文件的下载链接
            String templateUrl = "/templates/community-capacity-template.xlsx";
            return Result.success(templateUrl);
        } catch (Exception e) {
            log.error("下载社区行政村减灾能力数据导入模板失败", e);
            return Result.error("下载模板失败: " + e.getMessage());
        }
    }
}