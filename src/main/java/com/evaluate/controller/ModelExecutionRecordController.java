package com.evaluate.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evaluate.common.Result;
import com.evaluate.entity.ModelExecutionRecord;
import com.evaluate.entity.EvaluationResult;
import com.evaluate.service.EvaluationResultService;
import com.evaluate.mapper.ModelExecutionRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 模型执行记录控制器
 * 提供执行记录的查询、删除等接口
 */
@Slf4j
@RestController
@RequestMapping("/api/model-execution-record")
@CrossOrigin(origins = "*")
public class ModelExecutionRecordController {

    @Autowired
    private ModelExecutionRecordMapper modelExecutionRecordMapper;

    @Autowired
    private EvaluationResultService evaluationResultService;

    /**
     * 获取所有执行记录（分页）
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getExecutionRecords(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long modelId,
            @RequestParam(required = false) String executionStatus) {
        try {
            Page<ModelExecutionRecord> page = new Page<>(current, size);
            QueryWrapper<ModelExecutionRecord> queryWrapper = new QueryWrapper<>();

            if (modelId != null) {
                queryWrapper.eq("model_id", modelId);
            }
            if (executionStatus != null && !executionStatus.isEmpty()) {
                queryWrapper.eq("execution_status", executionStatus);
            }

            queryWrapper.orderByDesc("start_time");

            Page<ModelExecutionRecord> resultPage = modelExecutionRecordMapper.selectPage(page, queryWrapper);

            Map<String, Object> result = new HashMap<>();
            result.put("records", resultPage.getRecords());
            result.put("total", resultPage.getTotal());
            result.put("current", resultPage.getCurrent());
            result.put("size", resultPage.getSize());
            result.put("pages", resultPage.getPages());

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取执行记录失败", e);
            return Result.error("获取执行记录失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取执行记录详情
     */
    @GetMapping("/{id}")
    public Result<ModelExecutionRecord> getExecutionRecordById(@PathVariable("id") Long id) {
        try {
            ModelExecutionRecord record = modelExecutionRecordMapper.selectById(id);
            if (record == null) {
                return Result.error("执行记录不存在");
            }
            return Result.success(record);
        } catch (Exception e) {
            log.error("获取执行记录详情失败", e);
            return Result.error("获取执行记录详情失败: " + e.getMessage());
        }
    }

    /**
     * 根据执行记录ID获取评估结果
     */
    @GetMapping("/{id}/results")
    public Result<List<EvaluationResult>> getEvaluationResults(@PathVariable("id") Long id) {
        try {
            List<EvaluationResult> results = evaluationResultService.getEvaluationResultsByExecutionId(id);
            return Result.success(results);
        } catch (Exception e) {
            log.error("获取评估结果失败", e);
            return Result.error("获取评估结果失败: " + e.getMessage());
        }
    }

    /**
     * 删除执行记录（会先删除关联的评估结果）
     */
    @DeleteMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteExecutionRecord(@PathVariable("id") Long id) {
        try {
            ModelExecutionRecord record = modelExecutionRecordMapper.selectById(id);
            if (record == null) {
                return Result.error("执行记录不存在");
            }

            // 先删除相关的评估结果
            evaluationResultService.deleteByExecutionRecordId(id);

            // 再删除执行记录
            modelExecutionRecordMapper.deleteById(id);

            return Result.success();
        } catch (Exception e) {
            log.error("删除执行记录失败", e);
            return Result.error("删除执行记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取执行记录统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getExecutionStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();

            // 总记录数
            QueryWrapper<ModelExecutionRecord> totalQuery = new QueryWrapper<>();
            Long totalCount = modelExecutionRecordMapper.selectCount(totalQuery);
            statistics.put("totalCount", totalCount);

            // 成功记录数
            QueryWrapper<ModelExecutionRecord> successQuery = new QueryWrapper<>();
            successQuery.eq("execution_status", "SUCCESS");
            Long successCount = modelExecutionRecordMapper.selectCount(successQuery);
            statistics.put("successCount", successCount);

            // 失败记录数
            QueryWrapper<ModelExecutionRecord> failedQuery = new QueryWrapper<>();
            failedQuery.eq("execution_status", "FAILED");
            Long failedCount = modelExecutionRecordMapper.selectCount(failedQuery);
            statistics.put("failedCount", failedCount);

            // 运行中记录数
            QueryWrapper<ModelExecutionRecord> runningQuery = new QueryWrapper<>();
            runningQuery.eq("execution_status", "RUNNING");
            Long runningCount = modelExecutionRecordMapper.selectCount(runningQuery);
            statistics.put("runningCount", runningCount);

            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取执行统计信息失败", e);
            return Result.error("获取执行统计信息失败: " + e.getMessage());
        }
    }
}
