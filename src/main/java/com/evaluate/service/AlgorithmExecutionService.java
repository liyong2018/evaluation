package com.evaluate.service;

import com.evaluate.entity.AlgorithmConfig;
import com.evaluate.entity.SurveyData;

import java.util.List;
import java.util.Map;

/**
 * 算法执行服务接口
 */
public interface AlgorithmExecutionService {
    
    /**
     * 执行算法计算
     * 
     * @param algorithmConfig 算法配置
     * @param surveyDataList 调查数据列表
     * @param weightConfig 权重配置
     * @param regionIds 选择的地区ID列表
     * @return 计算结果
     */
    Map<String, Object> executeAlgorithm(
        AlgorithmConfig algorithmConfig, 
        List<SurveyData> surveyDataList, 
        Map<String, Double> weightConfig,
        List<Long> regionIds
    );
    
    /**
     * 验证算法参数
     * 
     * @param algorithmConfig 算法配置
     * @param parameters 参数
     * @return 验证结果
     */
    boolean validateAlgorithmParams(AlgorithmConfig algorithmConfig, Map<String, Object> parameters);
    
    /**
     * 获取算法执行进度
     * 
     * @param executionId 执行ID
     * @return 进度信息
     */
    Map<String, Object> getExecutionProgress(String executionId);
    
    /**
     * 停止算法执行
     * 
     * @param executionId 执行ID
     * @return 是否成功停止
     */
    boolean stopExecution(String executionId);
    
    /**
     * 获取支持的算法类型列表
     * 
     * @return 算法类型列表
     */
    List<String> getSupportedAlgorithmTypes();
    
    /**
     * 计算单个步骤结果
     * 
     * @param algorithmConfig 算法配置
     * @param stepId 步骤ID
     * @param stepIndex 步骤索引
     * @param formula 计算公式
     * @param regionIds 选择的地区ID列表（字符串格式）
     * @param parameters 算法参数
     * @return 步骤计算结果
     */
    Map<String, Object> calculateStepResult(
        AlgorithmConfig algorithmConfig,
        Long stepId,
        Integer stepIndex,
        String formula,
        List<String> regionIds,
        Map<String, Object> parameters
    );
}