package com.evaluate.service;

import com.evaluate.dto.topsis.TOPSISAlgorithmConfig;
import com.evaluate.entity.StepAlgorithm;
import com.evaluate.service.impl.TOPSISParameterParserImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * TOPSIS参数解析器测试类
 * 
 * 测试从ql_expression字段解析TOPSIS算法参数的功能
 * 
 * @author System
 * @since 2025-01-01
 */
class TOPSISParameterParserTest {
    
    @Mock
    private StepAlgorithmService stepAlgorithmService;
    
    @InjectMocks
    private TOPSISParameterParserImpl parameterParser;
    
    private StepAlgorithm testStepAlgorithm;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // 设置测试用的步骤算法实体
        testStepAlgorithm = new StepAlgorithm();
        testStepAlgorithm.setId(1L);
        testStepAlgorithm.setAlgorithmCode("TOPSIS");
        testStepAlgorithm.setOutputParam("comprehensive_score");
    }
    
    @Test
    void testParseFromExpression_ValidPositiveExpression() {
        // 测试有效的正理想解表达式
        String qlExpression = "@TOPSIS_POSITIVE:indicator1,indicator2,indicator3";
        
        // 执行解析
        TOPSISAlgorithmConfig config = parameterParser.parseFromExpression(
            qlExpression, testStepAlgorithm
        );
        
        // 验证结果
        assertNotNull(config, "配置不应为空");
        assertEquals(1L, config.getStepId(), "步骤ID应该匹配");
        assertEquals("TOPSIS", config.getAlgorithmCode(), "算法代码应该匹配");
        assertEquals("comprehensive_score", config.getOutputParam(), "输出参数应该匹配");
        assertTrue(config.isPositiveDistance(), "应该是正理想解");
        
        List<String> indicators = config.getIndicators();
        assertNotNull(indicators, "指标列表不应为空");
        assertEquals(3, indicators.size(), "应该有3个指标");
        assertEquals("indicator1", indicators.get(0));
        assertEquals("indicator2", indicators.get(1));
        assertEquals("indicator3", indicators.get(2));
    }
    
    @Test
    void testParseFromExpression_ValidNegativeExpression() {
        // 测试有效的负理想解表达式
        String qlExpression = "@TOPSIS_NEGATIVE:score1,score2";
        
        // 执行解析
        TOPSISAlgorithmConfig config = parameterParser.parseFromExpression(
            qlExpression, testStepAlgorithm
        );
        
        // 验证结果
        assertNotNull(config, "配置不应为空");
        assertFalse(config.isPositiveDistance(), "应该是负理想解");
        
        List<String> indicators = config.getIndicators();
        assertEquals(2, indicators.size(), "应该有2个指标");
        assertEquals("score1", indicators.get(0));
        assertEquals("score2", indicators.get(1));
    }
    
    @Test
    void testParseFromExpression_SingleIndicator() {
        // 测试单个指标的表达式
        String qlExpression = "@TOPSIS_POSITIVE:single_indicator";
        
        // 执行解析
        TOPSISAlgorithmConfig config = parameterParser.parseFromExpression(
            qlExpression, testStepAlgorithm
        );
        
        // 验证结果
        assertNotNull(config, "配置不应为空");
        List<String> indicators = config.getIndicators();
        assertEquals(1, indicators.size(), "应该有1个指标");
        assertEquals("single_indicator", indicators.get(0));
    }
    
    @Test
    void testParseFromExpression_WithSpaces() {
        // 测试包含空格的表达式
        String qlExpression = "@TOPSIS_POSITIVE: indicator1 , indicator2 , indicator3 ";
        
        // 执行解析
        TOPSISAlgorithmConfig config = parameterParser.parseFromExpression(
            qlExpression, testStepAlgorithm
        );
        
        // 验证结果 - 应该正确处理空格
        assertNotNull(config, "配置不应为空");
        List<String> indicators = config.getIndicators();
        assertEquals(3, indicators.size(), "应该有3个指标");
        assertEquals("indicator1", indicators.get(0), "应该去除空格");
        assertEquals("indicator2", indicators.get(1), "应该去除空格");
        assertEquals("indicator3", indicators.get(2), "应该去除空格");
    }
    
    @Test
    void testParseFromExpression_InvalidFormat() {
        // 测试无效格式的表达式
        String invalidExpression = "INVALID_FORMAT:indicator1,indicator2";
        
        // 执行解析
        TOPSISAlgorithmConfig config = parameterParser.parseFromExpression(
            invalidExpression, testStepAlgorithm
        );
        
        // 验证结果 - 应该返回null或抛出异常
        assertNull(config, "无效格式应该返回null");
    }
    
    @Test
    void testParseFromExpression_EmptyExpression() {
        // 测试空表达式
        String emptyExpression = "";
        
        // 执行解析
        TOPSISAlgorithmConfig config = parameterParser.parseFromExpression(
            emptyExpression, testStepAlgorithm
        );
        
        // 验证结果
        assertNull(config, "空表达式应该返回null");
    }
    
    @Test
    void testParseFromExpression_NullExpression() {
        // 测试null表达式
        TOPSISAlgorithmConfig config = parameterParser.parseFromExpression(
            null, testStepAlgorithm
        );
        
        // 验证结果
        assertNull(config, "null表达式应该返回null");
    }
    
    @Test
    void testValidateExpressionFormat_ValidFormats() {
        // 测试各种有效格式
        assertTrue(parameterParser.validateExpressionFormat("@TOPSIS_POSITIVE:indicator1"));
        assertTrue(parameterParser.validateExpressionFormat("@TOPSIS_NEGATIVE:indicator1,indicator2"));
        assertTrue(parameterParser.validateExpressionFormat("@TOPSIS_POSITIVE:a,b,c,d"));
        assertTrue(parameterParser.validateExpressionFormat("@TOPSIS_NEGATIVE: indicator1 , indicator2 "));
    }
    
    @Test
    void testValidateExpressionFormat_InvalidFormats() {
        // 测试各种无效格式
        assertFalse(parameterParser.validateExpressionFormat("TOPSIS_POSITIVE:indicator1")); // 缺少@
        assertFalse(parameterParser.validateExpressionFormat("@INVALID_TYPE:indicator1")); // 无效类型
        assertFalse(parameterParser.validateExpressionFormat("@TOPSIS_POSITIVE")); // 缺少冒号和指标
        assertFalse(parameterParser.validateExpressionFormat("@TOPSIS_POSITIVE:")); // 缺少指标
        assertFalse(parameterParser.validateExpressionFormat("")); // 空字符串
        assertFalse(parameterParser.validateExpressionFormat(null)); // null
    }
    
    @Test
    void testGetTOPSISConfig() {
        // 模拟服务行为
        StepAlgorithm mockStepAlgorithm = new StepAlgorithm();
        mockStepAlgorithm.setId(1L);
        mockStepAlgorithm.setAlgorithmCode("TOPSIS");
        mockStepAlgorithm.setQlExpression("@TOPSIS_POSITIVE:indicator1,indicator2");
        mockStepAlgorithm.setOutputParam("comprehensive_score");
        
        when(stepAlgorithmService.getByModelIdAndStepCode(1L, "step4"))
            .thenReturn(mockStepAlgorithm);
        
        // 执行获取配置
        TOPSISAlgorithmConfig config = parameterParser.getTOPSISConfig(1L, "step4");
        
        // 验证结果
        assertNotNull(config, "配置不应为空");
        assertEquals(1L, config.getStepId());
        assertEquals("TOPSIS", config.getAlgorithmCode());
        assertTrue(config.isPositiveDistance());
        assertEquals(2, config.getIndicators().size());
    }
    
    @Test
    void testGetTOPSISConfig_NotFound() {
        // 模拟找不到配置的情况
        when(stepAlgorithmService.getByModelIdAndStepCode(999L, "nonexistent"))
            .thenReturn(null);
        
        // 执行获取配置
        TOPSISAlgorithmConfig config = parameterParser.getTOPSISConfig(999L, "nonexistent");
        
        // 验证结果
        assertNull(config, "找不到配置时应该返回null");
    }
    
    @Test
    void testGenerateExpression_PositiveType() {
        // 准备配置
        TOPSISAlgorithmConfig config = TOPSISAlgorithmConfig.builder()
                .stepId(1L)
                .algorithmCode("TOPSIS")
                .indicators(Arrays.asList("indicator1", "indicator2", "indicator3"))
                .outputParam("comprehensive_score")
                .isPositiveDistance(true)
                .build();
        
        // 执行生成表达式
        String expression = parameterParser.generateExpression(config);
        
        // 验证结果
        assertNotNull(expression, "表达式不应为空");
        assertEquals("@TOPSIS_POSITIVE:indicator1,indicator2,indicator3", expression);
    }
    
    @Test
    void testGenerateExpression_NegativeType() {
        // 准备配置
        TOPSISAlgorithmConfig config = TOPSISAlgorithmConfig.builder()
                .stepId(1L)
                .algorithmCode("TOPSIS")
                .indicators(Arrays.asList("score1", "score2"))
                .outputParam("comprehensive_score")
                .isPositiveDistance(false)
                .build();
        
        // 执行生成表达式
        String expression = parameterParser.generateExpression(config);
        
        // 验证结果
        assertNotNull(expression, "表达式不应为空");
        assertEquals("@TOPSIS_NEGATIVE:score1,score2", expression);
    }
    
    @Test
    void testGenerateExpression_EmptyIndicators() {
        // 准备空指标配置
        TOPSISAlgorithmConfig config = TOPSISAlgorithmConfig.builder()
                .stepId(1L)
                .algorithmCode("TOPSIS")
                .indicators(Arrays.asList())
                .outputParam("comprehensive_score")
                .isPositiveDistance(true)
                .build();
        
        // 执行生成表达式
        String expression = parameterParser.generateExpression(config);
        
        // 验证结果 - 应该处理空指标列表
        assertNotNull(expression, "表达式不应为空");
        assertTrue(expression.startsWith("@TOPSIS_POSITIVE:"), "应该有正确的前缀");
    }
    
    @Test
    void testGenerateExpression_NullConfig() {
        // 测试null配置
        String expression = parameterParser.generateExpression(null);
        
        // 验证结果
        assertNull(expression, "null配置应该返回null表达式");
    }
    
    @Test
    void testParseFromExpression_ComplexIndicatorNames() {
        // 测试复杂的指标名称
        String qlExpression = "@TOPSIS_POSITIVE:disaster_prevention_score,emergency_response_capability,recovery_ability_index";
        
        // 执行解析
        TOPSISAlgorithmConfig config = parameterParser.parseFromExpression(
            qlExpression, testStepAlgorithm
        );
        
        // 验证结果
        assertNotNull(config, "配置不应为空");
        List<String> indicators = config.getIndicators();
        assertEquals(3, indicators.size(), "应该有3个指标");
        assertEquals("disaster_prevention_score", indicators.get(0));
        assertEquals("emergency_response_capability", indicators.get(1));
        assertEquals("recovery_ability_index", indicators.get(2));
    }
    
    @Test
    void testRoundTripParsing() {
        // 测试解析和生成的往返一致性
        String originalExpression = "@TOPSIS_POSITIVE:indicator1,indicator2,indicator3";
        
        // 解析表达式
        TOPSISAlgorithmConfig config = parameterParser.parseFromExpression(
            originalExpression, testStepAlgorithm
        );
        
        // 重新生成表达式
        String regeneratedExpression = parameterParser.generateExpression(config);
        
        // 验证往返一致性
        assertEquals(originalExpression, regeneratedExpression, 
            "解析后重新生成的表达式应该与原始表达式一致");
    }
}