package com.evaluate.service;

import com.evaluate.entity.AlgorithmConfig;
import com.evaluate.entity.AlgorithmStep;
import com.evaluate.entity.FormulaConfig;

import java.util.List;
import java.util.Map;

/**
 * 算法管理服务接口
 */
public interface AlgorithmManagementService {
    
    /**
     * 获取算法列表
     */
    List<AlgorithmConfig> getAlgorithmList();
    
    /**
     * 获取算法详情（包含步骤和公式）
     */
    Map<String, Object> getAlgorithmDetail(Long algorithmId);
    
    /**
     * 创建算法配置
     */
    boolean createAlgorithm(AlgorithmConfig algorithmConfig, List<AlgorithmStep> steps, List<FormulaConfig> formulas);
    
    /**
     * 更新算法配置
     */
    boolean updateAlgorithm(AlgorithmConfig algorithmConfig, List<AlgorithmStep> steps, List<FormulaConfig> formulas);
    
    /**
     * 删除算法配置
     */
    boolean deleteAlgorithm(Long algorithmId);
    
    /**
     * 获取算法步骤列表
     */
    List<AlgorithmStep> getAlgorithmSteps(Long algorithmId);
    
    /**
     * 创建算法步骤
     */
    boolean createAlgorithmStep(AlgorithmStep step);
    
    /**
     * 更新算法步骤
     */
    boolean updateAlgorithmStep(AlgorithmStep step);
    
    /**
     * 删除算法步骤
     */
    boolean deleteAlgorithmStep(Long stepId);
    
    /**
     * 批量更新算法步骤
     */
    boolean batchUpdateAlgorithmSteps(List<AlgorithmStep> steps);
    
    /**
     * 获取公式配置列表
     */
    List<FormulaConfig> getFormulaConfigs(String formulaType);
    
    /**
     * 根据步骤ID获取公式配置列表
     */
    List<FormulaConfig> getFormulasByStepId(Long stepId);
    
    /**
     * 创建公式配置
     */
    boolean createFormulaConfig(FormulaConfig formula);
    
    /**
     * 更新公式配置
     */
    boolean updateFormulaConfig(FormulaConfig formula);
    
    /**
     * 删除公式配置
     */
    boolean deleteFormulaConfig(Long formulaId);
    
    /**
     * 验证公式表达式
     */
    boolean validateFormulaExpression(String expression);
    
    /**
     * 复制算法配置
     */
    boolean copyAlgorithm(Long sourceAlgorithmId, String newAlgorithmName);
    
    /**
     * 导入算法配置
     */
    boolean importAlgorithm(Map<String, Object> algorithmData);
    
    /**
     * 导出算法配置
     */
    Map<String, Object> exportAlgorithm(Long algorithmId);
}