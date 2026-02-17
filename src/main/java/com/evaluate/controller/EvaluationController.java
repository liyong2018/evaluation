package com.evaluate.controller;

import com.evaluate.common.Result;
import com.evaluate.service.ModelExecutionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评估计算控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/evaluation")
public class EvaluationController {

    @Autowired
    private ModelExecutionService modelExecutionService;

    /**
     * 执行评估模型（基于模型配置）- 异步执行
     * 立即返回执行记录ID，实际计算在后台进行
     */
    @PostMapping("/execute-model")
    public Result<Map<String, Object>> executeModel(@RequestBody ModelExecutionRequest request) {
        log.info("开始异步执行评估模型, modelId={}, regionCodes={}, weightConfigId={}, year={}, createBy={}",
                request.getModelId(), request.getRegionCodes(), request.getWeightConfigId(), request.getYear(), request.getCreateBy());
        try {
            // 异步执行，立即返回执行记录ID
            Long executionRecordId = modelExecutionService.executeModelAsync(
                    request.getModelId(), request.getRegionCodes(), request.getWeightConfigId(),
                    request.getYear(), request.getOrgCode(), request.getCreateBy());

            Map<String, Object> result = new HashMap<>();
            result.put("executionRecordId", executionRecordId);
            result.put("status", "RUNNING");
            result.put("message", "评估任务已提交，正在后台执行中");

            return Result.success(result);
        } catch (Exception e) {
            log.error("提交评估任务失败", e);
            return Result.error("提交评估任务失败: " + e.getMessage());
        }
    }

    /**
     * 检查评估数据是否存在
     */
    @GetMapping("/check-data")
    public Result<Map<String, Object>> checkEvaluationData(
            @RequestParam Long modelId,
            @RequestParam List<String> regionCodes,
            @RequestParam Integer year,
            @RequestParam(required = false) String orgCode) {
        log.info("检查评估数据, modelId={}, regionCodes={}, year={}, orgCode={}", modelId, regionCodes, year, orgCode);
        try {
            Map<String, Object> result = modelExecutionService.checkEvaluationData(modelId, regionCodes, year, orgCode);
            return Result.success(result);
        } catch (Exception e) {
            log.error("检查评估数据失败", e);
            return Result.error("检查评估数据失败: " + e.getMessage());
        }
    }

    /**
     * 生成评估结果二维表
     */
    @PostMapping("/generate-table")
    public Result<List<Map<String, Object>>> generateResultTable(@RequestBody Map<String, Object> executionResults) {
        log.info("开始生成结果二维表");
        try {
            List<Map<String, Object>> tableData = modelExecutionService.generateResultTable(executionResults);
            log.info("结果二维表生成成功，行数: {}", tableData.size());
            return Result.success(tableData);
        } catch (Exception e) {
            log.error("生成结果二维表失败", e);
            return Result.error("生成结果二维表失败: " + e.getMessage());
        }
    }

    /**
     * 获取评估历史列表（RESTful风格，路径参数）
     * /api/evaluation/history/1 表示第1页，每页10条
     * /api/evaluation/history/1/5 表示第1页，每页5条
     */
    @GetMapping("/history/{page}")
    public Result<Map<String, Object>> getEvaluationHistoryListByPath(
            @PathVariable("page") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long modelId,
            @RequestParam(required = false) String executionStatus) {
        log.info("获取评估历史列表(RESTful), page={}, size={}, modelId={}, executionStatus={}", page, size, modelId, executionStatus);
        try {
            Map<String, Object> result = modelExecutionService.getEvaluationHistoryList(page, size, modelId, executionStatus);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取评估历史列表失败", e);
            return Result.error("获取评估历史列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取评估历史列表（支持分页）
     * /api/evaluation/history?page=1&size=10 表示第1页，每页10条
     */
    @GetMapping("/history")
    public Result<Map<String, Object>> getEvaluationHistoryList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long modelId,
            @RequestParam(required = false) String executionStatus,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String orgCode) {
        log.info("获取评估历史列表, page={}, size={}, modelId={}, executionStatus={}, year={}, orgCode={}", page, size, modelId, executionStatus, year, orgCode);
        try {
            Map<String, Object> result = modelExecutionService.getEvaluationHistoryList(page, size, modelId, executionStatus, year, orgCode);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取评估历史列表失败", e);
            return Result.error("获取评估历史列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取评估历史记录详情
     * /api/evaluation/history/detail/1 表示ID为1的记录详情
     */
    @GetMapping("/history/detail/{id}")
    public Result<Map<String, Object>> getEvaluationHistoryDetail(@PathVariable("id") Long id) {
        log.info("获取评估历史记录详情, id={}", id);
        try {
            Map<String, Object> result = modelExecutionService.getExecutionRecordDetail(id);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取评估历史记录详情失败", e);
            return Result.error("获取评估历史记录详情失败: " + e.getMessage());
        }
    }

    /**
     * 删除评估历史记录
     * /api/evaluation/history/1 表示删除ID为1的记录
     */
    @DeleteMapping("/history/{id}")
    public Result<Boolean> deleteEvaluationHistory(@PathVariable("id") Long id) {
        log.info("删除评估历史记录, id={}", id);
        try {
            boolean success = modelExecutionService.deleteEvaluationHistory(id);
            return success ? Result.success(true) : Result.error("删除失败，记录不存在");
        } catch (Exception e) {
            log.error("删除评估历史记录失败", e);
            return Result.error("删除评估历史记录失败: " + e.getMessage());
        }
    }
}

