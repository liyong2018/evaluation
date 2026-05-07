package com.evaluate.service;

import com.evaluate.util.GeojsonImportUtil;
import com.evaluate.util.GeojsonImportUtil.ImportResult;
import com.evaluate.util.OrganizationSqlUtil;
import com.evaluate.util.OrganizationSqlUtil.SqlScriptResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
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
    public void testApply2025OrganizationsFromSystemXlsAsAuthority() throws Exception {
        String xls2025 = "docs/2025年组织机构 - 系统.xls";
        int targetYear = 2025;

        Map<String, SimpleOrgRow> communityRows = readSystemXls(xls2025);
        List<SimpleOrgRow> validCommunities = communityRows.values().stream()
                .filter(Objects::nonNull)
                .filter(r -> StringUtils.hasText(r.code) && r.code.length() == 12)
                .collect(Collectors.toList());

        if (validCommunities.isEmpty()) {
            throw new IllegalStateException("2025 系统 XLS 未解析到 12 位社区/行政村编码");
        }

        long invalidNameCount = validCommunities.stream()
                .filter(r -> isUnavailableName(r.rawName))
                .count();

        log.info("===== 2025 系统组织机构表统计 =====");
        log.info("社区/行政村记录: {}, 名称不可用(#N/A等): {}", validCommunities.size(), invalidNameCount);
        log.info("乡镇前缀数: {}", validCommunities.stream().map(r -> r.code.substring(0, 9)).distinct().count());

        String suffix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String orgBak = "organization_bak_2025_system_" + suffix;
        String grassBak = "grassroots_organization_bak_2025_system_" + suffix;
        backupTable("organization", orgBak);
        backupTable("grassroots_organization", grassBak);
        log.info("已备份: organization -> {}, grassroots_organization -> {}", orgBak, grassBak);

        jdbcTemplate.execute("DROP TABLE IF EXISTS org_2025_system_xls_name_issue");
        jdbcTemplate.execute("CREATE TABLE org_2025_system_xls_name_issue (" +
                "code VARCHAR(32) NOT NULL PRIMARY KEY, " +
                "raw_name VARCHAR(512), " +
                "issue_type VARCHAR(64) NOT NULL, " +
                "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                ")");

        ApplyStats stats = new ApplyStats();

        Map<String, SimpleOrgRow> townshipRows = deriveTownshipRows(validCommunities);
        for (SimpleOrgRow township : townshipRows.values()) {
            ensureAdminHierarchyForRow(township, targetYear);
            upsertGrassrootsYearRow(township, targetYear);
            stats.grassrootsUpsert++;
        }

        for (SimpleOrgRow community : validCommunities) {
            ensureAdminHierarchyForRow(community, targetYear);
            SimpleOrgRow rowToApply = community;
            if (isUnavailableName(community.rawName)) {
                jdbcTemplate.update("INSERT INTO org_2025_system_xls_name_issue (code, raw_name, issue_type) VALUES (?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE raw_name = VALUES(raw_name), issue_type = VALUES(issue_type)",
                        community.code, community.rawName, "UNAVAILABLE_NAME");
                rowToApply = community.withFallbackLeafName(community.code);
            }
            upsertGrassrootsYearRow(rowToApply, targetYear);
            stats.grassrootsUpsert++;
        }

        markGrassrootsRowsMissingFromAuthority(targetYear, townshipRows.keySet(), validCommunities);
        repairGrassrootsParentLinks(targetYear);

        Long yearCommunityCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM grassroots_organization WHERE year = ? AND level = 5 AND is_deleted = 0",
                Long.class, targetYear);
        Long yearTownshipCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM grassroots_organization WHERE year = ? AND level = 4 AND is_deleted = 0",
                Long.class, targetYear);
        Long issueCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM org_2025_system_xls_name_issue",
                Long.class);

        log.info("===== 2025 系统组织机构表写入完成 =====");
        log.info("乡镇 upsert + 社区 upsert 总数: {}", stats.grassrootsUpsert);
        log.info("2025 有效乡镇: {}, 有效社区/行政村: {}, 名称问题: {}",
                yearTownshipCount, yearCommunityCount, issueCount);
    }

    @Test
    @Transactional(rollbackFor = Exception.class)
    @Rollback(false)
    public void testPrune2025SystemOrganizationsToIncrementalDelta() throws Exception {
        Map<String, SimpleOrgRow> rows2024 = readSystemXls("docs/2024年组织机构 - 系统.xls");
        Map<String, SimpleOrgRow> rows2025 = readSystemXls("docs/2025年组织机构 - 系统.xls");

        List<SimpleOrgRow> communities2024 = rows2024.values().stream()
                .filter(Objects::nonNull)
                .filter(r -> StringUtils.hasText(r.code) && r.code.length() == 12)
                .collect(Collectors.toList());
        List<SimpleOrgRow> communities2025 = rows2025.values().stream()
                .filter(Objects::nonNull)
                .filter(r -> StringUtils.hasText(r.code) && r.code.length() == 12)
                .collect(Collectors.toList());

        Map<String, SimpleOrgRow> effective2024 = new LinkedHashMap<>();
        effective2024.putAll(collectAdminRows(communities2024));
        effective2024.putAll(deriveTownshipRows(communities2024));
        for (SimpleOrgRow row : communities2024) {
            effective2024.put(row.code, row);
        }

        Map<String, SimpleOrgRow> effective2025 = new LinkedHashMap<>();
        effective2025.putAll(collectAdminRows(communities2025));
        effective2025.putAll(deriveTownshipRows(communities2025));
        for (SimpleOrgRow row : communities2025) {
            effective2025.put(row.code, row);
        }

        String suffix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String orgBak = "organization_bak_2025_prune_" + suffix;
        String grassBak = "grassroots_organization_bak_2025_prune_" + suffix;
        backupTable("organization", orgBak);
        backupTable("grassroots_organization", grassBak);
        log.info("已备份: organization -> {}, grassroots_organization -> {}", orgBak, grassBak);

        int redundantOrgRows = 0;
        int redundantGrassRows = 0;
        for (SimpleOrgRow row2025 : effective2025.values()) {
            if (row2025 == null || !StringUtils.hasText(row2025.code)) {
                continue;
            }
            SimpleOrgRow row2024 = effective2024.get(row2025.code);
            if (!isSameOrganizationRow(row2024, row2025)) {
                continue;
            }
            int level = resolveLevel(row2025.code);
            if (level <= 3) {
                redundantOrgRows += jdbcTemplate.update(
                        "DELETE FROM organization WHERE year = ? AND is_baseline = 0 AND code = ? AND is_deleted = 0",
                        2025, row2025.code);
            } else if (level == 4 || level == 5) {
                redundantGrassRows += jdbcTemplate.update(
                        "DELETE FROM grassroots_organization WHERE year = ? AND is_baseline = 0 AND code = ? AND is_deleted = 0",
                        2025, row2025.code);
            }
        }

        int org2025Active = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM organization WHERE year = 2025 AND is_baseline = 0 AND is_deleted = 0",
                Integer.class);
        int grassTownship2025Active = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM grassroots_organization WHERE year = 2025 AND is_baseline = 0 AND is_deleted = 0 AND level = 4",
                Integer.class);
        int grassCommunity2025Active = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM grassroots_organization WHERE year = 2025 AND is_baseline = 0 AND is_deleted = 0 AND level = 5",
                Integer.class);

        log.info("===== 2025 全量组织机构压缩为增量完成 =====");
        log.info("移除 2025 冗余省市县记录: {}", redundantOrgRows);
        log.info("移除 2025 冗余乡镇/社区记录: {}", redundantGrassRows);
        log.info("剩余 2025 有效省市县记录: {}", org2025Active);
        log.info("剩余 2025 有效乡镇: {}, 社区/行政村: {}", grassTownship2025Active, grassCommunity2025Active);
    }

    @Test
    @Transactional(rollbackFor = Exception.class)
    @Rollback(false)
    public void testPrune2024OrganizationsToIncrementalDelta() {
        String suffix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String orgBak = "organization_bak_2024_prune_" + suffix;
        String grassBak = "grassroots_organization_bak_2024_prune_" + suffix;
        backupTable("organization", orgBak);
        backupTable("grassroots_organization", grassBak);
        log.info("已备份: organization -> {}, grassroots_organization -> {}", orgBak, grassBak);

        Map<String, SimpleOrgRow> baselineAdmin = new LinkedHashMap<>();
        for (Organization org : organizationMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Organization>()
                        .eq("is_baseline", 1)
                        .eq("is_deleted", 0)
        )) {
            SimpleOrgRow row = fromOrganization(org);
            if (row != null) {
                baselineAdmin.put(row.code, row);
            }
        }

        Map<String, SimpleOrgRow> baselineGrassroots = new LinkedHashMap<>();
        for (GrassrootsOrganization org : grassrootsOrganizationMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<GrassrootsOrganization>()
                        .eq("is_baseline", 1)
                        .eq("is_deleted", 0)
        )) {
            SimpleOrgRow row = fromGrassrootsOrganization(org);
            if (row != null) {
                baselineGrassroots.put(row.code, row);
            }
        }

        int redundantOrgRows = 0;
        List<Organization> yearAdmins = organizationMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Organization>()
                        .eq("year", 2024)
                        .eq("is_baseline", 0)
                        .eq("is_deleted", 0)
        );
        for (Organization org : yearAdmins) {
            SimpleOrgRow row = fromOrganization(org);
            if (row != null && isSameOrganizationRow(baselineAdmin.get(row.code), row)) {
                redundantOrgRows += jdbcTemplate.update(
                        "DELETE FROM organization WHERE id = ?",
                        org.getId());
            }
        }

        int redundantGrassRows = 0;
        List<GrassrootsOrganization> yearGrassroots = grassrootsOrganizationMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<GrassrootsOrganization>()
                        .eq("year", 2024)
                        .eq("is_baseline", 0)
                        .eq("is_deleted", 0)
        );
        for (GrassrootsOrganization org : yearGrassroots) {
            SimpleOrgRow row = fromGrassrootsOrganization(org);
            if (row != null && isSameOrganizationRow(baselineGrassroots.get(row.code), row)) {
                redundantGrassRows += jdbcTemplate.update(
                        "DELETE FROM grassroots_organization WHERE id = ?",
                        org.getId());
            }
        }

        int org2024Active = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM organization WHERE year = 2024 AND is_baseline = 0 AND is_deleted = 0",
                Integer.class);
        int grassTownship2024Active = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM grassroots_organization WHERE year = 2024 AND is_baseline = 0 AND is_deleted = 0 AND level = 4",
                Integer.class);
        int grassCommunity2024Active = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM grassroots_organization WHERE year = 2024 AND is_baseline = 0 AND is_deleted = 0 AND level = 5",
                Integer.class);

        log.info("===== 2024 全量组织机构压缩为增量完成 =====");
        log.info("移除 2024 冗余省市县记录: {}", redundantOrgRows);
        log.info("移除 2024 冗余乡镇/社区记录: {}", redundantGrassRows);
        log.info("剩余 2024 有效省市县记录: {}", org2024Active);
        log.info("剩余 2024 有效乡镇: {}, 社区/行政村: {}", grassTownship2024Active, grassCommunity2024Active);
    }

    @Test
    @Disabled("错误的临时修复：510173715 不是 2025 系统组织机构表中的有效东部新区代码，保留禁用以避免误执行")
    @Transactional(rollbackFor = Exception.class)
    @Rollback(false)
    public void testRepairEastNewAreaLujiaNamesFromBaseline() {
        String suffix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String grassBak = "grassroots_lujia_repair_bak_" + suffix;
        backupTable("grassroots_organization", grassBak);
        log.info("已备份: grassroots_organization -> {}", grassBak);

        int townshipUpdated = jdbcTemplate.update("UPDATE grassroots_organization " +
                        "SET name = ?, township_name = ?, province_name = ?, city_name = ?, county_name = ?, update_time = NOW() " +
                        "WHERE year = 2025 AND is_deleted = 0 AND code = ?",
                "芦葭镇", "芦葭镇", "四川省", "成都市", "成都东部新区", "510173715");

        int communityUpdated = jdbcTemplate.update("UPDATE grassroots_organization target " +
                "JOIN grassroots_organization source " +
                "  ON source.code = CONCAT('510185121', RIGHT(target.code, 3)) " +
                " AND source.level = 5 " +
                " AND source.is_baseline = 1 " +
                " AND source.is_deleted = 0 " +
                "SET target.name = source.name, " +
                "    target.township_name = '芦葭镇', " +
                "    target.community_name = source.community_name, " +
                "    target.province_name = '四川省', " +
                "    target.city_name = '成都市', " +
                "    target.county_name = '成都东部新区', " +
                "    target.update_time = NOW() " +
                "WHERE target.year = 2025 " +
                "  AND target.is_deleted = 0 " +
                "  AND target.level = 5 " +
                "  AND target.code LIKE '510173715%'");

        log.info("===== 成都东部新区芦葭镇名称修复完成 =====");
        log.info("乡镇更新: {}, 村社更新: {}", townshipUpdated, communityUpdated);
    }

    @Test
    @Transactional(rollbackFor = Exception.class)
    @Rollback(false)
    public void testRepairEastNewArea2025GrassrootsFromSystemXls() throws Exception {
        String suffix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String grassBak = "grassroots_east_2025_bak_" + suffix;
        backupTable("grassroots_organization", grassBak);
        log.info("已备份: grassroots_organization -> {}", grassBak);

        Map<String, SimpleOrgRow> rows2025 = readSystemXls("docs/2025年组织机构 - 系统.xls");
        List<SimpleOrgRow> communities = rows2025.values().stream()
                .filter(Objects::nonNull)
                .filter(row -> StringUtils.hasText(row.code) && row.code.length() == 12)
                .filter(row -> row.code.startsWith("510173"))
                .filter(row -> !isUnavailableName(row.rawName))
                .collect(Collectors.toList());
        if (communities.isEmpty()) {
            throw new IllegalStateException("2025 系统组织机构表中未解析到成都东部新区有效村社记录");
        }

        Map<String, SimpleOrgRow> townshipRows = deriveTownshipRows(communities);
        for (SimpleOrgRow township : townshipRows.values()) {
            upsertGrassrootsYearRow(township, 2025);
        }
        for (SimpleOrgRow community : communities) {
            upsertGrassrootsYearRow(community, 2025);
        }

        Set<String> validCodes = new HashSet<>(townshipRows.keySet());
        communities.stream()
                .map(row -> normalizeText(row.code))
                .filter(Objects::nonNull)
                .forEach(validCodes::add);
        jdbcTemplate.update("UPDATE grassroots_organization " +
                        "SET is_deleted = 1, update_time = NOW() " +
                        "WHERE year = 2025 " +
                        "  AND is_deleted = 0 " +
                        "  AND level IN (4, 5) " +
                        "  AND code LIKE '510173%' " +
                        "  AND code NOT IN (" + validCodes.stream().map(code -> "?").collect(Collectors.joining(",")) + ")",
                validCodes.toArray());
        repairGrassrootsParentLinks(2025);

        Long activeTownshipCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM grassroots_organization WHERE year = 2025 AND is_deleted = 0 AND level = 4 AND code LIKE '510173%'",
                Long.class);
        Long activeCommunityCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM grassroots_organization WHERE year = 2025 AND is_deleted = 0 AND level = 5 AND code LIKE '510173%'",
                Long.class);
        Long unavailableActiveCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM grassroots_organization WHERE year = 2025 AND is_deleted = 0 AND code LIKE '510173%' AND (name = code OR community_name = '#N/A')",
                Long.class);
        log.info("===== 成都东部新区 2025 基层组织按系统表重建完成 =====");
        log.info("系统表有效乡镇: {}, 有效村社: {}", townshipRows.size(), communities.size());
        log.info("库中有效乡镇: {}, 有效村社: {}, 编码/#N/A残留: {}",
                activeTownshipCount, activeCommunityCount, unavailableActiveCount);
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

    @Test
    @Transactional(rollbackFor = Exception.class)
    @Rollback(false)
    public void testStage2020SystemXlsAndGenerateRepairCandidates() throws Exception {
        String xls2020 = "/Users/lql/Documents/projects/减灾中心/2020年组织机构 - 系统(1).xls";
        Map<String, SimpleOrgRow> rows2020 = readSystemXls(xls2020);
        List<SimpleOrgRow> communities = rows2020.values().stream()
                .filter(Objects::nonNull)
                .filter(row -> StringUtils.hasText(row.code) && row.code.length() == 12)
                .filter(row -> !isUnavailableName(row.rawName))
                .collect(Collectors.toList());
        if (communities.isEmpty()) {
            throw new IllegalStateException("2020 系统 XLS 未解析到有效村社记录: " + xls2020);
        }

        jdbcTemplate.execute("DROP TABLE IF EXISTS org_2020_system_xls_staging");
        jdbcTemplate.execute("CREATE TABLE org_2020_system_xls_staging (" +
                "code VARCHAR(32) NOT NULL PRIMARY KEY, " +
                "full_name VARCHAR(512) NOT NULL, " +
                "province_code VARCHAR(32), " +
                "city_code VARCHAR(32), " +
                "county_code VARCHAR(32), " +
                "township_code VARCHAR(32), " +
                "province_name VARCHAR(128), " +
                "city_name VARCHAR(128), " +
                "county_name VARCHAR(128), " +
                "township_name VARCHAR(128), " +
                "community_name VARCHAR(128), " +
                "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "KEY idx_county_code (county_code), " +
                "KEY idx_township_code (township_code)" +
                ")");

        List<Object[]> stageRows = new ArrayList<>();
        for (SimpleOrgRow row : communities) {
            stageRows.add(new Object[]{
                    row.code,
                    row.rawName,
                    row.code.substring(0, 2),
                    row.code.substring(0, 4),
                    row.code.substring(0, 6),
                    row.code.substring(0, 9),
                    row.provinceName,
                    row.cityName,
                    row.countyName,
                    row.townshipName,
                    row.communityName
            });
        }
        jdbcTemplate.batchUpdate("INSERT INTO org_2020_system_xls_staging (" +
                        "code, full_name, province_code, city_code, county_code, township_code, " +
                        "province_name, city_name, county_name, township_name, community_name" +
                        ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                stageRows);

        create2020RepairCandidateTable();
        generate2020RepairCandidates();

        Long stageCommunityCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM org_2020_system_xls_staging",
                Long.class);
        Long stageTownshipCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT township_code) FROM org_2020_system_xls_staging",
                Long.class);
        List<Map<String, Object>> summary = jdbcTemplate.queryForList(
                "SELECT issue_type, COUNT(1) AS cnt FROM org_2020_repair_candidate GROUP BY issue_type ORDER BY cnt DESC, issue_type");

        log.info("===== 2020 系统 XLS 暂存与修复候选生成完成 =====");
        log.info("系统表村社: {}, 派生乡镇: {}", stageCommunityCount, stageTownshipCount);
        for (Map<String, Object> row : summary) {
            log.info("{}: {}", row.get("issue_type"), row.get("cnt"));
        }
    }

    @Test
    @Transactional(rollbackFor = Exception.class)
    @Rollback(false)
    public void testApplySafeMissing2020GrassrootsFromSystemXls() throws Exception {
        testStage2020SystemXlsAndGenerateRepairCandidates();

        String suffix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String grassBak = "grassroots_2020_system_xls_safe_bak_" + suffix;
        backupTable("grassroots_organization", grassBak);
        log.info("已备份: grassroots_organization -> {}", grassBak);

        int townshipInserted = jdbcTemplate.update("INSERT INTO grassroots_organization (" +
                        "county_id, parent_id, code, name, level, year, data_source, " +
                        "province_name, city_name, county_name, township_name, community_name, " +
                        "create_time, update_time, is_deleted, is_baseline, baseline_code" +
                        ") " +
                        "SELECT county.id, county.id, x.township_code, MIN(x.township_name), 4, 2020, '2020_SYSTEM_XLS_REPAIR', " +
                        "       MIN(x.province_name), MIN(x.city_name), county.name, MIN(x.township_name), NULL, " +
                        "       NOW(), NOW(), 0, 1, x.township_code " +
                        "FROM org_2020_system_xls_staging x " +
                        "JOIN organization county " +
                        "  ON county.code = x.county_code AND county.year = 2020 AND county.level = 3 " +
                        " AND county.is_baseline = 1 AND county.is_deleted = 0 " +
                        "LEFT JOIN grassroots_organization existing " +
                        "  ON existing.year = 2020 AND existing.is_baseline = 1 AND existing.is_deleted = 0 AND existing.level = 4 " +
                        " AND (existing.code = x.township_code OR existing.baseline_code = x.township_code) " +
                        "WHERE existing.id IS NULL " +
                        "  AND x.township_name IS NOT NULL " +
                        "  AND CHAR_LENGTH(x.township_name) BETWEEN 2 AND 64 " +
                        "GROUP BY county.id, county.name, x.township_code " +
                        "ON DUPLICATE KEY UPDATE " +
                        "  county_id = VALUES(county_id), parent_id = VALUES(parent_id), name = VALUES(name), " +
                        "  data_source = VALUES(data_source), province_name = VALUES(province_name), city_name = VALUES(city_name), " +
                        "  county_name = VALUES(county_name), township_name = VALUES(township_name), " +
                        "  is_deleted = 0, is_baseline = 1, baseline_code = VALUES(baseline_code), update_time = NOW()");

        int communityInserted = jdbcTemplate.update("INSERT INTO grassroots_organization (" +
                        "county_id, parent_id, code, name, level, year, data_source, " +
                        "province_name, city_name, county_name, township_name, community_name, " +
                        "create_time, update_time, is_deleted, is_baseline, baseline_code" +
                        ") " +
                        "SELECT county.id, township.id, x.code, x.community_name, 5, 2020, '2020_SYSTEM_XLS_REPAIR', " +
                        "       x.province_name, x.city_name, county.name, township.name, x.community_name, " +
                        "       NOW(), NOW(), 0, 1, x.code " +
                        "FROM org_2020_system_xls_staging x " +
                        "JOIN organization county " +
                        "  ON county.code = x.county_code AND county.year = 2020 AND county.level = 3 " +
                        " AND county.is_baseline = 1 AND county.is_deleted = 0 " +
                        "JOIN grassroots_organization township " +
                        "  ON township.year = 2020 AND township.is_baseline = 1 AND township.is_deleted = 0 AND township.level = 4 " +
                        " AND (township.code = x.township_code OR township.baseline_code = x.township_code) " +
                        "LEFT JOIN grassroots_organization existing " +
                        "  ON existing.year = 2020 AND existing.is_baseline = 1 AND existing.is_deleted = 0 AND existing.level = 5 " +
                        " AND (existing.code = x.code OR existing.baseline_code = x.code) " +
                        "WHERE existing.id IS NULL " +
                        "  AND x.community_name IS NOT NULL " +
                        "  AND CHAR_LENGTH(x.community_name) BETWEEN 2 AND 64 " +
                        "  AND x.community_name NOT IN ('居民委员会', '村民委员会', '社区', '村', '居委会', '村委会') " +
                        "ON DUPLICATE KEY UPDATE " +
                        "  county_id = VALUES(county_id), parent_id = VALUES(parent_id), name = VALUES(name), " +
                        "  data_source = VALUES(data_source), province_name = VALUES(province_name), city_name = VALUES(city_name), " +
                        "  county_name = VALUES(county_name), township_name = VALUES(township_name), community_name = VALUES(community_name), " +
                        "  is_deleted = 0, is_baseline = 1, baseline_code = VALUES(baseline_code), update_time = NOW()");

        repairGrassrootsParentLinks(2020);
        create2020RepairCandidateTable();
        generate2020RepairCandidates();

        List<Map<String, Object>> summary = jdbcTemplate.queryForList(
                "SELECT issue_type, COUNT(1) AS cnt FROM org_2020_repair_candidate GROUP BY issue_type ORDER BY cnt DESC, issue_type");
        log.info("===== 2020 系统 XLS 安全补齐完成 =====");
        log.info("补齐乡镇: {}, 补齐村社: {}", townshipInserted, communityInserted);
        for (Map<String, Object> row : summary) {
            log.info("剩余 {}: {}", row.get("issue_type"), row.get("cnt"));
        }
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

    private void ensureAdminHierarchyForRow(SimpleOrgRow row, int year) {
        if (row == null || !StringUtils.hasText(row.code) || row.code.length() < 6) {
            return;
        }
        String code = row.code;
        if (StringUtils.hasText(row.provinceName) && code.length() >= 2) {
            upsertOrganizationYearRow(new SimpleOrgRow(
                    code.substring(0, 2),
                    row.provinceName,
                    row.provinceName,
                    null,
                    null,
                    null,
                    null,
                    row.provinceName
            ), year);
        }
        if (StringUtils.hasText(row.cityName) && code.length() >= 4) {
            upsertOrganizationYearRow(new SimpleOrgRow(
                    code.substring(0, 4),
                    row.cityName,
                    row.provinceName,
                    row.cityName,
                    null,
                    null,
                    null,
                    row.cityName
            ), year);
        }
        if (StringUtils.hasText(row.countyName) && code.length() >= 6) {
            upsertOrganizationYearRow(new SimpleOrgRow(
                    code.substring(0, 6),
                    row.countyName,
                    row.provinceName,
                    row.cityName,
                    row.countyName,
                    null,
                    null,
                    row.countyName
            ), year);
        }
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
        Long countyId = resolveCountyId(countyCode, year);

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
        Long countyId = resolveCountyId(countyCode, year);
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

    private Long resolveCountyId(String countyCode, int year) {
        Long baselineId = resolveBaselineCountyId(countyCode);
        if (baselineId != null) {
            return baselineId;
        }
        String normalized = normalizeText(countyCode);
        if (normalized == null || normalized.length() < 6) {
            return null;
        }
        List<Organization> yearRows = organizationMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Organization>()
                        .eq("code", normalized)
                        .eq("year", year)
                        .eq("is_deleted", 0)
                        .last("LIMIT 1")
        );
        if (yearRows == null || yearRows.isEmpty()) {
            return null;
        }
        return yearRows.get(0).getId();
    }

    private Map<String, SimpleOrgRow> deriveTownshipRows(List<SimpleOrgRow> communities) {
        Map<String, SimpleOrgRow> townshipRows = new LinkedHashMap<>();
        for (SimpleOrgRow community : communities) {
            if (community == null || !StringUtils.hasText(community.code) || community.code.length() < 9) {
                continue;
            }
            String townshipCode = community.code.substring(0, 9);
            String townshipName = normalizeText(community.townshipName);
            if (!StringUtils.hasText(townshipName)) {
                townshipName = townshipCode;
            }
            townshipRows.putIfAbsent(townshipCode, new SimpleOrgRow(
                    townshipCode,
                    townshipName,
                    community.provinceName,
                    community.cityName,
                    community.countyName,
                    townshipName,
                    null,
                    townshipName
            ));
        }
        return townshipRows;
    }

    private Map<String, SimpleOrgRow> collectAdminRows(List<SimpleOrgRow> communities) {
        Map<String, SimpleOrgRow> adminRows = new LinkedHashMap<>();
        for (SimpleOrgRow row : communities) {
            if (row == null || !StringUtils.hasText(row.code) || row.code.length() < 6) {
                continue;
            }
            if (StringUtils.hasText(row.provinceName)) {
                adminRows.putIfAbsent(row.code.substring(0, 2), new SimpleOrgRow(
                        row.code.substring(0, 2),
                        row.provinceName,
                        row.provinceName,
                        null,
                        null,
                        null,
                        null,
                        row.provinceName
                ));
            }
            if (StringUtils.hasText(row.cityName)) {
                adminRows.putIfAbsent(row.code.substring(0, 4), new SimpleOrgRow(
                        row.code.substring(0, 4),
                        row.cityName,
                        row.provinceName,
                        row.cityName,
                        null,
                        null,
                        null,
                        row.cityName
                ));
            }
            if (StringUtils.hasText(row.countyName)) {
                adminRows.putIfAbsent(row.code.substring(0, 6), new SimpleOrgRow(
                        row.code.substring(0, 6),
                        row.countyName,
                        row.provinceName,
                        row.cityName,
                        row.countyName,
                        null,
                        null,
                        row.countyName
                ));
            }
        }
        return adminRows;
    }

    private boolean isSameOrganizationRow(SimpleOrgRow left, SimpleOrgRow right) {
        if (left == null || right == null) {
            return false;
        }
        return Objects.equals(normalizeText(left.code), normalizeText(right.code))
                && Objects.equals(normalizeText(left.leafName), normalizeText(right.leafName))
                && Objects.equals(normalizeText(left.provinceName), normalizeText(right.provinceName))
                && Objects.equals(normalizeText(left.cityName), normalizeText(right.cityName))
                && Objects.equals(normalizeText(left.countyName), normalizeText(right.countyName))
                && Objects.equals(normalizeText(left.townshipName), normalizeText(right.townshipName))
                && Objects.equals(normalizeText(left.communityName), normalizeText(right.communityName));
    }

    private SimpleOrgRow fromOrganization(Organization org) {
        if (org == null || !StringUtils.hasText(org.getCode())) {
            return null;
        }
        String code = normalizeText(org.getCode());
        String name = normalizeText(org.getName());
        return new SimpleOrgRow(
                code,
                name,
                org.getProvinceName(),
                org.getCityName(),
                org.getCountyName(),
                null,
                null,
                name
        );
    }

    private SimpleOrgRow fromGrassrootsOrganization(GrassrootsOrganization org) {
        if (org == null || !StringUtils.hasText(org.getCode())) {
            return null;
        }
        String code = normalizeText(org.getCode());
        String name = normalizeText(org.getName());
        return new SimpleOrgRow(
                code,
                name,
                org.getProvinceName(),
                org.getCityName(),
                org.getCountyName(),
                org.getTownshipName(),
                org.getCommunityName(),
                name
        );
    }

    private void repairGrassrootsParentLinks(int year) {
        jdbcTemplate.update("UPDATE grassroots_organization c " +
                        "JOIN grassroots_organization t ON t.code = LEFT(c.code, 9) AND t.year = c.year AND t.level = 4 AND t.is_deleted = 0 " +
                        "SET c.parent_id = t.id, c.county_id = t.county_id " +
                        "WHERE c.year = ? AND c.level = 5 AND c.is_deleted = 0",
                year);
    }

    private void markGrassrootsRowsMissingFromAuthority(int year,
                                                        Collection<String> townshipCodes,
                                                        Collection<SimpleOrgRow> communities) {
        jdbcTemplate.execute("DROP TEMPORARY TABLE IF EXISTS tmp_org_authority_codes");
        jdbcTemplate.execute("CREATE TEMPORARY TABLE tmp_org_authority_codes (" +
                "code VARCHAR(32) NOT NULL PRIMARY KEY" +
                ")");

        List<Object[]> batch = new ArrayList<>();
        for (String code : townshipCodes) {
            String normalized = normalizeText(code);
            if (normalized != null) {
                batch.add(new Object[]{normalized});
            }
        }
        for (SimpleOrgRow community : communities) {
            if (community == null) {
                continue;
            }
            String normalized = normalizeText(community.code);
            if (normalized != null) {
                batch.add(new Object[]{normalized});
            }
        }
        jdbcTemplate.batchUpdate("INSERT IGNORE INTO tmp_org_authority_codes (code) VALUES (?)", batch);

        int marked = jdbcTemplate.update("UPDATE grassroots_organization g " +
                        "LEFT JOIN tmp_org_authority_codes e ON e.code = g.code " +
                        "SET g.is_deleted = 1, g.update_time = NOW() " +
                        "WHERE g.year = ? AND g.level IN (4, 5) AND g.is_deleted = 0 AND e.code IS NULL",
                year);
        log.info("按 2025 系统表标记删除的旧乡镇/社区记录: {}", marked);

        jdbcTemplate.execute("DROP TEMPORARY TABLE IF EXISTS tmp_org_authority_codes");
    }

    private void create2020RepairCandidateTable() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS org_2020_repair_candidate");
        jdbcTemplate.execute("CREATE TABLE org_2020_repair_candidate (" +
                "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                "issue_type VARCHAR(64) NOT NULL, " +
                "source_code VARCHAR(32), " +
                "display_code VARCHAR(32), " +
                "source_name VARCHAR(512), " +
                "current_name VARCHAR(512), " +
                "source_county VARCHAR(128), " +
                "current_county VARCHAR(128), " +
                "source_township VARCHAR(128), " +
                "current_township VARCHAR(128), " +
                "detail VARCHAR(1024), " +
                "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "KEY idx_issue_type (issue_type), " +
                "KEY idx_source_code (source_code), " +
                "KEY idx_display_code (display_code)" +
                ")");
    }

    private void generate2020RepairCandidates() {
        jdbcTemplate.update("INSERT INTO org_2020_repair_candidate (" +
                        "issue_type, source_code, source_name, source_county, source_township, detail" +
                        ") " +
                        "SELECT 'MISSING_COMMUNITY_FROM_XLS', s.code, s.community_name, s.county_name, s.township_name, s.full_name " +
                        "FROM org_2020_system_xls_staging s " +
                        "WHERE NOT EXISTS (" +
                        "    SELECT 1 FROM grassroots_organization g " +
                        "    WHERE g.code = s.code AND g.year = 2020 AND g.is_baseline = 1 AND g.is_deleted = 0 AND g.level = 5" +
                        ") AND NOT EXISTS (" +
                        "    SELECT 1 FROM grassroots_organization g " +
                        "    WHERE g.baseline_code = s.code AND g.year = 2020 AND g.is_baseline = 1 AND g.is_deleted = 0 AND g.level = 5" +
                        ")");

        jdbcTemplate.update("INSERT INTO org_2020_repair_candidate (" +
                        "issue_type, source_code, source_name, source_county, source_township, detail" +
                        ") " +
                        "SELECT 'MISSING_TOWNSHIP_FROM_XLS', t.township_code, MIN(t.township_name), MIN(t.county_name), MIN(t.township_name), " +
                        "       CONCAT('system_xls_child_count=', COUNT(*)) " +
                        "FROM org_2020_system_xls_staging t " +
                        "WHERE NOT EXISTS (" +
                        "    SELECT 1 FROM grassroots_organization g " +
                        "    WHERE g.code = t.township_code AND g.year = 2020 AND g.is_baseline = 1 AND g.is_deleted = 0 AND g.level = 4" +
                        ") AND NOT EXISTS (" +
                        "    SELECT 1 FROM grassroots_organization g " +
                        "    WHERE g.baseline_code = t.township_code AND g.year = 2020 AND g.is_baseline = 1 AND g.is_deleted = 0 AND g.level = 4" +
                        ") " +
                        "GROUP BY t.township_code");

        jdbcTemplate.update("INSERT INTO org_2020_repair_candidate (" +
                        "issue_type, display_code, current_name, current_county, current_township, detail" +
                        ") " +
                        "SELECT 'EXTRA_COMMUNITY_NOT_IN_XLS', g.code, g.name, g.county_name, g.township_name, " +
                        "       CONCAT('baseline_code=', COALESCE(g.baseline_code, '')) " +
                        "FROM grassroots_organization g " +
                        "WHERE g.year = 2020 AND g.is_baseline = 1 AND g.is_deleted = 0 AND g.level = 5 " +
                        "  AND NOT EXISTS (SELECT 1 FROM org_2020_system_xls_staging s WHERE s.code = g.code) " +
                        "  AND (g.baseline_code IS NULL OR NOT EXISTS (SELECT 1 FROM org_2020_system_xls_staging s WHERE s.code = g.baseline_code))");

        jdbcTemplate.update("INSERT INTO org_2020_repair_candidate (" +
                        "issue_type, display_code, current_name, current_county, current_township, detail" +
                        ") " +
                        "SELECT 'CURRENT_TOWNSHIP_NO_CHILD', town.code, town.name, town.county_name, town.township_name, " +
                        "       CONCAT('baseline_code=', COALESCE(town.baseline_code, '')) " +
                        "FROM grassroots_organization town " +
                        "WHERE town.year = 2020 AND town.is_baseline = 1 AND town.is_deleted = 0 AND town.level = 4 " +
                        "  AND NOT EXISTS (" +
                        "      SELECT 1 FROM grassroots_organization child " +
                        "      WHERE child.parent_id = town.id AND child.year = town.year AND child.level = 5 AND child.is_deleted = 0" +
                        "  ) AND NOT EXISTS (" +
                        "      SELECT 1 FROM grassroots_organization child " +
                        "      WHERE child.code LIKE CONCAT(town.code, '___') AND child.year = town.year AND child.level = 5 AND child.is_deleted = 0" +
                        "  )");

        jdbcTemplate.update("INSERT INTO org_2020_repair_candidate (" +
                        "issue_type, source_code, display_code, source_name, current_name, source_county, current_county, source_township, current_township, detail" +
                        ") " +
                        "SELECT 'FUNCTIONAL_CODE_MAPPING_PRESENT', g.baseline_code, g.code, s.community_name, g.name, " +
                        "       s.county_name, g.county_name, s.township_name, g.township_name, s.full_name " +
                        "FROM grassroots_organization g " +
                        "JOIN org_2020_system_xls_staging s ON s.code = g.baseline_code " +
                        "WHERE g.year = 2020 AND g.is_baseline = 1 AND g.is_deleted = 0 AND g.level = 5 " +
                        "  AND g.baseline_code IS NOT NULL AND g.baseline_code <> g.code");

        jdbcTemplate.update("INSERT INTO org_2020_repair_candidate (" +
                        "issue_type, source_code, source_name, source_county, source_township, detail" +
                        ") " +
                        "SELECT 'SOURCE_COUNTY_NAME_POLLUTION', LEFT(TRIM(region_code), 6), MAX(TRIM(county)), MAX(TRIM(county)), MAX(TRIM(township)), " +
                        "       CONCAT('survey_data city=', MAX(TRIM(city)), ', rows=', COUNT(*)) " +
                        "FROM survey_data " +
                        "WHERE year = 2020 AND is_deleted = 0 AND TRIM(county) = '旌阳区' AND TRIM(city) <> '德阳市' " +
                        "GROUP BY LEFT(TRIM(region_code), 6)");

        jdbcTemplate.update("INSERT INTO org_2020_repair_candidate (" +
                        "issue_type, source_code, source_name, source_county, source_township, detail" +
                        ") " +
                        "SELECT 'SOURCE_COUNTY_NAME_POLLUTION', LEFT(TRIM(region_code), 6), MAX(TRIM(county_name)), MAX(TRIM(county_name)), MAX(TRIM(township_name)), " +
                        "       CONCAT('community_disaster_reduction_capacity city=', MAX(TRIM(city_name)), ', rows=', COUNT(*)) " +
                        "FROM community_disaster_reduction_capacity " +
                        "WHERE year = 2020 AND TRIM(county_name) = '旌阳区' AND TRIM(city_name) <> '德阳市' " +
                        "GROUP BY LEFT(TRIM(region_code), 6)");
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

        private SimpleOrgRow withFallbackLeafName(String fallbackLeafName) {
            return new SimpleOrgRow(
                    code,
                    rawName,
                    provinceName,
                    cityName,
                    countyName,
                    townshipName,
                    communityName,
                    fallbackLeafName
            );
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
                        CellType cachedType = cell.getCachedFormulaResultType();
                        if (cachedType == CellType.ERROR) {
                            return FormulaError.forInt(cell.getErrorCellValue()).getString();
                        }
                        if (cachedType == CellType.NUMERIC) {
                            double formulaNumeric = cell.getNumericCellValue();
                            long formulaLong = (long) formulaNumeric;
                            if (Math.abs(formulaNumeric - formulaLong) < 1e-9) {
                                return String.valueOf(formulaLong);
                            }
                            return String.valueOf(formulaNumeric);
                        }
                        if (cachedType == CellType.BOOLEAN) {
                            return String.valueOf(cell.getBooleanCellValue());
                        }
                        return cell.getStringCellValue();
                    } catch (Exception ignore) {
                        try {
                            return String.valueOf(cell.getNumericCellValue());
                        } catch (Exception ignore2) {
                            return null;
                        }
                    }
                case ERROR:
                    return FormulaError.forInt(cell.getErrorCellValue()).getString();
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

    private static boolean isUnavailableName(String value) {
        String v = normalizeText(value);
        return v == null || "#N/A".equalsIgnoreCase(v) || "N/A".equalsIgnoreCase(v) || "NULL".equalsIgnoreCase(v);
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

        int cityEnd = firstPositiveIndex(remaining, "自治州", "地区", "盟", "市");
        String citySuffix = matchedSuffixAt(remaining, cityEnd, "自治州", "地区", "盟", "市");
        if (cityEnd >= 0 && citySuffix != null) {
            cityName = remaining.substring(0, cityEnd + citySuffix.length());
            remaining = remaining.substring(cityEnd + citySuffix.length());
        }

        int countyEnd = firstPositiveIndex(remaining, "新区", "开发区", "管理委员会", "区", "县", "市");
        String countySuffix = matchedSuffixAt(remaining, countyEnd, "新区", "开发区", "管理委员会", "区", "县", "市");
        if (countyEnd >= 0 && countySuffix != null) {
            countyName = remaining.substring(0, countyEnd + countySuffix.length());
            remaining = remaining.substring(countyEnd + countySuffix.length());
        }

        int townshipEnd = firstPositiveIndex(remaining, "街道", "镇", "乡", "管理办");
        String townshipSuffix = matchedSuffixAt(remaining, townshipEnd, "街道", "镇", "乡", "管理办");
        if (townshipEnd >= 0 && townshipSuffix != null) {
            townshipName = remaining.substring(0, townshipEnd + townshipSuffix.length());
            remaining = remaining.substring(townshipEnd + townshipSuffix.length());
        }

        if (StringUtils.hasText(remaining)) {
            communityName = remaining.trim();
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

    private int firstPositiveIndex(String value, String... tokens) {
        if (!StringUtils.hasText(value)) {
            return -1;
        }
        int best = -1;
        for (String token : tokens) {
            int idx = value.indexOf(token);
            if (idx >= 0 && (best < 0 || idx < best)) {
                best = idx;
            }
        }
        return best;
    }

    private String matchedSuffixAt(String value, int index, String... tokens) {
        if (!StringUtils.hasText(value) || index < 0) {
            return null;
        }
        for (String token : tokens) {
            if (value.startsWith(token, index)) {
                return token;
            }
        }
        return null;
    }
}
