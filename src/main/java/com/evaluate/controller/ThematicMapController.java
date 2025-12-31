package com.evaluate.controller;

import com.evaluate.entity.EvaluationResult;
import com.evaluate.service.EvaluationResultService;
import com.evaluate.service.IWordTemplateService;
import com.evaluate.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 专题图控制器
 *
 * @author System
 * @since 2025-12-23
 */
@Slf4j
@RestController
@RequestMapping("/api/thematic-map")
@CrossOrigin(origins = "*")
public class ThematicMapController {

    @Autowired
    private EvaluationResultService evaluationResultService;

    @Autowired
    private IWordTemplateService wordTemplateService;

    /**
     * 获取专题图数据
     * @param level 数据级别: township(乡镇), community_village(社区-行政村), community_township(社区-乡镇), comprehensive(综合)
     */
    @GetMapping("/data")
    public Result<List<Map<String, Object>>> getThematicData(
            @RequestParam(required = false) Long reportId,
            @RequestParam(required = false) Long surveyId,
            @RequestParam(required = false) Long algorithmId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String orgCode,
            @RequestParam(required = false, defaultValue = "township") String level) {

        log.info("获取专题图数据: reportId={}, surveyId={}, algorithmId={}, year={}, orgCode={}, level={}",
                reportId, surveyId, algorithmId, year, orgCode, level);

        try {
            // 获取所有评估结果
            List<EvaluationResult> allResults = evaluationResultService.getAllEvaluationResults();

            // 根据级别过滤数据
            List<EvaluationResult> filteredResults = allResults.stream()
                .filter(result -> {
                    String regionName = result.getRegionName();
                    if (regionName == null) return false;

                    // 根据级别参数过滤
                    switch (level) {
                        case "township":
                            // 乡镇级：只保留以"街道"、"镇"、"乡"结尾的数据
                            return regionName.endsWith("街道") ||
                                   regionName.endsWith("镇") ||
                                   regionName.endsWith("乡");
                        case "community_village":
                            // 社区-行政村级：只保留以"社区"、"村"结尾的数据
                            return regionName.endsWith("社区") ||
                                   regionName.endsWith("村");
                        case "community_township":
                            // 社区-乡镇级：保留以"社区"、"街道"、"镇"、"乡"结尾的数据
                            return regionName.endsWith("社区") ||
                                   regionName.endsWith("街道") ||
                                   regionName.endsWith("镇") ||
                                   regionName.endsWith("乡");
                        case "comprehensive":
                        default:
                            // 综合：包含所有数据
                            return true;
                    }
                })
                .filter(result -> {
                    // 按年份过滤 (如果结果中有年份字段)
                    if (year != null && result.getCreateTime() != null) {
                         if (result.getCreateTime().getYear() != year) {
                             return false;
                         }
                    }

                    // 按组织机构代码过滤
                    if (orgCode != null && !orgCode.isEmpty()) {
                        if (result.getOrgCode() != null && !result.getOrgCode().startsWith(orgCode)) {
                            // 简单的通过前缀匹配，或者精确匹配
                            // return false;
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());

            // 按地区名称去重，保留每个地区最新的评估结果
            Map<String, EvaluationResult> latestResults = new LinkedHashMap<>();
            for (EvaluationResult result : filteredResults) {
                String regionName = result.getRegionName();
                if (regionName == null) continue;

                EvaluationResult existing = latestResults.get(regionName);
                if (existing == null ||
                    (result.getCreateTime() != null &&
                     (existing.getCreateTime() == null ||
                      result.getCreateTime().isAfter(existing.getCreateTime())))) {
                    latestResults.put(regionName, result);
                }
            }

            // 转换为前端需要的格式
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (EvaluationResult item : latestResults.values()) {
                Map<String, Object> map = new HashMap<>();

                map.put("regionId", item.getId());
                map.put("regionName", item.getRegionName());
                map.put("county", "青神县"); // 默认为青神县，实际应从地区信息获取

                // 分数处理
                BigDecimal totalScore = item.getComprehensiveCapabilityScore();
                map.put("score", totalScore);
                map.put("totalScore", totalScore);

                // 能力等级
                map.put("capabilityLevel", item.getComprehensiveCapabilityLevel());

                // 详细能力分
                Map<String, Object> details = new HashMap<>();
                details.put("disasterPreventionCapability", item.getManagementCapabilityScore());
                details.put("emergencyResponseCapability", item.getSupportCapabilityScore());
                details.put("recoveryReconstructionCapability", item.getSelfRescueCapabilityScore());
                map.put("details", details);

                resultList.add(map);
            }

            log.info("返回专题图数据: 级别={}, {} 条数据", level, resultList.size());
            return Result.success(resultList);

        } catch (Exception e) {
            log.error("获取专题图数据失败", e);
            return Result.error("获取专题图数据失败: " + e.getMessage());
        }
    }

    /**
     * 保存专题图图片
     */
    @PostMapping("/save-image")
    public Result<String> saveMapImage(@RequestBody Map<String, Object> data) {
        log.info("保存专题图图片: title={}", data.get("title"));
        // 实际保存逻辑待实现，目前仅返回成功
        return Result.success("保存成功");
    }

    /**
     * 上传专题图图片
     */
    @PostMapping(value = "/upload-map-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String, String>> uploadMapImage(
            @RequestParam("image") MultipartFile imageFile,
            @RequestParam("year") Integer year,
            @RequestParam("orgCode") String orgCode) {

        log.info("上传专题图图片 - year: {}, orgCode: {}, size: {}, contentType: {}",
                year, orgCode, imageFile.getSize(), imageFile.getContentType());

        try {
            // 验证图片文件
            if (imageFile.isEmpty()) {
                return Result.error("图片文件不能为空");
            }

            // 验证文件类型
            String contentType = imageFile.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("只支持图片文件");
            }

            // 创建保存目录
            String tempDir = "uploads/thematic-maps/";
            Path directoryPath = Paths.get(tempDir);
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            // 生成文件名
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS"));
            String fileName = String.format("thematic_map_%s_%s_%s.png", year, orgCode, timestamp);
            Path filePath = directoryPath.resolve(fileName);

            // 保存文件
            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("专题图图片已保存: {}", filePath);

            // 返回图片访问URL
            String imageUrl = "/api/thematic-map/map-image/" + fileName;

            Map<String, String> result = new HashMap<>();
            result.put("fileName", fileName);
            result.put("filePath", filePath.toString());
            result.put("imageUrl", imageUrl);
            return Result.success(result);

        } catch (Exception e) {
            log.error("上传专题图图片失败", e);
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取专题图图片
     */
    @GetMapping("/map-image/{filename:.+}")
    public void getMapImage(@PathVariable String filename, HttpServletResponse response) {
        try {
            Path imagePath = Paths.get("uploads/thematic-maps", filename);
            if (!Files.exists(imagePath)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "图片不存在");
                return;
            }

            byte[] imageBytes = Files.readAllBytes(imagePath);
            response.setContentType("image/png");
            response.setContentLength(imageBytes.length);
            response.getOutputStream().write(imageBytes);
            response.getOutputStream().flush();

        } catch (Exception e) {
            log.error("获取图片失败", e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "获取图片失败");
            } catch (IOException ex) {
                log.error("发送错误响应失败", ex);
            }
        }
    }
}
