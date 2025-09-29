package com.evaluate.controller;

import com.evaluate.common.Result;
import com.evaluate.service.IEvaluationService;
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
}