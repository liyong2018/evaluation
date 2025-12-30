package com.evaluate.service;

import com.evaluate.entity.EvaluationResult;
import com.evaluate.entity.IndicatorWeight;
import com.evaluate.service.impl.WordTemplateServiceImpl;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordDataPreprocessorTest {

    public static void main(String[] args) {
        new WordDataPreprocessorTest().testProcessEvaluationData();
    }

    @Test
    public void testProcessEvaluationData() {
        WordDataPreprocessor preprocessor = new WordDataPreprocessor();

        // 1. Mock Data - Region
        String regionName = "青神县";
        String year = "2025";

        // 2. Mock Data - Indicator Weights (Township)
        List<IndicatorWeight> townshipWeights = new ArrayList<>();
        townshipWeights.add(new IndicatorWeight().setIndicatorCode("T1").setIndicatorName("灾害管理能力").setIndicatorLevel(1).setWeight(0.33).setSortOrder(1));
        townshipWeights.add(new IndicatorWeight().setIndicatorCode("T2").setIndicatorName("灾害备灾能力").setIndicatorLevel(1).setWeight(0.32).setSortOrder(2));
        townshipWeights.add(new IndicatorWeight().setIndicatorCode("T3").setIndicatorName("自救转移能力").setIndicatorLevel(1).setWeight(0.35).setSortOrder(3));

        // 3. Mock Data - Indicator Weights (Community)
        List<IndicatorWeight> communityWeights = new ArrayList<>();
        communityWeights.add(new IndicatorWeight().setIndicatorCode("C1").setIndicatorName("灾害管理能力").setIndicatorLevel(1).setWeight(0.32).setSortOrder(1));
        communityWeights.add(new IndicatorWeight().setIndicatorCode("C2").setIndicatorName("灾害备灾能力").setIndicatorLevel(1).setWeight(0.23).setSortOrder(2));

        // 4. Mock Data - Evaluation Results (Township)
        List<EvaluationResult> townshipResults = new ArrayList<>();
        townshipResults.add(createResult("青竹街道", 0.33, "较弱"));
        townshipResults.add(createResult("瑞峰镇", 0.75, "强"));
        townshipResults.add(createResult("西龙镇", 0.45, "中等"));
        townshipResults.add(createResult("高台镇", 0.60, "较强"));
        townshipResults.add(createResult("汉阳镇", 0.40, "较弱"));

        // 5. Mock Data - Evaluation Results (Community)
        List<EvaluationResult> communityResults = new ArrayList<>();
        communityResults.add(createResult("社区A", 0.80, "强"));
        communityResults.add(createResult("社区B", 0.20, "弱"));

        // Execute Processing
        WordDataPreprocessor.EvaluationReportData report = preprocessor.processEvaluationData(
                townshipResults,
                communityResults,
                new ArrayList<>(),
                new ArrayList<>(),
                townshipWeights,
                communityWeights,
                new HashMap<>(),
                year,
                regionName
        );

        // Output JSON
        String json = preprocessor.toJson(report);
        System.out.println("Generated JSON:");
        System.out.println(json);
    }

    @Test
    public void testDynamicTableReplacement_table6() throws Exception {
        WordTemplateServiceImpl service = new WordTemplateServiceImpl();

        Map<String, Object> variables = new HashMap<>();
        variables.put("year", "2025");
        variables.put("county", "青神县");

        List<Map<String, Object>> table6Data = new ArrayList<>();
        table6Data.add(buildTable6Row(1, "瑞峰镇", "强", "较强", "强", "较强"));
        table6Data.add(buildTable6Row(2, "青竹街道", "中等", "中等", "中等", "中等"));
        variables.put("table6_data", table6Data);

        byte[] docBytes = service.generateReportFromTemplate(variables);
        XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(docBytes));
        try {
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
            String allText = extractor.getText();
            Assertions.assertFalse(allText.contains("{{t6_idx}}"));
            Assertions.assertFalse(allText.contains("{{t6_name}}"));
            Assertions.assertFalse(allText.contains("{{t6_c1}}"));
        } finally {
            doc.close();
        }
    }

    private EvaluationResult createResult(String name, double score, String level) {
        return new EvaluationResult()
                .setRegionCode(name)
                .setRegionName(name)
                .setComprehensiveCapabilityScore(BigDecimal.valueOf(score))
                .setComprehensiveCapabilityLevel(level);
    }

    private Map<String, Object> buildTable6Row(int idx, String name, String c1, String c2, String c3, String c4) {
        Map<String, Object> row = new HashMap<>();
        row.put("t6_idx", idx);
        row.put("t6_name", name);
        row.put("t6_c1", c1);
        row.put("t6_c2", c2);
        row.put("t6_c3", c3);
        row.put("t6_c4", c4);
        return row;
    }
}
