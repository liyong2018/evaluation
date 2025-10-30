package com.evaluate.controller;

import com.evaluate.common.Result;
import com.evaluate.service.UnifiedEvaluationEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 统一评估控制器
 * 提供统一的评估计算接口，支持多种数据源类型
 *
 * @author System
 * @since 2025-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/unified-evaluation")
@CrossOrigin(origins = "*")
public class UnifiedEvaluationController {

    @Autowired
    private UnifiedEvaluationEngine unifiedEvaluationEngine;

    /**
     * 获取支持的数据源类型
     */
    @GetMapping("/data-source-types")
    public Result<Map<String, String>> getSupportedDataSourceTypes() {
        log.info("获取支持的数据源类型");
        try {
            Map<String, String> dataSourceTypes = unifiedEvaluationEngine.getSupportedDataSourceTypes();
            return Result.success(dataSourceTypes);
        } catch (Exception e) {
            log.error("获取支持的数据源类型失败", e);
            return Result.error("获取支持的数据源类型失败: " + e.getMessage());
        }
    }

    /**
     * 执行完整评估计算
     */
    @PostMapping("/execute")
    public Result<Map<String, Object>> executeEvaluation(@RequestBody Map<String, Object> request) {
        log.info("执行统一评估计算: {}", request);

        try {
            // 构建评估请求
            UnifiedEvaluationEngine.EvaluationRequest evaluationRequest = new UnifiedEvaluationEngine.EvaluationRequest();

            // 解析请求参数
            String dataSourceType = (String) request.get("dataSourceType");
            if (dataSourceType == null || dataSourceType.trim().isEmpty()) {
                return Result.error("数据源类型不能为空");
            }
            evaluationRequest.setDataSourceType(dataSourceType.trim());

            // 算法ID
            Object algorithmIdObj = request.get("algorithmId");
            if (algorithmIdObj == null) {
                return Result.error("算法ID不能为空");
            }
            evaluationRequest.setAlgorithmId(Long.valueOf(algorithmIdObj.toString()));

            // 权重配置ID
            Object weightConfigIdObj = request.get("weightConfigId");
            if (weightConfigIdObj == null) {
                return Result.error("权重配置ID不能为空");
            }
            evaluationRequest.setWeightConfigId(Long.valueOf(weightConfigIdObj.toString()));

            // 地区代码列表
            Object regionCodesObj = request.get("regionCodes");
            if (regionCodesObj == null) {
                return Result.error("地区代码列表不能为空");
            }
            @SuppressWarnings("unchecked")
            java.util.List<String> regionCodes = (java.util.List<String>) regionCodesObj;
            evaluationRequest.setRegionCodes(regionCodes);

            // 额外参数
            @SuppressWarnings("unchecked")
            Map<String, Object> additionalParams = (Map<String, Object>) request.get("additionalParams");
            if (additionalParams != null) {
                evaluationRequest.setAdditionalParams(additionalParams);
            }

            // 执行评估
            Map<String, Object> result = unifiedEvaluationEngine.executeEvaluation(evaluationRequest);

            return Result.success(result);

        } catch (NumberFormatException e) {
            log.error("参数格式错误", e);
            return Result.error("参数格式错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("执行统一评估计算失败", e);
            return Result.error("执行统一评估计算失败: " + e.getMessage());
        }
    }

    /**
     * 执行步骤评估计算
     */
    @PostMapping("/execute-step")
    public Result<Map<String, Object>> executeStepEvaluation(@RequestBody Map<String, Object> request) {
        log.info("执行统一步骤评估: {}", request);

        try {
            // 构建步骤评估请求
            UnifiedEvaluationEngine.StepEvaluationRequest stepEvaluationRequest = new UnifiedEvaluationEngine.StepEvaluationRequest();

            // 解析请求参数
            String dataSourceType = (String) request.get("dataSourceType");
            if (dataSourceType == null || dataSourceType.trim().isEmpty()) {
                return Result.error("数据源类型不能为空");
            }
            stepEvaluationRequest.setDataSourceType(dataSourceType.trim());

            // 算法ID
            Object algorithmIdObj = request.get("algorithmId");
            if (algorithmIdObj == null) {
                return Result.error("算法ID不能为空");
            }
            stepEvaluationRequest.setAlgorithmId(Long.valueOf(algorithmIdObj.toString()));

            // 权重配置ID
            Object weightConfigIdObj = request.get("weightConfigId");
            if (weightConfigIdObj == null) {
                return Result.error("权重配置ID不能为空");
            }
            stepEvaluationRequest.setWeightConfigId(Long.valueOf(weightConfigIdObj.toString()));

            // 步骤顺序
            Object stepOrderObj = request.get("stepOrder");
            if (stepOrderObj == null) {
                return Result.error("步骤顺序不能为空");
            }
            stepEvaluationRequest.setStepOrder(Integer.valueOf(stepOrderObj.toString()));

            // 地区代码列表
            Object regionCodesObj = request.get("regionCodes");
            if (regionCodesObj == null) {
                return Result.error("地区代码列表不能为空");
            }
            @SuppressWarnings("unchecked")
            java.util.List<String> regionCodes = (java.util.List<String>) regionCodesObj;
            stepEvaluationRequest.setRegionCodes(regionCodes);

            // 额外参数
            @SuppressWarnings("unchecked")
            Map<String, Object> additionalParams = (Map<String, Object>) request.get("additionalParams");
            if (additionalParams != null) {
                stepEvaluationRequest.setAdditionalParams(additionalParams);
            }

            // 执行步骤评估
            Map<String, Object> result = unifiedEvaluationEngine.executeStepEvaluation(stepEvaluationRequest);

            return Result.success(result);

        } catch (NumberFormatException e) {
            log.error("参数格式错误", e);
            return Result.error("参数格式错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("执行统一步骤评估失败", e);
            return Result.error("执行统一步骤评估失败: " + e.getMessage());
        }
    }

    /**
     * 验证评估参数
     */
    @PostMapping("/validate")
    public Result<Map<String, Object>> validateEvaluationParams(@RequestBody Map<String, Object> request) {
        log.info("验证统一评估参数: {}", request);

        try {
            Map<String, Object> validation = new java.util.HashMap<>();
            validation.put("valid", true);
            validation.put("messages", new java.util.ArrayList<String>());

            @SuppressWarnings("unchecked")
            java.util.List<String> messages = (java.util.List<String>) validation.get("messages");

            // 验证数据源类型
            String dataSourceType = (String) request.get("dataSourceType");
            if (dataSourceType == null || dataSourceType.trim().isEmpty()) {
                validation.put("valid", false);
                messages.add("数据源类型不能为空");
            } else {
                Map<String, String> supportedTypes = unifiedEvaluationEngine.getSupportedDataSourceTypes();
                if (!supportedTypes.containsKey(dataSourceType.trim().toUpperCase())) {
                    validation.put("valid", false);
                    messages.add("不支持的数据源类型: " + dataSourceType);
                }
            }

            // 验证算法ID
            Object algorithmIdObj = request.get("algorithmId");
            if (algorithmIdObj == null) {
                validation.put("valid", false);
                messages.add("算法ID不能为空");
            } else {
                try {
                    Long algorithmId = Long.valueOf(algorithmIdObj.toString());
                    validation.put("algorithmId", algorithmId);
                } catch (NumberFormatException e) {
                    validation.put("valid", false);
                    messages.add("算法ID格式错误");
                }
            }

            // 验证权重配置ID
            Object weightConfigIdObj = request.get("weightConfigId");
            if (weightConfigIdObj == null) {
                validation.put("valid", false);
                messages.add("权重配置ID不能为空");
            } else {
                try {
                    Long weightConfigId = Long.valueOf(weightConfigIdObj.toString());
                    validation.put("weightConfigId", weightConfigId);
                } catch (NumberFormatException e) {
                    validation.put("valid", false);
                    messages.add("权重配置ID格式错误");
                }
            }

            // 验证地区代码列表
            Object regionCodesObj = request.get("regionCodes");
            if (regionCodesObj == null) {
                validation.put("valid", false);
                messages.add("地区代码列表不能为空");
            } else {
                try {
                    @SuppressWarnings("unchecked")
                    java.util.List<String> regionCodes = (java.util.List<String>) regionCodesObj;
                    if (regionCodes.isEmpty()) {
                        validation.put("valid", false);
                        messages.add("地区代码列表不能为空");
                    } else {
                        validation.put("regionCount", regionCodes.size());
                    }
                } catch (ClassCastException e) {
                    validation.put("valid", false);
                    messages.add("地区代码列表格式错误");
                }
            }

            // 验证步骤顺序（如果是步骤评估）
            Object stepOrderObj = request.get("stepOrder");
            if (stepOrderObj != null) {
                try {
                    Integer stepOrder = Integer.valueOf(stepOrderObj.toString());
                    if (stepOrder <= 0) {
                        validation.put("valid", false);
                        messages.add("步骤顺序必须是正整数");
                    } else {
                        validation.put("stepOrder", stepOrder);
                    }
                } catch (NumberFormatException e) {
                    validation.put("valid", false);
                    messages.add("步骤顺序格式错误");
                }
            }

            return Result.success(validation);

        } catch (Exception e) {
            log.error("验证评估参数失败", e);
            return Result.error("验证评估参数失败: " + e.getMessage());
        }
    }

    /**
     * 获取统一评估接口的使用说明
     */
    @GetMapping("/help")
    public Result<Map<String, Object>> getApiHelp() {
        log.info("获取统一评估接口使用说明");

        try {
            Map<String, Object> help = new java.util.HashMap<>();

            // 接口基本信息
            help.put("title", "统一评估引擎接口");
            help.put("version", "1.0.0");
            help.put("description", "提供统一的评估计算接口，支持多种数据源类型（乡镇、社区等）");

            // 支持的数据源类型
            help.put("supportedDataSourceTypes", unifiedEvaluationEngine.getSupportedDataSourceTypes());

            // 接口列表
            java.util.List<Map<String, Object>> apis = new java.util.ArrayList<>();

            // 获取数据源类型
            Map<String, Object> api1 = new java.util.HashMap<>();
            api1.put("path", "GET /api/unified-evaluation/data-source-types");
            api1.put("description", "获取支持的数据源类型");
            api1.put("parameters", "无");
            api1.put("example", "curl -X GET http://localhost:8080/api/unified-evaluation/data-source-types");
            apis.add(api1);

            // 执行完整评估
            Map<String, Object> api2 = new java.util.HashMap<>();
            api2.put("path", "POST /api/unified-evaluation/execute");
            api2.put("description", "执行完整的评估计算");
            // 参数说明
            Map<String, String> parameters2 = new java.util.HashMap<>();
            parameters2.put("dataSourceType", "数据源类型（TOWNSHIP/COMMUNITY）");
            parameters2.put("algorithmId", "算法ID");
            parameters2.put("weightConfigId", "权重配置ID");
            parameters2.put("regionCodes", "地区代码列表");
            parameters2.put("additionalParams", "额外参数（可选）");
            api2.put("parameters", parameters2);

            // 示例请求
            Map<String, Object> exampleRequest2 = new java.util.HashMap<>();
            exampleRequest2.put("dataSourceType", "TOWNSHIP");
            exampleRequest2.put("algorithmId", 1);
            exampleRequest2.put("weightConfigId", 1);
            exampleRequest2.put("regionCodes", java.util.Arrays.asList("110101", "110102"));

            Map<String, Object> example2 = new java.util.HashMap<>();
            example2.put("request", exampleRequest2);
            example2.put("curl", "curl -X POST -H 'Content-Type: application/json' -d '{\"dataSourceType\":\"TOWNSHIP\",\"algorithmId\":1,\"weightConfigId\":1,\"regionCodes\":[\"110101\",\"110102\"]}' http://localhost:8080/api/unified-evaluation/execute");
            api2.put("example", example2);
            apis.add(api2);

            // 执行步骤评估
            Map<String, Object> api3 = new java.util.HashMap<>();
            api3.put("path", "POST /api/unified-evaluation/execute-step");
            api3.put("description", "执行指定步骤的评估计算");
            // 参数说明
            Map<String, String> parameters3 = new java.util.HashMap<>();
            parameters3.put("dataSourceType", "数据源类型（TOWNSHIP/COMMUNITY）");
            parameters3.put("algorithmId", "算法ID");
            parameters3.put("weightConfigId", "权重配置ID");
            parameters3.put("stepOrder", "步骤顺序");
            parameters3.put("regionCodes", "地区代码列表");
            parameters3.put("additionalParams", "额外参数（可选）");
            api3.put("parameters", parameters3);
            apis.add(api3);

            // 验证参数
            Map<String, Object> api4 = new java.util.HashMap<>();
            api4.put("path", "POST /api/unified-evaluation/validate");
            api4.put("description", "验证评估参数");
            api4.put("parameters", "与execute接口相同的参数");
            apis.add(api4);

            help.put("apis", apis);

            // 使用示例
            Map<String, Object> usageExamples = new java.util.HashMap<>();

            // 完整评估示例
            Map<String, Object> fullEvalExample = new java.util.HashMap<>();
            fullEvalExample.put("description", "执行完整的乡镇评估");
            Map<String, Object> fullEvalRequest = new java.util.HashMap<>();
            fullEvalRequest.put("dataSourceType", "TOWNSHIP");
            fullEvalRequest.put("algorithmId", 1);
            fullEvalRequest.put("weightConfigId", 1);
            fullEvalRequest.put("regionCodes", java.util.Arrays.asList("110101", "110102", "110103"));
            fullEvalExample.put("request", fullEvalRequest);

            // 步骤评估示例
            Map<String, Object> stepEvalExample = new java.util.HashMap<>();
            stepEvalExample.put("description", "只执行第3步的计算");
            Map<String, Object> stepEvalRequest = new java.util.HashMap<>();
            stepEvalRequest.put("dataSourceType", "COMMUNITY");
            stepEvalRequest.put("algorithmId", 2);
            stepEvalRequest.put("weightConfigId", 1);
            stepEvalRequest.put("stepOrder", 3);
            stepEvalRequest.put("regionCodes", java.util.Arrays.asList("110101001", "110101002"));
            stepEvalExample.put("request", stepEvalRequest);

            usageExamples.put("完整评估", fullEvalExample);
            usageExamples.put("步骤评估", stepEvalExample);

            help.put("usageExamples", usageExamples);

            return Result.success(help);

        } catch (Exception e) {
            log.error("获取接口使用说明失败", e);
            return Result.error("获取接口使用说明失败: " + e.getMessage());
        }
    }
}