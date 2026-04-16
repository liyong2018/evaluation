package com.evaluate.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evaluate.entity.FamilyDisasterReductionCapacity;
import com.evaluate.common.Result;
import com.evaluate.service.IFamilyDisasterReductionCapacityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 家庭减灾能力控制器
 *
 * @author System
 * @since 2025-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/family-capacity")
public class FamilyDisasterReductionCapacityController {

    @Autowired
    private IFamilyDisasterReductionCapacityService familyDisasterReductionCapacityService;

    /**
     * 分页查询家庭减灾能力数据
     */
    @GetMapping("/list")
    public Result<Page<FamilyDisasterReductionCapacity>> getList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestParam(value = "orgCode", required = false) String orgCode,
            @RequestParam(value = "year", required = false) Integer year) {
        Page<FamilyDisasterReductionCapacity> pageParam = new Page<>(page, size);
        QueryWrapper<FamilyDisasterReductionCapacity> queryWrapper = new QueryWrapper<>();
        
        if (StringUtils.hasText(orgCode)) {
            queryWrapper.likeRight("region_code", orgCode);
        }
        if (year != null) {
            queryWrapper.eq("year", year);
        }
        
        Page<FamilyDisasterReductionCapacity> result = familyDisasterReductionCapacityService.page(pageParam, queryWrapper);
        return Result.success(result);
    }

    /**
     * 批量导入家庭减灾能力数据
     *
     * @param file Excel文件
     * @return 导入结果
     */
    @PostMapping("/import")
    public Result<Map<String, Object>> importFamilyCapacityData(@RequestParam("file") MultipartFile file) {
        log.info("导入家庭减灾能力数据，文件名: {}", file != null ? file.getOriginalFilename() : "null");
        
        if (file == null || file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        
        try {
            Map<String, Object> result = familyDisasterReductionCapacityService.importFamilyCapacityData(file);
            if ((Boolean) result.getOrDefault("success", false)) {
                return Result.success(result);
            } else {
                return Result.error((String) result.getOrDefault("message", "导入失败"));
            }
        } catch (Exception e) {
            log.error("导入家庭减灾能力数据异常", e);
            return Result.error("导入异常：" + e.getMessage());
        }
    }
}
