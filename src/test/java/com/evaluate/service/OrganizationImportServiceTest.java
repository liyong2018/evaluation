package com.evaluate.service;

import com.evaluate.util.GeojsonImportUtil;
import com.evaluate.util.GeojsonImportUtil.ImportResult;
import com.evaluate.util.OrganizationSqlUtil;
import com.evaluate.util.OrganizationSqlUtil.SqlScriptResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 组织机构导入服务测试
 * 用于测试和执行 2024 年组织机构数据导入
 */
@Slf4j
@SpringBootTest
public class OrganizationImportServiceTest {

    @Autowired
    private GeojsonImportUtil geojsonImportUtil;

    @Autowired
    private OrganizationSqlUtil organizationSqlUtil;

    /**
     * 导入 2024 年乡镇数据
     * 文件路径：frontend/public/zzjg/2024-xzjznl-example.geojson
     */
    @Test
    public void testImport2024Townships() {
        String geojsonPath = "frontend/public/zzjg/2024-xzjznl-example.geojson";
        Integer year = 2024;

        log.info("===== 开始导入 2024 年乡镇数据 =====");
        log.info("文件路径：{}", geojsonPath);

        try {
            ImportResult result = geojsonImportUtil.import2024Townships(geojsonPath, year);

            log.info("===== 导入完成 =====");
            log.info("{}", result.getSummary());

            // 打印新增列表
            if (!result.getAdded().isEmpty()) {
                log.info("\n--- 新增乡镇列表 ---");
                result.getAdded().forEach(record ->
                    log.info("  - {} ({})", record.getName(), record.getCode())
                );
            }

            // 打印删除列表
            if (!result.getRemoved().isEmpty()) {
                log.info("\n--- 删除乡镇列表 ---");
                result.getRemoved().forEach(record ->
                    log.info("  - {} ({})", record.getName(), record.getCode())
                );
            }

            // 打印变更列表
            if (!result.getChanged().isEmpty()) {
                log.info("\n--- 变更乡镇列表 ---");
                result.getChanged().forEach(change ->
                    log.info("  - {}: {} -> {}", change.getCode(), change.getOldName(), change.getNewName())
                );
            }

        } catch (Exception e) {
            log.error("导入失败", e);
        }
    }

    /**
     * 生成 2024 年乡镇数据变更 SQL 脚本并保存到文件
     */
    @Test
    public void testGenerate2024TownshipChangeSql() {
        String geojsonPath = "frontend/public/zzjg/2024-xzjznl-example.geojson";
        Integer year = 2024;
        String outputPath = "docs/organization_2024_change.sql";

        log.info("===== 开始生成 2024 年乡镇数据变更 SQL =====");
        log.info("文件路径：{}", geojsonPath);

        try {
            SqlScriptResult result = organizationSqlUtil.generate2024TownshipChangeSql(geojsonPath, year);

            log.info("===== SQL 生成完成 =====");
            log.info("{}", result.getSummary());

            // 保存 SQL 到文件
            Files.writeString(Paths.get(outputPath), result.getSql());
            log.info("SQL 文件已保存到：{}", outputPath);

        } catch (IOException e) {
            log.error("生成 SQL 失败", e);
        }
    }
}
