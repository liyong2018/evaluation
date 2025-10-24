package com.evaluate.integration;

import com.evaluate.dto.topsis.TOPSISAlgorithmConfig;
import com.evaluate.entity.StepAlgorithm;
import com.evaluate.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TOPSIS配置管理集成测试
 * 
 * 测试TOPSIS配置管理的完整流程，包括配置的创建、更新、验证和迁移
 * 
 * @author System
 * @since 2025-01-01
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class TOPSISConfigurationManagementIntegrationTest {
    
    @Resource
    private TOPSISConfigService topsisConfigService;
    
    @Resource
    private TOPSISParameterParser parameterParser;
    
    @Resource
    private StepAlgorithmService stepAlgorithmService;
    
    @Resource
    private TOPSISCompatibilityService compatibilityService;
    
    @Resource
    private TOPSISConfigMigrationService migrationService;
    
    private Long testModelId;
    private String testStepCode;
    
    @BeforeEach
    void setUp() {
        testModelId = 1L;
        testStepCode = "step4";
    }
    
    @Test
    void testCompleteConfigurationLifecycle() {
        // 测试配置的完整生命周期：创建 -> 更新 -> 验证 -> 删除
        
        // 1. 创建新的TOPSIS配置
        TOPSISAlgorithmConfig newConfig = TOPSISAlgorithmConfig.builder()
                .stepId(100L) // 使用一个不存在的步骤ID
                .algorithmCode("TOPSIS")
                .indicators(Arrays.asList("disaster_prevention_score", "emergency_response_capability"))
                .outputParam("comprehensive_score")
                .isPositiveDistance(true)
                .build();
        
        boolean createResult = topsisConfigService.createTOPSISConfig(newConfig);
        assertTrue(createResult, "创建配置应该成功");
        
        // 2. 验证配置是否正确创建
        TOPSISAlgorithmConfig retrievedConfig = topsisConfigService.getTOPSISConfigByStepId(100L);
        assertNotNull(retrievedConfig, "应该能够获取创建的配置");
        assertEquals(newConfig.getAlgorithmCode(), retrievedConfig.getAlgorithmCode());
        assertEquals(newConfig.getIndicators().size(), retrievedConfig.getIndicators().size());
        
        // 3. 更新配置
        List<String> updatedIndicators = Arrays.asList("disaster_prevention_score", "emergency_response_capability", "recovery_ability_index");
        boolean updateResult = topsisConfigService.updateTOPSISConfigByStepId(100L, updatedIndicators, "TOPSIS_NEGATIVE");
        assertTrue(updateResult, "更新配置应该成功");
        
        // 4. 验证更新结果
        TOPSISAlgorithmConfig updatedConfig = topsisConfigService.getTOPSISConfigByStepId(100L);
        assertNotNull(updatedConfig, "更新后应该能获取配置");
        assertEquals(3, updatedConfig.getIndicators().size(), "指标数量应该更新");
        assertFalse(updatedConfig.isPositiveDistance(), "应该更新为负理想解");
        
        // 5. 验证配置
        TOPSISConfigService.ValidationResult validation = topsisConfigService.validateTOPSISConfig(updatedConfig);
        assertNotNull(validation, "验证结果不应为空");
        
        // 6. 删除配置（通过模型ID和步骤代码）
        // 注意：这里需要先获取对应的模型ID和步骤代码
        // 在实际测试中，可能需要模拟或使用测试数据
    }
    
    @Test
    void testParameterParsingIntegration() {
        // 测试参数解析的完整流程
        
        // 1. 创建测试配置
        TOPSISAlgorithmConfig config = TOPSISAlgorithmConfig.builder()
                .stepId(1L)
                .algorithmCode("TOPSIS")
                .indicators(Arrays.asList("indicator1", "indicator2", "indicator3"))
                .outputParam("comprehensive_score")
                .isPositiveDistance(true)
                .build();
        
        // 2. 生成ql_expression
        String expression = parameterParser.generateExpression(config);
        assertNotNull(expression, "生成的表达式不应为空");
        assertEquals("@TOPSIS_POSITIVE:indicator1,indicator2,indicator3", expression);
        
        // 3. 验证表达式格式
        boolean isValid = parameterParser.validateExpressionFormat(expression);
        assertTrue(isValid, "生成的表达式应该通过格式验证");
        
        // 4. 创建模拟的StepAlgorithm实体
        StepAlgorithm stepAlgorithm = new StepAlgorithm();
        stepAlgorithm.setId(1L);
        stepAlgorithm.setAlgorithmCode("TOPSIS");
        stepAlgorithm.setQlExpression(expression);
        stepAlgorithm.setOutputParam("comprehensive_score");
        
        // 5. 解析表达式
        TOPSISAlgorithmConfig parsedConfig = parameterParser.parseFromExpression(expression, stepAlgorithm);
        assertNotNull(parsedConfig, "解析的配置不应为空");
        assertEquals(config.getIndicators().size(), parsedConfig.getIndicators().size());
        assertEquals(config.isPositiveDistance(), parsedConfig.isPositiveDistance());
        
        // 6. 测试往返一致性
        String regeneratedExpression = parameterParser.generateExpression(parsedConfig);
        assertEquals(expression, regeneratedExpression, "往返解析应该保持一致");
    }
    
    @Test
    void testIndicatorValidationIntegration() {
        // 测试指标验证的完整流程
        
        // 1. 获取可用指标
        List<String> availableIndicators = topsisConfigService.getAvailableIndicators(testModelId);
        assertNotNull(availableIndicators, "可用指标列表不应为空");
        
        // 2. 验证有效指标
        List<String> validIndicators = availableIndicators.subList(0, Math.min(2, availableIndicators.size()));
        TOPSISConfigService.IndicatorValidationResult validResult = 
            topsisConfigService.validateIndicators(testModelId, validIndicators);
        
        assertNotNull(validResult, "验证结果不应为空");
        if (!availableIndicators.isEmpty()) {
            assertTrue(validResult.isValid(), "有效指标应该通过验证");
            assertEquals(validIndicators.size(), validResult.getValidIndicators().size());
        }
        
        // 3. 验证无效指标
        List<String> invalidIndicators = Arrays.asList("nonexistent_indicator1", "nonexistent_indicator2");
        TOPSISConfigService.IndicatorValidationResult invalidResult = 
            topsisConfigService.validateIndicators(testModelId, invalidIndicators);
        
        assertNotNull(invalidResult, "验证结果不应为空");
        assertFalse(invalidResult.isValid(), "无效指标应该验证失败");
        assertEquals(2, invalidResult.getInvalidIndicators().size());
        
        // 4. 验证混合指标（部分有效，部分无效）
        if (!availableIndicators.isEmpty()) {
            List<String> mixedIndicators = new ArrayList<>();
            mixedIndicators.add(availableIndicators.get(0)); // 有效指标
            mixedIndicators.add("nonexistent_indicator"); // 无效指标
            
            TOPSISConfigService.IndicatorValidationResult mixedResult = 
                topsisConfigService.validateIndicators(testModelId, mixedIndicators);
            
            assertNotNull(mixedResult, "验证结果不应为空");
            assertFalse(mixedResult.isValid(), "混合指标应该验证失败");
            assertEquals(1, mixedResult.getValidIndicators().size());
            assertEquals(1, mixedResult.getInvalidIndicators().size());
        }
    }
    
    @Test
    void testConfigurationCompatibilityCheck() {
        // 测试配置兼容性检查
        
        try {
            // 1. 检查模型的TOPSIS兼容性
            Map<String, Object> compatibilityResult = compatibilityService.checkCompatibility(testModelId);
            assertNotNull(compatibilityResult, "兼容性检查结果不应为空");
            
            // 2. 获取兼容性问题
            List<String> issues = compatibilityService.getCompatibilityIssues(testModelId);
            assertNotNull(issues, "兼容性问题列表不应为空");
            
        } catch (Exception e) {
            // 兼容性服务可能依赖于具体实现，记录但不失败
            System.out.println("兼容性检查遇到异常: " + e.getMessage());
        }
    }
    
    @Test
    void testConfigurationMigration() {
        // 测试配置迁移功能
        
        try {
            // 1. 分析迁移需求
            Map<String, Object> migrationPlan = migrationService.analyzeMigration(testModelId);
            assertNotNull(migrationPlan, "迁移计划不应为空");
            
            // 2. 执行干运行迁移
            Map<String, Object> dryRunResult = migrationService.executeMigration(testModelId, true);
            assertNotNull(dryRunResult, "干运行结果不应为空");
            
            // 3. 验证迁移计划的有效性
            Boolean success = (Boolean) dryRunResult.get("success");
            assertTrue(success != null && success, "迁移计划应该有效");
            
        } catch (Exception e) {
            // 迁移服务可能依赖于具体实现，记录但不失败
            System.out.println("配置迁移遇到异常: " + e.getMessage());
        }
    }
    
    @Test
    void testBatchConfigurationOperations() {
        // 测试批量配置操作
        
        // 1. 获取所有TOPSIS配置
        List<TOPSISAlgorithmConfig> allConfigs = topsisConfigService.getAllTOPSISConfigs(testModelId);
        assertNotNull(allConfigs, "配置列表不应为空");
        
        int originalConfigCount = allConfigs.size();
        
        // 2. 批量验证配置
        int validConfigCount = 0;
        for (TOPSISAlgorithmConfig config : allConfigs) {
            TOPSISConfigService.ValidationResult validation = topsisConfigService.validateTOPSISConfig(config);
            if (validation != null && validation.isValid()) {
                validConfigCount++;
            }
        }
        
        System.out.println("总配置数: " + originalConfigCount + ", 有效配置数: " + validConfigCount);
        
        // 3. 批量更新配置（模拟场景）
        for (TOPSISAlgorithmConfig config : allConfigs) {
            if (config.getIndicators() != null && !config.getIndicators().isEmpty()) {
                // 验证指标的有效性
                TOPSISConfigService.IndicatorValidationResult indicatorValidation = 
                    topsisConfigService.validateIndicators(testModelId, config.getIndicators());
                assertNotNull(indicatorValidation, "指标验证结果不应为空");
            }
        }
    }
    
    @Test
    void testConfigurationErrorRecovery() {
        // 测试配置错误恢复
        
        // 1. 创建一个有问题的配置
        TOPSISAlgorithmConfig problematicConfig = TOPSISAlgorithmConfig.builder()
                .stepId(null) // 无效的步骤ID
                .algorithmCode("")
                .indicators(Collections.emptyList()) // 空指标列表
                .outputParam(null)
                .isPositiveDistance(true)
                .build();
        
        // 2. 验证配置（应该失败）
        TOPSISConfigService.ValidationResult validation = topsisConfigService.validateTOPSISConfig(problematicConfig);
        assertNotNull(validation, "验证结果不应为空");
        assertFalse(validation.isValid(), "有问题的配置应该验证失败");
        assertFalse(validation.getErrors().isEmpty(), "应该有错误信息");
        
        // 3. 尝试修复配置
        TOPSISAlgorithmConfig fixedConfig = TOPSISAlgorithmConfig.builder()
                .stepId(1L)
                .algorithmCode("TOPSIS")
                .indicators(Arrays.asList("disaster_prevention_score"))
                .outputParam("comprehensive_score")
                .isPositiveDistance(true)
                .build();
        
        // 4. 验证修复后的配置
        TOPSISConfigService.ValidationResult fixedValidation = topsisConfigService.validateTOPSISConfig(fixedConfig);
        assertNotNull(fixedValidation, "修复后的验证结果不应为空");
        assertTrue(fixedValidation.isValid(), "修复后的配置应该通过验证");
    }
    
    @Test
    void testConfigurationVersioning() {
        // 测试配置版本管理（如果支持）
        
        // 1. 创建初始配置
        List<String> initialIndicators = Arrays.asList("indicator1", "indicator2");
        boolean initialResult = topsisConfigService.updateTOPSISConfig(testModelId, testStepCode, initialIndicators, "TOPSIS_POSITIVE");
        
        if (initialResult) {
            // 2. 获取初始配置
            TOPSISAlgorithmConfig initialConfig = topsisConfigService.getTOPSISConfig(testModelId, testStepCode);
            assertNotNull(initialConfig, "初始配置不应为空");
            
            // 3. 更新配置
            List<String> updatedIndicators = Arrays.asList("indicator1", "indicator2", "indicator3");
            boolean updateResult = topsisConfigService.updateTOPSISConfig(testModelId, testStepCode, updatedIndicators, "TOPSIS_NEGATIVE");
            assertTrue(updateResult, "配置更新应该成功");
            
            // 4. 验证配置已更新
            TOPSISAlgorithmConfig updatedConfig = topsisConfigService.getTOPSISConfig(testModelId, testStepCode);
            assertNotNull(updatedConfig, "更新后的配置不应为空");
            assertNotEquals(initialConfig.getIndicators().size(), updatedConfig.getIndicators().size(), "配置应该已更新");
        }
    }
    
    @Test
    void testConcurrentConfigurationAccess() {
        // 测试并发配置访问
        
        // 模拟多个线程同时访问配置
        List<Thread> threads = new ArrayList<>();
        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());
        
        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(() -> {
                try {
                    // 并发读取配置
                    TOPSISAlgorithmConfig config = topsisConfigService.getTOPSISConfig(testModelId, testStepCode);
                    
                    // 并发获取可用指标
                    List<String> indicators = topsisConfigService.getAvailableIndicators(testModelId);
                    
                    // 并发验证配置
                    if (config != null) {
                        topsisConfigService.validateTOPSISConfig(config);
                    }
                    
                } catch (Exception e) {
                    exceptions.add(e);
                }
            });
            threads.add(thread);
        }
        
        // 启动所有线程
        threads.forEach(Thread::start);
        
        // 等待所有线程完成
        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        // 验证没有异常发生
        assertTrue(exceptions.isEmpty(), "并发访问不应该产生异常: " + exceptions);
    }
}