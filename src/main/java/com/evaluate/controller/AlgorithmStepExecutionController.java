package com.evaluate.controller;

import com.evaluate.common.Result;
import com.evaluate.entity.AlgorithmConfig;
import com.evaluate.service.AlgorithmManagementService;
import com.evaluate.service.ModelExecutionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 算法步骤执行控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/algorithm-step-execution")
@CrossOrigin(origins = "*")
public class AlgorithmStepExecutionController {

    @Autowired
    private ModelExecutionService modelExecutionService;

    @Autowired
    private AlgorithmManagementService algorithmManagementService;

    /**
     * 获取算法的所有步骤信息（用于显示步骤按钮）
     */
    @GetMapping("/{algorithmId}/steps")
    public Result<Map<String, Object>> getAlgorithmSteps(@PathVariable("algorithmId") Long algorithmId) {
        log.info("获取算法步骤列表, algorithmId={}", algorithmId);
        try {
            Map<String, Object> stepsInfo = modelExecutionService.getAlgorithmStepsInfo(algorithmId);
            return Result.success(stepsInfo);
        } catch (Exception e) {
            log.error("获取算法步骤列表失败", e);
            return Result.error("获取算法步骤列表失败: " + e.getMessage());
        }
    }

    /**
     * 执行指定步骤并返回2D表格结果
     */
    @PostMapping("/{algorithmId}/step/{stepOrder}/execute")
    public Result<Map<String, Object>> executeStep(
            @PathVariable("algorithmId") Long algorithmId,
            @PathVariable("stepOrder") Integer stepOrder,
            @RequestBody Map<String, Object> request) {
        log.info("执行算法步骤, algorithmId={}, stepOrder={}", algorithmId, stepOrder);
        try {
            @SuppressWarnings("unchecked")
            List<String> regionCodes = (List<String>) request.get("regionCodes");
            Long weightConfigId = request.get("weightConfigId") != null
                    ? Long.valueOf(request.get("weightConfigId").toString()) : null;
            Integer year = null;
            if (request.get("year") != null) {
                try { year = Integer.valueOf(request.get("year").toString()); } catch (Exception ignore) {}
            }

            if (regionCodes == null || regionCodes.isEmpty()) {
                return Result.error("地区代码列表不能为空");
            }
            if (stepOrder == null || stepOrder <= 0) {
                return Result.error("步骤顺序必须是正整数");
            }

            Map<String, Object> result = modelExecutionService.executeAlgorithmStep(
                    algorithmId, stepOrder, regionCodes, weightConfigId, year);
            return Result.success(result);
        } catch (Exception e) {
            log.error("执行算法步骤失败", e);
            return Result.error("执行算法步骤失败: " + e.getMessage());
        }
    }

    /**
     * 执行多个步骤直到指定步骤
     */
    @PostMapping("/{algorithmId}/steps/execute-up-to/{upToStepOrder}")
    public Result<Map<String, Object>> executeStepsUpTo(
            @PathVariable("algorithmId") Long algorithmId,
            @PathVariable("upToStepOrder") Integer upToStepOrder,
            @RequestBody Map<String, Object> request) {
        log.info("批量执行算法步骤到第{}步, algorithmId={}", upToStepOrder, algorithmId);
        try {
            @SuppressWarnings("unchecked")
            List<String> regionCodes = (List<String>) request.get("regionCodes");
            Long weightConfigId = request.get("weightConfigId") != null
                    ? Long.valueOf(request.get("weightConfigId").toString()) : null;
            Integer year = null;
            if (request.get("year") != null) {
                try { year = Integer.valueOf(request.get("year").toString()); } catch (Exception ignore) {}
            }

            if (regionCodes == null || regionCodes.isEmpty()) {
                return Result.error("地区代码列表不能为空");
            }
            if (upToStepOrder == null || upToStepOrder <= 0) {
                return Result.error("步骤顺序必须是正整数");
            }

            Map<String, Object> result = modelExecutionService.executeAlgorithmStepsUpTo(
                    algorithmId, upToStepOrder, regionCodes, weightConfigId, year);
            return Result.success(result);
        } catch (Exception e) {
            log.error("批量执行算法步骤失败", e);
            return Result.error("批量执行算法步骤失败: " + e.getMessage());
        }
    }

    /**
     * 获取算法详细信息（包括步骤和公式）
     */
    @GetMapping("/{algorithmId}/detail")
    public Result<Map<String, Object>> getAlgorithmDetail(@PathVariable("algorithmId") Long algorithmId) {
        log.info("获取算法详细信息, algorithmId={}", algorithmId);
        try {
            Map<String, Object> detail = algorithmManagementService.getAlgorithmDetail(algorithmId);
            return Result.success(detail);
        } catch (Exception e) {
            log.error("获取算法详细信息失败", e);
            return Result.error("获取算法详细信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取算法列表（用于选择算法）
     */
    @GetMapping("/algorithms")
    public Result<List<Map<String, Object>>> getAlgorithmList() {
        log.info("获取算法列表");
        try {
            List<AlgorithmConfig> algorithms = algorithmManagementService.getAlgorithmList();
            List<Map<String, Object>> simplifiedList = algorithms.stream().map(algorithm -> {
                Map<String, Object> item = new java.util.HashMap<>();
                item.put("id", algorithm.getId());
                item.put("name", algorithm.getConfigName());
                item.put("description", algorithm.getDescription());
                item.put("version", algorithm.getVersion());
                item.put("status", algorithm.getStatus());
                item.put("createTime", algorithm.getCreateTime());
                return item;
            }).collect(java.util.stream.Collectors.toList());
            return Result.success(simplifiedList);
        } catch (Exception e) {
            log.error("获取算法列表失败", e);
            return Result.error("获取算法列表失败: " + e.getMessage());
        }
    }
}

