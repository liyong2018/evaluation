package com.evaluate.controller;

import com.evaluate.entity.EvaluationResult;
import com.evaluate.service.EvaluationResultService;
import com.evaluate.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 专题图控制器
 *
 * @author System
 * @since 2025-12-23
 */
@Slf4j
@RestController
@RequestMapping("/api/thematic-map")
@CrossOrigin(origins = "*")
public class ThematicMapController {

    @Autowired
    private EvaluationResultService evaluationResultService;

    /**
     * 获取专题图数据
     */
    @GetMapping("/data")
    public Result<List<Map<String, Object>>> getThematicData(
            @RequestParam(required = false) Long reportId,
            @RequestParam(required = false) Long surveyId,
            @RequestParam(required = false) Long algorithmId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String orgCode) {
        
        log.info("获取专题图数据: reportId={}, surveyId={}, algorithmId={}, year={}, orgCode={}", 
                reportId, surveyId, algorithmId, year, orgCode);
        
        try {
            // 获取所有评估结果
            List<EvaluationResult> allResults = evaluationResultService.getAllEvaluationResults();
            
            // 过滤数据
            List<EvaluationResult> filteredResults = allResults.stream()
                .filter(result -> {
                    // 按年份过滤 (如果结果中有年份字段)
                    // 目前EvaluationResult没有显式的年份字段，暂不按年份过滤，或者假设createTime年份
                    if (year != null && result.getCreateTime() != null) {
                         if (result.getCreateTime().getYear() != year) {
                             return false;
                         }
                    }
                    
                    // 按组织机构代码过滤
                    if (orgCode != null && !orgCode.isEmpty()) {
                        if (result.getOrgCode() != null && !result.getOrgCode().startsWith(orgCode)) {
                            // 简单的通过前缀匹配，或者精确匹配
                            // return false;
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
                
            // 转换为前端需要的格式
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (int i = 0; i < filteredResults.size(); i++) {
                EvaluationResult item = filteredResults.get(i);
                Map<String, Object> map = new HashMap<>();
                
                map.put("regionId", item.getId());
                map.put("regionName", item.getRegionName());
                map.put("county", "青神县"); // 默认为青神县，实际应从地区信息获取
                
                // 分数处理
                BigDecimal totalScore = item.getComprehensiveCapabilityScore();
                map.put("score", totalScore);
                map.put("totalScore", totalScore);
                
                // 能力等级
                map.put("capabilityLevel", item.getComprehensiveCapabilityLevel());
                
                // 详细能力分
                Map<String, Object> details = new HashMap<>();
                details.put("disasterPreventionCapability", item.getManagementCapabilityScore());
                details.put("emergencyResponseCapability", item.getSupportCapabilityScore());
                details.put("recoveryReconstructionCapability", item.getSelfRescueCapabilityScore());
                map.put("details", details);
                
                resultList.add(map);
            }
            
            return Result.success(resultList);
            
        } catch (Exception e) {
            log.error("获取专题图数据失败", e);
            return Result.error("获取专题图数据失败: " + e.getMessage());
        }
    }

    /**
     * 保存专题图图片
     */
    @PostMapping("/save-image")
    public Result<String> saveMapImage(@RequestBody Map<String, Object> data) {
        log.info("保存专题图图片: title={}", data.get("title"));
        // 实际保存逻辑待实现，目前仅返回成功
        return Result.success("保存成功");
    }
}
