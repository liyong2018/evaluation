package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.evaluate.entity.IndicatorWeight;
import com.evaluate.entity.WeightConfig;
import com.evaluate.mapper.WeightConfigMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IndicatorWeightServiceImplTest {

    @Test
    void derivesCountyComprehensivePrimaryWeightsFromCityComprehensiveModel20() {
        TestableIndicatorWeightService service = new TestableIndicatorWeightService();
        WeightConfigMapper weightConfigMapper = mock(WeightConfigMapper.class);
        WeightConfig cityComprehensiveConfig = new WeightConfig();
        cityComprehensiveConfig.setId(200L);
        when(weightConfigMapper.selectOne(ArgumentMatchers.<Wrapper<WeightConfig>>any()))
                .thenReturn(cityComprehensiveConfig);
        ReflectionTestUtils.setField(service, "weightConfigMapper", weightConfigMapper);

        service.put(100L, weight(1L, 100L, "L1_TOWNSHIP", 1, 0.50, 1, null));
        service.put(100L, weight(2L, 100L, "L1_COMMUNITY", 1, 0.50, 2, null));
        service.put(100L, weight(3L, 100L, "L2_TOWNSHIP_DISASTER_MANAGEMENT", 2, 0.33, 1, 1L));
        service.put(100L, weight(4L, 100L, "L2_TOWNSHIP_DISASTER_PREPAREDNESS", 2, 0.32, 2, 1L));
        service.put(100L, weight(5L, 100L, "L2_TOWNSHIP_SELF_RESCUE_TRANSFER", 2, 0.35, 3, 1L));
        service.put(100L, weight(6L, 100L, "L2_COMMUNITY_DISASTER_MANAGEMENT", 2, 0.32, 4, 2L));
        service.put(100L, weight(7L, 100L, "L2_COMMUNITY_DISASTER_PREPAREDNESS", 2, 0.31, 5, 2L));
        service.put(100L, weight(8L, 100L, "L2_COMMUNITY_SELF_RESCUE_TRANSFER", 2, 0.37, 6, 2L));
        service.put(200L, weight(9L, 200L, "L1_TOWNSHIP", 1, 0.18, 4, null));
        service.put(200L, weight(10L, 200L, "L1_COMMUNITY", 1, 0.17, 5, null));

        assertTrue(service.ensureComprehensiveCountyWeights(100L, "510104", 2024));

        assertEquals(0.18 / 0.35, service.get(100L, "L1_TOWNSHIP").getWeight(), 1e-12);
        assertEquals(0.17 / 0.35, service.get(100L, "L1_COMMUNITY").getWeight(), 1e-12);
    }

    @Test
    void fullInheritanceDerivesCountyComprehensivePrimaryWeightsAtRuntime() {
        TestableIndicatorWeightService service = new TestableIndicatorWeightService();
        WeightConfigMapper weightConfigMapper = mock(WeightConfigMapper.class);
        WeightConfig cityComprehensiveConfig = new WeightConfig();
        cityComprehensiveConfig.setId(200L);
        when(weightConfigMapper.selectOne(ArgumentMatchers.<Wrapper<WeightConfig>>any()))
                .thenReturn(cityComprehensiveConfig);
        ReflectionTestUtils.setField(service, "weightConfigMapper", weightConfigMapper);

        service.put(100L, weight(1L, 100L, "L1_TOWNSHIP", 1, 0.50, 1, null));
        service.put(100L, weight(2L, 100L, "L1_COMMUNITY", 1, 0.50, 2, null));
        service.put(200L, weight(3L, 200L, "L1_TOWNSHIP", 1, 0.18, 4, null));
        service.put(200L, weight(4L, 200L, "L1_COMMUNITY", 1, 0.17, 5, null));

        List<IndicatorWeight> weights = service.getWeightsWithFullInheritance(
                100L, "510104", 2024, 11L, "综合减灾能力评估模型");

        assertEquals(0.18 / 0.35, find(weights, "L1_TOWNSHIP").getWeight(), 1e-12);
        assertEquals(0.17 / 0.35, find(weights, "L1_COMMUNITY").getWeight(), 1e-12);
        assertEquals(0.50, service.get(100L, "L1_TOWNSHIP").getWeight(), 1e-12);
        assertEquals(0.50, service.get(100L, "L1_COMMUNITY").getWeight(), 1e-12);
    }

    @Test
    void resolvesBaselineOrgcodeCandidatesAsCountyCityProvince() {
        assertEquals(listOf("510104", "5101", "51"),
                IndicatorWeightServiceImpl.resolveBaselineOrgcodeCandidates("510104"));
    }

    private static IndicatorWeight find(List<IndicatorWeight> weights, String code) {
        for (IndicatorWeight weight : weights) {
            if (code.equals(weight.getIndicatorCode())) {
                return weight;
            }
        }
        throw new AssertionError("Missing weight code: " + code);
    }

    private static List<String> listOf(String first, String second, String third) {
        List<String> list = new ArrayList<>();
        list.add(first);
        list.add(second);
        list.add(third);
        return list;
    }

    private static IndicatorWeight weight(Long id, Long configId, String code, Integer level,
                                          Double value, Integer sortOrder, Long parentId) {
        IndicatorWeight weight = new IndicatorWeight();
        weight.setId(id);
        weight.setConfigId(configId);
        weight.setIndicatorCode(code);
        weight.setIndicatorName(code);
        weight.setIndicatorLevel(level);
        weight.setWeight(value);
        weight.setSortOrder(sortOrder);
        weight.setParentId(parentId);
        return weight;
    }

    private static class TestableIndicatorWeightService extends IndicatorWeightServiceImpl {
        private final Map<Long, Map<String, IndicatorWeight>> weightsByConfigId = new HashMap<>();

        void put(Long configId, IndicatorWeight weight) {
            if (!weightsByConfigId.containsKey(configId)) {
                weightsByConfigId.put(configId, new HashMap<String, IndicatorWeight>());
            }
            weightsByConfigId.get(configId).put(weight.getIndicatorCode(), weight);
        }

        IndicatorWeight get(Long configId, String code) {
            return weightsByConfigId.get(configId).get(code);
        }

        @Override
        public IndicatorWeight getByConfigIdAndCode(Long configId, String indicatorCode) {
            Map<String, IndicatorWeight> byCode = weightsByConfigId.get(configId);
            return byCode == null ? null : byCode.get(indicatorCode);
        }

        @Override
        public List<IndicatorWeight> getByConfigId(Long configId) {
            Map<String, IndicatorWeight> byCode = weightsByConfigId.get(configId);
            return byCode == null ? new ArrayList<IndicatorWeight>() : new ArrayList<>(byCode.values());
        }

        @Override
        public boolean updateById(IndicatorWeight entity) {
            put(entity.getConfigId(), entity);
            return true;
        }
    }
}
