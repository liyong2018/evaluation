package com.evaluate.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evaluate.common.Result;
import com.evaluate.entity.FirefighterConfig;
import com.evaluate.service.IFirefighterConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 消防员配置控制器
 *
 * @author System
 * @since 2025-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/firefighter-config")
@CrossOrigin(origins = "*")
public class FirefighterConfigController {

    @Autowired
    private IFirefighterConfigService firefighterConfigService;

    /**
     * 分页查询消防员配置列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getFirefighterConfigList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String regionCode,
            @RequestParam(required = false) String provinceName,
            @RequestParam(required = false) String cityName,
            @RequestParam(required = false) String countyName,
            @RequestParam(required = false) String townshipName,
            @RequestParam(required = false) Integer status
    ) {
        try {
            Page<FirefighterConfig> pageParam = new Page<>(page, size);
            QueryWrapper<FirefighterConfig> queryWrapper = new QueryWrapper<>();

            if (StringUtils.hasText(keyword)) {
                queryWrapper.and(wrapper -> wrapper
                        .like("province_name", keyword)
                        .or().like("city_name", keyword)
                        .or().like("county_name", keyword)
                        .or().like("township_name", keyword)
                        .or().like("region_code", keyword)
                );
            }
            // regionCode 前缀匹配（优先级高于名称过滤）
            if (StringUtils.hasText(regionCode)) {
                queryWrapper.likeRight("region_code", regionCode.trim());
            } else {
                // 名称精确过滤（仅在不使用 regionCode 时生效）
                if (StringUtils.hasText(provinceName)) {
                    queryWrapper.eq("province_name", provinceName);
                }
                if (StringUtils.hasText(cityName)) {
                    queryWrapper.eq("city_name", cityName);
                }
                if (StringUtils.hasText(countyName)) {
                    queryWrapper.eq("county_name", countyName);
                }
                if (StringUtils.hasText(townshipName)) {
                    queryWrapper.eq("township_name", townshipName);
                }
            }
            if (status != null) {
                queryWrapper.eq("status", status);
            }

            queryWrapper.orderByDesc("created_time");

            Page<FirefighterConfig> resultPage = firefighterConfigService.page(pageParam, queryWrapper);

            Map<String, Object> response = new java.util.HashMap<>();
            response.put("list", resultPage.getRecords());
            response.put("total", resultPage.getTotal());
            response.put("page", resultPage.getCurrent());
            response.put("size", resultPage.getSize());

            return Result.success(response);
        } catch (Exception e) {
            log.error("获取消防员配置列表失败", e);
            return Result.error("获取消防员配置列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有消防员配置（不分页）
     */
    @GetMapping
    public Result<List<FirefighterConfig>> getAllFirefighterConfigs(
            @RequestParam(required = false) String regionCode
    ) {
        try {
            QueryWrapper<FirefighterConfig> queryWrapper = new QueryWrapper<>();
            if (StringUtils.hasText(regionCode)) {
                queryWrapper.likeRight("region_code", regionCode.trim());
            }
            queryWrapper.orderByDesc("created_time");

            List<FirefighterConfig> list = firefighterConfigService.list(queryWrapper);
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取消防员配置列表失败", e);
            return Result.error("获取消防员配置列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询消防员配置
     */
    @GetMapping("/{id}")
    public Result<FirefighterConfig> getFirefighterConfigById(@PathVariable Long id) {
        try {
            FirefighterConfig config = firefighterConfigService.getById(id);
            if (config == null) {
                return Result.error("消防员配置不存在");
            }
            return Result.success(config);
        } catch (Exception e) {
            log.error("获取消防员配置详情失败", e);
            return Result.error("获取消防员配置详情失败: " + e.getMessage());
        }
    }

    /**
     * 根据行政区划代码查询
     */
    @GetMapping("/region/{regionCode}")
    public Result<FirefighterConfig> getByRegionCode(@PathVariable String regionCode) {
        try {
            QueryWrapper<FirefighterConfig> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("region_code", regionCode);
            FirefighterConfig config = firefighterConfigService.getOne(queryWrapper);
            if (config == null) {
                return Result.error("未找到该地区的消防员配置");
            }
            return Result.success(config);
        } catch (Exception e) {
            log.error("根据行政区划代码查询失败", e);
            return Result.error("根据行政区划代码查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据地理位置查询
     */
    @GetMapping("/location")
    public Result<List<FirefighterConfig>> getByLocation(
            @RequestParam(required = false) String provinceName,
            @RequestParam(required = false) String cityName,
            @RequestParam(required = false) String countyName,
            @RequestParam(required = false) String townshipName
    ) {
        try {
            List<FirefighterConfig> list = firefighterConfigService.getByLocation(
                    provinceName, cityName, countyName, townshipName
            );
            return Result.success(list);
        } catch (Exception e) {
            log.error("根据地理位置查询失败", e);
            return Result.error("根据地理位置查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据县名查询所有乡镇的消防员配置
     */
    @GetMapping("/county/{countyName}")
    public Result<List<FirefighterConfig>> getByCountyName(@PathVariable String countyName) {
        try {
            List<FirefighterConfig> list = firefighterConfigService.getByCountyName(countyName);
            return Result.success(list);
        } catch (Exception e) {
            log.error("根据县名查询失败", e);
            return Result.error("根据县名查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取某县的消防员总数
     */
    @GetMapping("/county/{countyName}/sum")
    public Result<Integer> sumFirefighterCountByCounty(@PathVariable String countyName) {
        try {
            Integer sum = firefighterConfigService.sumFirefighterCountByCounty(countyName);
            return Result.success(sum);
        } catch (Exception e) {
            log.error("获取县消防员总数失败", e);
            return Result.error("获取县消防员总数失败: " + e.getMessage());
        }
    }

    /**
     * 创建消防员配置
     */
    @PostMapping
    public Result<Boolean> createFirefighterConfig(@RequestBody FirefighterConfig config) {
        try {
            boolean result = firefighterConfigService.saveOrUpdateFirefighterConfig(config);
            return result ? Result.success(true) : Result.error("创建消防员配置失败");
        } catch (Exception e) {
            log.error("创建消防员配置失败", e);
            return Result.error("创建消防员配置失败: " + e.getMessage());
        }
    }

    /**
     * 更新消防员配置
     */
    @PutMapping("/update")
    public Result<Boolean> updateFirefighterConfig(@RequestBody FirefighterConfig config) {
        try {
            if (config.getId() == null) {
                return Result.error("配置ID不能为空");
            }
            boolean result = firefighterConfigService.saveOrUpdateFirefighterConfig(config);
            return result ? Result.success(true) : Result.error("更新消防员配置失败");
        } catch (Exception e) {
            log.error("更新消防员配置失败", e);
            return Result.error("更新消防员配置失败: " + e.getMessage());
        }
    }

    /**
     * 删除消防员配置
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteFirefighterConfig(@PathVariable Long id) {
        try {
            boolean result = firefighterConfigService.deleteById(id);
            return result ? Result.success(true) : Result.error("删除消防员配置失败");
        } catch (Exception e) {
            log.error("删除消防员配置失败", e);
            return Result.error("删除消防员配置失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除消防员配置
     */
    @DeleteMapping("/batch")
    public Result<Boolean> batchDelete(@RequestBody List<Long> ids) {
        try {
            boolean result = firefighterConfigService.removeByIds(ids);
            return result ? Result.success(true) : Result.error("批量删除失败");
        } catch (Exception e) {
            log.error("批量删除消防员配置失败", e);
            return Result.error("批量删除消防员配置失败: " + e.getMessage());
        }
    }

    /**
     * 更新状态
     */
    @PostMapping("/{id}/status")
    public Result<Boolean> updateStatus(
            @PathVariable Long id,
            @RequestParam Integer status
    ) {
        try {
            boolean result = firefighterConfigService.updateStatus(id, status);
            return result ? Result.success(true) : Result.error("更新状态失败");
        } catch (Exception e) {
            log.error("更新状态失败", e);
            return Result.error("更新状态失败: " + e.getMessage());
        }
    }

    /**
     * 检查是否有数据
     */
    @GetMapping("/has-data")
    public Result<Boolean> hasAnyData() {
        try {
            boolean hasData = firefighterConfigService.hasAnyData();
            return Result.success(hasData);
        } catch (Exception e) {
            log.error("检查数据失败", e);
            return Result.error("检查数据失败: " + e.getMessage());
        }
    }
}
