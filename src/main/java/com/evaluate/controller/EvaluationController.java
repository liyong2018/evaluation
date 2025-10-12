package com.evaluate.controller;

import com.evaluate.common.Result;
import com.evaluate.service.IEvaluationService;
import com.evaluate.service.ModelExecutionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 评估计算控制器
 * 
 * @author System
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/evaluation")
public class EvaluationController {

    @Autowired
    private IEvaluationService evaluationService;

    @Autowired
    private ModelExecutionService modelExecutionService;

    @PostMapping("/calculate")
    public Result<Map<String, Object>> performEvaluation(
            @RequestParam Long surveyId,
            @RequestParam Long algorithmId,
            @RequestParam Long weightConfigId) {
        log.info("开始评估计算，surveyId: {}, algorithmId: {}, weightConfigId: {}", surveyId, algorithmId, weightConfigId);
        try {
            if (!evaluationService.validateEvaluationParams(surveyId, algorithmId, weightConfigId)) {
                log.error("评估参数验证失败");
                return Result.error("评估参数验证失败");
            }
            
            Map<String, Object> result = evaluationService.performEvaluation(surveyId, algorithmId, weightConfigId);
            log.info("评估计算成功，结果: {}", result);
            return Result.success(result);
        } catch (Exception e) {
            log.error("执行评估计算失败", e);
            return Result.error("执行评估计算失败: " + e.getMessage());
        }
    }

    @PostMapping("/recalculate")
    public Result<Map<String, Object>> recalculateEvaluation(
            @RequestParam Long surveyId,
            @RequestParam Long algorithmId,
            @RequestParam Long weightConfigId) {
        try {
            Map<String, Object> result = evaluationService.recalculateEvaluation(surveyId, algorithmId, weightConfigId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("重新计算评估结果失败", e);
            return Result.error("重新计算评估结果失败: " + e.getMessage());
        }
    }

    @PostMapping("/batch")
    public Result<List<Map<String, Object>>> batchEvaluation(
            @RequestBody List<Long> surveyIds,
            @RequestParam Long algorithmId,
            @RequestParam Long weightConfigId) {
        try {
            List<Map<String, Object>> results = evaluationService.batchEvaluation(surveyIds, algorithmId, weightConfigId);
            return Result.success(results);
        } catch (Exception e) {
            log.error("批量评估计算失败", e);
            return Result.error("批量评估计算失败: " + e.getMessage());
        }
    }

    @GetMapping("/process")
    public Result<Map<String, Object>> getAlgorithmProcessData(
            @RequestParam Long surveyId,
            @RequestParam Long algorithmId,
            @RequestParam Long weightConfigId) {
        try {
            Map<String, Object> processData = evaluationService.getAlgorithmProcessData(surveyId, algorithmId, weightConfigId);
            return Result.success(processData);
        } catch (Exception e) {
            log.error("获取算法过程数据失败", e);
            return Result.error("获取算法过程数据失败: " + e.getMessage());
        }
    }

    @GetMapping("/history/{surveyId}")
    public Result<List<Map<String, Object>>> getEvaluationHistory(@PathVariable Long surveyId) {
        try {
            List<Map<String, Object>> history = evaluationService.getEvaluationHistory(surveyId);
            return Result.success(history);
        } catch (Exception e) {
            log.error("获取评估历史记录失败", e);
            return Result.error("获取评估历史记录失败: " + e.getMessage());
        }
    }

    @PostMapping("/validate")
    public Result<Boolean> validateEvaluationParams(
            @RequestParam Long surveyId,
            @RequestParam Long algorithmId,
            @RequestParam Long weightConfigId) {
        try {
            boolean result = evaluationService.validateEvaluationParams(surveyId, algorithmId, weightConfigId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("验证评估参数失败", e);
            return Result.error("验证评估参数失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/results")
    public Result<Boolean> deleteEvaluationResults(
            @RequestParam Long surveyId,
            @RequestParam Long algorithmId,
            @RequestParam Long weightConfigId) {
        try {
            boolean result = evaluationService.deleteEvaluationResults(surveyId, algorithmId, weightConfigId);
            return result ? Result.success(true) : Result.error("删除评估结果失败");
        } catch (Exception e) {
            log.error("删除评估结果失败", e);
            return Result.error("删除评估结果失败: " + e.getMessage());
        }
    }

    /**
     * 执行评估模型（基于模型配置）
     * 
     * @param modelId 模型ID
     * @param regionCodes 地区代码列表
     * @param weightConfigId 权重配置ID
     * @return 执行结果
     */
    @PostMapping("/execute-model")
    public Result<Map<String, Object>> executeModel(
            @RequestParam Long modelId,
            @RequestBody List<String> regionCodes,
            @RequestParam Long weightConfigId) {
        log.info("开始执行评估模型, modelId={}, regionCodes={}, weightConfigId={}", 
                modelId, regionCodes, weightConfigId);
        try {
            Map<String, Object> result = modelExecutionService.executeModel(modelId, regionCodes, weightConfigId);
            log.info("评估模型执行成功");
            return Result.success(result);
        } catch (Exception e) {
            log.error("执行评估模型失败", e);
            return Result.error("执行评估模型失败: " + e.getMessage());
        }
    }

    /**
     * 生成评估结果二维表
     * 
     * @param executionResults 执行结果
     * @return 二维表数据
     */
    @PostMapping("/generate-table")
    public Result<List<Map<String, Object>>> generateResultTable(
            @RequestBody Map<String, Object> executionResults) {
        log.info("开始生成结果二维表");
        try {
            List<Map<String, Object>> tableData = modelExecutionService.generateResultTable(executionResults);
            log.info("结果二维表生成成功，共 {} 行数据", tableData.size());
            return Result.success(tableData);
        } catch (Exception e) {
            log.error("生成结果二维表失败", e);
            return Result.error("生成结果二维表失败: " + e.getMessage());
        }
    }

    /**
     * 获取算法步骤信息
     * 
     * @param algorithmId 算法ID
     * @return 算法步骤列表信息
     */
    @GetMapping("/algorithm/{algorithmId}/steps-info")
    public Result<Map<String, Object>> getAlgorithmStepsInfo(@PathVariable Long algorithmId) {
        log.info("获取算法步骤信息, algorithmId={}", algorithmId);
        try {
            Map<String, Object> stepsInfo = modelExecutionService.getAlgorithmStepsInfo(algorithmId);
            return Result.success(stepsInfo);
        } catch (Exception e) {
            log.error("获取算法步骤信息失败", e);
            return Result.error("获取算法步骤信息失败: " + e.getMessage());
        }
    }

    /**
     * 执行算法的单个步骤并返回2D表格结果
     * 
     * @param algorithmId 算法ID
     * @param stepOrder 步骤顺序（从1开始）
     * @param request 请求体，包含regionCodes和weightConfigId
     * @return 步骤执行结果，包含2D表格数据
     */
    @PostMapping("/algorithm/{algorithmId}/step/{stepOrder}/execute")
    public Result<Map<String, Object>> executeAlgorithmStep(
            @PathVariable Long algorithmId,
            @PathVariable Integer stepOrder,
            @RequestBody Map<String, Object> request) {
        log.info("执行算法步骤, algorithmId={}, stepOrder={}", algorithmId, stepOrder);
        try {
            @SuppressWarnings("unchecked")
            List<String> regionCodes = (List<String>) request.get("regionCodes");
            Long weightConfigId = request.get("weightConfigId") != null 
                    ? Long.valueOf(request.get("weightConfigId").toString()) : null;
                    
            if (regionCodes == null || regionCodes.isEmpty()) {
                return Result.error("地区代码列表不能为空");
            }
            
            Map<String, Object> result = modelExecutionService.executeAlgorithmStep(
                    algorithmId, stepOrder, regionCodes, weightConfigId);
            log.info("算法步骤执行成功");
            return Result.success(result);
        } catch (Exception e) {
            log.error("执行算法步骤失败", e);
            return Result.error("执行算法步骤失败: " + e.getMessage());
        }
    }

    /**
     * 批量执行算法步骤（直到指定步骤）
     * 
     * @param algorithmId 算法ID
     * @param upToStepOrder 执行到第几步（包含该步骤）
     * @param request 请求体，包含regionCodes和weightConfigId
     * @return 所有已执行步骤的结果
     */
    @PostMapping("/algorithm/{algorithmId}/steps-up-to/{upToStepOrder}/execute")
    public Result<Map<String, Object>> executeAlgorithmStepsUpTo(
            @PathVariable Long algorithmId,
            @PathVariable Integer upToStepOrder,
            @RequestBody Map<String, Object> request) {
        log.info("批量执行算法步骤到第{}步, algorithmId={}", upToStepOrder, algorithmId);
        try {
            @SuppressWarnings("unchecked")
            List<String> regionCodes = (List<String>) request.get("regionCodes");
            Long weightConfigId = request.get("weightConfigId") != null 
                    ? Long.valueOf(request.get("weightConfigId").toString()) : null;
                    
            if (regionCodes == null || regionCodes.isEmpty()) {
                return Result.error("地区代码列表不能为空");
            }
            
            Map<String, Object> result = modelExecutionService.executeAlgorithmStepsUpTo(
                    algorithmId, upToStepOrder, regionCodes, weightConfigId);
            log.info("批量执行算法步骤成功");
            return Result.success(result);
        } catch (Exception e) {
            log.error("批量执行算法步骤失败", e);
            return Result.error("批量执行算法步骤失败: " + e.getMessage());
        }
    }
}
