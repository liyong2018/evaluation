package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.evaluate.entity.ModelExecutionStrategy;
import com.evaluate.mapper.ModelExecutionStrategyMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
