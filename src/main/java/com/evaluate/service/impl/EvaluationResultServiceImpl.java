package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluate.entity.EvaluationResult;
import com.evaluate.entity.ModelExecutionRecord;
import com.evaluate.mapper.EvaluationResultMapper;
import com.evaluate.mapper.ModelExecutionRecordMapper;
import com.evaluate.service.EvaluationResultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评估结果服务实现类
 *
 * @author admin
 * @since 2025-10-28
 */
@Slf4j
@Service
public class EvaluationResultServiceImpl extends ServiceImpl<EvaluationResultMapper, EvaluationResult> implements EvaluationResultService {

    @Autowired
    private EvaluationResultMapper evaluationResultMapper;

    @Autowired
    private ModelExecutionRecordMapper modelExecutionRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> saveEvaluationResults(List<EvaluationResult> evaluationResults, Long executionRecordId, String createBy) {
        if (evaluationResults == null || evaluationResults.isEmpty()) {
            log.warn("No evaluation results to save");
            return new ArrayList<>();
        }

        // 设置执行记录ID和创建人
        for (EvaluationResult result : evaluationResults) {
            result.setExecutionRecordId(executionRecordId);
            result.setCreateBy(createBy);
        }

        // 批量插入
        int insertedCount = evaluationResultMapper.insertBatch(evaluationResults);
        log.info("Saved {} evaluation results for execution record {}", insertedCount, executionRecordId);

        // 返回插入的ID列表
        List<Long> resultIds = evaluationResults.stream()
                .map(EvaluationResult::getId)
                .collect(Collectors.toList());

        return resultIds;
    }

    @Override
    public List<EvaluationResult> getResultsByExecutionRecordId(Long executionRecordId) {
        return evaluationResultMapper.selectByExecutionRecordId(executionRecordId);
    }

    @Override
    public List<EvaluationResult> getResultsByModelIdAndDataSource(Long modelId, String dataSource) {
        return evaluationResultMapper.selectByModelIdAndDataSource(modelId, dataSource);
    }

    @Override
    public List<EvaluationResult> getResultsByRegionCode(String regionCode) {
        return evaluationResultMapper.selectByRegionCode(regionCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ModelExecutionRecord createExecutionRecord(Long modelId, List<String> regionIds, Long weightConfigId, String createBy) {
        // 生成执行代码
        String executionCode = generateExecutionCode(modelId);

        ModelExecutionRecord record = new ModelExecutionRecord();
        record.setModelId(modelId);
        record.setExecutionCode(executionCode);
        record.setRegionIds(String.join(",", regionIds));
        record.setWeightConfigId(weightConfigId);
        record.setExecutionStatus("RUNNING");
        record.setCreateBy(createBy);
        record.setStartTime(LocalDateTime.now());

        modelExecutionRecordMapper.insert(record);
        log.info("Created execution record: {} for model {} with {} regions", executionCode, modelId, regionIds.size());

        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateExecutionRecord(Long executionRecordId, String executionStatus, String resultSummary, List<Long> resultIds, String errorMessage) {
        String resultIdsStr = resultIds != null && !resultIds.isEmpty()
                ? resultIds.stream().map(String::valueOf).collect(Collectors.joining(","))
                : null;
        Integer resultCount = resultIds != null ? resultIds.size() : 0;

        modelExecutionRecordMapper.updateExecutionResult(
                executionRecordId, executionStatus, resultSummary, resultIdsStr, resultCount, errorMessage);

        log.info("Updated execution record {}: status={}, resultCount={}",
                executionRecordId, executionStatus, resultCount);
    }

    @Override
    public ModelExecutionRecord getExecutionRecordByCode(String executionCode) {
        return modelExecutionRecordMapper.selectByExecutionCode(executionCode);
    }

    @Override
    public List<ModelExecutionRecord> getAllExecutionRecords() {
        QueryWrapper<ModelExecutionRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("start_time");
        return modelExecutionRecordMapper.selectList(queryWrapper);
    }

    @Override
    public List<EvaluationResult> getEvaluationResultsByExecutionId(Long executionRecordId) {
        return getResultsByExecutionRecordId(executionRecordId);
    }

    @Override
    public List<EvaluationResult> getAllEvaluationResults() {
        QueryWrapper<EvaluationResult> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        return evaluationResultMapper.selectList(queryWrapper);
    }

    /**
     * 生成执行代码
     *
     * @param modelId 模型ID
     * @return 执行代码
     */
    private String generateExecutionCode(Long modelId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return String.format("EXEC_%d_%s", modelId, timestamp);
    }
}