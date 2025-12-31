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
 */
@Slf4j
@RestController
@RequestMapping("/api/evaluation")
public class EvaluationController {

    @Autowired
    private IEvaluationService evaluationService;

    @Autowired
    private ModelExecutionService modelExecutionService;

    /**
     * 执行评估模型（基于模型配置）
     */
    @PostMapping("/execute-model")
    public Result<Map<String, Object>> executeModel(@RequestBody ModelExecutionRequest request) {
        log.info("开始执行评估模型, modelId={}, regionCodes={}, weightConfigId={}, year={}, createBy={}",
                request.getModelId(), request.getRegionCodes(), request.getWeightConfigId(), request.getYear(), request.getCreateBy());
        try {
            Map<String, Object> result = modelExecutionService.executeModel(
                    request.getModelId(), request.getRegionCodes(), request.getWeightConfigId(), request.getYear(), request.getOrgCode(), request.getCreateBy());
            return Result.success(result);
        } catch (Exception e) {
            log.error("执行评估模型失败", e);
            return Result.error("执行评估模型失败: " + e.getMessage());
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
     * 获取算法步骤信息
     */
    @GetMapping("/algorithm/{algorithmId}/steps-info")
    public Result<Map<String, Object>> getAlgorithmStepsInfo(@PathVariable("algorithmId") Long algorithmId) {
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
     */
    @PostMapping("/algorithm/{algorithmId}/step/{stepOrder}/execute")
    public Result<Map<String, Object>> executeAlgorithmStep(
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
                try {
                    year = Integer.valueOf(request.get("year").toString());
                } catch (Exception ignore) {}
            }

            if (regionCodes == null || regionCodes.isEmpty()) {
                return Result.error("地区代码列表不能为空");
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
     * 批量执行算法步骤（直到指定步骤）
     */
    @PostMapping("/algorithm/{algorithmId}/steps-up-to/{upToStepOrder}/execute")
    public Result<Map<String, Object>> executeAlgorithmStepsUpTo(
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
                try {
                    year = Integer.valueOf(request.get("year").toString());
                } catch (Exception ignore) {}
            }

            if (regionCodes == null || regionCodes.isEmpty()) {
                return Result.error("地区代码列表不能为空");
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

