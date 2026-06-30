package com.evaluate.service.impl;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpecialAlgorithmServiceImplTest {

    @Test
    void determineGradeCapsStrongThresholdAtMaximumScore() throws Exception {
        SpecialAlgorithmServiceImpl service = new SpecialAlgorithmServiceImpl();
        Method method = SpecialAlgorithmServiceImpl.class
                .getDeclaredMethod("determineGrade", double.class, double.class, double.class);
        method.setAccessible(true);

        Object grade = method.invoke(service, 1.0, 0.580818326, 0.339987810);

        assertEquals("强", grade);
    }
}
