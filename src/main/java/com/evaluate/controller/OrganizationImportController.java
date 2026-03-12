package com.evaluate.controller;

import com.evaluate.common.Result;
import com.evaluate.util.GeojsonImportUtil;
import com.evaluate.util.GeojsonImportUtil.ImportResult;
import com.evaluate.util.OrganizationSqlUtil;
import com.evaluate.util.OrganizationSqlUtil.SqlScriptResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 组织机构导入控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/organization-import")
public class OrganizationImportController {

    @Autowired
    private GeojsonImportUtil geojsonImportUtil;

    @Autowired
    private OrganizationSqlUtil organizationSqlUtil;

    /**
     * 从 GeoJSON 文件导入乡镇数据（支持任意年份）
     */
    @PostMapping("/import-townships")
    public Result<Map<String, Object>> importTownships(
            @RequestParam String filePath,
            @RequestParam(defaultValue = "2024") Integer year) {
        log.info("导入 {} 年乡镇数据，文件：{}, 年份：{}", year, filePath, year);
        try {
            ImportResult result = geojsonImportUtil.import2024Townships(filePath, year);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("summary", result.getSummary());
            response.put("addedCount", result.getAddedCount());
            response.put("removedCount", result.getRemovedCount());
            response.put("changedCount", result.getChangedCount());
            response.put("added", result.getAdded());
            response.put("removed", result.getRemoved());
            response.put("changed", result.getChanged());

            return Result.success(response);
        } catch (Exception e) {
            log.error("导入乡镇数据失败", e);
            return Result.error("导入失败：" + e.getMessage());
        }
    }

    /**
     * 从 GeoJSON 文件导入 2024 年乡镇数据（兼容旧 API）
     */
    @PostMapping("/import-2024-townships")
    public Result<Map<String, Object>> import2024Townships(
            @RequestParam String filePath,
            @RequestParam(defaultValue = "2024") Integer year) {
        return importTownships(filePath, year);
    }

    /**
     * 生成任意年份乡镇数据变更 SQL 脚本
     */
    @PostMapping("/generate-sql")
    public Result<Map<String, Object>> generateSql(
            @RequestParam String filePath,
            @RequestParam(defaultValue = "2024") Integer year) {
        log.info("生成 SQL 脚本，文件：{}, 年份：{}", filePath, year);
        try {
            SqlScriptResult result = organizationSqlUtil.generateTownshipChangeSql(filePath, year);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("summary", result.getSummary());
            response.put("sql", result.getSql());

            return Result.success(response);
        } catch (Exception e) {
            log.error("生成 SQL 脚本失败", e);
            return Result.error("生成失败：" + e.getMessage());
        }
    }

    /**
     * 下载任意年份乡镇数据变更 SQL 脚本文件
     */
    @GetMapping("/download-sql")
    public ResponseEntity<InputStreamResource> downloadSql(
            @RequestParam String filePath,
            @RequestParam(defaultValue = "2024") Integer year) throws IOException {
        log.info("生成并下载 SQL 脚本文件，文件：{}, 年份：{}", filePath, year);

        SqlScriptResult result = organizationSqlUtil.generateTownshipChangeSql(filePath, year);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(
                result.getSql().getBytes(StandardCharsets.UTF_8)
        );

        String filename = String.format("organization_%s_change_%s.sql",
                year,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(new InputStreamResource(inputStream));
    }
}
