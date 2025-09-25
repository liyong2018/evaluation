package com.evaluate.controller;

import com.evaluate.common.Result;
import com.evaluate.entity.AlgorithmConfig;
import com.evaluate.entity.SurveyData;
import com.evaluate.service.IAlgorithmConfigService;
import com.evaluate.service.AlgorithmExecutionService;
import com.evaluate.service.ISurveyDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 算法执行控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/algorithm/execution")
@CrossOrigin(origins = "*")
public class AlgorithmExecutionController {
    
    @Autowired
    private AlgorithmExecutionService algorithmExecutionService;
    
    @Autowired
    private IAlgorithmConfigService algorithmConfigService;
    
    @Autowired
    private ISurveyDataService surveyDataService;
    
    /**
     * 执行算法计算
     */
    @PostMapping("/execute")
    public Result<Map<String, Object>> executeAlgorithm(@RequestBody Map<String, Object> request) {
        try {
            Long algorithmId = Long.valueOf(request.get("algorithmId").toString());
            Long surveyId = request.get("surveyId") != null ? Long.valueOf(request.get("surveyId").toString()) : null;
            
            @SuppressWarnings("unchecked")
            List<Long> regionIds = (List<Long>) request.get("regionIds");
            
            @SuppressWarnings("unchecked")
            Map<String, Double> weightConfig = (Map<String, Double>) request.get("weightConfig");
            
            // 获取算法配置
            AlgorithmConfig algorithmConfig = algorithmConfigService.getById(algorithmId);
            if (algorithmConfig == null) {
                return Result.error("算法配置不存在");
            }
            
            // 获取调查数据
            List<SurveyData> surveyDataList;
            if (surveyId != null) {
                SurveyData surveyData = surveyDataService.getById(surveyId);
                if (surveyData == null) {
                    return Result.error("调查数据不存在");
                }
                surveyDataList = List.of(surveyData);
            } else {
                surveyDataList = surveyDataService.list();
            }
            
            // 执行算法
            Map<String, Object> result = algorithmExecutionService.executeAlgorithm(
                algorithmConfig, surveyDataList, weightConfig, regionIds
            );
            
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("执行算法失败", e);
            return Result.error("执行算法失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证算法参数
     */
    @PostMapping("/validate")
    public Result<Boolean> validateAlgorithmParams(@RequestBody Map<String, Object> request) {
        try {
            Long algorithmId = Long.valueOf(request.get("algorithmId").toString());
            
            @SuppressWarnings("unchecked")
            Map<String, Object> parameters = (Map<String, Object>) request.get("parameters");
            
            // 获取算法配置
            AlgorithmConfig algorithmConfig = algorithmConfigService.getById(algorithmId);
            if (algorithmConfig == null) {
                return Result.error("算法配置不存在");
            }
            
            // 验证参数
            boolean isValid = algorithmExecutionService.validateAlgorithmParams(algorithmConfig, parameters);
            
            return Result.success(isValid);
            
        } catch (Exception e) {
            log.error("验证算法参数失败", e);
            return Result.error("验证算法参数失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取算法执行进度
     */
    @GetMapping("/progress/{executionId}")
    public Result<Map<String, Object>> getExecutionProgress(@PathVariable String executionId) {
        try {
            Map<String, Object> progress = algorithmExecutionService.getExecutionProgress(executionId);
            
            if (progress == null) {
                return Result.error("执行记录不存在");
            }
            
            return Result.success(progress);
            
        } catch (Exception e) {
            log.error("获取执行进度失败", e);
            return Result.error("获取执行进度失败: " + e.getMessage());
        }
    }
    
    /**
     * 停止算法执行
     */
    @PostMapping("/stop/{executionId}")
    public Result<Boolean> stopExecution(@PathVariable String executionId) {
        try {
            boolean stopped = algorithmExecutionService.stopExecution(executionId);
            
            return stopped ? Result.success(true) : Result.error("停止执行失败");
            
        } catch (Exception e) {
            log.error("停止算法执行失败", e);
            return Result.error("停止算法执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取支持的算法类型
     */
    @GetMapping("/types")
    public Result<List<String>> getSupportedAlgorithmTypes() {
        try {
            List<String> types = algorithmExecutionService.getSupportedAlgorithmTypes();
            return Result.success(types);
            
        } catch (Exception e) {
            log.error("获取支持的算法类型失败", e);
            return Result.error("获取支持的算法类型失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量执行算法
     */
    @PostMapping("/batch")
    public Result<Map<String, Object>> batchExecuteAlgorithm(@RequestBody Map<String, Object> request) {
        try {
            Long algorithmId = Long.valueOf(request.get("algorithmId").toString());
            
            @SuppressWarnings("unchecked")
            List<Long> surveyIds = (List<Long>) request.get("surveyIds");
            
            @SuppressWarnings("unchecked")
            List<Long> regionIds = (List<Long>) request.get("regionIds");
            
            @SuppressWarnings("unchecked")
            Map<String, Double> weightConfig = (Map<String, Double>) request.get("weightConfig");
            
            // 获取算法配置
            AlgorithmConfig algorithmConfig = algorithmConfigService.getById(algorithmId);
            if (algorithmConfig == null) {
                return Result.error("算法配置不存在");
            }
            
            Map<String, Object> batchResult = new HashMap<>();
            batchResult.put("algorithmId", algorithmId);
            batchResult.put("totalTasks", surveyIds.size());
            batchResult.put("results", new HashMap<>());
            
            // 批量执行
            for (Long surveyId : surveyIds) {
                try {
                    SurveyData surveyData = surveyDataService.getById(surveyId);
                    if (surveyData != null) {
                        List<SurveyData> surveyDataList = List.of(surveyData);
                        Map<String, Object> result = algorithmExecutionService.executeAlgorithm(
                            algorithmConfig, surveyDataList, weightConfig, regionIds
                        );
                        
                        @SuppressWarnings("unchecked")
                        Map<String, Object> results = (Map<String, Object>) batchResult.get("results");
                        results.put(surveyId.toString(), result);
                    }
                } catch (Exception e) {
                    log.error("批量执行算法失败，surveyId: {}", surveyId, e);
                    
                    @SuppressWarnings("unchecked")
                    Map<String, Object> results = (Map<String, Object>) batchResult.get("results");
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("error", e.getMessage());
                    results.put(surveyId.toString(), errorResult);
                }
            }
            
            return Result.success(batchResult);
            
        } catch (Exception e) {
            log.error("批量执行算法失败", e);
            return Result.error("批量执行算法失败: " + e.getMessage());
        }
    }
    
    /**
     * 计算单个步骤结果
     */
    @PostMapping("/step/calculate")
    public Result<Map<String, Object>> calculateStepResult(@RequestBody Map<String, Object> request) {
        try {
            Long algorithmId = Long.valueOf(request.get("algorithmId").toString());
            Long stepId = Long.valueOf(request.get("stepId").toString());
            Integer stepIndex = Integer.valueOf(request.get("stepIndex").toString());
            String formula = (String) request.get("formula");
            
            @SuppressWarnings("unchecked")
            List<String> regionIds = (List<String>) request.get("regions");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> parameters = (Map<String, Object>) request.get("parameters");
            
            // 获取算法配置
            AlgorithmConfig algorithmConfig = algorithmConfigService.getById(algorithmId);
            if (algorithmConfig == null) {
                return Result.error("算法配置不存在");
            }
            
            // 计算步骤结果
            Map<String, Object> stepResult = algorithmExecutionService.calculateStepResult(
                algorithmConfig, stepId, stepIndex, formula, regionIds, parameters
            );
            
            return Result.success(stepResult);
            
        } catch (Exception e) {
            log.error("计算步骤结果失败", e);
            return Result.error("计算步骤结果失败: " + e.getMessage());
        }
    }
}