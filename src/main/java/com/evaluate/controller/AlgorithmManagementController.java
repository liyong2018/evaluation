package com.evaluate.controller;

import com.evaluate.common.Result;
import com.evaluate.entity.AlgorithmConfig;
import com.evaluate.entity.AlgorithmStep;
import com.evaluate.entity.FormulaConfig;
import com.evaluate.service.AlgorithmManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 算法管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/algorithm/management")
@CrossOrigin(origins = "*")
public class AlgorithmManagementController {
    
    @Autowired
    private AlgorithmManagementService algorithmManagementService;
    
    /**
     * 获取算法列表
     */
    @GetMapping("/list")
    public Result<List<AlgorithmConfig>> getAlgorithmList() {
        try {
            List<AlgorithmConfig> algorithms = algorithmManagementService.getAlgorithmList();
            return Result.success(algorithms);
        } catch (Exception e) {
            log.error("获取算法列表失败", e);
            return Result.error("获取算法列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取算法详情
     */
    @GetMapping("/detail/{algorithmId}")
    public Result<Map<String, Object>> getAlgorithmDetail(@PathVariable Long algorithmId) {
        try {
            Map<String, Object> detail = algorithmManagementService.getAlgorithmDetail(algorithmId);
            return Result.success(detail);
        } catch (Exception e) {
            log.error("获取算法详情失败", e);
            return Result.error("获取算法详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建算法配置
     */
    @PostMapping("/create")
    public Result<Boolean> createAlgorithm(@RequestBody Map<String, Object> request) {
        try {
            AlgorithmConfig algorithmConfig = parseAlgorithmConfig(request);
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> stepMaps = (List<Map<String, Object>>) request.get("steps");
            List<AlgorithmStep> steps = parseAlgorithmSteps(stepMaps);
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> formulaMaps = (List<Map<String, Object>>) request.get("formulas");
            List<FormulaConfig> formulas = parseFormulaConfigs(formulaMaps);
            
            boolean success = algorithmManagementService.createAlgorithm(algorithmConfig, steps, formulas);
            
            return success ? Result.success(true) : Result.error("创建算法配置失败");
            
        } catch (Exception e) {
            log.error("创建算法配置失败", e);
            return Result.error("创建算法配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新算法配置
     */
    @PutMapping("/update")
    public Result<Boolean> updateAlgorithm(@RequestBody Map<String, Object> request) {
        try {
            AlgorithmConfig algorithmConfig = parseAlgorithmConfig(request);
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> stepMaps = (List<Map<String, Object>>) request.get("steps");
            List<AlgorithmStep> steps = parseAlgorithmSteps(stepMaps);
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> formulaMaps = (List<Map<String, Object>>) request.get("formulas");
            List<FormulaConfig> formulas = parseFormulaConfigs(formulaMaps);
            
            boolean success = algorithmManagementService.updateAlgorithm(algorithmConfig, steps, formulas);
            
            return success ? Result.success(true) : Result.error("更新算法配置失败");
            
        } catch (Exception e) {
            log.error("更新算法配置失败", e);
            return Result.error("更新算法配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除算法配置
     */
    @DeleteMapping("/delete/{algorithmId}")
    public Result<Boolean> deleteAlgorithm(@PathVariable Long algorithmId) {
        try {
            boolean success = algorithmManagementService.deleteAlgorithm(algorithmId);
            return success ? Result.success(true) : Result.error("删除算法配置失败");
        } catch (Exception e) {
            log.error("删除算法配置失败", e);
            return Result.error("删除算法配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取算法步骤列表
     */
    @GetMapping("/steps/{algorithmId}")
    public Result<List<AlgorithmStep>> getAlgorithmSteps(@PathVariable Long algorithmId) {
        try {
            List<AlgorithmStep> steps = algorithmManagementService.getAlgorithmSteps(algorithmId);
            return Result.success(steps);
        } catch (Exception e) {
            log.error("获取算法步骤失败", e);
            return Result.error("获取算法步骤失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建算法步骤
     */
    @PostMapping("/step/create")
    public Result<Boolean> createAlgorithmStep(@RequestBody AlgorithmStep step) {
        try {
            boolean success = algorithmManagementService.createAlgorithmStep(step);
            return success ? Result.success(true) : Result.error("创建算法步骤失败");
        } catch (Exception e) {
            log.error("创建算法步骤失败", e);
            return Result.error("创建算法步骤失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新算法步骤
     */
    @PutMapping("/step/update")
    public Result<Boolean> updateAlgorithmStep(@RequestBody AlgorithmStep step) {
        try {
            boolean success = algorithmManagementService.updateAlgorithmStep(step);
            return success ? Result.success(true) : Result.error("更新算法步骤失败");
        } catch (Exception e) {
            log.error("更新算法步骤失败", e);
            return Result.error("更新算法步骤失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除算法步骤
     */
    @DeleteMapping("/step/delete/{stepId}")
    public Result<Boolean> deleteAlgorithmStep(@PathVariable Long stepId) {
        try {
            boolean success = algorithmManagementService.deleteAlgorithmStep(stepId);
            return success ? Result.success(true) : Result.error("删除算法步骤失败");
        } catch (Exception e) {
            log.error("删除算法步骤失败", e);
            return Result.error("删除算法步骤失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量更新算法步骤
     */
    @PutMapping("/steps/batch")
    public Result<Boolean> batchUpdateAlgorithmSteps(@RequestBody List<AlgorithmStep> steps) {
        try {
            boolean success = algorithmManagementService.batchUpdateAlgorithmSteps(steps);
            return success ? Result.success(true) : Result.error("批量更新算法步骤失败");
        } catch (Exception e) {
            log.error("批量更新算法步骤失败", e);
            return Result.error("批量更新算法步骤失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取公式配置列表
     */
    @GetMapping("/formulas")
    public Result<List<FormulaConfig>> getFormulaConfigs(@RequestParam(required = false) String formulaType) {
        try {
            List<FormulaConfig> formulas = algorithmManagementService.getFormulaConfigs(formulaType);
            return Result.success(formulas);
        } catch (Exception e) {
            log.error("获取公式配置失败", e);
            return Result.error("获取公式配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据步骤ID获取公式配置列表
     */
    @GetMapping("/steps/{stepId}/formulas")
    public Result<List<FormulaConfig>> getFormulasByStepId(@PathVariable Long stepId) {
        try {
            List<FormulaConfig> formulas = algorithmManagementService.getFormulasByStepId(stepId);
            return Result.success(formulas);
        } catch (Exception e) {
            log.error("根据步骤ID获取公式配置失败", e);
            return Result.error("根据步骤ID获取公式配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建公式配置
     */
    @PostMapping("/formula/create")
    public Result<Boolean> createFormulaConfig(@RequestBody FormulaConfig formula) {
        try {
            boolean success = algorithmManagementService.createFormulaConfig(formula);
            return success ? Result.success(true) : Result.error("创建公式配置失败");
        } catch (Exception e) {
            log.error("创建公式配置失败", e);
            return Result.error("创建公式配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新公式配置
     */
    @PutMapping("/formula/update")
    public Result<Boolean> updateFormulaConfig(@RequestBody FormulaConfig formula) {
        try {
            boolean success = algorithmManagementService.updateFormulaConfig(formula);
            return success ? Result.success(true) : Result.error("更新公式配置失败");
        } catch (Exception e) {
            log.error("更新公式配置失败", e);
            return Result.error("更新公式配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除公式配置
     */
    @DeleteMapping("/formula/delete/{formulaId}")
    public Result<Boolean> deleteFormulaConfig(@PathVariable Long formulaId) {
        try {
            boolean success = algorithmManagementService.deleteFormulaConfig(formulaId);
            return success ? Result.success(true) : Result.error("删除公式配置失败");
        } catch (Exception e) {
            log.error("删除公式配置失败", e);
            return Result.error("删除公式配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证公式表达式
     */
    @PostMapping("/formula/validate")
    public Result<Boolean> validateFormulaExpression(@RequestBody Map<String, String> request) {
        try {
            String expression = request.get("expression");
            boolean isValid = algorithmManagementService.validateFormulaExpression(expression);
            return Result.success(isValid);
        } catch (Exception e) {
            log.error("验证公式表达式失败", e);
            return Result.error("验证公式表达式失败: " + e.getMessage());
        }
    }
    
    /**
     * 复制算法配置
     */
    @PostMapping("/copy/{sourceAlgorithmId}")
    public Result<Boolean> copyAlgorithm(@PathVariable Long sourceAlgorithmId, @RequestBody Map<String, String> request) {
        try {
            String newAlgorithmName = request.get("newAlgorithmName");
            boolean success = algorithmManagementService.copyAlgorithm(sourceAlgorithmId, newAlgorithmName);
            return success ? Result.success(true) : Result.error("复制算法配置失败");
        } catch (Exception e) {
            log.error("复制算法配置失败", e);
            return Result.error("复制算法配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 导入算法配置
     */
    @PostMapping("/import")
    public Result<Boolean> importAlgorithm(@RequestBody Map<String, Object> algorithmData) {
        try {
            boolean success = algorithmManagementService.importAlgorithm(algorithmData);
            return success ? Result.success(true) : Result.error("导入算法配置失败");
        } catch (Exception e) {
            log.error("导入算法配置失败", e);
            return Result.error("导入算法配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 导出算法配置
     */
    @GetMapping("/export/{algorithmId}")
    public Result<Map<String, Object>> exportAlgorithm(@PathVariable Long algorithmId) {
        try {
            Map<String, Object> algorithmData = algorithmManagementService.exportAlgorithm(algorithmId);
            return Result.success(algorithmData);
        } catch (Exception e) {
            log.error("导出算法配置失败", e);
            return Result.error("导出算法配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 解析算法配置
     */
    private AlgorithmConfig parseAlgorithmConfig(Map<String, Object> request) {
        AlgorithmConfig config = new AlgorithmConfig();
        
        if (request.get("id") != null) {
            config.setId(Long.valueOf(request.get("id").toString()));
        }
        
        config.setConfigName((String) request.get("configName"));
        config.setDescription((String) request.get("description"));
        config.setVersion((String) request.get("version"));
        
        if (request.get("status") != null) {
            config.setStatus(Integer.valueOf(request.get("status").toString()));
        }
        
        return config;
    }
    
    /**
     * 解析算法步骤列表
     */
    private List<AlgorithmStep> parseAlgorithmSteps(List<Map<String, Object>> stepMaps) {
        if (stepMaps == null) {
            return null;
        }
        
        return stepMaps.stream().map(stepMap -> {
            AlgorithmStep step = new AlgorithmStep();
            
            if (stepMap.get("id") != null) {
                step.setId(Long.valueOf(stepMap.get("id").toString()));
            }
            
            step.setStepName((String) stepMap.get("stepName"));
            step.setStepDescription((String) stepMap.get("stepDescription"));
            
            if (stepMap.get("stepOrder") != null) {
                step.setStepOrder(Integer.valueOf(stepMap.get("stepOrder").toString()));
            }
            
            if (stepMap.get("status") != null) {
                step.setStatus(Integer.valueOf(stepMap.get("status").toString()));
            }
            
            return step;
        }).collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 解析公式配置列表
     */
    private List<FormulaConfig> parseFormulaConfigs(List<Map<String, Object>> formulaMaps) {
        if (formulaMaps == null) {
            return null;
        }
        
        return formulaMaps.stream().map(formulaMap -> {
            FormulaConfig formula = new FormulaConfig();
            
            if (formulaMap.get("id") != null) {
                formula.setId(Long.valueOf(formulaMap.get("id").toString()));
            }
            
            if (formulaMap.get("algorithmStepId") != null) {
                formula.setAlgorithmStepId(formulaMap.get("algorithmStepId").toString());
            }
            
            formula.setFormulaName((String) formulaMap.get("formulaName"));
            formula.setFormulaExpression((String) formulaMap.get("formulaExpression"));
            formula.setInputVariables((String) formulaMap.get("inputVariables"));
            formula.setOutputVariable((String) formulaMap.get("outputVariable"));
            formula.setDescription((String) formulaMap.get("description"));
            
            return formula;
        }).collect(java.util.stream.Collectors.toList());
    }
}