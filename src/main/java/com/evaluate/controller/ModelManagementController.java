package com.evaluate.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.evaluate.entity.EvaluationModel;
import com.evaluate.entity.ModelStep;
import com.evaluate.entity.StepAlgorithm;
import com.evaluate.mapper.EvaluationModelMapper;
import com.evaluate.mapper.ModelStepMapper;
import com.evaluate.mapper.StepAlgorithmMapper;
import com.evaluate.service.QLExpressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模型管理控制器
 * 
 * @author System
 * @since 2025-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/model-management")
@CrossOrigin(origins = "*")
public class ModelManagementController {

    @Autowired
    private EvaluationModelMapper evaluationModelMapper;

    @Autowired
    private ModelStepMapper modelStepMapper;

    @Autowired
    private StepAlgorithmMapper stepAlgorithmMapper;

    @Autowired
    private QLExpressService qlExpressService;

    /**
     * 获取所有模型
     */
    @GetMapping("/models")
    public Map<String, Object> getAllModels() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<EvaluationModel> models = evaluationModelMapper.selectList(
                new QueryWrapper<EvaluationModel>().eq("status", 1).orderByAsc("id")
            );
            result.put("success", true);
            result.put("data", models);
        } catch (Exception e) {
            log.error("获取模型列表失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 获取模型详情（包含步骤和算法）
     */
    @GetMapping("/models/{modelId}/detail")
    public Map<String, Object> getModelDetail(@PathVariable Long modelId) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 获取模型基本信息
            EvaluationModel model = evaluationModelMapper.selectById(modelId);
            if (model == null) {
                result.put("success", false);
                result.put("message", "模型不存在");
                return result;
            }

            // 获取模型步骤
            List<ModelStep> steps = modelStepMapper.selectList(
                new QueryWrapper<ModelStep>()
                    .eq("model_id", modelId)
                    .eq("status", 1)
                    .orderByAsc("step_order")
            );

            // 获取每个步骤的算法
            for (ModelStep step : steps) {
                List<StepAlgorithm> algorithms = stepAlgorithmMapper.selectList(
                    new QueryWrapper<StepAlgorithm>()
                        .eq("step_id", step.getId())
                        .eq("status", 1)
                        .orderByAsc("algorithm_order")
                );
                // 将算法列表作为步骤的属性（这里简化处理，实际应该用DTO）
                step.setDescription(step.getDescription() + "|ALGORITHMS|" + JSON.toJSONString(algorithms));
            }

            Map<String, Object> data = new HashMap<>();
            data.put("model", model);
            data.put("steps", steps);

            result.put("success", true);
            result.put("data", data);
        } catch (Exception e) {
            log.error("获取模型详情失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 创建新模型
     */
    @PostMapping("/models")
    public Map<String, Object> createModel(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            EvaluationModel model = new EvaluationModel();
            model.setModelName((String) request.get("modelName"));
            model.setModelCode((String) request.get("modelCode"));
            model.setDescription((String) request.get("description"));
            model.setVersion((String) request.get("version"));
            model.setStatus(1);
            model.setIsDefault(false);

            evaluationModelMapper.insert(model);

            result.put("success", true);
            result.put("data", model);
            result.put("message", "模型创建成功");
        } catch (Exception e) {
            log.error("创建模型失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 为模型添加步骤
     */
    @PostMapping("/models/{modelId}/steps")
    public Map<String, Object> createStep(@PathVariable Long modelId, @RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            ModelStep step = new ModelStep();
            step.setModelId(modelId);
            step.setStepName((String) request.get("stepName"));
            step.setStepCode((String) request.get("stepCode"));
            step.setStepOrder((Integer) request.get("stepOrder"));
            step.setStepType((String) request.get("stepType"));
            step.setDescription((String) request.get("description"));
            step.setStatus(1);

            modelStepMapper.insert(step);

            result.put("success", true);
            result.put("data", step);
            result.put("message", "步骤创建成功");
        } catch (Exception e) {
            log.error("创建步骤失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 为步骤添加算法
     */
    @PostMapping("/steps/{stepId}/algorithms")
    public Map<String, Object> createAlgorithm(@PathVariable Long stepId, @RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String expression = (String) request.get("qlExpression");
            
            // 验证QLExpress表达式
            if (!qlExpressService.validate(expression)) {
                result.put("success", false);
                result.put("message", "QLExpress表达式语法错误");
                return result;
            }

            StepAlgorithm algorithm = new StepAlgorithm();
            algorithm.setStepId(stepId);
            algorithm.setAlgorithmName((String) request.get("algorithmName"));
            algorithm.setAlgorithmCode((String) request.get("algorithmCode"));
            algorithm.setAlgorithmOrder((Integer) request.get("algorithmOrder"));
            algorithm.setQlExpression(expression);
            algorithm.setInputParams((String) request.get("inputParams"));
            algorithm.setOutputParam((String) request.get("outputParam"));
            algorithm.setDescription((String) request.get("description"));
            algorithm.setStatus(1);

            stepAlgorithmMapper.insert(algorithm);

            result.put("success", true);
            result.put("data", algorithm);
            result.put("message", "算法创建成功");
        } catch (Exception e) {
            log.error("创建算法失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 更新算法表达式
     */
    @PutMapping("/algorithms/{algorithmId}")
    public Map<String, Object> updateAlgorithm(@PathVariable Long algorithmId, @RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String expression = (String) request.get("qlExpression");
            
            // 验证QLExpress表达式
            if (!qlExpressService.validate(expression)) {
                result.put("success", false);
                result.put("message", "QLExpress表达式语法错误");
                return result;
            }

            StepAlgorithm algorithm = stepAlgorithmMapper.selectById(algorithmId);
            if (algorithm == null) {
                result.put("success", false);
                result.put("message", "算法不存在");
                return result;
            }

            algorithm.setAlgorithmName((String) request.get("algorithmName"));
            algorithm.setQlExpression(expression);
            algorithm.setInputParams((String) request.get("inputParams"));
            algorithm.setOutputParam((String) request.get("outputParam"));
            algorithm.setDescription((String) request.get("description"));

            stepAlgorithmMapper.updateById(algorithm);

            result.put("success", true);
            result.put("data", algorithm);
            result.put("message", "算法更新成功");
        } catch (Exception e) {
            log.error("更新算法失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 验证QLExpress表达式
     */
    @PostMapping("/validate-expression")
    public Map<String, Object> validateExpression(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String expression = (String) request.get("expression");
            @SuppressWarnings("unchecked")
            Map<String, Object> context = (Map<String, Object>) request.get("context");

            boolean isValid = qlExpressService.validate(expression);
            String errorMessage = null;
            
            if (!isValid) {
                errorMessage = qlExpressService.getErrorMessage(expression, context);
            }

            result.put("success", true);
            result.put("valid", isValid);
            result.put("errorMessage", errorMessage);
        } catch (Exception e) {
            log.error("验证表达式失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 删除算法
     */
    @DeleteMapping("/algorithms/{algorithmId}")
    public Map<String, Object> deleteAlgorithm(@PathVariable Long algorithmId) {
        Map<String, Object> result = new HashMap<>();
        try {
            stepAlgorithmMapper.deleteById(algorithmId);
            result.put("success", true);
            result.put("message", "算法删除成功");
        } catch (Exception e) {
            log.error("删除算法失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 删除步骤
     */
    @DeleteMapping("/steps/{stepId}")
    public Map<String, Object> deleteStep(@PathVariable Long stepId) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 先删除步骤下的所有算法
            stepAlgorithmMapper.delete(new QueryWrapper<StepAlgorithm>().eq("step_id", stepId));
            // 再删除步骤
            modelStepMapper.deleteById(stepId);
            
            result.put("success", true);
            result.put("message", "步骤删除成功");
        } catch (Exception e) {
            log.error("删除步骤失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }
}