package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.evaluate.entity.CommunityDisasterReductionCapacity;
import com.evaluate.entity.ModelExecutionStrategy;
import com.evaluate.entity.ModelStep;
import com.evaluate.entity.StepAlgorithm;
import com.evaluate.entity.SurveyData;
import com.evaluate.mapper.CommunityDisasterReductionCapacityMapper;
import com.evaluate.mapper.ModelExecutionStrategyMapper;
import com.evaluate.mapper.ModelStepMapper;
import com.evaluate.mapper.StepAlgorithmMapper;
import com.evaluate.mapper.SurveyDataMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ModelExecutionServiceImplTest {

    @Test
    void calculateLevelFromScoreUsesConfiguredThresholdsWhenPresent() throws Exception {
        ModelExecutionServiceImpl service = new ModelExecutionServiceImpl();
        ModelExecutionStrategyMapper strategyMapper = mock(ModelExecutionStrategyMapper.class);
        ReflectionTestUtils.setField(service, "modelExecutionStrategyMapper", strategyMapper);

        ModelExecutionStrategy strategy = new ModelExecutionStrategy();
        strategy.setStrategyValue("[{\"min\":0.9,\"level\":\"优秀\"},{\"min\":0.5,\"level\":\"达标\"},{\"min\":0,\"level\":\"待提升\"}]");
        when(strategyMapper.selectList(ArgumentMatchers.<Wrapper<ModelExecutionStrategy>>any()))
                .thenReturn(Collections.singletonList(strategy));

        Method method = ModelExecutionServiceImpl.class
                .getDeclaredMethod("calculateLevelFromScore", Long.class, BigDecimal.class);
        method.setAccessible(true);

        assertEquals("达标", method.invoke(service, 99L, new BigDecimal("0.70")));
        assertEquals("优秀", method.invoke(service, 99L, new BigDecimal("0.95")));
    }

    @Test
    void addSurveyDataToContextDefaultsNullNumericFieldsToZero() throws Exception {
        ModelExecutionServiceImpl service = new ModelExecutionServiceImpl();
        SurveyData surveyData = new SurveyData();
        surveyData.setRegionCode("510704400");
        surveyData.setTownship("四川省新华劳动教育管理所");

        Method method = ModelExecutionServiceImpl.class
                .getDeclaredMethod("addSurveyDataToContext", Map.class, SurveyData.class);
        method.setAccessible(true);

        Map<String, Object> context = new java.util.HashMap<>();
        method.invoke(service, context, surveyData);

        assertEquals(0L, context.get("population"));
        assertEquals(0, context.get("management_staff"));
        assertEquals(0.0, context.get("funding_amount"));
        assertEquals(0, context.get("shelter_capacity"));
    }

    @Test
    void supplementSurveyVolunteerMilitiaFromCommunitiesUsesCommunitySumsWhenSurveyValuesMissing() throws Exception {
        ModelExecutionServiceImpl service = new ModelExecutionServiceImpl();
        CommunityDisasterReductionCapacityMapper communityDataMapper = mock(CommunityDisasterReductionCapacityMapper.class);
        ReflectionTestUtils.setField(service, "communityDataMapper", communityDataMapper);

        Map<String, Object> totals = new java.util.HashMap<>();
        totals.put("registered_volunteer_count", 824);
        totals.put("militia_reserve_count", 203);
        when(communityDataMapper.selectMaps(ArgumentMatchers.<Wrapper<CommunityDisasterReductionCapacity>>any()))
                .thenReturn(Collections.singletonList(totals));

        Map<String, Object> context = new java.util.HashMap<>();
        context.put("volunteers", 0);
        context.put("volunteer_count", 0);
        context.put("militiaReserve", 0);
        context.put("militia_reserve", 0);

        Method method = ModelExecutionServiceImpl.class
                .getDeclaredMethod("supplementSurveyVolunteerMilitiaFromCommunities", Map.class, String.class, Integer.class);
        method.setAccessible(true);
        method.invoke(service, context, "511425001", 2025);

        assertEquals(824.0, context.get("volunteers"));
        assertEquals(824.0, context.get("volunteer_count"));
        assertEquals(203.0, context.get("militiaReserve"));
        assertEquals(203.0, context.get("militia_reserve"));
    }

    @Test
    void communityPopulationSafeDivisionZerosFundingWhenPopulationMissing() throws Exception {
        ModelExecutionServiceImpl service = new ModelExecutionServiceImpl();
        Map<String, Object> context = new java.util.HashMap<>();
        context.put("resident_population", null);
        context.put("population", 0);
        context.put("last_year_funding_amount", 0.5);
        context.put("lastYearFundingAmount", 0.5);
        context.put("fundingAmount", 0.5);
        context.put("funding_amount", 0.5);

        Method method = ModelExecutionServiceImpl.class
                .getDeclaredMethod("applyCommunityPopulationSafeDivision", Map.class);
        method.setAccessible(true);
        method.invoke(service, context);

        assertEquals(0.0, context.get("last_year_funding_amount"));
        assertEquals(0.0, context.get("lastYearFundingAmount"));
        assertEquals(0.0, context.get("fundingAmount"));
        assertEquals(0.0, context.get("funding_amount"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void townshipAggregationAddsZeroRowsForTownshipsWithoutCommunities() throws Exception {
        ModelExecutionServiceImpl service = new ModelExecutionServiceImpl();

        ModelStepMapper modelStepMapper = mock(ModelStepMapper.class);
        StepAlgorithmMapper stepAlgorithmMapper = mock(StepAlgorithmMapper.class);
        CommunityDisasterReductionCapacityMapper communityDataMapper = mock(CommunityDisasterReductionCapacityMapper.class);
        SurveyDataMapper surveyDataMapper = mock(SurveyDataMapper.class);
        ReflectionTestUtils.setField(service, "modelStepMapper", modelStepMapper);
        ReflectionTestUtils.setField(service, "stepAlgorithmMapper", stepAlgorithmMapper);
        ReflectionTestUtils.setField(service, "communityDataMapper", communityDataMapper);
        ReflectionTestUtils.setField(service, "surveyDataMapper", surveyDataMapper);
        QLExpressServiceImpl qlExpressService = new QLExpressServiceImpl();
        qlExpressService.init();
        ReflectionTestUtils.setField(service, "qlExpressService", qlExpressService);

        ModelStep step = new ModelStep();
        step.setId(20L);
        step.setStepName("乡镇数据聚合");
        step.setStepCode("township_aggregation");
        step.setStepOrder(2);
        step.setStatus(1);
        when(modelStepMapper.selectById(20L)).thenReturn(step);

        StepAlgorithm algorithm = new StepAlgorithm();
        algorithm.setAlgorithmName("预案建设能力");
        algorithm.setQlExpression("SUM(HAS_EMERGENCY_PLAN)/communityCount");
        algorithm.setOutputParam("PLAN_CONSTRUCTION");
        when(stepAlgorithmMapper.selectList(ArgumentMatchers.<Wrapper<StepAlgorithm>>any()))
                .thenReturn(Collections.singletonList(algorithm));

        CommunityDisasterReductionCapacity community = new CommunityDisasterReductionCapacity();
        community.setRegionCode("513232100001");
        community.setTownshipName("唐克镇");
        when(communityDataMapper.selectList(ArgumentMatchers.<Wrapper<CommunityDisasterReductionCapacity>>any()))
                .thenReturn(Collections.singletonList(community));

        SurveyData withCommunity = township("513232100", "唐克镇");
        SurveyData withoutCommunity = township("513232400", "白河牧场");
        when(surveyDataMapper.selectList(ArgumentMatchers.<Wrapper<SurveyData>>any()))
                .thenReturn(Arrays.asList(withCommunity, withoutCommunity));

        Map<String, Object> step1 = new java.util.HashMap<>();
        Map<String, Map<String, Object>> step1Regions = new java.util.HashMap<>();
        Map<String, Object> communityOutputs = new java.util.HashMap<>();
        communityOutputs.put("HAS_EMERGENCY_PLAN", 1.0);
        step1Regions.put("513232100001", communityOutputs);
        step1.put("regionResults", step1Regions);

        Map<String, Object> inputData = new java.util.HashMap<>();
        inputData.put("year", 2020);
        inputData.put("orgCode", "513232");
        inputData.put("step_community_indicators", step1);

        Method method = ModelExecutionServiceImpl.class
                .getDeclaredMethod("executeTownshipAggregation", Long.class, List.class, Map.class);
        method.setAccessible(true);

        Map<String, Object> result = (Map<String, Object>) method.invoke(
                service, 20L, Collections.singletonList("513232100001"), inputData);
        Map<String, Map<String, Object>> regionResults =
                (Map<String, Map<String, Object>>) result.get("regionResults");

        assertTrue(regionResults.containsKey("513232400"));
        assertEquals(0.0, regionResults.get("513232400").get("PLAN_CONSTRUCTION"));
        assertEquals("白河牧场", regionResults.get("513232400").get("_townshipName"));
    }

    private static SurveyData township(String regionCode, String name) {
        SurveyData data = new SurveyData();
        data.setRegionCode(regionCode);
        data.setTownship(name);
        data.setYear(2020);
        data.setPopulation(1L);
        return data;
    }
}
