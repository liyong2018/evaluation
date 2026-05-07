package com.evaluate.batch;

import com.evaluate.service.ModelExecutionService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class Batch2020CountyEvaluationRunnerTest {

    @Test
    void runClearsExistingResultsAndExecutesModelsInOrderForCountiesWithData(
            @org.junit.jupiter.api.io.TempDir Path tempDir) throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        ModelExecutionService modelExecutionService = mock(ModelExecutionService.class);
        Batch2020CountyEvaluationRunner runner = new Batch2020CountyEvaluationRunner(
                jdbcTemplate, modelExecutionService, tempDir);

        when(jdbcTemplate.queryForList(anyString())).thenReturn(Collections.singletonList(county("510302", "自流井区", "自贡市")));
        when(jdbcTemplate.queryForObject(contains("model_execution_strategy"), eq(Integer.class),
                eq(11L), eq("data_validation"), eq("dependency_models"))).thenReturn(0);
        when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);
        when(jdbcTemplate.queryForList(contains("model_execution_record"), any(Object[].class), eq(Long.class)))
                .thenReturn(Arrays.asList(101L, 102L));
        when(jdbcTemplate.queryForObject(contains("survey_data"), eq(Long.class), eq(2020), eq("510302%"))).thenReturn(1L);
        when(jdbcTemplate.queryForObject(contains("community_disaster_reduction_capacity"), eq(Long.class),
                eq(2020), eq("510302%"))).thenReturn(1L);
        when(modelExecutionService.executeModel(any(), any(), any(), any(), any(), any()))
                .thenReturn(new LinkedHashMap<String, Object>());

        runner.run(new DefaultApplicationArguments());

        verify(jdbcTemplate).update("DELETE FROM evaluation_result WHERE execution_record_id IN (?,?)", 101L, 102L);
        verify(jdbcTemplate).update("DELETE FROM model_execution_record WHERE id IN (?,?)", 101L, 102L);

        ArgumentCaptor<Long> modelIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<List> regionCodesCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Integer> yearCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<String> orgCodeCaptor = ArgumentCaptor.forClass(String.class);
        verify(modelExecutionService, times(4)).executeModel(
                modelIdCaptor.capture(),
                regionCodesCaptor.capture(),
                eq(null),
                yearCaptor.capture(),
                orgCodeCaptor.capture(),
                eq("batch-2020-county-evaluation")
        );

        assertEquals(Arrays.asList(3L, 4L, 8L, 11L), modelIdCaptor.getAllValues());
        assertEquals(Collections.singletonList("510302"), regionCodesCaptor.getAllValues().get(0));
        assertEquals(Arrays.asList(2020, 2020, 2020, 2020), yearCaptor.getAllValues());
        assertEquals(Arrays.asList("510302", "510302", "510302", "510302"), orgCodeCaptor.getAllValues());
    }

    @Test
    void runSkipsCountyWhenSurveyOrCommunityDataIsMissing(@org.junit.jupiter.api.io.TempDir Path tempDir)
            throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        ModelExecutionService modelExecutionService = mock(ModelExecutionService.class);
        Batch2020CountyEvaluationRunner runner = new Batch2020CountyEvaluationRunner(
                jdbcTemplate, modelExecutionService, tempDir);

        when(jdbcTemplate.queryForList(anyString())).thenReturn(Collections.singletonList(county("510671", "德阳经济技术开发区", "德阳市")));
        when(jdbcTemplate.queryForObject(contains("model_execution_strategy"), eq(Integer.class),
                eq(11L), eq("data_validation"), eq("dependency_models"))).thenReturn(1);
        when(jdbcTemplate.queryForList(contains("model_execution_record"), any(Object[].class), eq(Long.class)))
                .thenReturn(Collections.emptyList());
        when(jdbcTemplate.queryForObject(contains("survey_data"), eq(Long.class), eq(2020), eq("510671%"))).thenReturn(0L);
        when(jdbcTemplate.queryForObject(contains("community_disaster_reduction_capacity"), eq(Long.class),
                eq(2020), eq("510671%"))).thenReturn(0L);

        runner.run(new DefaultApplicationArguments());

        verify(modelExecutionService, times(0)).executeModel(any(), any(), any(), any(), any(), any());
    }

    private Map<String, Object> county(String code, String name, String cityName) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("code", code);
        row.put("name", name);
        row.put("city_name", cityName);
        return row;
    }
}
