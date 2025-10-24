package com.evaluate.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TOPSIS控制器集成测试
 * 
 * 测试TOPSIS相关的REST API端点，验证前端到后端的完整HTTP交互
 * 
 * @author System
 * @since 2025-01-01
 */
@SpringBootTest
@AutoConfigureWebMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class TOPSISControllerIntegrationTest {
    
    @Resource
    private WebApplicationContext webApplicationContext;
    
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
    }
    
    @Test
    void testGetTOPSISConfig() throws Exception {
        // 测试获取TOPSIS配置的API
        Long modelId = 1L;
        String stepCode = "step4";
        
        mockMvc.perform(get("/api/topsis/config")
                .param("modelId", modelId.toString())
                .param("stepCode", stepCode)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    
    @Test
    void testUpdateTOPSISConfig() throws Exception {
        // 测试更新TOPSIS配置的API
        Long modelId = 1L;
        String stepCode = "step4";
        
        Map<String, Object> configRequest = new HashMap<>();
        configRequest.put("modelId", modelId);
        configRequest.put("stepCode", stepCode);
        configRequest.put("indicators", Arrays.asList("disaster_prevention_score", "emergency_response_capability"));
        configRequest.put("algorithmType", "TOPSIS_POSITIVE");
        
        String requestJson = objectMapper.writeValueAsString(configRequest);
        
        mockMvc.perform(post("/api/topsis/config")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    void testGetAvailableIndicators() throws Exception {
        // 测试获取可用指标的API
        Long modelId = 1L;
        
        mockMvc.perform(get("/api/topsis/indicators")
                .param("modelId", modelId.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").isArray());
    }
    
    @Test
    void testValidateIndicators() throws Exception {
        // 测试验证指标的API
        Long modelId = 1L;
        List<String> indicators = Arrays.asList("disaster_prevention_score", "emergency_response_capability");
        
        Map<String, Object> validationRequest = new HashMap<>();
        validationRequest.put("modelId", modelId);
        validationRequest.put("indicators", indicators);
        
        String requestJson = objectMapper.writeValueAsString(validationRequest);
        
        mockMvc.perform(post("/api/topsis/indicators/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.valid").exists());
    }
    
    @Test
    void testTOPSISCalculation() throws Exception {
        // 测试TOPSIS计算的API
        Map<String, Object> calculationRequest = new HashMap<>();
        calculationRequest.put("modelId", 1L);
        calculationRequest.put("stepCode", "step4");
        calculationRequest.put("regionCodes", Arrays.asList("region1", "region2"));
        calculationRequest.put("weightConfigId", 1L);
        
        String requestJson = objectMapper.writeValueAsString(calculationRequest);
        
        mockMvc.perform(post("/api/topsis/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }
    
    @Test
    void testTOPSISDiagnostic() throws Exception {
        // 测试TOPSIS诊断的API
        Map<String, Object> diagnosticRequest = new HashMap<>();
        diagnosticRequest.put("modelId", 1L);
        diagnosticRequest.put("stepCode", "step4");
        diagnosticRequest.put("regionCodes", Arrays.asList("region1", "region2"));
        diagnosticRequest.put("weightConfigId", 1L);
        
        String requestJson = objectMapper.writeValueAsString(diagnosticRequest);
        
        mockMvc.perform(post("/api/topsis/diagnostic")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasIssues").exists())
                .andExpect(jsonPath("$.data.metrics").exists());
    }
    
    @Test
    void testTOPSISConfigTest() throws Exception {
        // 测试TOPSIS配置测试的API
        Map<String, Object> testRequest = new HashMap<>();
        testRequest.put("modelId", 1L);
        testRequest.put("stepCode", "step4");
        testRequest.put("indicators", Arrays.asList("disaster_prevention_score", "emergency_response_capability"));
        testRequest.put("algorithmType", "TOPSIS_POSITIVE");
        testRequest.put("sampleRegionCodes", Arrays.asList("region1", "region2"));
        
        String requestJson = objectMapper.writeValueAsString(testRequest);
        
        mockMvc.perform(post("/api/topsis/config/test")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.testResults").exists());
    }
    
    @Test
    void testGetAllTOPSISConfigs() throws Exception {
        // 测试获取所有TOPSIS配置的API
        Long modelId = 1L;
        
        mockMvc.perform(get("/api/topsis/configs")
                .param("modelId", modelId.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").isArray());
    }
    
    @Test
    void testCreateTOPSISConfig() throws Exception {
        // 测试创建TOPSIS配置的API
        Map<String, Object> createRequest = new HashMap<>();
        createRequest.put("stepId", 2L);
        createRequest.put("algorithmCode", "TOPSIS");
        createRequest.put("indicators", Arrays.asList("new_indicator1", "new_indicator2"));
        createRequest.put("outputParam", "comprehensive_score");
        createRequest.put("isPositiveDistance", true);
        
        String requestJson = objectMapper.writeValueAsString(createRequest);
        
        mockMvc.perform(post("/api/topsis/config/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    void testDeleteTOPSISConfig() throws Exception {
        // 测试删除TOPSIS配置的API
        Long modelId = 1L;
        String stepCode = "step4";
        
        mockMvc.perform(delete("/api/topsis/config")
                .param("modelId", modelId.toString())
                .param("stepCode", stepCode)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    void testTOPSISCompatibilityCheck() throws Exception {
        // 测试TOPSIS兼容性检查的API
        Long modelId = 1L;
        
        mockMvc.perform(get("/api/topsis/compatibility")
                .param("modelId", modelId.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.compatible").exists())
                .andExpect(jsonPath("$.data.issues").exists());
    }
    
    @Test
    void testTOPSISMigration() throws Exception {
        // 测试TOPSIS配置迁移的API
        Long modelId = 1L;
        
        Map<String, Object> migrationRequest = new HashMap<>();
        migrationRequest.put("modelId", modelId);
        migrationRequest.put("dryRun", true);
        
        String requestJson = objectMapper.writeValueAsString(migrationRequest);
        
        mockMvc.perform(post("/api/topsis/migrate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.migrationPlan").exists());
    }
    
    @Test
    void testErrorHandling() throws Exception {
        // 测试错误处理
        
        // 1. 测试无效的模型ID
        mockMvc.perform(get("/api/topsis/config")
                .param("modelId", "invalid")
                .param("stepCode", "step4")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        
        // 2. 测试缺少必需参数
        mockMvc.perform(get("/api/topsis/config")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        
        // 3. 测试无效的JSON格式
        mockMvc.perform(post("/api/topsis/config")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
                .andExpect(status().isBadRequest());
        
        // 4. 测试空的请求体
        mockMvc.perform(post("/api/topsis/config")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testConcurrentRequests() throws Exception {
        // 测试并发请求处理
        Long modelId = 1L;
        String stepCode = "step4";
        
        // 模拟多个并发的配置获取请求
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/api/topsis/config")
                    .param("modelId", modelId.toString())
                    .param("stepCode", stepCode)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }
    
    @Test
    void testRequestValidation() throws Exception {
        // 测试请求参数验证
        
        // 1. 测试指标列表为空的情况
        Map<String, Object> emptyIndicatorsRequest = new HashMap<>();
        emptyIndicatorsRequest.put("modelId", 1L);
        emptyIndicatorsRequest.put("stepCode", "step4");
        emptyIndicatorsRequest.put("indicators", Arrays.asList());
        emptyIndicatorsRequest.put("algorithmType", "TOPSIS_POSITIVE");
        
        String requestJson = objectMapper.writeValueAsString(emptyIndicatorsRequest);
        
        mockMvc.perform(post("/api/topsis/config")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
        
        // 2. 测试无效的算法类型
        Map<String, Object> invalidAlgorithmRequest = new HashMap<>();
        invalidAlgorithmRequest.put("modelId", 1L);
        invalidAlgorithmRequest.put("stepCode", "step4");
        invalidAlgorithmRequest.put("indicators", Arrays.asList("indicator1"));
        invalidAlgorithmRequest.put("algorithmType", "INVALID_TYPE");
        
        requestJson = objectMapper.writeValueAsString(invalidAlgorithmRequest);
        
        mockMvc.perform(post("/api/topsis/config")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testResponseFormat() throws Exception {
        // 测试响应格式的一致性
        Long modelId = 1L;
        
        // 测试成功响应格式
        mockMvc.perform(get("/api/topsis/indicators")
                .param("modelId", modelId.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").exists())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.message").exists());
        
        // 测试错误响应格式
        mockMvc.perform(get("/api/topsis/config")
                .param("modelId", "999999") // 不存在的模型ID
                .param("stepCode", "nonexistent")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }
}