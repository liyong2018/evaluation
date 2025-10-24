package com.evaluate.service;

import com.evaluate.dto.topsis.TOPSISAlgorithmConfig;
import com.evaluate.service.impl.TOPSISResultValidatorImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TOPSIS结果验证器测试类
 * 
 * 测试TOPSIS计算结果验证和修复功能的核心逻辑
 * 
 * @author System
 * @since 2025-01-01
 */
class TOPSISResultValidatorTest {
    
    private TOPSISResultValidator validator;
    private TOPSISAlgorithmConfig algorithmConfig;
    
    @BeforeEach
    void setUp() {
        validator = new TOPSISResultValidatorImpl();
        
        algorithmConfig = TOPSISAlgorithmConfig.builder()
                .stepId(1L)
                .algorithmCode("TOPSIS")
                .indicators(Arrays.asList("indicator1", "indicator2", "indicator3"))
                .outputParam("comprehensive_score")
                .isPositiveDistance(true)
                .build();
    }
    
    @Test
    void testValidateResults_ValidData() {
        // 准备有效的TOPSIS结果数据
        Map<String, Map<String, Double>> topsisResults = new HashMap<>();
        
        Map<String, Double> region1 = new HashMap<>();
        region1.put("comprehensive_score_positive", 0.5);
        region1.put("comprehensive_score_negative", 0.3);
        region1.put("comprehensive_score", 0.375); // 0.3 / (0.3 + 0.5)
        topsisResults.put("region1", region1);
        
        Map<String, Double> region2 = new HashMap<>();
        region2.put("comprehensive_score_positive", 0.8);
        region2.put("comprehensive_score_negative", 0.2);
        region2.put("comprehensive_score", 0.2); // 0.2 / (0.2 + 0.8)
        topsisResults.put("region2", region2);
        
        // 执行验证
        TOPSISResultValidator.ValidationResult result = validator.validateResults(topsisResults, algorithmConfig);
        
        // 验证结果
        assertTrue(result.isValid(), "有效数据应该通过验证");
        assertTrue(result.getIssues().isEmpty(), "有效数据不应该有问题");
        assertEquals(2, result.getStatistics().get("totalRegions"));
        assertEquals(2, result.getStatistics().get("validRegions"));
    }
    
    @Test
    void testValidateResults_InvalidData() {
        // 准备无效的TOPSIS结果数据
        Map<String, Map<String, Double>> topsisResults = new HashMap<>();
        
        Map<String, Double> region1 = new HashMap<>();
        region1.put("comprehensive_score_positive", Double.NaN); // Invalid value
        region1.put("comprehensive_score_negative", -0.3); // Negative value
        region1.put("comprehensive_score", 1.5); // Out of range
        topsisResults.put("region1", region1);
        
        // 执行验证
        TOPSISResultValidator.ValidationResult result = validator.validateResults(topsisResults, algorithmConfig);
        
        // 验证结果
        assertFalse(result.isValid(), "Invalid data should fail validation");
        assertFalse(result.getIssues().isEmpty(), "Invalid data should have issues");
        assertEquals(1, result.getStatistics().get("totalRegions"));
        assertEquals(0, result.getStatistics().get("validRegions"));
    }
    
    @Test
    void testValidateRegionResult_ValidRegion() {
        // Prepare valid region result
        Map<String, Double> regionResult = new HashMap<>();
        regionResult.put("comprehensive_score_positive", 0.6);
        regionResult.put("comprehensive_score_negative", 0.4);
        regionResult.put("comprehensive_score", 0.4); // 0.4 / (0.4 + 0.6)
        
        // 执行验证
        TOPSISResultValidator.RegionValidationResult result = 
            validator.validateRegionResult("test_region", regionResult, algorithmConfig);
        
        // 验证结果
        assertTrue(result.isValid(), "Valid region result should pass validation");
        assertTrue(result.getIssues().isEmpty(), "Valid region result should have no issues");
        assertEquals("test_region", result.getRegionCode());
    }
    
    @Test
    void testValidateRegionResult_NaNValues() {
        // Prepare region result with NaN values
        Map<String, Double> regionResult = new HashMap<>();
        regionResult.put("comprehensive_score_positive", Double.NaN);
        regionResult.put("comprehensive_score_negative", 0.4);
        regionResult.put("comprehensive_score", Double.NaN);
        
        // 执行验证
        TOPSISResultValidator.RegionValidationResult result = 
            validator.validateRegionResult("test_region", regionResult, algorithmConfig);
        
        // 验证结果
        assertFalse(result.isValid(), "Result with NaN values should fail validation");
        assertFalse(result.getIssues().isEmpty(), "Should report NaN issues");
        assertTrue(result.getIssues().stream().anyMatch(issue -> issue.contains("NaN")));
    }
    
    @Test
    void testRepairResults_FixInvalidValues() {
        // Prepare TOPSIS results that need repair
        Map<String, Map<String, Double>> topsisResults = new HashMap<>();
        
        Map<String, Double> region1 = new HashMap<>();
        region1.put("comprehensive_score_positive", Double.NaN);
        region1.put("comprehensive_score_negative", -0.3);
        region1.put("comprehensive_score", 1.5);
        topsisResults.put("region1", region1);
        
        // 执行修复
        Map<String, Map<String, Double>> repairedResults = 
            validator.repairResults(topsisResults, algorithmConfig);
        
        // 验证修复结果
        assertNotNull(repairedResults);
        assertTrue(repairedResults.containsKey("region1"));
        
        Map<String, Double> repairedRegion = repairedResults.get("region1");
        
        // Verify repaired values
        Double positiveDistance = repairedRegion.get("comprehensive_score_positive");
        Double negativeDistance = repairedRegion.get("comprehensive_score_negative");
        Double comprehensiveScore = repairedRegion.get("comprehensive_score");
        
        assertNotNull(positiveDistance);
        assertNotNull(negativeDistance);
        assertNotNull(comprehensiveScore);
        
        assertFalse(Double.isNaN(positiveDistance), "Repaired positive distance should not be NaN");
        assertTrue(negativeDistance >= 0, "Repaired negative distance should be non-negative");
        assertTrue(comprehensiveScore >= 0 && comprehensiveScore <= 1, "Repaired comprehensive score should be in [0,1] range");
    }
    
    @Test
    void testRepairRegionResult_ZeroDistances() {
        // Prepare region result with zero distances
        Map<String, Double> regionResult = new HashMap<>();
        regionResult.put("comprehensive_score_positive", 0.0);
        regionResult.put("comprehensive_score_negative", 0.0);
        regionResult.put("comprehensive_score", 0.0);
        
        // 执行修复
        Map<String, Double> repairedResult = 
            validator.repairRegionResult("test_region", regionResult, algorithmConfig);
        
        // 验证修复结果
        assertNotNull(repairedResult);
        
        Double comprehensiveScore = repairedResult.get("comprehensive_score");
        assertNotNull(comprehensiveScore);
        assertEquals(0.5, comprehensiveScore, 0.001, "Zero distance case should use default comprehensive score 0.5");
    }
    
    @Test
    void testValidateResults_EmptyData() {
        // Test empty data
        Map<String, Map<String, Double>> emptyResults = new HashMap<>();
        
        TOPSISResultValidator.ValidationResult result = validator.validateResults(emptyResults, algorithmConfig);
        
        assertFalse(result.isValid(), "Empty data should fail validation");
        assertTrue(result.getIssues().stream().anyMatch(issue -> issue.contains("空")));
    }
    
    @Test
    void testValidateResults_InvalidConfig() {
        // Test invalid configuration
        TOPSISAlgorithmConfig invalidConfig = TOPSISAlgorithmConfig.builder()
                .stepId(null) // Invalid configuration
                .build();
        
        Map<String, Map<String, Double>> topsisResults = new HashMap<>();
        Map<String, Double> region1 = new HashMap<>();
        region1.put("score", 0.5);
        topsisResults.put("region1", region1);
        
        TOPSISResultValidator.ValidationResult result = validator.validateResults(topsisResults, invalidConfig);
        
        assertFalse(result.isValid(), "Invalid configuration should fail validation");
        assertTrue(result.getIssues().stream().anyMatch(issue -> issue.contains("配置无效")));
    }
}