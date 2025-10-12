package com.evaluate.controller;

import com.evaluate.common.Result;
import com.evaluate.entity.AlgorithmConfig;
import com.evaluate.service.AlgorithmManagementService;
import com.evaluate.service.ModelExecutionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 算法步骤执行控制器
 * 专门处理算法步骤的分步执行和管理
 * 
 * @author System
 * @since 2025-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/algorithm-step-execution")
@CrossOrigin(origins = "*")
public class AlgorithmStepExecutionController {
    
    @Autowired
    private ModelExecutionService modelExecutionService;
    
    @Autowired
    private AlgorithmManagementService algorithmManagementService;
    
    /**
     * 获取算法的所有步骤信息（用于显示步骤按钮）
     * 
     * @param algorithmId 算法ID
     * @return 算法步骤列表信息
     */
    @GetMapping("/{algorithmId}/steps")
    public Result<Map<String, Object>> getAlgorithmSteps(@PathVariable Long algorithmId) {
        log.info("获取算法步骤列表, algorithmId={}", algorithmId);
        try {
            Map<String, Object> stepsInfo = modelExecutionService.getAlgorithmStepsInfo(algorithmId);
            return Result.success(stepsInfo);
        } catch (Exception e) {
            log.error("获取算法步骤列表失败", e);
            return Result.error("获取算法步骤列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行指定步骤并返回2D表格结果
     * 
     * @param algorithmId 算法ID
     * @param stepOrder 步骤顺序
     * @param request 执行参数（regionCodes, weightConfigId）
     * @return 步骤执行结果和2D表格
     */
    @PostMapping("/{algorithmId}/step/{stepOrder}/execute")
    public Result<Map<String, Object>> executeStep(
            @PathVariable Long algorithmId,
            @PathVariable Integer stepOrder,
            @RequestBody Map<String, Object> request) {
        log.info("执行算法步骤, algorithmId={}, stepOrder={}", algorithmId, stepOrder);
        
        try {
            // 解析请求参数
            @SuppressWarnings("unchecked")
            List<String> regionCodes = (List<String>) request.get("regionCodes");
            Long weightConfigId = request.get("weightConfigId") != null 
                    ? Long.valueOf(request.get("weightConfigId").toString()) : null;
            
            // 参数验证
            if (regionCodes == null || regionCodes.isEmpty()) {
                return Result.error("地区代码列表不能为空");
            }
            
            if (stepOrder == null || stepOrder <= 0) {
                return Result.error("步骤顺序必须是正整数");
            }
            
            // 执行步骤
            Map<String, Object> result = modelExecutionService.executeAlgorithmStep(
                    algorithmId, stepOrder, regionCodes, weightConfigId);
            
            log.info("算法步骤 {} 执行完成", stepOrder);
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("执行算法步骤失败", e);
            return Result.error("执行算法步骤失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行多个步骤直到指定步骤
     * 
     * @param algorithmId 算法ID
     * @param upToStepOrder 执行到第几步
     * @param request 执行参数
     * @return 所有已执行步骤的结果
     */
    @PostMapping("/{algorithmId}/steps/execute-up-to/{upToStepOrder}")
    public Result<Map<String, Object>> executeStepsUpTo(
            @PathVariable Long algorithmId,
            @PathVariable Integer upToStepOrder,
            @RequestBody Map<String, Object> request) {
        log.info("批量执行算法步骤到第{}步, algorithmId={}", upToStepOrder, algorithmId);
        
        try {
            // 解析请求参数
            @SuppressWarnings("unchecked")
            List<String> regionCodes = (List<String>) request.get("regionCodes");
            Long weightConfigId = request.get("weightConfigId") != null 
                    ? Long.valueOf(request.get("weightConfigId").toString()) : null;
            
            // 参数验证
            if (regionCodes == null || regionCodes.isEmpty()) {
                return Result.error("地区代码列表不能为空");
            }
            
            if (upToStepOrder == null || upToStepOrder <= 0) {
                return Result.error("步骤顺序必须是正整数");
            }
            
            // 执行步骤
            Map<String, Object> result = modelExecutionService.executeAlgorithmStepsUpTo(
                    algorithmId, upToStepOrder, regionCodes, weightConfigId);
            
            log.info("批量执行算法步骤到第{}步完成", upToStepOrder);
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("批量执行算法步骤失败", e);
            return Result.error("批量执行算法步骤失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取算法的详细配置信息（包括步骤和公式）
     * 
     * @param algorithmId 算法ID
     * @return 算法详细信息
     */
    @GetMapping("/{algorithmId}/detail")
    public Result<Map<String, Object>> getAlgorithmDetail(@PathVariable Long algorithmId) {
        log.info("获取算法详细信息, algorithmId={}", algorithmId);
        try {
            Map<String, Object> detail = algorithmManagementService.getAlgorithmDetail(algorithmId);
            return Result.success(detail);
        } catch (Exception e) {
            log.error("获取算法详细信息失败", e);
            return Result.error("获取算法详细信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取算法列表（用于选择算法）
     * 
     * @return 算法列表
     */
    @GetMapping("/algorithms")
    public Result<List<Map<String, Object>>> getAlgorithmList() {
        log.info("获取算法列表");
        try {
            List<AlgorithmConfig> algorithms = algorithmManagementService.getAlgorithmList();
            
            // 转换为简化的信息格式
            List<Map<String, Object>> simplifiedList = algorithms.stream().map(algorithm -> {
                Map<String, Object> item = new java.util.HashMap<>();
                item.put("id", algorithm.getId());
                item.put("name", algorithm.getConfigName());
                item.put("description", algorithm.getDescription());
                item.put("version", algorithm.getVersion());
                item.put("status", algorithm.getStatus());
                item.put("createTime", algorithm.getCreateTime());
                return item;
            }).collect(java.util.stream.Collectors.toList());
            
            return Result.success(simplifiedList);
        } catch (Exception e) {
            log.error("获取算法列表失败", e);
            return Result.error("获取算法列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证算法步骤的执行参数
     * 
     * @param algorithmId 算法ID
     * @param request 包含regionCodes和weightConfigId的请求
     * @return 验证结果
     */
    @PostMapping("/{algorithmId}/validate-params")
    public Result<Map<String, Object>> validateExecutionParams(
            @PathVariable Long algorithmId,
            @RequestBody Map<String, Object> request) {
        log.info("验证算法执行参数, algorithmId={}", algorithmId);
        
        try {
            @SuppressWarnings("unchecked")
            List<String> regionCodes = (List<String>) request.get("regionCodes");
            Long weightConfigId = request.get("weightConfigId") != null 
                    ? Long.valueOf(request.get("weightConfigId").toString()) : null;
            
            Map<String, Object> validation = new java.util.HashMap<>();
            validation.put("valid", true);
            validation.put("messages", new java.util.ArrayList<String>());
            
            @SuppressWarnings("unchecked")
            List<String> messages = (List<String>) validation.get("messages");
            
            // 验证算法是否存在
            try {
                Map<String, Object> stepsInfo = modelExecutionService.getAlgorithmStepsInfo(algorithmId);
                if (!Boolean.TRUE.equals(stepsInfo.get("success"))) {
                    validation.put("valid", false);
                    messages.add("算法配置不存在或无效");
                }
            } catch (Exception e) {
                validation.put("valid", false);
                messages.add("算法配置验证失败: " + e.getMessage());
            }
            
            // 验证地区代码
            if (regionCodes == null || regionCodes.isEmpty()) {
                validation.put("valid", false);
                messages.add("地区代码列表不能为空");
            } else {
                validation.put("regionCount", regionCodes.size());
            }
            
            // 验证权重配置（可选）
            if (weightConfigId != null) {
                validation.put("hasWeightConfig", true);
                validation.put("weightConfigId", weightConfigId);
            } else {
                validation.put("hasWeightConfig", false);
            }
            
            return Result.success(validation);
            
        } catch (Exception e) {
            log.error("验证算法执行参数失败", e);
            return Result.error("验证算法执行参数失败: " + e.getMessage());
        }
    }
}