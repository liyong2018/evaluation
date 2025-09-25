package com.evaluate.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.evaluate.entity.AlgorithmConfig;
import com.evaluate.entity.AlgorithmStep;
import com.evaluate.entity.FormulaConfig;
import com.evaluate.mapper.AlgorithmConfigMapper;
import com.evaluate.mapper.AlgorithmStepMapper;
import com.evaluate.mapper.FormulaConfigMapper;
import com.evaluate.service.AlgorithmManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Collectors;

/**
 * 算法管理服务实现类
 */
@Slf4j
@Service
public class AlgorithmManagementServiceImpl implements AlgorithmManagementService {
    
    @Autowired
    private AlgorithmConfigMapper algorithmConfigMapper;
    
    @Autowired
    private AlgorithmStepMapper algorithmStepMapper;
    
    @Autowired
    private FormulaConfigMapper formulaConfigMapper;
    
    @Override
    public List<AlgorithmConfig> getAlgorithmList() {
        QueryWrapper<AlgorithmConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        return algorithmConfigMapper.selectList(queryWrapper);
    }
    
    @Override
    public Map<String, Object> getAlgorithmDetail(Long algorithmId) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取算法配置
        AlgorithmConfig algorithm = algorithmConfigMapper.selectById(algorithmId);
        if (algorithm == null) {
            return result;
        }
        
        // 获取算法步骤
        QueryWrapper<AlgorithmStep> stepQuery = new QueryWrapper<>();
        stepQuery.eq("algorithm_config_id", algorithmId);
        stepQuery.orderByAsc("step_order");
        List<AlgorithmStep> steps = algorithmStepMapper.selectList(stepQuery);
        steps.sort(Comparator.comparing(AlgorithmStep::getStepOrder));
        
        // 获取相关公式配置
        List<FormulaConfig> formulas = new ArrayList<>();
        for (AlgorithmStep step : steps) {
            QueryWrapper<FormulaConfig> formulaQuery = new QueryWrapper<>();
            formulaQuery.eq("formula_type", step.getStepCode())
                       .eq("status", 1);
            List<FormulaConfig> stepFormulas = formulaConfigMapper.selectList(formulaQuery);
            formulas.addAll(stepFormulas);
        }
        
        result.put("algorithm", algorithm);
        result.put("steps", steps);
        result.put("formulas", formulas);
        result.put("stepCount", steps.size());
        result.put("formulaCount", formulas.size());
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createAlgorithm(AlgorithmConfig algorithmConfig, List<AlgorithmStep> steps, List<FormulaConfig> formulas) {
        try {
            // 创建算法配置
            algorithmConfig.setCreateTime(LocalDateTime.now());
            algorithmConfig.setStatus(1);
            algorithmConfigMapper.insert(algorithmConfig);
            
            Long algorithmId = algorithmConfig.getId();
            
            // 创建算法步骤
            if (steps != null && !steps.isEmpty()) {
                for (int i = 0; i < steps.size(); i++) {
                    AlgorithmStep step = steps.get(i);
                    step.setAlgorithmConfigId(algorithmId);
                    step.setStepOrder(i + 1);
                    step.setStatus(1);
                    step.setCreateTime(LocalDateTime.now());
                }
                algorithmStepMapper.batchInsert(steps);
            }
            
            // 创建公式配置
            if (formulas != null && !formulas.isEmpty()) {
                for (FormulaConfig formula : formulas) {
                    formula.setStatus(1);
                    formula.setCreateTime(LocalDateTime.now());
                    formula.setUpdateTime(LocalDateTime.now());
                    formulaConfigMapper.insert(formula);
                }
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("创建算法配置失败", e);
            throw new RuntimeException("创建算法配置失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAlgorithm(AlgorithmConfig algorithmConfig, List<AlgorithmStep> steps, List<FormulaConfig> formulas) {
        try {
            // 更新算法配置
            algorithmConfigMapper.updateById(algorithmConfig);
            
            Long algorithmId = algorithmConfig.getId();
            
            // 删除原有步骤
            algorithmStepMapper.deleteByAlgorithmId(algorithmId);
            
            // 创建新步骤
            if (steps != null && !steps.isEmpty()) {
                for (int i = 0; i < steps.size(); i++) {
                    AlgorithmStep step = steps.get(i);
                    step.setId(null); // 确保是新插入
                    step.setAlgorithmConfigId(algorithmId);
                    step.setStepOrder(i + 1);
                    step.setStatus(1);
                    step.setCreateTime(LocalDateTime.now());
                }
                algorithmStepMapper.batchInsert(steps);
            }
            
            // 更新公式配置
            if (formulas != null && !formulas.isEmpty()) {
                for (FormulaConfig formula : formulas) {
                    if (formula.getId() != null) {
                        formula.setUpdateTime(LocalDateTime.now());
                        formulaConfigMapper.updateById(formula);
                    } else {
                        formula.setStatus(1);
                        formula.setCreateTime(LocalDateTime.now());
                        formula.setUpdateTime(LocalDateTime.now());
                        formulaConfigMapper.insert(formula);
                    }
                }
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("更新算法配置失败", e);
            throw new RuntimeException("更新算法配置失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAlgorithm(Long algorithmId) {
        try {
            // 删除算法步骤
            algorithmStepMapper.deleteByAlgorithmId(algorithmId);
            
            // 删除算法配置
            algorithmConfigMapper.deleteById(algorithmId);
            
            return true;
            
        } catch (Exception e) {
            log.error("删除算法配置失败", e);
            return false;
        }
    }
    
    @Override
    public List<AlgorithmStep> getAlgorithmSteps(Long algorithmId) {
        QueryWrapper<AlgorithmStep> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("algorithm_config_id", algorithmId);
        queryWrapper.orderByAsc("step_order");
        List<AlgorithmStep> steps = algorithmStepMapper.selectList(queryWrapper);
        steps.sort(Comparator.comparing(AlgorithmStep::getStepOrder));
        
        // 为每个步骤关联对应的公式
        for (AlgorithmStep step : steps) {
            // 使用FIND_IN_SET查询支持多个步骤ID
            List<FormulaConfig> formulas = formulaConfigMapper.selectByStepId(step.getId().toString());
            step.setFormulas(formulas);
        }
        
        return steps;
    }
    
    @Override
    public boolean createAlgorithmStep(AlgorithmStep step) {
        try {
            step.setStatus(1);
            step.setCreateTime(LocalDateTime.now());
            return algorithmStepMapper.insert(step) > 0;
        } catch (Exception e) {
            log.error("创建算法步骤失败", e);
            return false;
        }
    }
    
    @Override
    public boolean updateAlgorithmStep(AlgorithmStep step) {
        try {
            return algorithmStepMapper.updateById(step) > 0;
        } catch (Exception e) {
            log.error("更新算法步骤失败", e);
            return false;
        }
    }
    
    @Override
    public boolean deleteAlgorithmStep(Long stepId) {
        try {
            return algorithmStepMapper.deleteById(stepId) > 0;
        } catch (Exception e) {
            log.error("删除算法步骤失败", e);
            return false;
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateAlgorithmSteps(List<AlgorithmStep> steps) {
        try {
            for (AlgorithmStep step : steps) {
                algorithmStepMapper.updateById(step);
            }
            return true;
        } catch (Exception e) {
            log.error("批量更新算法步骤失败", e);
            return false;
        }
    }
    
    @Override
    public List<FormulaConfig> getFormulaConfigs(String formulaType) {
        QueryWrapper<FormulaConfig> queryWrapper = new QueryWrapper<>();
        if (formulaType != null && !formulaType.isEmpty()) {
            queryWrapper.eq("formula_type", formulaType);
        }
        queryWrapper.orderByDesc("create_time");
        return formulaConfigMapper.selectList(queryWrapper);
    }
    
    @Override
    public List<FormulaConfig> getFormulasByStepId(Long stepId) {
        return formulaConfigMapper.selectByStepId(stepId.toString());
    }
    
    @Override
    public boolean createFormulaConfig(FormulaConfig formula) {
        try {
            formula.setCreateTime(LocalDateTime.now());
            return formulaConfigMapper.insert(formula) > 0;
        } catch (Exception e) {
            log.error("创建公式配置失败", e);
            return false;
        }
    }
    
    @Override
    public boolean updateFormulaConfig(FormulaConfig formula) {
        try {
            return formulaConfigMapper.updateById(formula) > 0;
        } catch (Exception e) {
            log.error("更新公式配置失败", e);
            return false;
        }
    }
    
    @Override
    public boolean deleteFormulaConfig(Long formulaId) {
        try {
            return formulaConfigMapper.deleteById(formulaId) > 0;
        } catch (Exception e) {
            log.error("删除公式配置失败", e);
            return false;
        }
    }
    
    @Override
    public boolean validateFormulaExpression(String expression) {
        try {
            // 简单的公式验证逻辑
            if (expression == null || expression.trim().isEmpty()) {
                return false;
            }
            
            // 检查基本的数学表达式格式
            String cleanExpression = expression.replaceAll("\\s+", "");
            
            // 检查括号匹配
            int openParentheses = 0;
            for (char c : cleanExpression.toCharArray()) {
                if (c == '(') {
                    openParentheses++;
                } else if (c == ')') {
                    openParentheses--;
                    if (openParentheses < 0) {
                        return false;
                    }
                }
            }
            
            return openParentheses == 0;
            
        } catch (Exception e) {
            log.error("验证公式表达式失败", e);
            return false;
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean copyAlgorithm(Long sourceAlgorithmId, String newAlgorithmName) {
        try {
            // 获取源算法配置
            AlgorithmConfig sourceAlgorithm = algorithmConfigMapper.selectById(sourceAlgorithmId);
            if (sourceAlgorithm == null) {
                return false;
            }
            
            // 创建新算法配置
            AlgorithmConfig newAlgorithm = new AlgorithmConfig();
            newAlgorithm.setConfigName(newAlgorithmName);
            newAlgorithm.setDescription("复制自: " + sourceAlgorithm.getConfigName());
            newAlgorithm.setVersion("1.0");
            newAlgorithm.setStatus(1);
            newAlgorithm.setCreateTime(LocalDateTime.now());
            algorithmConfigMapper.insert(newAlgorithm);
            
            // 复制算法步骤
            QueryWrapper<AlgorithmStep> stepQueryWrapper = new QueryWrapper<>();
            stepQueryWrapper.eq("algorithm_config_id", sourceAlgorithmId);
            List<AlgorithmStep> sourceSteps = algorithmStepMapper.selectList(stepQueryWrapper);
            if (!sourceSteps.isEmpty()) {
                List<AlgorithmStep> newSteps = sourceSteps.stream().map(step -> {
                    AlgorithmStep newStep = new AlgorithmStep();
                    newStep.setAlgorithmConfigId(newAlgorithm.getId());
                    newStep.setStepName(step.getStepName());
                    newStep.setStepDescription(step.getStepDescription());
                    newStep.setStepOrder(step.getStepOrder());

                    newStep.setStatus(step.getStatus());
                    newStep.setCreateTime(LocalDateTime.now());
                    return newStep;
                }).collect(Collectors.toList());
                
                algorithmStepMapper.batchInsert(newSteps);
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("复制算法配置失败", e);
            return false;
        }
    }
    
    @Override
    public boolean importAlgorithm(Map<String, Object> algorithmData) {
        try {
            // 解析算法数据并创建
            // 这里可以根据具体的数据格式进行解析
            log.info("导入算法配置: {}", algorithmData);
            return true;
        } catch (Exception e) {
            log.error("导入算法配置失败", e);
            return false;
        }
    }
    
    @Override
    public Map<String, Object> exportAlgorithm(Long algorithmId) {
        try {
            return getAlgorithmDetail(algorithmId);
        } catch (Exception e) {
            log.error("导出算法配置失败", e);
            return new HashMap<>();
        }
    }
}