package com.evaluate.service;

import com.evaluate.dto.topsis.TOPSISDiagnosticReport;
import com.evaluate.service.impl.TOPSISDiagnosticServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * TOPSIS诊断服务测试类
 * 
 * @author System
 * @since 2025-01-01
 */
public class TOPSISDiagnosticServiceTest {
    
    @Mock
    private TOPSISCalculator topsisCalculator;
    
    @Mock
    private TOPSISConfigurationAnalyzer configurationAnalyzer;
    
    @InjectMocks
    private TOPSISDiagnosticServiceImpl diagnosticService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    void testDiagnoseWeightedData_WithValidData() {
        // 准备测试数据
        Map<String, Map<String, Double>> weightedData = createValidWeightedData();
        
        // 模拟TOPSIS计算器行为
        when(topsisCalculator.calculateDistances(any())).thenReturn(createValidTopsisResults());
        when(topsisCalculator.calculateIdealSolutions(any())).thenReturn(createValidIdealSolution());
        
        // 执行诊断
        TOPSISDiagnosticReport report = diagnosticService.diagnoseWeightedData(weightedData, 1L, "step4");
        
        // 验证结果
        assertNotNull(report);
        assertEquals(1L, report.getModelId());
        assertEquals("step4", report.getStepCode());
        assertNotNull(report.getMetrics());
        assertNotNull(report.getInputDataSummary());
        assertNotNull(report.getCalculationDetails());
    }
    
    @Test
    void testDiagnoseWeightedData_WithEmptyData() {
        // 准备空数据
        Map<String, Map<String, Double>> emptyData = new HashMap<>();
        
        // 执行诊断
        TOPSISDiagnosticReport report = diagnosticService.diagnoseWeightedData(emptyData, 1L, "step4");
        
        // 验证结果
        assertNotNull(report);
        assertTrue(report.isHasIssues());
        assertTrue(report.getIssues().contains("定权数据为空"));
    }
    
    @Test
    void testValidateInputData_WithValidData() {
        // 准备测试数据
        Map<String, Map<String, Double>> weightedData = createValidWeightedData();
        
        // 执行验证
        Map<String, Object> validation = diagnosticService.validateInputData(weightedData);
        
        // 验证结果
        assertNotNull(validation);
        assertTrue((Boolean) validation.get("valid"));
        assertEquals(2, validation.get("regionCount"));
        assertEquals(3, validation.get("indicatorCount"));
    }
    
    @Test
    void testValidateInputData_WithInvalidData() {
        // 准备包含无效数据的测试数据
        Map<String, Map<String, Double>> invalidData = createInvalidWeightedData();
        
        // 执行验证
        Map<String, Object> validation = diagnosticService.validateInputData(invalidData);
        
        // 验证结果
        assertNotNull(validation);
        assertFalse((Boolean) validation.get("valid"));
        @SuppressWarnings("unchecked")
        List<String> issues = (List<String>) validation.get("issues");
        assertFalse(issues.isEmpty());
    }
    
    @Test
    void testGenerateCalculationLog() {
        // 准备测试数据
        Map<String, Map<String, Double>> weightedData = createValidWeightedData();
        
        // 模拟TOPSIS计算器行为
        when(topsisCalculator.calculateIdealSolutions(any())).thenReturn(createValidIdealSolution());
        when(topsisCalculator.calculateDistances(any())).thenReturn(createValidTopsisResults());
        
        // 执行日志生成
        Map<String, Object> log = diagnosticService.generateCalculationLog(weightedData, 1L, "step4");
        
        // 验证结果
        assertNotNull(log);
        assertEquals(1L, log.get("modelId"));
        assertEquals("step4", log.get("stepCode"));
        assertNotNull(log.get("steps"));
        assertEquals(2, log.get("inputDataSize"));
    }
    
    /**
     * 创建有效的定权数据
     */
    private Map<String, Map<String, Double>> createValidWeightedData() {
        Map<String, Map<String, Double>> data = new HashMap<>();
        
        Map<String, Double> region1 = new HashMap<>();
        region1.put("indicator1", 10.0);
        region1.put("indicator2", 20.0);
        region1.put("indicator3", 30.0);
        data.put("region1", region1);
        
        Map<String, Double> region2 = new HashMap<>();
        region2.put("indicator1", 15.0);
        region2.put("indicator2", 25.0);
        region2.put("indicator3", 35.0);
        data.put("region2", region2);
        
        return data;
    }
    
    /**
     * 创建包含无效数据的定权数据
     */
    private Map<String, Map<String, Double>> createInvalidWeightedData() {
        Map<String, Map<String, Double>> data = new HashMap<>();
        
        Map<String, Double> region1 = new HashMap<>();
        region1.put("indicator1", Double.NaN);
        region1.put("indicator2", Double.POSITIVE_INFINITY);
        region1.put("indicator3", null);
        data.put("region1", region1);
        
        data.put("region2", null); // 空区域数据
        
        return data;
    }
    
    /**
     * 创建有效的TOPSIS计算结果
     */
    private Map<String, Map<String, Double>> createValidTopsisResults() {
        Map<String, Map<String, Double>> results = new HashMap<>();
        
        Map<String, Double> region1Results = new HashMap<>();
        region1Results.put("comprehensive_positive", 5.0);
        region1Results.put("comprehensive_negative", 3.0);
        results.put("region1", region1Results);
        
        Map<String, Double> region2Results = new HashMap<>();
        region2Results.put("comprehensive_positive", 4.0);
        region2Results.put("comprehensive_negative", 6.0);
        results.put("region2", region2Results);
        
        return results;
    }
    
    /**
     * 创建有效的理想解
     */
    private TOPSISCalculator.IdealSolution createValidIdealSolution() {
        Map<String, Double> positiveIdeal = new HashMap<>();
        positiveIdeal.put("indicator1", 15.0);
        positiveIdeal.put("indicator2", 25.0);
        positiveIdeal.put("indicator3", 35.0);
        
        Map<String, Double> negativeIdeal = new HashMap<>();
        negativeIdeal.put("indicator1", 10.0);
        negativeIdeal.put("indicator2", 20.0);
        negativeIdeal.put("indicator3", 30.0);
        
        return new TOPSISCalculator.IdealSolution(positiveIdeal, negativeIdeal);
    }
}