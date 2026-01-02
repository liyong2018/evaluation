package com.evaluate.controller;

import com.evaluate.common.Result;
import com.evaluate.entity.CommunityDisasterReductionCapacity;
import com.evaluate.service.IWordTemplateService;
import com.evaluate.service.ICommunityDisasterReductionCapacityService;
import com.evaluate.service.IEvaluationService;
import com.evaluate.service.EvaluationResultService;
import com.evaluate.entity.EvaluationResult;
import com.evaluate.service.WordDataPreprocessor;
import com.evaluate.service.WordDataPreprocessor.EvaluationReportData;
import com.evaluate.service.IIndicatorWeightService;
import com.evaluate.service.IIndicatorWeightScoreService;
import com.evaluate.service.IWeightConfigService;
import com.evaluate.entity.IndicatorWeight;
import com.evaluate.entity.WeightConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Word模板处理控制器
 *
 * @author System
 * @since 2025-12-18
 */
@Slf4j
@RestController
@RequestMapping("/api/word-template")
public class WordTemplateController {

    @Autowired
    private IWordTemplateService wordTemplateService;

    @Autowired
    private ICommunityDisasterReductionCapacityService communityService;

    @Autowired
    private IEvaluationService evaluationService;

    @Autowired
    private EvaluationResultService evaluationResultService;

    @Autowired
    private WordDataPreprocessor wordDataPreprocessor;

    @Autowired
    private IIndicatorWeightService indicatorWeightService;

    @Autowired(required = false)
    private IIndicatorWeightScoreService indicatorWeightScoreService;

    @Autowired
    private IWeightConfigService weightConfigService;

    /**
     * 基于模板生成Word报告
     */
    @RequestMapping(value = "/generate-report", method = {RequestMethod.GET, RequestMethod.POST})
    public void generateReportFromTemplate(@RequestParam(value = "year", required = false) Integer year,
                                         @RequestParam(value = "orgCode", required = false) String orgCode,
                                         HttpServletResponse response) {
        try {
            // 1. 获取预处理后的结构化数据
            EvaluationReportData reportData = fetchReportData(year, orgCode);

            // 2. 转换为模板所需的扁平化变量
            Map<String, Object> variables = mapReportDataToTemplate(reportData);

            // 2.1 查找所有级别的最新专题图图片
            Map<String, String> thematicMapImages = findAllThematicMapImages(year, orgCode);
            if (!thematicMapImages.isEmpty()) {
                log.info("找到{}张专题图图片: {}", thematicMapImages.size(), thematicMapImages.keySet());
                // 将图片路径也添加到变量中，以便在文本替换中也能使用（作为后备或调试）
                for (Map.Entry<String, String> entry : thematicMapImages.entrySet()) {
                    variables.put("thematic_map_" + entry.getKey(), entry.getValue());
                    variables.put("{{thematic_map_" + entry.getKey() + "}}", entry.getValue());
                }
            }

            log.info("生成报告准备数据完成，变量数: {}", variables.size());
            if (variables.containsKey("table6_data")) {
                List<?> list = (List<?>) variables.get("table6_data");
                log.info("table6_data present, size: {}", list.size());
                if (!list.isEmpty() && list.get(0) instanceof Map) {
                     log.info("table6_data keys: {}", ((Map<?, ?>)list.get(0)).keySet());
                }
            } else {
                log.warn("table6_data MISSING in variables map!");
            }
            if (variables.containsKey("table7_data")) log.info("table7_data present, size: {}", ((List<?>)variables.get("table7_data")).size());
            if (variables.containsKey("table8_data")) log.info("table8_data present, size: {}", ((List<?>)variables.get("table8_data")).size());
            if (variables.containsKey("table9_data")) log.info("table9_data present, size: {}", ((List<?>)variables.get("table9_data")).size());

            // 3. 生成Word文件（包含专题图图片替换）
            byte[] wordData = wordTemplateService.generateReportFromTemplate(variables, thematicMapImages);

            // 4. 保存到临时文件供OnlyOffice使用
            String tempDir = "uploads/generated/";
            java.io.File directory = new java.io.File(tempDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "青神县减灾能力评估技术报告_" + timestamp + ".docx";
            String filePath = tempDir + fileName;

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(wordData);
            }
            log.info("Word报告已保存到临时文件: {}", filePath);

            // 设置响应头 - 使用inline以便OnlyOffice可以预览
            String encodedFilename = java.net.URLEncoder.encode(fileName, "UTF-8");
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            response.setHeader("Content-Disposition", "inline; filename*=UTF-8''" + encodedFilename);
            response.setContentLength(wordData.length);

            // 写入响应
            response.getOutputStream().write(wordData);
            response.getOutputStream().flush();

            log.info("Word模板报告生成成功: {}", fileName);

        } catch (Exception e) {
            log.error("生成Word报告失败", e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "生成Word报告失败: " + e.getMessage());
            } catch (IOException ex) {
                log.error("发送错误响应失败", ex);
            }
        }
    }

    /**
     * 获取最新生成的Word报告文件（供OnlyOffice使用）
     * 这个端点返回已生成的静态文件，而不是动态生成
     */
    @GetMapping("/latest-report")
    public void getLatestReport(@RequestParam(required = false) Integer year,
                                @RequestParam(required = false) String orgCode,
                                HttpServletResponse response) {
        try {
            String tempDir = "uploads/generated/";
            java.io.File directory = new java.io.File(tempDir);

            if (!directory.exists() || !directory.isDirectory()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "生成的报告目录不存在");
                return;
            }

            // 获取目录中所有的.docx文件
            java.io.File[] files = directory.listFiles((dir, name) -> name.endsWith(".docx"));

            if (files == null || files.length == 0) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "没有找到生成的报告文件");
                return;
            }

            // 按修改时间排序，获取最新的文件
            java.io.File latestFile = Arrays.stream(files)
                .max(Comparator.comparingLong(java.io.File::lastModified))
                .orElse(files[0]);

            // 读取文件内容
            byte[] fileContent = java.nio.file.Files.readAllBytes(latestFile.toPath());

            // 设置响应头
            String encodedFilename = java.net.URLEncoder.encode(latestFile.getName(), "UTF-8");
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            response.setHeader("Content-Disposition", "inline; filename*=UTF-8''" + encodedFilename);
            response.setContentLength(fileContent.length);

            // 写入响应
            response.getOutputStream().write(fileContent);
            response.getOutputStream().flush();

            log.info("返回最新生成的报告文件: {}", latestFile.getName());

        } catch (Exception e) {
            log.error("获取最新报告文件失败", e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "获取报告文件失败");
            } catch (IOException ex) {
                log.error("发送错误响应失败", ex);
            }
        }
    }

    /**
     * 获取JSON数据预览 (供前端或OnlyOffice使用)
     */
    @GetMapping("/preview-json")
    public Result<EvaluationReportData> previewJson(@RequestParam(required = false) Integer year,
                                                    @RequestParam(required = false) String orgCode) {
        try {
            EvaluationReportData data = fetchReportData(year, orgCode);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取JSON数据失败", e);
            return Result.error("获取JSON数据失败: " + e.getMessage());
        }
    }

    /**
     * 调试用：获取扁平化的模板变量Map
     */
    @GetMapping("/debug-template-vars")
    public Result<Map<String, Object>> debugTemplateVars(@RequestParam(required = false) Integer year,
                                                         @RequestParam(required = false) String orgCode) {
        try {
            EvaluationReportData data = fetchReportData(year, orgCode);
            Map<String, Object> variables = mapReportDataToTemplate(data);
            return Result.success(variables);
        } catch (Exception e) {
            log.error("获取调试数据失败", e);
            return Result.error("获取调试数据失败: " + e.getMessage());
        }
    }

    /**
     * 调试用：获取模板文件内容解析
     */
    @GetMapping("/debug-template-content")
    public Result<Map<String, Object>> debugTemplateContent() {
        try {
            return Result.success(wordTemplateService.debugTemplateContent());
        } catch (Exception e) {
            log.error("获取模板内容失败", e);
            return Result.error("获取模板内容失败: " + e.getMessage());
        }
    }

    @GetMapping("/debug-m8")
    @ResponseBody
    public String debugM8(@RequestParam(required = false) Integer year, @RequestParam(required = false) String orgCode) {
        if (year == null) year = 2025;
        if (orgCode == null) orgCode = "511425";
        List<EvaluationResult> results = evaluationResultService.getResultsByModelIdAndYearAndOrgCode(8L, year, orgCode);
        StringBuilder sb = new StringBuilder();
        sb.append("Year: ").append(year).append(", OrgCode: ").append(orgCode).append("\n");
        sb.append("Size: ").append(results == null ? "null" : results.size()).append("\n");
        if (results != null) {
             for (EvaluationResult r : results) {
                 sb.append("ID: ").append(r.getId())
                   .append(", Region: ").append(r.getRegionCode())
                   .append(", Level: ").append(r.getComprehensiveCapabilityLevel())
                   .append("\n");
             }
         }
        return sb.toString();
    }

    private EvaluationReportData fetchReportData(Integer year, String orgCode) {
        // Fix: Default to current year (2025) if not provided
        if (year == null) year = 2025;
        if (orgCode == null) orgCode = "511425";

        // Fetch Results
        // Model 3: Township Disaster Reduction Capability Assessment Model (Uses Township Data)
        List<EvaluationResult> townshipResults = evaluationResultService.getResultsByModelIdAndYearAndOrgCode(3L, year, orgCode);
        
        // Model 8: Community-Township Capability Assessment Model (Uses Community Data aggregated by Township)
        List<EvaluationResult> communityByTownResults = evaluationResultService.getResultsByModelIdAndYearAndOrgCode(8L, year, orgCode);
        
        // Model 4: Community-Administrative Village Capability Assessment Model (Uses Community Data)
        List<EvaluationResult> communityResults = evaluationResultService.getResultsByModelIdAndYearAndOrgCode(4L, year, orgCode);
        
        // Model 11: Comprehensive Disaster Reduction Capability Assessment Model (Comprehensive Data)
        // Use model 11 for township data as it has the latest comprehensive evaluation results
        List<EvaluationResult> comprehensiveResults = evaluationResultService.getResultsByModelIdAndYearAndOrgCode(11L, year, orgCode);

        log.info("Fetched Results - Township(M3): {}, CommunityByTown(M8): {}, Community(M4): {}, Comprehensive(M11): {}",
                townshipResults != null ? townshipResults.size() : 0,
                communityByTownResults != null ? communityByTownResults.size() : 0,
                communityResults != null ? communityResults.size() : 0,
                comprehensiveResults != null ? comprehensiveResults.size() : 0);

        // Use comprehensive model (M11) data for township report tables if available, otherwise fallback to M3
        List<EvaluationResult> townshipReportData = (comprehensiveResults != null && !comprehensiveResults.isEmpty())
                ? comprehensiveResults : townshipResults;

        if (townshipReportData != null && !townshipReportData.isEmpty()) {
            String sampleNames = townshipReportData.stream().limit(5).map(EvaluationResult::getRegionName).collect(Collectors.joining(", "));
            log.info("Sample Township Report Data Names (using {}): {}",
                    (comprehensiveResults != null && !comprehensiveResults.isEmpty()) ? "M11" : "M3", sampleNames);
        }

        // Fetch Weights (Heuristic -> Strict)
        List<WeightConfig> configs = weightConfigService.getEnabledConfigs();
        Long townshipConfigId = configs.stream()
            .filter(c -> c.getConfigName() != null && c.getConfigName().contains("乡镇") && !c.getConfigName().contains("社区"))
            .map(WeightConfig::getId)
            .findFirst()
            .orElse(null);
            
        Long communityConfigId = configs.stream()
            .filter(c -> c.getConfigName() != null && c.getConfigName().contains("社区") && c.getConfigName().contains("社区单元"))
            .map(WeightConfig::getId)
            .findFirst()
            .orElse(null);
        
        // Fallback if strict matching fails (try looser matching)
        if (townshipConfigId == null) {
            townshipConfigId = configs.stream()
                .filter(c -> c.getConfigName() != null && c.getConfigName().contains("乡镇"))
                .map(WeightConfig::getId)
                .findFirst()
                .orElse(null);
        }
        if (communityConfigId == null) {
             communityConfigId = configs.stream()
                .filter(c -> c.getConfigName() != null && c.getConfigName().contains("社区"))
                .map(WeightConfig::getId)
                .findFirst()
                .orElse(null);
        }
        
        // Final Fallback
        if (townshipConfigId == null && !configs.isEmpty()) townshipConfigId = configs.get(0).getId();
        if (communityConfigId == null && !configs.isEmpty()) communityConfigId = configs.get(0).getId();

        log.info("Selected Weight Configs - Township: {}, Community: {}", townshipConfigId, communityConfigId);

        List<IndicatorWeight> townshipWeights = townshipConfigId != null ? getWeightsWithAverageScore(townshipConfigId) : new ArrayList<>();
        List<IndicatorWeight> communityWeights = communityConfigId != null ? getWeightsWithAverageScore(communityConfigId) : new ArrayList<>();

        String regionName = "511425".equals(orgCode) ? "青神县" : "青神县";

        // Build Community to Township Map
        Map<String, String> communityToTownshipMap = new HashMap<>();
        Map<String, String> townshipCodeMap = new HashMap<>();
        
        if (townshipResults != null) {
            for (EvaluationResult t : townshipResults) {
                if (t.getRegionCode() != null) {
                    townshipCodeMap.put(t.getRegionCode(), t.getRegionName());
                }
            }
        }
        
        if (communityResults != null) {
            for (EvaluationResult c : communityResults) {
                String cCode = c.getRegionCode();
                if (cCode != null && cCode.length() >= 9) {
                    String tCode = cCode.substring(0, 9);
                    String tName = townshipCodeMap.get(tCode);
                    if (tName != null) {
                        communityToTownshipMap.put(c.getRegionName(), tName);
                    }
                }
            }
        }

        return wordDataPreprocessor.processEvaluationData(
                townshipResults, communityResults, communityByTownResults, comprehensiveResults, townshipWeights, communityWeights, communityToTownshipMap, String.valueOf(year), regionName);
    }

    private List<IndicatorWeight> getWeightsWithAverageScore(Long configId) {
        List<IndicatorWeight> weights = indicatorWeightService.getByConfigId(configId);
        if (indicatorWeightScoreService == null) {
            return weights;
        }

        Map<String, Double> averageWeights = indicatorWeightScoreService.calculateAverageWeights(configId);
        if (averageWeights == null || averageWeights.isEmpty()) {
            return weights;
        }

        for (IndicatorWeight w : weights) {
            Double avg = averageWeights.get(w.getIndicatorCode());
            if (avg != null) {
                w.setWeight(avg);
            }
        }
        return weights;
    }

    private Map<String, Object> mapReportDataToTemplate(EvaluationReportData data) {
        Map<String, Object> flatMap = new LinkedHashMap<>();

        // 1. Basic Info
        if (data.getRegionInfo() != null) {
            flatMap.put("year", data.getRegionInfo().getYear());
            flatMap.put("county", data.getRegionInfo().getName());
        }
        flatMap.put("city", "眉山市");
        flatMap.put("province", "四川省");

        // 2. Indicators (Weights)
        // addWeightTablePlaceholders(flatMap, data); // Deprecated as per user request


        // 3. Merge Statistics from Preprocessor
        if (data.getResults() != null && data.getResults().getStatistics() != null) {
            flatMap.putAll(data.getResults().getStatistics());

            // Map comprehensive statistics to template variables (without prefix)
            // 模板使用 strong_count, mediumStrong_count 等，而不是 comprehensive_strong_count
            // 注意：排除 *_townships_* 变量以避免覆盖 Model 3 的乡镇数据
            Map<String, Object> stats = data.getResults().getStatistics();
            for (String key : stats.keySet()) {
                if (key.startsWith("comprehensive_")) {
                    String newKey = key.substring("comprehensive_".length());
                    // 排除 _townships_ 后缀的变量，这些应该保留前缀以区分综合能力和乡镇能力
                    if (!newKey.contains("_townships_") && !newKey.endsWith("_townships_list")) {
                        flatMap.put(newKey, stats.get(key));
                        // Also map with template format (with {{ }} wrapper)
                        flatMap.put("{{" + newKey + "}}", stats.get(key));
                    }
                    // 保留带前缀的版本供需要区分的地方使用
                    flatMap.put(key, stats.get(key));
                    flatMap.put("{{" + key + "}}", stats.get(key));
                }
            }
        }

        // 4. Map Table Data
        if (data.getResults() != null) {
            if (data.getResults().getTable6Data() != null) {
                log.info("Table 6 Data Size: {}", data.getResults().getTable6Data().size());
                if (!data.getResults().getTable6Data().isEmpty()) {
                    log.info("Table 6 Sample Keys: {}", data.getResults().getTable6Data().get(0).keySet());
                    log.info("Table 6 Sample Row: {}", data.getResults().getTable6Data().get(0));
                }
                flatMap.put("table6_data", data.getResults().getTable6Data());
            } else {
                log.warn("Table 6 Data is NULL");
            }
            
            if (data.getResults().getTable7Data() != null) {
                log.info("Table 7 Data Size: {}", data.getResults().getTable7Data().size());
                flatMap.put("table7_data", data.getResults().getTable7Data());
            }
            if (data.getResults().getTable8Data() != null) flatMap.put("table8_data", data.getResults().getTable8Data());
            if (data.getResults().getTable9Data() != null) flatMap.put("table9_data", data.getResults().getTable9Data());
            
            if (data.getResults().getTable8Footer() != null) {
                flatMap.putAll(data.getResults().getTable8Footer());
                log.info("community_by_town_strong_count: {}", flatMap.get("community_by_town_strong_count"));
                log.info("community_by_town_strong_percent: {}", flatMap.get("community_by_town_strong_percent"));
                log.info("community_strong_up_town_count: {}", flatMap.get("community_strong_up_town_count"));
            }
            
            // Map legacy summary fields if not covered by statistics
            if (data.getResults().getTownship() != null) {
                if (data.getResults().getTownship().getSummary() != null && !flatMap.containsKey("township_stats_summary")) {
                    flatMap.put("township_stats_summary", String.join("、", data.getResults().getTownship().getSummary()));
                }
            }
        }
        
        // 5. Compatibility / Fallback
        flatMap.putIfAbsent("year", "2025");
        
        // Map hardcoded template placeholders to dynamic values
        // "中等" in template -> The mode level itself (e.g. "强", "较弱")
        if (flatMap.containsKey("township_assessment_level")) {
            flatMap.put("中等", flatMap.get("township_assessment_level"));
        }
        
        // Ensure missing params have default
        flatMap.putIfAbsent("township_assessment_level", "中等");
        
        return flatMap;
    }

    // Method addWeightTablePlaceholders removed as per user request (deprecated fixed-row tables)


    private List<Map<String, Object>> buildTableData(List<EvaluationResult> results, String prefix) {
        List<Map<String, Object>> tableData = new ArrayList<>();
        int idx = 1;
        for (EvaluationResult r : results) {
            Map<String, Object> row = new HashMap<>();
            row.put(prefix + "_idx", idx++);
            row.put(prefix + "_name", r.getRegionName());

            // c1: 减灾能力等级 (综合能力等级)
            row.put(prefix + "_c1", r.getComprehensiveCapabilityLevel() != null ? r.getComprehensiveCapabilityLevel() : "中等");
            // c2: 灾害管理能力等级
            row.put(prefix + "_c2", r.getManagementCapabilityLevel() != null ? r.getManagementCapabilityLevel() : "中等");
            // c3: 灾害备灾能力等级
            row.put(prefix + "_c3", r.getSupportCapabilityLevel() != null ? r.getSupportCapabilityLevel() : "中等");
            // c4: 自救转移能力等级
            row.put(prefix + "_c4", r.getSelfRescueCapabilityLevel() != null ? r.getSelfRescueCapabilityLevel() : "中等");

            tableData.add(row);
        }
        return tableData;
    }



    /**
     * 检查模板文件是否存在
     */
    @GetMapping("/template-exists")
    public Result<Map<String, Object>> checkTemplateExists() {
        try {
            boolean exists = wordTemplateService.templateExists();
            Map<String, Object> result = new HashMap<>();
            result.put("exists", exists);

            if (exists) {
                List<String> variables = wordTemplateService.getTemplateVariables();
                result.put("variables", variables);
                result.put("variableCount", variables.size());
            }

            return Result.success(result);
        } catch (Exception e) {
            log.error("检查模板文件是否存在失败", e);
            return Result.error("检查模板文件失败: " + e.getMessage());
        }
    }

    /**
     * 获取模板预览数据
     */
    @GetMapping("/preview-variables")
    public Result<Map<String, Object>> previewVariables(@RequestParam(required = false) Integer year,
                                                      @RequestParam(required = false) String orgCode) {
        try {
            Map<String, Object> variables = getStructuredData(year, orgCode);
            return Result.success(variables);
        } catch (Exception e) {
            log.error("获取预览数据失败", e);
            return Result.error("获取预览数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取结构化评估数据
     */
    private Map<String, Object> getStructuredData(Integer year, String orgCode) {
        if (year == null) year = 2025;
        if (orgCode == null) orgCode = "511425";

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("年份", String.valueOf(year));
        result.put("区县", "511425".equals(orgCode) ? "青神县" : "青神县");

        // 获取乡镇级数据 (Model ID 3)
        List<EvaluationResult> townshipResults = evaluationResultService.getResultsByModelIdAndYearAndOrgCode(3L, year, orgCode);
        // 获取社区级数据 (Model ID 4)
        List<EvaluationResult> communityResults = evaluationResultService.getResultsByModelIdAndYearAndOrgCode(4L, year, orgCode);
        // 获取社区-乡镇级数据 (Model ID 8)
        List<EvaluationResult> communityByTownResults = evaluationResultService.getResultsByModelIdAndYearAndOrgCode(8L, year, orgCode);
        // 获取综合减灾能力数据 (Model ID 11) - 用于综合减灾能力评估结果
        List<EvaluationResult> comprehensiveResults = evaluationResultService.getResultsByModelIdAndYearAndOrgCode(11L, year, orgCode);
        
        // 构建社区到乡镇的映射
        List<CommunityDisasterReductionCapacity> communityBaseList = communityService.lambdaQuery()
                    .eq(CommunityDisasterReductionCapacity::getYear, year).list();
        Map<String, String> communityToTownshipMap = communityBaseList.stream()
                .filter(i -> i.getCommunityName() != null && i.getTownshipName() != null)
                .collect(Collectors.toMap(CommunityDisasterReductionCapacity::getCommunityName, 
                                        CommunityDisasterReductionCapacity::getTownshipName, (k1, k2) -> k1));

        // 1. 综合减灾能力 (使用 Model 11 综合减灾能力模型数据)
        // 如果 Model 11 数据为空，则回退到使用乡镇数据
        List<EvaluationResult> comprehensiveDataSource = (comprehensiveResults != null && !comprehensiveResults.isEmpty())
                ? comprehensiveResults : townshipResults;
        Map<String, Object> comprehensiveResult = buildDetailedAssessmentResults(comprehensiveDataSource, "comprehensive", false, null);
        result.put("综合减灾能力评估结果", comprehensiveResult.get("等级分布"));
        result.put("综合减灾能力评估结果_统计", comprehensiveResult.get("统计特征"));

        // 2. 分项能力评估 (乡镇级)
        Map<String, Object> subDimensions = new LinkedHashMap<>();
        
        Map<String, Object> management = buildDetailedAssessmentResults(townshipResults, "management", false, null);
        subDimensions.put("管理能力", management.get("等级分布"));
        subDimensions.put("管理能力_统计", management.get("统计特征"));
        
        Map<String, Object> support = buildDetailedAssessmentResults(townshipResults, "support", false, null);
        subDimensions.put("保障能力", support.get("等级分布"));
        subDimensions.put("保障能力_统计", support.get("统计特征"));
        
        Map<String, Object> selfRescue = buildDetailedAssessmentResults(townshipResults, "selfRescue", false, null);
        subDimensions.put("自救能力", selfRescue.get("等级分布"));
        subDimensions.put("自救能力_统计", selfRescue.get("统计特征"));
        
        result.put("分项能力评估结果", subDimensions);

        // 3. 社区级评估
        Map<String, Object> communityResult = buildDetailedAssessmentResults(communityResults, "comprehensive", false, null);
        result.put("社区-行政村减灾能力评估结果", communityResult.get("等级分布"));
        result.put("社区-行政村减灾能力评估结果_统计", communityResult.get("统计特征"));
        
        // 4. 社区-乡镇分组评估 (保持原有逻辑，用于生成特定的列表字符串)
        Map<String, Object> communityByTownResult = buildDetailedAssessmentResults(communityResults, "comprehensive", true, communityToTownshipMap);
        result.put("社区-乡镇减灾能力评估结果", communityByTownResult.get("等级分布"));
        // 这个通常不需要统计特征，或者统计特征与上面相同

        // 5. 兼容旧版“乡镇（街道）减灾能力评估结果”Key，指向综合结果
        result.put("乡镇（街道）减灾能力评估结果", result.get("综合减灾能力评估结果"));
        result.put("乡镇（街道）减灾能力评估结果_统计", result.get("综合减灾能力评估结果_统计"));

        // 6. 构建表格数据 (Table 6, 7)
        result.put("table6_data", buildTableData(townshipResults, "t6"));
        result.put("table7_data", buildTableData(communityResults, "t7"));

        // 7. 构建表格数据 (Table 9: 乡镇评估结果列表 - 组合三个模型的数据)
        // 创建快速查找映射
        Map<String, EvaluationResult> townshipMap = townshipResults.stream()
                .collect(Collectors.toMap(EvaluationResult::getRegionCode, r -> r, (a, b) -> a));
        Map<String, EvaluationResult> communityByTownMap = new HashMap<>();
        if (communityByTownResults != null) {
            communityByTownMap = communityByTownResults.stream()
                    .collect(Collectors.toMap(EvaluationResult::getRegionCode, r -> r, (a, b) -> a));
        }
        Map<String, EvaluationResult> comprehensiveMap = new HashMap<>();
        if (comprehensiveResults != null) {
            comprehensiveMap = comprehensiveResults.stream()
                    .collect(Collectors.toMap(EvaluationResult::getRegionCode, r -> r, (a, b) -> a));
        }

        List<Map<String, Object>> table9Data = new ArrayList<>();
        int idx = 1;
        // 使用 townshipResults 作为基础行数据，但组合三个模型的等级数据
        for (EvaluationResult r : townshipResults) {
            String regionCode = r.getRegionCode();
            Map<String, Object> row = new HashMap<>();
            row.put("t9_idx", idx++);
            row.put("t9_name", r.getRegionName());

            // M11: 综合减灾能力等级
            EvaluationResult m11Data = comprehensiveMap.get(regionCode);
            String compLevel = (m11Data != null && m11Data.getComprehensiveCapabilityLevel() != null)
                    ? m11Data.getComprehensiveCapabilityLevel() : "/";
            row.put("t9_comp", compLevel);

            // M3: 乡镇减灾能力等级
            EvaluationResult m3Data = townshipMap.get(regionCode);
            String mgtLevel = (m3Data != null && m3Data.getManagementCapabilityLevel() != null)
                    ? m3Data.getManagementCapabilityLevel() : "/";
            String supLevel = (m3Data != null && m3Data.getSupportCapabilityLevel() != null)
                    ? m3Data.getSupportCapabilityLevel() : "/";
            String selfLevel = (m3Data != null && m3Data.getSelfRescueCapabilityLevel() != null)
                    ? m3Data.getSelfRescueCapabilityLevel() : "/";
            String townLevel = (m3Data != null && m3Data.getComprehensiveCapabilityLevel() != null)
                    ? m3Data.getComprehensiveCapabilityLevel() : "/";
            row.put("t9_mgt", townLevel);

            // M8: 社区-乡镇减灾能力等级
            EvaluationResult m8Data = communityByTownMap.get(regionCode);
            String commTownLevel = (m8Data != null && m8Data.getComprehensiveCapabilityLevel() != null)
                    ? m8Data.getComprehensiveCapabilityLevel() : "/";
            row.put("t9_sup", commTownLevel);
            row.put("t9_self", selfLevel);

            table9Data.add(row);
        }
        result.put("table9_data", table9Data);

        // 8. 构建表格数据 (Table 8: 社区统计列表 - 按乡镇)
        // 结构: 序号, 乡镇, 强, 较强, 中等, 较弱, 弱
        List<Map<String, Object>> table8Data = new ArrayList<>();
        Map<String, Object> table8Footer = new HashMap<>();
        
        // 7.1 分组统计
        Map<String, Map<String, Integer>> townStats = new HashMap<>(); // Town -> Level -> Count
        String[] levels = {"强", "较强", "中等", "较弱", "弱"};
        Map<String, Integer> totalStats = new HashMap<>(); // Level -> Total Count
        for (String level : levels) totalStats.put(level, 0);
        int grandTotal = 0;

        for (EvaluationResult r : communityResults) {
            String cName = r.getRegionName();
            String tName = communityToTownshipMap.get(cName);
            if (tName == null) tName = "其他"; // Should not happen ideally
            
            String level = r.getComprehensiveCapabilityLevel();
            if (level == null) level = "中等";
            
            townStats.putIfAbsent(tName, new HashMap<>());
            Map<String, Integer> stats = townStats.get(tName);
            stats.put(level, stats.getOrDefault(level, 0) + 1);
            
            // Grand Total logic
            if (totalStats.containsKey(level)) {
                totalStats.put(level, totalStats.get(level) + 1);
                grandTotal++;
            }
        }
        
        // 7.2 构建列表行
        int t8Idx = 1;
        // 排序乡镇名?
        List<String> townNames = new ArrayList<>(townStats.keySet());
        Collections.sort(townNames);
        
        for (String tName : townNames) {
            Map<String, Integer> stats = townStats.get(tName);
            Map<String, Object> row = new HashMap<>();
            row.put("t8_idx", t8Idx++);
            row.put("t8_name", tName);
            row.put("t8_c1", stats.getOrDefault("强", 0));
            row.put("t8_c2", stats.getOrDefault("较强", 0));
            row.put("t8_c3", stats.getOrDefault("中等", 0));
            row.put("t8_c4", stats.getOrDefault("较弱", 0));
            row.put("t8_c5", stats.getOrDefault("弱", 0));
            table8Data.add(row);
        }
        result.put("table8_data", table8Data);
        
        // 7.3 构建页脚 (合计与占比)
        table8Footer.put("{{t8_total_c1}}", totalStats.get("强"));
        table8Footer.put("{{t8_total_c2}}", totalStats.get("较强"));
        table8Footer.put("{{t8_total_c3}}", totalStats.get("中等"));
        table8Footer.put("{{t8_total_c4}}", totalStats.get("较弱"));
        table8Footer.put("{{t8_total_c5}}", totalStats.get("弱"));
        
        if (grandTotal > 0) {
            table8Footer.put("{{t8_pct_c1}}", String.format("%.2f%%", (double)totalStats.get("强") / grandTotal * 100));
            table8Footer.put("{{t8_pct_c2}}", String.format("%.2f%%", (double)totalStats.get("较强") / grandTotal * 100));
            table8Footer.put("{{t8_pct_c3}}", String.format("%.2f%%", (double)totalStats.get("中等") / grandTotal * 100));
            table8Footer.put("{{t8_pct_c4}}", String.format("%.2f%%", (double)totalStats.get("较弱") / grandTotal * 100));
            table8Footer.put("{{t8_pct_c5}}", String.format("%.2f%%", (double)totalStats.get("弱") / grandTotal * 100));
        } else {
             table8Footer.put("{{t8_pct_c1}}", "0.00%");
             table8Footer.put("{{t8_pct_c2}}", "0.00%");
             table8Footer.put("{{t8_pct_c3}}", "0.00%");
             table8Footer.put("{{t8_pct_c4}}", "0.00%");
             table8Footer.put("{{t8_pct_c5}}", "0.00%");
        }
        result.put("table8_footer", table8Footer);

        return result;
    }

    /**
     * 构建详细评估结果（包含统计特征和等级分布）
     */
    private Map<String, Object> buildDetailedAssessmentResults(List<EvaluationResult> results, String dimension, boolean isCommunityByTownship, Map<String, String> communityToTownshipMap) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        // 1. 计算统计特征 (平均分、最高分、最低分)
        if (!isCommunityByTownship) {
            Map<String, Object> stats = new LinkedHashMap<>();
            DoubleSummaryStatistics scoreStats = results.stream()
                .map(r -> getDimensionScore(r, dimension))
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .summaryStatistics();
            
            if (scoreStats.getCount() > 0) {
                stats.put("平均得分", String.format("%.2f", scoreStats.getAverage()));
                stats.put("最高分", String.format("%.2f", scoreStats.getMax()));
                stats.put("最低分", String.format("%.2f", scoreStats.getMin()));
            } else {
                stats.put("平均得分", "0.00");
                stats.put("最高分", "0.00");
                stats.put("最低分", "0.00");
            }
            result.put("统计特征", stats);
        }

        // 2. 构建等级分布
        List<Map<String, Object>> distribution = new ArrayList<>();
        String[] levels = {"强", "较强", "中等", "较弱", "弱"};
        Map<String, List<String>> grouped = new HashMap<>();
        for(String level : levels) grouped.put(level, new ArrayList<>());
        
        if (isCommunityByTownship) {
             Map<String, Map<String, List<String>>> townshipGroups = new HashMap<>();
             for (EvaluationResult r : results) {
                 String cName = r.getRegionName();
                 String level = getDimensionLevel(r, dimension);
                 if (level == null) level = "中等";
                 if (!grouped.containsKey(level)) level = "中等";
                 
                 String tName = communityToTownshipMap != null ? communityToTownshipMap.get(cName) : null;
                 if (tName != null) {
                     townshipGroups.putIfAbsent(tName, new HashMap<>());
                     townshipGroups.get(tName).putIfAbsent(level, new ArrayList<>());
                     townshipGroups.get(tName).get(level).add(cName);
                 }
             }
             for (String level : levels) {
                 List<String> items = new ArrayList<>();
                 for (Map.Entry<String, Map<String, List<String>>> entry : townshipGroups.entrySet()) {
                     String tName = entry.getKey();
                     List<String> cList = entry.getValue().get(level);
                     if (cList != null && !cList.isEmpty()) {
                         items.add(tName + "（" + String.join("、", cList) + "）");
                     }
                 }
                 grouped.put(level, items);
             }
        } else {
            for (EvaluationResult r : results) {
                String name = r.getRegionName();
                String level = getDimensionLevel(r, dimension);
                if (level == null) level = "中等";
                if (!grouped.containsKey(level)) level = "中等";
                grouped.get(level).add(name);
            }
        }
        
        long sumCounts = 0;
        for(String level : levels) sumCounts += grouped.get(level).size();
        
        for (String level : levels) {
            Map<String, Object> item = new LinkedHashMap<>();
            List<String> names = grouped.get(level);
            item.put("分级", level);
            item.put("数量", String.valueOf(names.size()));
            String percent = "0%";
            if (sumCounts > 0) {
                 percent = String.format("%.2f%%", (double)names.size() / sumCounts * 100); 
            }
            item.put("占比", percent);
            item.put("列表", names);
            distribution.add(item);
        }
        
        result.put("等级分布", distribution);
        return result;
    }

    private String getDimensionLevel(EvaluationResult r, String dimension) {
        switch (dimension) {
            case "management": return r.getManagementCapabilityLevel();
            case "support": return r.getSupportCapabilityLevel();
            case "selfRescue": return r.getSelfRescueCapabilityLevel();
            case "comprehensive": 
            default: return r.getComprehensiveCapabilityLevel();
        }
    }

    private BigDecimal getDimensionScore(EvaluationResult r, String dimension) {
        switch (dimension) {
            case "management": return r.getManagementCapabilityScore();
            case "support": return r.getSupportCapabilityScore();
            case "selfRescue": return r.getSelfRescueCapabilityScore();
            case "comprehensive": 
            default: return r.getComprehensiveCapabilityScore();
        }
    }

    /**
     * 调试API：获取模板文件的原始段落文本（增强版）
     */
    @GetMapping("/debug-content-enhanced")
    public ResponseEntity<Map<String, Object>> debugTemplateContentEnhanced() {
        try {
            log.info("开始调试模板文件内容（增强版）");
            Map<String, Object> result = wordTemplateService.debugTemplateContent();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("调试模板文件内容失败", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "调试失败: " + e.getMessage());
            errorResult.put("stackTrace", getStackTraceAsString(e));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
        }
    }

    /**
     * 获取Word模板的实际内容预览
     */
    @GetMapping("/real-template-content")
    public ResponseEntity<Map<String, Object>> getRealTemplateContent() {
        try {
            log.info("开始获取Word模板实际内容预览");
            Map<String, Object> result = wordTemplateService.getRealTemplateContent();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取Word模板实际内容失败", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "获取失败: " + e.getMessage());
            errorResult.put("stackTrace", getStackTraceAsString(e));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
        }
    }

    /**
     * 下载原始Word模板文件
     */
    @GetMapping("/download-template")
    public void downloadTemplate(HttpServletResponse response) {
        try {
            log.info("开始下载Word模板文件");
            byte[] templateData = wordTemplateService.getTemplateFile();

            String filename = "四川省眉山市青神县减灾能力评估技术报告-系统模板.docx";
            String contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

            response.setContentType(contentType);
            // 使用URL编码解决中文文件名问题
            String encodedFilename = java.net.URLEncoder.encode(filename, "UTF-8");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename);
            response.setContentLength(templateData.length);

            try (ServletOutputStream outputStream = response.getOutputStream()) {
                outputStream.write(templateData);
                outputStream.flush();
            }
        } catch (Exception e) {
            log.error("下载Word模板文件失败", e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "下载Word模板文件失败");
            } catch (IOException ex) {
                log.error("发送错误响应失败", ex);
            }
        }
    }

    /**
     * 保存编辑后的Word文档内容
     */
    @PostMapping("/save-edited-content")
    public ResponseEntity<Map<String, Object>> saveEditedContent(@RequestBody Map<String, String> request) {
        try {
            log.info("开始保存编辑后的Word文档内容");

            String htmlContent = request.get("htmlContent");
            if (htmlContent == null || htmlContent.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "HTML内容不能为空");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            boolean success = wordTemplateService.saveEditedContent(htmlContent);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);

            if (success) {
                response.put("message", "Word文档保存成功");
                log.info("编辑后的Word文档保存成功");
            } else {
                response.put("message", "Word文档保存失败");
                log.error("编辑后的Word文档保存失败");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("保存编辑后的Word文档内容失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "保存失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 下载编辑后的Word文档
     */
    @PostMapping("/download-edited")
    public void downloadEditedDocument(@RequestBody Map<String, String> request, HttpServletResponse response) {
        try {
            log.info("开始下载编辑后的Word文档");

            String htmlContent = request.get("htmlContent");
            if (htmlContent == null || htmlContent.trim().isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "HTML内容不能为空");
                return;
            }

            // 将HTML转换为Word文档
            byte[] wordData = wordTemplateService.convertHtmlToWord(htmlContent);

            if (wordData == null || wordData.length == 0) {
                throw new RuntimeException("生成的Word文档为空");
            }

            log.info("生成的Word文档大小: {} bytes", wordData.length);

            String filename = "青神县减灾能力评估技术报告_修改版.docx";
            String contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

            response.setContentType(contentType);
            // 使用URL编码解决中文文件名问题
            String encodedFilename = java.net.URLEncoder.encode(filename, "UTF-8");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename);
            response.setContentLength(wordData.length);

            try (ServletOutputStream outputStream = response.getOutputStream()) {
                outputStream.write(wordData);
                outputStream.flush();
            }

            log.info("编辑后的Word文档下载成功");
        } catch (Exception e) {
            log.error("下载编辑后的Word文档失败", e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "下载编辑后的Word文档失败");
            } catch (IOException ex) {
                log.error("发送错误响应失败", ex);
            }
        }
    }

    /**
     * 上传编辑后的文档
     */
    @PostMapping("/upload-edited")
    public ResponseEntity<Map<String, Object>> uploadEditedDocument(@RequestParam("file") MultipartFile file) {
        try {
            log.info("开始上传编辑后的Word文档: {}", file.getOriginalFilename());

            // 验证文件类型
            if (!file.getOriginalFilename().toLowerCase().endsWith(".docx")) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "请上传Word文档(.docx格式)");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // 创建上传目录
            String uploadDir = "uploads/edited/";
            java.io.File directory = new java.io.File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 生成唯一文件名
            String fileName = "edited_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String filePath = uploadDir + fileName;

            // 保存文件
            java.nio.file.Files.copy(file.getInputStream(), java.nio.file.Paths.get(filePath));

            log.info("编辑后的Word文档上传成功: {}", filePath);

            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("success", true);
            successResponse.put("message", "文档上传成功");
            successResponse.put("filePath", filePath);
            successResponse.put("fileName", fileName);
            return ResponseEntity.ok(successResponse);

        } catch (Exception e) {
            log.error("上传编辑后的Word文档失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "上传文档失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 获取完整的报告预览数据（包括格式化的统计数据）
     */
    @GetMapping("/preview-report")
    public Result<Map<String, Object>> previewReport(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String orgCode) {
        try {
            Map<String, Object> previewData = new HashMap<>();

            // 基础变量数据
            previewData.put("variables", prepareReportVariables(year, orgCode));

            // 格式化的统计数据
            Map<String, Object> statistics = formatStatisticsForPreview(year, orgCode);
            previewData.put("statistics", statistics);

            // 报告元数据
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("title", "四川省雅安市青神县减灾能力评估技术报告");
            metadata.put("generateTime", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")));
            metadata.put("organization", "青神县应急管理局");
            metadata.put("technicalSupport", "减灾能力评估系统");
            previewData.put("metadata", metadata);

            return Result.success(previewData);
        } catch (Exception e) {
            log.error("获取报告预览失败", e);
            return Result.error("获取报告预览失败: " + e.getMessage());
        }
    }

    /**
     * 准备报告变量数据
     */
    private Map<String, Object> prepareReportVariables(Integer year, String orgCode) {
        // 设置默认值
        if (year == null) year = LocalDate.now().getYear();
        if (orgCode == null) orgCode = "511425"; // 默认青神县

        Map<String, Object> variables = new HashMap<>();

        // 时间相关变量
        LocalDate currentDate = LocalDate.now();
        // 如果查询年份是当前年份，使用当前日期，否则使用查询年份的12月31日或者其他逻辑
        // 这里保持当前日期作为生成日期
        variables.put("{{year}}", String.valueOf(year));
        variables.put("{{month}}", String.valueOf(currentDate.getMonthValue()));
        variables.put("{{day}}", String.valueOf(currentDate.getDayOfMonth()));
        variables.put("{{current_date}}", currentDate.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")));

        // 地理信息变量
        variables.put("{{province}}", "四川省");
        
        // 根据orgCode设置城市信息
        if ("511425".equals(orgCode)) {
            variables.put("{{city}}", "眉山市");
            variables.put("{{county}}", "青神县");
            variables.put("{{full_address}}", "四川省眉山市青神县");
        } else {
            // 默认值，或者根据其他orgCode逻辑
            variables.put("{{city}}", "眉山市"); // 默认为眉山市
            variables.put("{{county}}", "青神县");
            variables.put("{{full_address}}", "四川省眉山市青神县");
        }

        // 评估统计数据
        Map<String, Object> stats = getEvaluationStatistics(year, orgCode);
        variables.putAll(stats);

        // 单位信息
        variables.put("{{organization}}", "青神县应急管理局");
        variables.put("{{technical_support}}", "减灾能力评估系统");

        // ---------------------------------------------------------
        // 统一化参数映射 (处理文档中的绿色背景占位符)
        // ---------------------------------------------------------
        
        // 1. 基础信息映射
        variables.put("青神县", variables.get("{{county}}"));
        variables.put("眉山市", variables.get("{{city}}"));
        variables.put("四川省", variables.get("{{province}}"));
        variables.put("2025", variables.get("{{year}}"));
        variables.put("XX", variables.get("{{year}}")); // 处理可能的XX年份占位符
        
        // 2. 编制信息映射
        variables.put("编制单位：", "编制单位：" + variables.get("{{organization}}"));
        variables.put("编制时间：", "编制时间：" + variables.get("{{current_date}}"));
        
        // 3. 统计数据映射 (将特定的占位符数字映射到动态统计值)
        // 注意：这里需要根据实际模板中的占位符数值进行对应，以下为根据提取列表推测的映射
        // 如果模板中的占位符改变，这里也需要更新
        
        // 示例：将模板中硬编码的统计数值映射到计算出的统计变量
        // variables.put("14.29", stats.get("{{strong_percent}}")); 
        // variables.put("42.86", stats.get("{{medium_percent}}"));
        
        // 4. 等级映射
        variables.put("强", "强");
        variables.put("较强", "较强");
        variables.put("中等", "中等");
        variables.put("较弱", "较弱");
        variables.put("弱", "弱");

        // 5. 列表数据映射 (处理特定的长文本占位符)
        // 乡镇列表
        variables.put("汉阳镇", stats.get("{{strong_townships_list}}"));
        variables.put("汉阳镇、高台镇和罗波乡", stats.get("{{mediumStrong_townships_list}}"));
        variables.put("瑞峰镇", stats.get("{{medium_townships_list}}"));
        variables.put("白果乡", stats.get("{{weak_townships_list}}"));
        variables.put("白果乡和罗波乡", stats.get("{{weak_townships_list}}")); // 可能的重复占位符
        
        // 社区分组列表
        variables.put("青竹街道〔凤阳社区、文林社区、花园社区等17个社区（行政村）〕、西龙镇（桂花村、长池村）、罗波乡（龙泉村）3", 
            stats.get("{{community_medium_list}}"));
            
        variables.put("青竹街道（沙河村、建华社区、青衣社区）、瑞峰镇（天池村、刘家场社区）、西龙镇（观金社区）、高台镇（诸葛村、麻柳社区、百家池村、杨店村）、白果乡（三清寺村、甘家沟村、官厅坝村 、罗湾村）、罗波乡（西坝村、官斗山村）6",
            stats.get("{{community_weak_list}}"));
            
        // 其他可能出现的占位符映射
        variables.put("青竹街道", stats.get("{{mediumStrong_townships_list}}"));
        variables.put("青竹街道、汉阳镇和西龙镇", stats.get("{{medium_townships_list}}"));
        variables.put("青竹街道、西龙镇和白果乡", stats.get("{{weak_townships_list}}"));

        return variables;
    }

    /**
     * 获取评估统计数据（真实数据）
     */
    private Map<String, Object> getEvaluationStatistics(Integer year, String orgCode) {
        Map<String, Object> stats = new HashMap<>();

        try {
            // 1. 获取乡镇（模型ID=3）评估结果 - 用于乡镇减灾能力统计
            List<EvaluationResult> townshipResults = evaluationResultService.getResultsByModelIdAndYearAndOrgCode(3L, year, orgCode);

            // 2. 获取社区（模型ID=4）评估结果
            List<EvaluationResult> communityResults = evaluationResultService.getResultsByModelIdAndYearAndOrgCode(4L, year, orgCode);

            // 2.5. 获取综合减灾能力（模型ID=11）评估结果 - 用于综合减灾能力统计
            List<EvaluationResult> comprehensiveResults = evaluationResultService.getResultsByModelIdAndYearAndOrgCode(11L, year, orgCode);

            // 3. 获取社区基础数据用于映射乡镇关系
            List<CommunityDisasterReductionCapacity> communityBaseList =
                communityService.lambdaQuery()
                    .eq(CommunityDisasterReductionCapacity::getYear, year)
                    .list();

            // 建立 社区名 -> 乡镇名 的映射
            Map<String, String> communityToTownshipMap = new HashMap<>();
            for (CommunityDisasterReductionCapacity item : communityBaseList) {
                if (item.getCommunityName() != null && item.getTownshipName() != null) {
                    communityToTownshipMap.put(item.getCommunityName(), item.getTownshipName());
                }
            }

            // 4. 统计各等级数量及列表
            Map<String, Long> townshipStats = new HashMap<>();
            Map<String, Long> communityStats = new HashMap<>();

            // 存储各等级的乡镇/社区列表
            Map<String, List<String>> townshipLevelLists = new HashMap<>();
            Map<String, List<String>> communityLevelLists = new HashMap<>();
            Map<String, Map<String, List<String>>> townshipCommunityGroups = new HashMap<>(); // 乡镇下的社区分组

            // 初始化统计
            Arrays.asList("强", "较强", "中等", "较弱", "弱").forEach(level -> {
                townshipStats.put(level, 0L);
                communityStats.put(level, 0L);
                townshipLevelLists.put(level, new ArrayList<>());
                communityLevelLists.put(level, new ArrayList<>());
            });

            // 处理乡镇结果 - 使用综合减灾能力模型（Model 11）数据
            // 如果 Model 11 数据为空，则回退到使用乡镇模型（Model 3）数据
            List<EvaluationResult> townshipDataSource = (comprehensiveResults != null && !comprehensiveResults.isEmpty())
                    ? comprehensiveResults : townshipResults;

            for (EvaluationResult result : townshipDataSource) {
                String level = result.getComprehensiveCapabilityLevel();
                String name = result.getRegionName();
                if (level == null) level = "中等"; // 默认
                
                // 确保level在预定义范围内
                if (!townshipStats.containsKey(level)) level = "中等";
                
                townshipStats.put(level, townshipStats.get(level) + 1);
                townshipLevelLists.get(level).add(name);
                
                // 初始化分组Map
                townshipCommunityGroups.putIfAbsent(name, new HashMap<>());
                for (String l : Arrays.asList("强", "较强", "中等", "较弱", "弱")) {
                    townshipCommunityGroups.get(name).putIfAbsent(l, new ArrayList<>());
                }
            }
            
            // 处理社区结果
            for (EvaluationResult result : communityResults) {
                String level = result.getComprehensiveCapabilityLevel();
                String name = result.getRegionName();
                if (level == null) level = "中等";
                
                if (!communityStats.containsKey(level)) level = "中等";
                
                communityStats.put(level, communityStats.get(level) + 1);
                communityLevelLists.get(level).add(name);
                
                // 找到所属乡镇
                String townshipName = communityToTownshipMap.get(name);
                if (townshipName != null) {
                    townshipCommunityGroups.putIfAbsent(townshipName, new HashMap<>());
                    for (String l : Arrays.asList("强", "较强", "中等", "较弱", "弱")) {
                        townshipCommunityGroups.get(townshipName).putIfAbsent(l, new ArrayList<>());
                    }
                    townshipCommunityGroups.get(townshipName).get(level).add(name);
                }
            }

            // 5. 计算总数和百分比
            long totalTownships = townshipDataSource.size();
            long totalCommunities = communityResults.size();

            // 生成列表字符串辅助方法
            java.util.function.Function<List<String>, String> formatList = list -> {
                if (list == null || list.isEmpty()) return "无";
                return String.join("、", list);
            };

            // 生成带有统计的列表字符串 (例如: A镇、B镇等X个乡镇)
            java.util.function.BiFunction<List<String>, String, String> formatListWithCount = (list, suffix) -> {
                if (list == null || list.isEmpty()) return "无";
                if (list.size() > 5) {
                    return String.join("、", list.subList(0, 5)) + "等" + list.size() + "个" + suffix;
                }
                return String.join("、", list);
            };
            
            // 生成乡镇-社区分组字符串
            java.util.function.Function<String, String> formatTownshipCommunityGroup = level -> {
                List<String> parts = new ArrayList<>();
                int totalCount = 0;
                for (Map.Entry<String, Map<String, List<String>>> entry : townshipCommunityGroups.entrySet()) {
                    String township = entry.getKey();
                    List<String> communities = entry.getValue().get(level);
                    if (communities != null && !communities.isEmpty()) {
                        totalCount += communities.size();
                        parts.add(township + "（" + String.join("、", communities) + "）");
                    }
                }
                if (parts.isEmpty()) return "无";
                return String.join("、", parts) + totalCount; // 结尾加上总数，模拟模板格式
            };

            // 乡镇级统计
            stats.put("{{total_townships}}", String.valueOf(totalTownships));
            stats.put("{{strong_count}}", String.valueOf(townshipStats.getOrDefault("强", 0L)));
            stats.put("{{strong_percent}}", totalTownships > 0 ?
                String.format("%.2f", (double) townshipStats.getOrDefault("强", 0L) / totalTownships * 100) : "0.00");
            stats.put("{{strong_townships_list}}", formatList.apply(townshipLevelLists.get("强")));
            
            stats.put("{{mediumStrong_count}}", String.valueOf(townshipStats.getOrDefault("较强", 0L)));
            stats.put("{{mediumStrong_percent}}", totalTownships > 0 ?
                String.format("%.2f", (double) townshipStats.getOrDefault("较强", 0L) / totalTownships * 100) : "0.00");
            stats.put("{{mediumStrong_townships_list}}", formatList.apply(townshipLevelLists.get("较强")));
            
            stats.put("{{medium_count}}", String.valueOf(townshipStats.getOrDefault("中等", 0L)));
            stats.put("{{medium_percent}}", totalTownships > 0 ?
                String.format("%.2f", (double) townshipStats.getOrDefault("中等", 0L) / totalTownships * 100) : "0.00");
            stats.put("{{medium_townships_list}}", formatList.apply(townshipLevelLists.get("中等")));
            
            stats.put("{{weak_count}}", String.valueOf(townshipStats.getOrDefault("较弱", 0L)));
            stats.put("{{weak_percent}}", totalTownships > 0 ?
                String.format("%.2f", (double) townshipStats.getOrDefault("较弱", 0L) / totalTownships * 100) : "0.00");
            stats.put("{{weak_townships_list}}", formatList.apply(townshipLevelLists.get("较弱")));
            
            stats.put("{{veryWeak_count}}", String.valueOf(townshipStats.getOrDefault("弱", 0L)));
            stats.put("{{veryWeak_percent}}", totalTownships > 0 ?
                String.format("%.2f", (double) townshipStats.getOrDefault("弱", 0L) / totalTownships * 100) : "0.00");
            stats.put("{{veryWeak_townships_list}}", formatList.apply(townshipLevelLists.get("弱")));

            // 社区级统计
            stats.put("{{total_communities}}", String.valueOf(totalCommunities));
            
            stats.put("{{community_strong_count}}", String.valueOf(communityStats.getOrDefault("强", 0L)));
            stats.put("{{community_strong_percent}}", totalCommunities > 0 ?
                String.format("%.2f", (double) communityStats.getOrDefault("强", 0L) / totalCommunities * 100) : "0.00");
            stats.put("{{community_strong_list}}", formatTownshipCommunityGroup.apply("强"));
            
            stats.put("{{community_mediumStrong_count}}", String.valueOf(communityStats.getOrDefault("较强", 0L)));
            stats.put("{{community_mediumStrong_percent}}", totalCommunities > 0 ?
                String.format("%.2f", (double) communityStats.getOrDefault("较强", 0L) / totalCommunities * 100) : "0.00");
            stats.put("{{community_mediumStrong_list}}", formatTownshipCommunityGroup.apply("较强"));
            
            stats.put("{{community_medium_count}}", String.valueOf(communityStats.getOrDefault("中等", 0L)));
            stats.put("{{community_medium_percent}}", totalCommunities > 0 ?
                String.format("%.2f", (double) communityStats.getOrDefault("中等", 0L) / totalCommunities * 100) : "0.00");
            stats.put("{{community_medium_list}}", formatTownshipCommunityGroup.apply("中等"));
            
            stats.put("{{community_weak_count}}", String.valueOf(communityStats.getOrDefault("较弱", 0L)));
            stats.put("{{community_weak_percent}}", totalCommunities > 0 ?
                String.format("%.2f", (double) communityStats.getOrDefault("较弱", 0L) / totalCommunities * 100) : "0.00");
            stats.put("{{community_weak_list}}", formatTownshipCommunityGroup.apply("较弱"));
            
            stats.put("{{community_veryWeak_count}}", String.valueOf(communityStats.getOrDefault("弱", 0L)));
            stats.put("{{community_veryWeak_percent}}", totalCommunities > 0 ?
                String.format("%.2f", (double) communityStats.getOrDefault("弱", 0L) / totalCommunities * 100) : "0.00");
            stats.put("{{community_veryWeak_list}}", formatTownshipCommunityGroup.apply("弱"));

            log.info("获取到评估统计数据 - 乡镇总数: {}, 社区总数: {}", totalTownships, totalCommunities);

        } catch (Exception e) {
            log.error("获取评估统计数据失败，使用默认数据", e);
            // 如果获取失败，使用默认数据
            return getDefaultStatistics();
        }

        return stats;
    }

    /**
     * 计算社区综合能力等级
     */
    private String calculateCapabilityLevel(CommunityDisasterReductionCapacity item) {
        int score = 0;
        int count = 0;

        // 基础设施和预案
        if ("是".equals(item.getHasEmergencyPlan())) { score += 1; count++; }
        if ("是".equals(item.getHasVulnerableGroupsList())) { score += 1; count++; }
        if ("是".equals(item.getHasDisasterPointsList())) { score += 1; count++; }
        if ("是".equals(item.getHasDisasterMap())) { score += 1; count++; }

        // 物资和资金
        if (item.getLastYearFundingAmount() != null && item.getLastYearFundingAmount().doubleValue() > 0) { score += 1; count++; }
        if (item.getMaterialsEquipmentValue() != null && item.getMaterialsEquipmentValue().doubleValue() > 0) { score += 1; count++; }

        // 人力资源
        if (item.getMedicalServiceCount() != null && item.getMedicalServiceCount() > 0) { score += 1; count++; }
        if (item.getMilitiaReserveCount() != null && item.getMilitiaReserveCount() > 0) { score += 1; count++; }
        if (item.getRegisteredVolunteerCount() != null && item.getRegisteredVolunteerCount() > 0) { score += 1; count++; }

        // 培训和演练
        if (item.getLastYearTrainingParticipants() != null && item.getLastYearTrainingParticipants() > 0) { score += 1; count++; }
        if (item.getLastYearDrillParticipants() != null && item.getLastYearDrillParticipants() > 0) { score += 1; count++; }

        // 避难场所
        if (item.getEmergencyShelterCapacity() != null && item.getEmergencyShelterCapacity() > 0) { score += 1; count++; }

        // 示范社区
        if ("是".equals(item.getIsNationalDemoCommunity())) { score += 1; count++; }
        if ("是".equals(item.getIsProvincialDemoCommunity())) { score += 1; count++; }

        if (count == 0) return "中等";

        double ratio = (double) score / count;
        if (ratio >= 0.8) return "强";
        else if (ratio >= 0.6) return "较强";
        else if (ratio >= 0.4) return "中等";
        else if (ratio >= 0.2) return "较弱";
        else return "弱";
    }

    /**
     * 根据评分获取能力等级
     */
    private String getCapabilityLevel(Object score) {
        if (score == null) return "中等";

        double value;
        if (score instanceof String) {
            try {
                value = Double.parseDouble((String) score);
            } catch (NumberFormatException e) {
                return "中等";
            }
        } else if (score instanceof Number) {
            value = ((Number) score).doubleValue();
        } else {
            return "中等";
        }

        // 根据TOPSIS算法结果的数值范围进行分级（0-1之间）
        if (value >= 0.8) return "强";
        else if (value >= 0.6) return "较强";
        else if (value >= 0.4) return "中等";
        else if (value >= 0.2) return "较弱";
        else return "弱";
    }

    /**
     * 根据等级获取分数
     */
    private int getScoreForLevel(String level) {
        switch (level) {
            case "强": return 5;
            case "较强": return 4;
            case "中等": return 3;
            case "较弱": return 2;
            case "弱": return 1;
            default: return 3;
        }
    }

    /**
     * 默认统计数据（当无法获取真实数据时使用）
     */
    private Map<String, Object> getDefaultStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("{{total_townships}}", "7");
        stats.put("{{strong_count}}", "1");
        stats.put("{{strong_percent}}", "14.29");
        stats.put("{{mediumStrong_count}}", "1");
        stats.put("{{mediumStrong_percent}}", "14.29");
        stats.put("{{medium_count}}", "3");
        stats.put("{{medium_percent}}", "42.86");
        stats.put("{{weak_count}}", "2");
        stats.put("{{weak_percent}}", "28.57");
        stats.put("{{veryWeak_count}}", "0");
        stats.put("{{veryWeak_percent}}", "0.00");

        stats.put("{{total_communities}}", "58");
        stats.put("{{community_strong_count}}", "5");
        stats.put("{{community_strong_percent}}", "8.62");
        stats.put("{{community_mediumStrong_count}}", "12");
        stats.put("{{community_mediumStrong_percent}}", "20.69");
        stats.put("{{community_medium_count}}", "16");
        stats.put("{{community_medium_percent}}", "27.59");
        stats.put("{{community_weak_count}}", "25");
        stats.put("{{community_weak_percent}}", "43.10");
        stats.put("{{community_veryWeak_count}}", "0");
        stats.put("{{community_veryWeak_percent}}", "0.00");

        return stats;
    }

    /**
     * 格式化统计数据用于预览
     */
    private Map<String, Object> formatStatisticsForPreview(Integer year, String orgCode) {
        Map<String, Object> formattedStats = new HashMap<>();

        try {
            Map<String, Object> rawStats = getEvaluationStatistics(year, orgCode);

            // 乡镇级统计数据
            List<Map<String, Object>> townshipData = new ArrayList<>();
            String[] townshipLevels = {"强", "较强", "中等", "较弱", "弱"};
            String[] townshipKeys = {"strong", "mediumStrong", "medium", "weak", "veryWeak"};

            for (int i = 0; i < townshipLevels.length; i++) {
                Map<String, Object> levelData = new HashMap<>();
                levelData.put("level", townshipLevels[i]);
                levelData.put("count", Integer.parseInt((String) rawStats.get("{{" + townshipKeys[i] + "_count}}")));
                levelData.put("percent", Double.parseDouble((String) rawStats.get("{{" + townshipKeys[i] + "_percent}}")));
                townshipData.add(levelData);
            }

            formattedStats.put("townshipData", townshipData);
            formattedStats.put("totalTownships", Integer.parseInt((String) rawStats.get("{{total_townships}}")));

            // 社区级统计数据
            List<Map<String, Object>> communityData = new ArrayList<>();
            String[] communityKeys = {"community_strong", "community_mediumStrong", "community_medium", "community_weak", "community_veryWeak"};

            for (int i = 0; i < townshipLevels.length; i++) {
                Map<String, Object> levelData = new HashMap<>();
                levelData.put("level", townshipLevels[i]);
                levelData.put("count", Integer.parseInt((String) rawStats.get("{{" + communityKeys[i] + "_count}}")));
                levelData.put("percent", Double.parseDouble((String) rawStats.get("{{" + communityKeys[i] + "_percent}}")));
                communityData.add(levelData);
            }

            formattedStats.put("communityData", communityData);
            formattedStats.put("totalCommunities", Integer.parseInt((String) rawStats.get("{{total_communities}}")));

            // 评估结论生成
            formattedStats.put("conclusion", generateEvaluationConclusion(townshipData));

        } catch (Exception e) {
            log.error("格式化统计数据失败", e);
            // 使用默认数据
            formattedStats = getDefaultFormattedStatistics();
        }

        return formattedStats;
    }

    /**
     * 生成评估结论
     */
    private String generateEvaluationConclusion(List<Map<String, Object>> townshipData) {
        StringBuilder conclusion = new StringBuilder();

        // 找出最多的等级
        String maxLevel = "中等";
        int maxCount = 0;
        for (Map<String, Object> level : townshipData) {
            int count = (Integer) level.get("count");
            if (count > maxCount) {
                maxCount = count;
                maxLevel = (String) level.get("level");
            }
        }

        conclusion.append("根据评估数据，青神县各乡镇的减灾能力以").append(maxLevel).append("为主，");

        // 分析整体情况
        long strongCount = townshipData.stream()
            .filter(level -> level.get("level").equals("强") || level.get("level").equals("较强"))
            .mapToInt(level -> (Integer) level.get("count"))
            .sum();

        int totalTownships = (Integer) townshipData.stream()
            .mapToInt(level -> (Integer) level.get("count"))
            .sum();

        double strongPercent = (double) strongCount / totalTownships * 100;

        if (strongPercent >= 50) {
            conclusion.append("整体减灾能力较强，");
        } else if (strongPercent >= 30) {
            conclusion.append("整体减灾能力中等，");
        } else {
            conclusion.append("整体减灾能力有待提升，");
        }

        conclusion.append("建议各乡镇结合自身实际情况，针对薄弱环节采取有效措施，全面提升防灾减灾救灾能力。");

        return conclusion.toString();
    }

    /**
     * 默认格式化统计数据
     */
    private Map<String, Object> getDefaultFormattedStatistics() {
        Map<String, Object> defaultStats = new HashMap<>();

        // 乡镇数据
        List<Map<String, Object>> townshipData = new ArrayList<>();
        townshipData.add(createLevelData("强", 1, 14.29));
        townshipData.add(createLevelData("较强", 1, 14.29));
        townshipData.add(createLevelData("中等", 3, 42.86));
        townshipData.add(createLevelData("较弱", 2, 28.57));
        townshipData.add(createLevelData("弱", 0, 0.00));

        defaultStats.put("townshipData", townshipData);
        defaultStats.put("totalTownships", 7);

        // 社区数据
        List<Map<String, Object>> communityData = new ArrayList<>();
        communityData.add(createLevelData("强", 5, 8.62));
        communityData.add(createLevelData("较强", 12, 20.69));
        communityData.add(createLevelData("中等", 16, 27.59));
        communityData.add(createLevelData("较弱", 25, 43.10));
        communityData.add(createLevelData("弱", 0, 0.00));

        defaultStats.put("communityData", communityData);
        defaultStats.put("totalCommunities", 58);
        defaultStats.put("conclusion", "根据评估数据，青神县各乡镇的减灾能力以中等为主，建议各乡镇结合自身实际情况，针对薄弱环节采取有效措施，全面提升防灾减灾救灾能力。");

        return defaultStats;
    }

    /**
     * 创建等级数据
     */
    private Map<String, Object> createLevelData(String level, int count, double percent) {
        Map<String, Object> data = new HashMap<>();
        data.put("level", level);
        data.put("count", count);
        data.put("percent", percent);
        return data;
    }

    /**
     * OnlyOffice回调处理
     */
    @PostMapping("/callback")
    public ResponseEntity<String> handleOnlyOfficeCallback(@RequestBody Map<String, Object> callbackData) {
        try {
            log.info("收到OnlyOffice回调: {}", callbackData);

            Number statusNumber = (Number) callbackData.get("status");
            Integer status = statusNumber == null ? null : statusNumber.intValue();
            String key = (String) callbackData.get("key");
            String url = (String) callbackData.get("url");

            if (status == null || key == null) {
                log.error("OnlyOffice回调缺少必要参数");
                return ResponseEntity.badRequest().body("{\"error\": 1, \"message\": \"缺少必要参数\"}");
            }

            // OnlyOffice状态码处理
            switch (status) {
                case 2: // 文档已准备好保存
                    log.info("文档已准备好保存: {}", key);
                    if (url != null && !url.isEmpty()) {
                        new Thread(() -> saveDocumentFromUrl(url, key)).start();
                    }
                    break;

                case 3: // 文档编辑已关闭
                    log.info("文档编辑已关闭: {}", key);
                    break;

                case 6: // 文档编辑已完成，正在保存
                    log.info("正在保存文档: {}", key);
                    if (url != null && !url.isEmpty()) {
                        new Thread(() -> saveDocumentFromUrl(url, key)).start();
                    }
                    break;

                case 7: // 文档保存错误
                    log.error("文档保存错误: {}", key);
                    break;

                default:
                    log.info("未处理的OnlyOffice回调状态: {}", status);
                    break;
            }

            // 返回成功响应
            Map<String, Object> response = new HashMap<>();
            response.put("error", 0);
            response.put("message", "成功");

            return ResponseEntity.ok("{\"error\": 0}");

        } catch (Exception e) {
            log.error("处理OnlyOffice回调失败", e);
            return ResponseEntity.status(500).body("{\"error\": 1, \"message\": \"处理回调失败\"}");
        }
    }

    /**
     * 从URL保存文档
     */
    private void saveDocumentFromUrl(String url, String key) {
        try {
            log.info("从URL保存文档: {}", url);

            java.net.URL documentUrl = new java.net.URL(url);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) documentUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(30000);

            if (connection.getResponseCode() == 200) {
                // 创建保存目录
                String saveDir = "uploads/onlyoffice/";
                java.io.File directory = new java.io.File(saveDir);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                // 生成保存文件名
                String fileName = "document_" + key + "_" + System.currentTimeMillis() + ".docx";
                String filePath = saveDir + fileName;

                // 下载并保存文件
                try (java.io.InputStream inputStream = connection.getInputStream();
                     java.io.FileOutputStream outputStream = new java.io.FileOutputStream(filePath)) {

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }

                log.info("文档保存成功: {}", filePath);
            } else {
                log.error("下载文档失败，HTTP状态码: {}", connection.getResponseCode());
            }

            connection.disconnect();

        } catch (Exception e) {
            log.error("从URL保存文档失败: {}", url, e);
        }
    }

    /**
     * 将HTML转换为Word文档（用于OnlyOffice初始化）
     */
    @PostMapping("/convert-to-word")
    public ResponseEntity<byte[]> convertHtmlToWord(@RequestBody Map<String, String> request) {
        try {
            String htmlContent = request.get("htmlContent");
            if (htmlContent == null || htmlContent.trim().isEmpty()) {
                // 如果没有HTML内容，返回空模板
                htmlContent = "<html><body><p>减灾能力评估技术报告</p></body></html>";
            }

            byte[] wordData = wordTemplateService.convertHtmlToWord(htmlContent);

            return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=document.docx")
                .body(wordData);

        } catch (Exception e) {
            log.error("HTML转Word失败", e);
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * 将HTML转换为Word文档（供在线编辑器使用）
     */
    @PostMapping("/convert-html-to-word")
    public void convertHtmlToWordForEditor(@RequestBody Map<String, String> request, HttpServletResponse response) {
        try {
            log.info("开始将HTML转换为Word文档");

            String htmlContent = request.get("htmlContent");
            if (htmlContent == null || htmlContent.trim().isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "HTML内容不能为空");
                return;
            }

            // 将HTML转换为Word文档
            byte[] wordData = wordTemplateService.convertHtmlToWord(htmlContent);

            String filename = "青神县减灾能力评估技术报告_" +
                            java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + ".docx";
            String contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

            response.setContentType(contentType);
            // 使用URL编码解决中文文件名问题
            String encodedFilename = java.net.URLEncoder.encode(filename, "UTF-8");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename);
            response.setContentLength(wordData.length);

            try (ServletOutputStream outputStream = response.getOutputStream()) {
                outputStream.write(wordData);
                outputStream.flush();
            }

            log.info("HTML转Word文档转换成功");
        } catch (Exception e) {
            log.error("HTML转Word文档失败", e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "HTML转Word失败");
            } catch (IOException ex) {
                log.error("发送错误响应失败", ex);
            }
        }
    }

    /**
     * 将异常堆栈转换为字符串
     */
    private String getStackTraceAsString(Exception e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * 查找最新上传的专题图图片
     *
     * @param year 年份
     * @param orgCode 组织机构代码
     * @return 图片文件路径，如果未找到则返回null
     */
    private String findLatestThematicMapImage(Integer year, String orgCode) {
        Map<String, String> allMaps = findAllThematicMapImages(year, orgCode);
        return allMaps.isEmpty() ? null : allMaps.values().iterator().next();
    }

    /**
     * 查找所有级别的最新专题图图片
     *
     * @param year 年份
     * @param orgCode 组织机构代码
     * @return 图片文件路径Map (级别 -> 路径)
     */
    private Map<String, String> findAllThematicMapImages(Integer year, String orgCode) {
        Map<String, String> resultMap = new LinkedHashMap<>();
        try {
            String thematicMapDir = "uploads/thematic-maps/";
            java.io.File directory = new java.io.File(thematicMapDir);

            if (!directory.exists() || !directory.isDirectory()) {
                log.info("专题图目录不存在: {}", thematicMapDir);
                return resultMap;
            }

            // 定义4个级别（按Word模板中图片的顺序）
            // 图片顺序: 1.乡镇 2.社区-乡镇 3.社区-行政村 4.综合
            String[] levels = {"township", "community_township", "community_village", "comprehensive"};

            // 获取目录中所有的.png文件
            java.io.File[] files = directory.listFiles((dir, name) ->
                name.endsWith(".png") && name.contains("thematic_map_"));

            if (files == null || files.length == 0) {
                log.info("未找到专题图图片文件");
                return resultMap;
            }

            // 为每个级别查找最新的图片
            for (String level : levels) {
                final String targetLevel = level;
                java.io.File[] levelFiles = Arrays.stream(files)
                    .filter(f -> f.getName().contains("thematic_map_" + targetLevel + "_"))
                    .toArray(java.io.File[]::new);

                if (levelFiles.length > 0) {
                    // 按修改时间排序，获取最新的文件
                    java.io.File latestFile = Arrays.stream(levelFiles)
                        .max(Comparator.comparingLong(java.io.File::lastModified))
                        .orElse(levelFiles[0]);

                    log.info("找到{}级别专题图图片: {}", targetLevel, latestFile.getAbsolutePath());
                    resultMap.put(targetLevel, latestFile.getAbsolutePath());
                }
            }

            log.info("共找到{}张专题图图片", resultMap.size());

        } catch (Exception e) {
            log.error("查找专题图图片失败", e);
        }
        return resultMap;
    }
}
