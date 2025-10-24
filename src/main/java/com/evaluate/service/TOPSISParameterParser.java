package com.evaluate.service;

import com.evaluate.dto.topsis.TOPSISAlgorithmConfig;
import com.evaluate.entity.StepAlgorithm;

/**
 * TOPSIS参数解析器接口
 * 
 * 负责从step_algorithm表的ql_expression字段解析TOPSIS算法参数
 * 
 * @author System
 * @since 2025-01-01
 */
public interface TOPSISParameterParser {
    
    /**
     * 从ql_expression解析TOPSIS算法配置
     * 
     * @param qlExpression ql_expression字段内容
     * @param stepAlgorithm 步骤算法实体
     * @return TOPSIS算法配置
     */
    TOPSISAlgorithmConfig parseFromExpression(String qlExpression, StepAlgorithm stepAlgorithm);
    
    /**
     * 验证ql_expression格式是否正确
     * 
     * @param qlExpression ql_expression字段内容
     * @return 验证结果
     */
    boolean validateExpressionFormat(String qlExpression);
    
    /**
     * 从模型ID和步骤代码获取TOPSIS配置
     * 
     * @param modelId 模型ID
     * @param stepCode 步骤代码
     * @return TOPSIS算法配置
     */
    TOPSISAlgorithmConfig getTOPSISConfig(Long modelId, String stepCode);
    
    /**
     * 生成ql_expression字符串
     * 
     * @param algorithmConfig TOPSIS算法配置
     * @return ql_expression字符串
     */
    String generateExpression(TOPSISAlgorithmConfig algorithmConfig);
}