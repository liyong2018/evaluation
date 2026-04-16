package com.evaluate.service;

import com.evaluate.util.GeojsonImportUtil;
import com.evaluate.util.GeojsonImportUtil.ImportResult;
import com.evaluate.util.OrganizationSqlUtil;
import com.evaluate.util.OrganizationSqlUtil.SqlScriptResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.evaluate.entity.Organization;
import com.evaluate.entity.GrassrootsOrganization;
import com.evaluate.mapper.OrganizationMapper;
import com.evaluate.mapper.GrassrootsOrganizationMapper;

import org.apache.poi.ss.usermodel.*;

import java.io.InputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private OrganizationMapper organizationMapper;

    @Autowired
    private GrassrootsOrganizationMapper grassrootsOrganizationMapper;

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

    @Test
    @Transactional(rollbackFor = Exception.class)
    @Rollback(false)
    public void testApply2025DiffFromSystemXls() throws Exception {
        String xls2024 = "docs/2024年组织机构 - 系统.xls";
        String xls2025 = "docs/2025年组织机构 - 系统.xls";
        int baseYear = 2024;
        int targetYear = 2025;

        Map<String, SimpleOrgRow> rows2024 = readSystemXls(xls2024);
        Map<String, SimpleOrgRow> rows2025 = readSystemXls(xls2025);

        DiffResult diff = diffByCode(rows2024, rows2025);

        log.info("===== XLS 差异统计 =====");
        log.info("2024 唯一code: {}", rows2024.size());
        log.info("2025 唯一code: {}", rows2025.size());
        log.info("新增: {}, 删除: {}, 变更: {}", diff.added.size(), diff.removed.size(), diff.changed.size());

        String suffix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String orgBak = "organization_bak_" + suffix;
        String grassBak = "grassroots_organization_bak_" + suffix;
        backupTable("organization", orgBak);
        backupTable("grassroots_organization", grassBak);
        log.info("已备份: organization -> {}, grassroots_organization -> {}", orgBak, grassBak);

        ApplyStats stats = new ApplyStats();

        for (String code : diff.added) {
            SimpleOrgRow row = rows2025.get(code);
            if (row == null) {
                continue;
            }
            applyActiveRow(row, targetYear, stats);
        }

        for (String code : diff.changed) {
            SimpleOrgRow row = rows2025.get(code);
            if (row == null) {
                continue;
            }
            applyActiveRow(row, targetYear, stats);
        }

        for (String code : diff.removed) {
            SimpleOrgRow row = rows2024.get(code);
            if (row == null) {
                continue;
            }
            applyDeleteMarker(row, targetYear, stats);
        }

        log.info("===== 写库完成（{} -> {}）=====", baseYear, targetYear);
        log.info("organization: upsert={}, deleteMarker={}", stats.organizationUpsert, stats.organizationDeleteMarker);
        log.info("grassroots_organization: upsert={}, deleteMarker={}", stats.grassrootsUpsert, stats.grassrootsDeleteMarker);
    }

    @Test
    @Transactional(rollbackFor = Exception.class)
    @Rollback(false)
    public void testRepairFangcaoToHighTech() throws Exception {
        int year2024 = 2024;
        int year2025 = 2025;
        String townshipCode = "510172704";

        String suffix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String grassBak = "grassroots_organization_bak_" + suffix;
        backupTable("grassroots_organization", grassBak);
        log.info("已备份: grassroots_organization -> {}", grassBak);

        Map<String, SimpleOrgRow> rows2024 = readSystemXls("docs/2024年组织机构 - 系统.xls");
        Map<String, SimpleOrgRow> rows2025 = readSystemXls("docs/2025年组织机构 - 系统.xls");

        List<SimpleOrgRow> communities2024 = rows2024.values().stream()
                .filter(Objects::nonNull)
                .filter(r -> StringUtils.hasText(r.code) && r.code.startsWith(townshipCode) && r.code.length() == 12)
                .collect(Collectors.toList());
        List<SimpleOrgRow> communities2025 = rows2025.values().stream()
                .filter(Objects::nonNull)
                .filter(r -> StringUtils.hasText(r.code) && r.code.startsWith(townshipCode) && r.code.length() == 12)
                .collect(Collectors.toList());

        SimpleOrgRow sample = !communities2025.isEmpty() ? communities2025.get(0) : (!communities2024.isEmpty() ? communities2024.get(0) : null);
        if (sample == null) {
            throw new IllegalStateException("未在系统 XLS 中找到芳草街街道的社区数据（510172704****）");
        }

        SimpleOrgRow townshipRow = new SimpleOrgRow(
                townshipCode,
                sample.rawName,
                sample.provinceName,
                sample.cityName,
                sample.countyName,
                sample.townshipName,
                null,
                sample.townshipName != null ? sample.townshipName : "芳草街街道"
        );

        Long countyId = resolveBaselineCountyId(townshipCode.substring(0, 6));
        if (countyId == null) {
            throw new IllegalStateException("未找到 510172（成都高新区）的区县基准记录，无法修复芳草街数据归属");
        }

        upsertGrassrootsYearRow(townshipRow, year2024);
        for (SimpleOrgRow row : communities2024) {
            upsertGrassrootsYearRow(row, year2024);
        }

        upsertGrassrootsYearRow(townshipRow, year2025);
        if (communities2025.isEmpty()) {
            log.info("2025 系统 XLS 未出现 510172704**** 社区明细，将复用 2024 的社区作为 2025 的有效下级（sourceYear=2024）");
        }

        jdbcTemplate.update("UPDATE grassroots_organization SET parent_id = NULL WHERE code = ? AND year IN (?, ?)",
                townshipCode, year2024, year2025);

        long c24 = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM grassroots_organization WHERE year = ? AND code LIKE ?",
                Long.class, year2024, townshipCode + "%");
        long c25 = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM grassroots_organization WHERE year = ? AND code LIKE ?",
                Long.class, year2025, townshipCode + "%");
        log.info("修复完成：{} 年 {}* 记录数={}, {} 年 {}* 记录数={}", year2024, townshipCode, c24, year2025, townshipCode, c25);
    }

    @Test
    @Transactional(rollbackFor = Exception.class)
    @Rollback(false)
    public void testRepairHighTechTownshipsAndRemoveFromWuhou() throws Exception {
        int year2024 = 2024;
        int year2025 = 2025;
        String[] townshipCodes = new String[]{"510172701", "510172702", "510172704"};

        String suffix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String grassBak = "grassroots_organization_bak_" + suffix;
        backupTable("grassroots_organization", grassBak);
        log.info("已备份: grassroots_organization -> {}", grassBak);

        Long highTechCountyId = resolveBaselineCountyId("510172");
        if (highTechCountyId == null) {
            throw new IllegalStateException("未找到 510172（成都高新区）的区县基准记录");
        }
        Long wuhouCountyId = resolveBaselineCountyId("510107");
        if (wuhouCountyId == null) {
            throw new IllegalStateException("未找到 510107（武侯区）的区县基准记录");
        }

        Map<String, SimpleOrgRow> rows2024 = readSystemXls("docs/2024年组织机构 - 系统.xls");
        Map<String, SimpleOrgRow> rows2025 = readSystemXls("docs/2025年组织机构 - 系统.xls");

        for (String townshipCode : townshipCodes) {
            List<SimpleOrgRow> communities2024 = rows2024.values().stream()
                    .filter(Objects::nonNull)
                    .filter(r -> StringUtils.hasText(r.code) && r.code.startsWith(townshipCode) && r.code.length() == 12)
                    .collect(Collectors.toList());
            List<SimpleOrgRow> communities2025 = rows2025.values().stream()
                    .filter(Objects::nonNull)
                    .filter(r -> StringUtils.hasText(r.code) && r.code.startsWith(townshipCode) && r.code.length() == 12)
                    .collect(Collectors.toList());

            SimpleOrgRow sample = !communities2025.isEmpty() ? communities2025.get(0) : (!communities2024.isEmpty() ? communities2024.get(0) : null);
            if (sample == null) {
                throw new IllegalStateException("未在系统 XLS 中找到街道的社区数据: " + townshipCode + "****");
            }

            SimpleOrgRow townshipRow = new SimpleOrgRow(
                    townshipCode,
                    sample.rawName,
                    sample.provinceName,
                    sample.cityName,
                    sample.countyName,
                    sample.townshipName,
                    null,
                    sample.townshipName != null ? sample.townshipName : townshipCode
            );

            upsertGrassrootsYearRow(townshipRow, year2024);
            for (SimpleOrgRow row : communities2024) {
                upsertGrassrootsYearRow(row, year2024);
            }
            upsertGrassrootsYearRow(townshipRow, year2025);

            jdbcTemplate.update("UPDATE grassroots_organization SET county_id = ?, county_name = '成都高新区' " +
                            "WHERE code LIKE ? AND county_id = ? AND (is_baseline = 1 OR year <= ?)",
                    highTechCountyId, townshipCode + "%", wuhouCountyId, year2025);

            jdbcTemplate.update("UPDATE grassroots_organization SET parent_id = NULL WHERE code LIKE ? AND year IN (?, ?)",
                    townshipCode + "%", year2024, year2025);
        }

        jdbcTemplate.update("UPDATE grassroots_organization SET county_id = ?, county_name = '成都高新区' " +
                        "WHERE code LIKE '5101727%' AND county_id <> ? AND (is_baseline = 1 OR year <= ?)",
                highTechCountyId, highTechCountyId, year2025);

        long wuhouStill = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM grassroots_organization WHERE county_id = ? AND (is_baseline = 1 OR year <= ?) AND code LIKE '5101727%'",
                Long.class, wuhouCountyId, year2025);
        log.info("武侯区残留 5101727* 记录数（应为0）: {}", wuhouStill);
    }

    private void backupTable(String sourceTable, String backupTable) {
        jdbcTemplate.execute("DROP TABLE IF EXISTS " + backupTable);
        jdbcTemplate.execute("CREATE TABLE " + backupTable + " LIKE " + sourceTable);
        jdbcTemplate.execute("INSERT INTO " + backupTable + " SELECT * FROM " + sourceTable);
    }

    private void applyActiveRow(SimpleOrgRow row, int year, ApplyStats stats) {
        int level = resolveLevel(row.code);
        if (level <= 0) {
            return;
        }
        if (level <= 3) {
            upsertOrganizationYearRow(row, year);
            stats.organizationUpsert++;
            return;
        }
        if (level == 4 || level == 5) {
            upsertGrassrootsYearRow(row, year);
            stats.grassrootsUpsert++;
        }
    }

    private void applyDeleteMarker(SimpleOrgRow row, int year, ApplyStats stats) {
        int level = resolveLevel(row.code);
        if (level <= 0) {
            return;
        }
        if (level <= 3) {
            createOrMarkOrganizationDelete(row, year);
            stats.organizationDeleteMarker++;
            return;
        }
        if (level == 4 || level == 5) {
            createOrMarkGrassrootsDelete(row, year);
            stats.grassrootsDeleteMarker++;
        }
    }

    private void upsertOrganizationYearRow(SimpleOrgRow row, int year) {
        String code = normalizeText(row.code);
        String name = normalizeText(row.leafName);
        if (code == null || name == null) {
            return;
        }
        int level = resolveLevel(code);
        String provinceName = normalizeText(row.provinceName);
        String cityName = normalizeText(row.cityName);
        String countyName = normalizeText(row.countyName);

        Organization existing = organizationMapper.selectByCodeAndYearIncludeDeleted(code, year);
        if (existing != null) {
            if (existing.getIsDeleted() != null && existing.getIsDeleted() == 1) {
                organizationMapper.markAsUndeletedByCodeAndYear(code, year);
            }
            Organization patch = new Organization();
            patch.setId(existing.getId());
            patch.setName(name);
            patch.setLevel(level);
            patch.setYear(year);
            patch.setIsBaseline(0);
            patch.setBaselineCode(code);
            patch.setIsDeleted(0);
            patch.setDataSource("IMPORT");
            patch.setProvinceName(provinceName);
            patch.setCityName(cityName);
            patch.setCountyName(countyName);
            patch.setTownshipName(null);
            patch.setCommunityName(null);
            organizationMapper.updateById(patch);
            return;
        }

        Organization created = new Organization();
        created.setCode(code);
        created.setName(name);
        created.setLevel(level);
        created.setYear(year);
        created.setIsBaseline(0);
        created.setBaselineCode(code);
        created.setIsDeleted(0);
        created.setDataSource("IMPORT");
        created.setProvinceName(provinceName);
        created.setCityName(cityName);
        created.setCountyName(countyName);
        organizationMapper.insert(created);
    }

    private void createOrMarkOrganizationDelete(SimpleOrgRow row, int year) {
        String code = normalizeText(row.code);
        String name = normalizeText(row.leafName);
        if (code == null) {
            return;
        }
        int level = resolveLevel(code);
        Organization existing = organizationMapper.selectByCodeAndYearIncludeDeleted(code, year);
        if (existing != null) {
            if (existing.getIsDeleted() == null || existing.getIsDeleted() != 1) {
                organizationMapper.markAsDeleted(existing.getId());
            }
            if (name != null && !Objects.equals(normalizeText(existing.getName()), name)) {
                Organization patch = new Organization();
                patch.setId(existing.getId());
                patch.setName(name);
                organizationMapper.updateById(patch);
            }
            return;
        }
        Organization marker = new Organization();
        marker.setCode(code);
        marker.setName(name != null ? name : code);
        marker.setLevel(level);
        marker.setYear(year);
        marker.setIsBaseline(0);
        marker.setBaselineCode(code);
        marker.setIsDeleted(1);
        marker.setDataSource("IMPORT");
        organizationMapper.insert(marker);
    }

    private void upsertGrassrootsYearRow(SimpleOrgRow row, int year) {
        String code = normalizeText(row.code);
        String name = normalizeText(row.leafName);
        if (code == null || name == null) {
            return;
        }
        int level = resolveLevel(code);
        if (level != 4 && level != 5) {
            return;
        }
        String provinceName = normalizeText(row.provinceName);
        String cityName = normalizeText(row.cityName);
        String countyName = normalizeText(row.countyName);
        String townshipName = normalizeText(row.townshipName);
        String communityName = normalizeText(row.communityName);
        String countyCode = code.length() >= 6 ? code.substring(0, 6) : null;
        Long countyId = resolveBaselineCountyId(countyCode);

        GrassrootsOrganization existing = grassrootsOrganizationMapper.selectByCodeAndYearIncludeDeleted(code, year);
        if (existing != null) {
            if (existing.getIsDeleted() != null && existing.getIsDeleted() == 1) {
                grassrootsOrganizationMapper.restoreAndUpdateYearRecord(
                        code, year, name, level, countyId, existing.getParentId(),
                        provinceName, cityName, countyName, townshipName, communityName);
                return;
            }
            GrassrootsOrganization patch = new GrassrootsOrganization();
            patch.setId(existing.getId());
            patch.setName(name);
            patch.setLevel(level);
            patch.setYear(year);
            patch.setIsBaseline(0);
            patch.setBaselineCode(code);
            patch.setIsDeleted(0);
            patch.setDataSource("IMPORT");
            patch.setCountyId(countyId);
            patch.setProvinceName(provinceName);
            patch.setCityName(cityName);
            patch.setCountyName(countyName);
            patch.setTownshipName(townshipName);
            patch.setCommunityName(level == 5 ? (communityName != null ? communityName : name) : null);
            grassrootsOrganizationMapper.updateById(patch);
            return;
        }

        GrassrootsOrganization created = new GrassrootsOrganization();
        created.setCode(code);
        created.setName(name);
        created.setLevel(level);
        created.setYear(year);
        created.setIsBaseline(0);
        created.setBaselineCode(code);
        created.setIsDeleted(0);
        created.setDataSource("IMPORT");
        created.setCountyId(countyId);
        created.setProvinceName(provinceName);
        created.setCityName(cityName);
        created.setCountyName(countyName);
        created.setTownshipName(townshipName);
        created.setCommunityName(level == 5 ? (communityName != null ? communityName : name) : null);
        grassrootsOrganizationMapper.insert(created);
    }

    private void createOrMarkGrassrootsDelete(SimpleOrgRow row, int year) {
        String code = normalizeText(row.code);
        String name = normalizeText(row.leafName);
        if (code == null) {
            return;
        }
        int level = resolveLevel(code);
        if (level != 4 && level != 5) {
            return;
        }
        GrassrootsOrganization existing = grassrootsOrganizationMapper.selectByCodeAndYearIncludeDeleted(code, year);
        if (existing != null) {
            if (existing.getIsDeleted() == null || existing.getIsDeleted() != 1) {
                grassrootsOrganizationMapper.markAsDeleted(existing.getId());
            }
            if (name != null && !Objects.equals(normalizeText(existing.getName()), name)) {
                GrassrootsOrganization patch = new GrassrootsOrganization();
                patch.setId(existing.getId());
                patch.setName(name);
                patch.setTownshipName(normalizeText(row.townshipName));
                patch.setCommunityName(normalizeText(row.communityName));
                grassrootsOrganizationMapper.updateById(patch);
            }
            return;
        }
        String countyCode = code.length() >= 6 ? code.substring(0, 6) : null;
        Long countyId = resolveBaselineCountyId(countyCode);
        GrassrootsOrganization marker = new GrassrootsOrganization();
        marker.setCode(code);
        marker.setName(name != null ? name : code);
        marker.setLevel(level);
        marker.setYear(year);
        marker.setIsBaseline(0);
        marker.setBaselineCode(code);
        marker.setIsDeleted(1);
        marker.setDataSource("IMPORT");
        marker.setCountyId(countyId);
        marker.setProvinceName(normalizeText(row.provinceName));
        marker.setCityName(normalizeText(row.cityName));
        marker.setCountyName(normalizeText(row.countyName));
        marker.setTownshipName(normalizeText(row.townshipName));
        marker.setCommunityName(normalizeText(row.communityName));
        grassrootsOrganizationMapper.insert(marker);
    }

    private Long resolveBaselineCountyId(String countyCode) {
        String normalized = normalizeText(countyCode);
        if (normalized == null || normalized.length() < 6) {
            return null;
        }
        List<Organization> baseline = organizationMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Organization>()
                        .eq("code", normalized)
                        .eq("is_baseline", 1)
                        .last("LIMIT 1")
        );
        if (baseline == null || baseline.isEmpty()) {
            return null;
        }
        return baseline.get(0).getId();
    }

    private static class SimpleOrgRow {
        final String code;
        final String rawName;
        final String provinceName;
        final String cityName;
        final String countyName;
        final String townshipName;
        final String communityName;
        final String leafName;

        private SimpleOrgRow(String code,
                             String rawName,
                             String provinceName,
                             String cityName,
                             String countyName,
                             String townshipName,
                             String communityName,
                             String leafName) {
            this.code = code;
            this.rawName = rawName;
            this.provinceName = provinceName;
            this.cityName = cityName;
            this.countyName = countyName;
            this.townshipName = townshipName;
            this.communityName = communityName;
            this.leafName = leafName;
        }
    }

    private static class DiffResult {
        final List<String> added;
        final List<String> removed;
        final List<String> changed;

        private DiffResult(List<String> added, List<String> removed, List<String> changed) {
            this.added = added;
            this.removed = removed;
            this.changed = changed;
        }
    }

    private static class ApplyStats {
        long organizationUpsert;
        long organizationDeleteMarker;
        long grassrootsUpsert;
        long grassrootsDeleteMarker;
    }

    private DiffResult diffByCode(Map<String, SimpleOrgRow> oldMap, Map<String, SimpleOrgRow> newMap) {
        Set<String> oldCodes = oldMap.keySet();
        Set<String> newCodes = newMap.keySet();

        List<String> added = new ArrayList<>();
        List<String> removed = new ArrayList<>();
        List<String> changed = new ArrayList<>();

        for (String code : newCodes) {
            if (!oldCodes.contains(code)) {
                added.add(code);
                continue;
            }
            SimpleOrgRow oldRow = oldMap.get(code);
            SimpleOrgRow newRow = newMap.get(code);
            if (oldRow == null || newRow == null) {
                continue;
            }
            String oldName = normalizeText(oldRow.rawName);
            String newName = normalizeText(newRow.rawName);
            if (!Objects.equals(oldName, newName)) {
                changed.add(code);
            }
        }

        for (String code : oldCodes) {
            if (!newCodes.contains(code)) {
                removed.add(code);
            }
        }

        Comparator<String> codeComparator = Comparator.nullsLast(String::compareTo);
        added.sort(codeComparator);
        removed.sort(codeComparator);
        changed.sort(codeComparator);
        return new DiffResult(added, removed, changed);
    }

    private Map<String, SimpleOrgRow> readSystemXls(String relativePath) throws Exception {
        Map<String, SimpleOrgRow> rows = new LinkedHashMap<>();
        try (InputStream in = Files.newInputStream(Paths.get(relativePath))) {
            Workbook workbook = WorkbookFactory.create(in);
            for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
                Sheet sheet = workbook.getSheetAt(s);
                if (sheet == null) {
                    continue;
                }
                Map<String, Integer> header = detectHeader(sheet);
                if (header == null) {
                    continue;
                }
                Integer codeIdx = header.get("code");
                Integer nameIdx = header.get("name");
                if (codeIdx == null || nameIdx == null) {
                    continue;
                }
                int startRow = header.getOrDefault("rowIndex", 0) + 1;
                if (header.getOrDefault("rowIndex", 0) < 0) {
                    startRow = 0;
                }
                for (int r = startRow; r <= sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                    if (row == null) {
                        continue;
                    }
                    String code = getCellString(row.getCell(codeIdx));
                    String rawName = getCellString(row.getCell(nameIdx));
                    code = normalizeText(code);
                    rawName = normalizeText(rawName);
                    if (code == null || rawName == null) {
                        continue;
                    }
                    int level = resolveLevel(code);
                    if (level <= 0) {
                        continue;
                    }
                    AddressParts parts = parseAddress(rawName);
                    String leafName = resolveLeafName(level, parts, rawName);
                    rows.put(code, new SimpleOrgRow(
                            code,
                            rawName,
                            parts.provinceName,
                            parts.cityName,
                            parts.countyName,
                            parts.townshipName,
                            parts.communityName,
                            leafName
                    ));
                }
            }
        }
        if (rows.isEmpty()) {
            throw new IllegalStateException("未从 XLS 解析到任何有效组织记录: " + relativePath);
        }
        return rows;
    }

    private Map<String, Integer> detectHeader(Sheet sheet) {
        if (sheet == null) {
            return null;
        }
        int maxScanRows = Math.min(sheet.getLastRowNum(), 20);
        for (int r = 0; r <= maxScanRows; r++) {
            Row row = sheet.getRow(r);
            if (row == null) {
                continue;
            }
            int maxScanCells = Math.min(row.getLastCellNum(), 50);
            Integer codeIdx = null;
            Integer nameIdx = null;
            for (int c = 0; c < maxScanCells; c++) {
                String v = normalizeText(getCellString(row.getCell(c)));
                if (v == null) {
                    continue;
                }
                if (codeIdx == null && (v.contains("行政区划代码") || v.contains("区域代码") || v.equals("code") || v.equals("编码") || v.contains("机构代码"))) {
                    codeIdx = c;
                }
                if (nameIdx == null && (v.contains("名称") || v.contains("机构名称") || v.equals("name"))) {
                    nameIdx = c;
                }
            }
            if (codeIdx != null && nameIdx != null) {
                Map<String, Integer> header = new HashMap<>();
                header.put("rowIndex", r);
                header.put("code", codeIdx);
                header.put("name", nameIdx);
                return header;
            }
        }
        return guessHeaderByData(sheet);
    }

    private Map<String, Integer> guessHeaderByData(Sheet sheet) {
        int maxRows = Math.min(sheet.getLastRowNum(), 200);
        int maxCols = 0;
        for (int r = 0; r <= Math.min(maxRows, 10); r++) {
            Row row = sheet.getRow(r);
            if (row != null) {
                maxCols = Math.max(maxCols, row.getLastCellNum());
            }
        }
        maxCols = Math.min(maxCols, 80);
        if (maxCols <= 0) {
            return null;
        }

        Map<Integer, Integer> codeHits = new HashMap<>();
        for (int r = 0; r <= maxRows; r++) {
            Row row = sheet.getRow(r);
            if (row == null) {
                continue;
            }
            for (int c = 0; c < maxCols; c++) {
                String v = normalizeText(getCellString(row.getCell(c)));
                if (isLikelyRegionCode(v)) {
                    codeHits.put(c, codeHits.getOrDefault(c, 0) + 1);
                }
            }
        }

        int bestCodeIdx = -1;
        int bestCodeScore = 0;
        for (Map.Entry<Integer, Integer> e : codeHits.entrySet()) {
            if (e.getValue() > bestCodeScore) {
                bestCodeScore = e.getValue();
                bestCodeIdx = e.getKey();
            }
        }
        if (bestCodeIdx < 0 || bestCodeScore < 5) {
            return null;
        }

        Map<Integer, Integer> nameHits = new HashMap<>();
        for (int r = 0; r <= maxRows; r++) {
            Row row = sheet.getRow(r);
            if (row == null) {
                continue;
            }
            String code = normalizeText(getCellString(row.getCell(bestCodeIdx)));
            if (!isLikelyRegionCode(code)) {
                continue;
            }
            for (int c = 0; c < maxCols; c++) {
                if (c == bestCodeIdx) {
                    continue;
                }
                String v = normalizeText(getCellString(row.getCell(c)));
                if (isLikelyName(v)) {
                    nameHits.put(c, nameHits.getOrDefault(c, 0) + 1);
                }
            }
        }

        int bestNameIdx = -1;
        int bestNameScore = 0;
        for (Map.Entry<Integer, Integer> e : nameHits.entrySet()) {
            if (e.getValue() > bestNameScore) {
                bestNameScore = e.getValue();
                bestNameIdx = e.getKey();
            }
        }
        if (bestNameIdx < 0 || bestNameScore < 5) {
            return null;
        }

        Map<String, Integer> header = new HashMap<>();
        header.put("rowIndex", -1);
        header.put("code", bestCodeIdx);
        header.put("name", bestNameIdx);
        return header;
    }

    private boolean isLikelyRegionCode(String value) {
        String v = normalizeText(value);
        if (v == null) {
            return false;
        }
        if (!v.matches("^\\d{2,12}$")) {
            return false;
        }
        int len = v.length();
        return len == 2 || len == 4 || len == 6 || len == 9 || len == 12;
    }

    private boolean isLikelyName(String value) {
        String v = normalizeText(value);
        if (v == null) {
            return false;
        }
        if (v.matches("^\\d+$")) {
            return false;
        }
        if (v.length() < 2 || v.length() > 60) {
            return false;
        }
        return true;
    }

    private static String getCellString(Cell cell) {
        if (cell == null) {
            return null;
        }
        try {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return cell.getDateCellValue().toString();
                    }
                    double d = cell.getNumericCellValue();
                    long l = (long) d;
                    if (Math.abs(d - l) < 1e-9) {
                        return String.valueOf(l);
                    }
                    return String.valueOf(d);
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                case FORMULA:
                    try {
                        return cell.getStringCellValue();
                    } catch (Exception ignore) {
                        try {
                            return String.valueOf(cell.getNumericCellValue());
                        } catch (Exception ignore2) {
                            return null;
                        }
                    }
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private static String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String v = value.trim();
        return v.isEmpty() ? null : v;
    }

    private int resolveLevel(String code) {
        String c = normalizeText(code);
        if (c == null) {
            return 0;
        }
        int len = c.length();
        if (len == 2) return 1;
        if (len == 4) return 2;
        if (len == 6) return 3;
        if (len == 9) return 4;
        if (len == 12) return 5;
        return 0;
    }

    private static class AddressParts {
        final String provinceName;
        final String cityName;
        final String countyName;
        final String townshipName;
        final String communityName;

        private AddressParts(String provinceName, String cityName, String countyName, String townshipName, String communityName) {
            this.provinceName = provinceName;
            this.cityName = cityName;
            this.countyName = countyName;
            this.townshipName = townshipName;
            this.communityName = communityName;
        }
    }

    private AddressParts parseAddress(String address) {
        if (!StringUtils.hasText(address)) {
            return new AddressParts(null, null, null, null, null);
        }
        String remaining = address.trim();
        String provinceName = null;
        String cityName = null;
        String countyName = null;
        String townshipName = null;
        String communityName = null;

        int provIdx = remaining.indexOf("省");
        if (provIdx >= 0) {
            provinceName = remaining.substring(0, provIdx + 1);
            remaining = remaining.substring(provIdx + 1);
        }

        int cityIdx = remaining.indexOf("市");
        if (cityIdx >= 0) {
            cityName = remaining.substring(0, cityIdx + 1);
            remaining = remaining.substring(cityIdx + 1);
        }

        int districtIdx = remaining.indexOf("区");
        int countyIdx = remaining.indexOf("县");
        if (districtIdx >= 0 && (countyIdx < 0 || districtIdx < countyIdx)) {
            countyName = remaining.substring(0, districtIdx + 1);
            remaining = remaining.substring(districtIdx + 1);
        } else if (countyIdx >= 0) {
            countyName = remaining.substring(0, countyIdx + 1);
            remaining = remaining.substring(countyIdx + 1);
        }

        int streetIdx = remaining.indexOf("街道");
        int townIdx = remaining.indexOf("镇");
        if (streetIdx >= 0 && (townIdx < 0 || streetIdx < townIdx)) {
            townshipName = remaining.substring(0, streetIdx + 2);
            remaining = remaining.substring(streetIdx + 2);
        } else if (townIdx >= 0) {
            townshipName = remaining.substring(0, townIdx + 1);
            remaining = remaining.substring(townIdx + 1);
        }

        int commIdx = remaining.indexOf("社区");
        int villIdx = remaining.indexOf("村");
        if (commIdx >= 0 && (villIdx < 0 || commIdx < villIdx)) {
            communityName = remaining.substring(0, commIdx + 2);
        } else if (villIdx >= 0) {
            communityName = remaining.substring(0, villIdx + 1);
        }

        return new AddressParts(
                normalizeText(provinceName),
                normalizeText(cityName),
                normalizeText(countyName),
                normalizeText(townshipName),
                normalizeText(communityName)
        );
    }

    private String resolveLeafName(int level, AddressParts parts, String fallback) {
        if (parts == null) {
            return normalizeText(fallback);
        }
        if (level == 1) return normalizeText(parts.provinceName != null ? parts.provinceName : fallback);
        if (level == 2) return normalizeText(parts.cityName != null ? parts.cityName : fallback);
        if (level == 3) return normalizeText(parts.countyName != null ? parts.countyName : fallback);
        if (level == 4) return normalizeText(parts.townshipName != null ? parts.townshipName : fallback);
        if (level == 5) return normalizeText(parts.communityName != null ? parts.communityName : fallback);
        return normalizeText(fallback);
    }
}
