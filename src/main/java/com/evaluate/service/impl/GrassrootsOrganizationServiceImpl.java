package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluate.entity.GrassrootsOrganization;
import com.evaluate.entity.Organization;
import com.evaluate.mapper.GrassrootsOrganizationMapper;
import com.evaluate.mapper.OrganizationMapper;
import com.evaluate.service.IGrassrootsOrganizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 基层组织机构服务实现（乡镇和社区）
 */
@Slf4j
@Service
public class GrassrootsOrganizationServiceImpl extends ServiceImpl<GrassrootsOrganizationMapper, GrassrootsOrganization>
        implements IGrassrootsOrganizationService {

    @Autowired
    private OrganizationMapper organizationMapper;

    private static final int LEVEL_TOWNSHIP = 4;
    private static final int LEVEL_COMMUNITY = 5;

    @Override
    public List<GrassrootsOrganization> getTownshipsByCountyId(Long countyId, Integer year) {
        try {
            QueryWrapper<GrassrootsOrganization> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("county_id", countyId);
            queryWrapper.eq("level", LEVEL_TOWNSHIP);
            // 增量存储：查询当年变更 OR 基准记录
            if (year != null) {
                queryWrapper.and(wrapper -> wrapper
                        .and(w -> w.eq("year", year).eq("is_baseline", 0))
                        .or().eq("is_baseline", 1)
                );
            } else {
                queryWrapper.eq("is_baseline", 1);
            }
            queryWrapper.orderByAsc("code");
            List<GrassrootsOrganization> result = list(queryWrapper);
            if (year != null && !result.isEmpty()) {
                result = mergeBaselineWithYearData(result, year);
            }
            return result;
        } catch (Exception e) {
            log.error("根据区县ID获取乡镇列表失败: countyId={}, year={}", countyId, year, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<GrassrootsOrganization> getTownshipsByCountyCode(String countyCode, Integer year) {
        try {
            // 先通过区县代码找到区县ID
            QueryWrapper<Organization> orgQuery = new QueryWrapper<>();
            orgQuery.eq("code", countyCode.trim());
            orgQuery.eq("level", 3); // 区县级别
            if (year != null) {
                orgQuery.eq("year", year);
            }
            Organization county = organizationMapper.selectOne(orgQuery);
            if (county == null) {
                log.warn("未找到区县: countyCode={}, year={}", countyCode, year);
                return new ArrayList<>();
            }

            return getTownshipsByCountyId(county.getId(), year);
        } catch (Exception e) {
            log.error("根据区县代码获取乡镇列表失败: countyCode={}, year={}", countyCode, year, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<GrassrootsOrganization> getCommunitiesByTownshipId(Long townshipId, Integer year) {
        try {
            QueryWrapper<GrassrootsOrganization> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("parent_id", townshipId);
            queryWrapper.eq("level", LEVEL_COMMUNITY);
            // 增量存储：查询当年变更 OR 基准记录
            if (year != null) {
                queryWrapper.and(wrapper -> wrapper
                        .and(w -> w.eq("year", year).eq("is_baseline", 0))
                        .or().eq("is_baseline", 1)
                );
            } else {
                queryWrapper.eq("is_baseline", 1);
            }
            queryWrapper.orderByAsc("code");
            List<GrassrootsOrganization> result = list(queryWrapper);
            if (year != null && !result.isEmpty()) {
                result = mergeBaselineWithYearData(result, year);
            }
            return result;
        } catch (Exception e) {
            log.error("根据乡镇ID获取社区列表失败: townshipId={}, year={}", townshipId, year, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<GrassrootsOrganization> getCommunitiesByTownshipCode(String townshipCode, Integer year) {
        try {
            QueryWrapper<GrassrootsOrganization> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("code", townshipCode.trim());
            queryWrapper.eq("level", LEVEL_TOWNSHIP);
            if (year != null) {
                queryWrapper.and(wrapper -> wrapper
                        .and(w -> w.eq("year", year).eq("is_baseline", 0))
                        .or().eq("is_baseline", 1)
                );
            } else {
                queryWrapper.eq("is_baseline", 1);
            }
            GrassrootsOrganization township = getOne(queryWrapper, false);
            if (township == null) {
                log.warn("未找到乡镇: townshipCode={}, year={}", townshipCode, year);
                return new ArrayList<>();
            }

            return getCommunitiesByTownshipId(township.getId(), year);
        } catch (Exception e) {
            log.error("根据乡镇代码获取社区列表失败: townshipCode={}, year={}", townshipCode, year, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getTreeByCountyId(Long countyId, Integer year) {
        try {
            QueryWrapper<GrassrootsOrganization> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("county_id", countyId);
            // 增量存储：查询当年变更 OR 基准记录
            if (year != null) {
                queryWrapper.and(wrapper -> wrapper
                        .and(w -> w.eq("year", year).eq("is_baseline", 0))
                        .or().eq("is_baseline", 1)
                );
            } else {
                queryWrapper.eq("is_baseline", 1);
            }
            queryWrapper.orderByAsc("level", "code");
            List<GrassrootsOrganization> all = list(queryWrapper);
            if (year != null && !all.isEmpty()) {
                all = mergeBaselineWithYearData(all, year);
            }

            return buildTree(all, null);
        } catch (Exception e) {
            log.error("根据区县ID获取树形结构失败: countyId={}, year={}", countyId, year, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getTreeByCountyCode(String countyCode, Integer year) {
        try {
            QueryWrapper<Organization> orgQuery = new QueryWrapper<>();
            orgQuery.eq("code", countyCode.trim());
            orgQuery.eq("level", 3);
            if (year != null) {
                orgQuery.and(wrapper -> wrapper
                        .and(w -> w.eq("year", year).eq("is_baseline", 0))
                        .or().eq("is_baseline", 1)
                );
            } else {
                orgQuery.eq("is_baseline", 1);
            }
            Organization county = organizationMapper.selectOne(orgQuery);
            if (county == null) {
                log.warn("未找到区县: countyCode={}, year={}", countyCode, year);
                return new ArrayList<>();
            }

            return getTreeByCountyId(county.getId(), year);
        } catch (Exception e) {
            log.error("根据区县代码获取树形结构失败: countyCode={}, year={}", countyCode, year, e);
            return new ArrayList<>();
        }
    }

    @Override
    public GrassrootsOrganization getByCode(String code, Integer year) {
        try {
            QueryWrapper<GrassrootsOrganization> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("code", code.trim());
            if (year != null) {
                queryWrapper.eq("year", year);
            }
            return getOne(queryWrapper, false);
        } catch (Exception e) {
            log.error("根据编码获取基层组织机构失败: code={}, year={}", code, year, e);
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createGrassrootsOrganization(GrassrootsOrganization organization) {
        try {
            // 检查编码是否已存在
            QueryWrapper<GrassrootsOrganization> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("code", organization.getCode());
            if (organization.getYear() != null) {
                queryWrapper.eq("year", organization.getYear());
            }
            GrassrootsOrganization existing = getOne(queryWrapper, false);
            if (existing != null) {
                throw new RuntimeException("基层组织机构编码已存在: " + organization.getCode());
            }

            // 验证county_id
            if (organization.getCountyId() == null) {
                throw new RuntimeException("区县ID不能为空");
            }
            Organization county = organizationMapper.selectById(organization.getCountyId());
            if (county == null || county.getLevel() != 3) {
                throw new RuntimeException("区县ID无效");
            }

            // 设置默认值
            if (organization.getLevel() == null) {
                organization.setLevel(LEVEL_TOWNSHIP);
            }
            if (organization.getLevel() != LEVEL_TOWNSHIP && organization.getLevel() != LEVEL_COMMUNITY) {
                throw new RuntimeException("级别必须是4（乡镇）或5（社区）");
            }

            return save(organization);
        } catch (Exception e) {
            log.error("创建基层组织机构失败: {}", organization.getCode(), e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateGrassrootsOrganization(GrassrootsOrganization organization) {
        try {
            GrassrootsOrganization existing = getById(organization.getId());
            if (existing == null) {
                throw new RuntimeException("基层组织机构不存在: " + organization.getId());
            }

            // 检查是否是基准记录
            boolean isBaseline = existing.getIsBaseline() != null && existing.getIsBaseline() == 1;
            Integer updateYear = organization.getYear();

            // 如果更新基准记录（2020年或其他基准年），允许直接更新
            if (updateYear == null || (isBaseline && updateYear.equals(existing.getYear()))) {
                // 如果修改了编码，检查新编码是否已被其他记录使用
                if (!existing.getCode().equals(organization.getCode())) {
                    QueryWrapper<GrassrootsOrganization> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("code", organization.getCode());
                    queryWrapper.ne("id", organization.getId());
                    if (updateYear != null) {
                        queryWrapper.eq("year", updateYear);
                    }
                    GrassrootsOrganization codeExisting = getOne(queryWrapper, false);
                    if (codeExisting != null) {
                        throw new RuntimeException("基层组织机构编码已存在: " + organization.getCode());
                    }
                }
                return updateById(organization);
            }

            // 更新非基准年数据：创建或更新年度变更记录，不修改基准记录
            if (updateYear != null && !updateYear.equals(existing.getYear())) {
                // 检查是否已存在该年度的变更记录
                QueryWrapper<GrassrootsOrganization> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("code", existing.getCode());
                queryWrapper.eq("year", updateYear);
                GrassrootsOrganization yearRecord = getOne(queryWrapper, false);

                if (yearRecord != null) {
                    // 更新现有年度记录
                    organization.setId(yearRecord.getId());
                    organization.setIsBaseline(0);
                    organization.setBaselineCode(existing.getCode());
                    return updateById(organization);
                } else {
                    // 创建新的年度变更记录
                    GrassrootsOrganization newYearOrg = new GrassrootsOrganization();
                    newYearOrg.setCode(existing.getCode());
                    newYearOrg.setName(organization.getName() != null ? organization.getName() : existing.getName());
                    newYearOrg.setLevel(existing.getLevel());
                    newYearOrg.setCountyId(organization.getCountyId() != null ? organization.getCountyId() : existing.getCountyId());
                    newYearOrg.setParentId(organization.getParentId() != null ? organization.getParentId() : existing.getParentId());
                    newYearOrg.setYear(updateYear);
                    newYearOrg.setIsBaseline(0);
                    newYearOrg.setBaselineCode(existing.getCode());
                    newYearOrg.setProvinceName(organization.getProvinceName() != null ? organization.getProvinceName() : existing.getProvinceName());
                    newYearOrg.setCityName(organization.getCityName() != null ? organization.getCityName() : existing.getCityName());
                    newYearOrg.setCountyName(organization.getCountyName() != null ? organization.getCountyName() : existing.getCountyName());
                    newYearOrg.setTownshipName(organization.getTownshipName() != null ? organization.getTownshipName() : existing.getTownshipName());
                    newYearOrg.setCommunityName(organization.getCommunityName() != null ? organization.getCommunityName() : existing.getCommunityName());
                    return save(newYearOrg);
                }
            }

            // 如果更新的是同年度记录，直接更新
            return updateById(organization);
        } catch (Exception e) {
            log.error("更新基层组织机构失败: {}", organization.getId(), e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteGrassrootsOrganization(Long id) {
        try {
            return cascadeDeleteGrassrootsOrganizations(Collections.singletonList(id));
        } catch (Exception e) {
            log.error("删除基层组织机构失败: {}", id, e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteGrassrootsOrganization(Long id, Integer year) {
        try {
            // 如果没有指定年份，使用原有的硬删除逻辑
            if (year == null) {
                return cascadeDeleteGrassrootsOrganizations(Collections.singletonList(id));
            }

            // 增量存储：按年份删除，使用删除标记
            GrassrootsOrganization org = getById(id);
            if (org == null) {
                throw new IllegalArgumentException("基层组织机构不存在: " + id);
            }

            // 查找该年度的记录
            QueryWrapper<GrassrootsOrganization> yearRecordQuery = new QueryWrapper<>();
            yearRecordQuery.eq("code", org.getCode());
            yearRecordQuery.eq("year", year);
            GrassrootsOrganization yearRecord = getOne(yearRecordQuery, false);

            if (yearRecord != null) {
                // 如果该年度有记录，标记为已删除
                yearRecord.setIsDeleted(1);
                return updateById(yearRecord);
            } else {
                // 如果该年度没有记录，创建删除标记记录
                GrassrootsOrganization deleteMarker = new GrassrootsOrganization();
                deleteMarker.setCode(org.getCode());
                deleteMarker.setName(org.getName());
                deleteMarker.setLevel(org.getLevel());
                deleteMarker.setCountyId(org.getCountyId());
                deleteMarker.setParentId(org.getParentId());
                deleteMarker.setYear(year);
                deleteMarker.setIsBaseline(0);
                deleteMarker.setBaselineCode(org.getCode());
                deleteMarker.setIsDeleted(1); // 标记为已删除
                deleteMarker.setProvinceName(org.getProvinceName());
                deleteMarker.setCityName(org.getCityName());
                deleteMarker.setCountyName(org.getCountyName());
                deleteMarker.setTownshipName(org.getTownshipName());
                deleteMarker.setCommunityName(org.getCommunityName());
                return save(deleteMarker);
            }
        } catch (Exception e) {
            log.error("删除基层组织机构失败: id={}, year={}", id, year, e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteGrassrootsOrganizations(List<Long> ids) {
        try {
            return cascadeDeleteGrassrootsOrganizations(ids);
        } catch (Exception e) {
            log.error("批量删除基层组织机构失败: {}", ids, e);
            throw e;
        }
    }

    private boolean cascadeDeleteGrassrootsOrganizations(List<Long> rootIds) {
        if (rootIds == null || rootIds.isEmpty()) {
            return true;
        }

        List<GrassrootsOrganization> roots = listByIds(rootIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (roots.isEmpty()) {
            return true;
        }

        List<String> prefixes = roots.stream()
                .map(GrassrootsOrganization::getCode)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());

        if (prefixes.isEmpty()) {
            removeByIds(rootIds);
            return true;
        }

        QueryWrapper<GrassrootsOrganization> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(wrapper -> {
            for (String prefix : prefixes) {
                wrapper.or().likeRight("code", prefix);
            }
        });

        List<GrassrootsOrganization> organizations = list(queryWrapper);
        if (organizations == null || organizations.isEmpty()) {
            return true;
        }

        Set<Long> deleteIds = organizations.stream()
                .map(GrassrootsOrganization::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (deleteIds.isEmpty()) {
            return true;
        }

        // 按层级从低到高删除（先删除社区，再删除乡镇）
        Map<Integer, List<Long>> byLevel = new HashMap<>();
        for (GrassrootsOrganization org : organizations) {
            if (org == null || org.getId() == null) {
                continue;
            }
            int level = org.getLevel() == null ? 0 : org.getLevel();
            byLevel.computeIfAbsent(level, k -> new ArrayList<>()).add(org.getId());
        }

        for (int level = 5; level >= 4; level--) {
            List<Long> idsAtLevel = byLevel.get(level);
            if (idsAtLevel == null || idsAtLevel.isEmpty()) {
                continue;
            }
            removeByIds(idsAtLevel);
        }

        return true;
    }

    @Override
    public List<GrassrootsOrganization> searchGrassrootsOrganizations(Long countyId, String keyword, Integer level, Integer year) {
        try {
            QueryWrapper<GrassrootsOrganization> queryWrapper = new QueryWrapper<>();

            if (countyId != null) {
                queryWrapper.eq("county_id", countyId);
            }

            if (year != null) {
                queryWrapper.eq("year", year);
            }

            if (StringUtils.hasText(keyword)) {
                String searchKeyword = keyword.trim();
                queryWrapper.and(wrapper -> wrapper
                        .like("code", searchKeyword)
                        .or().like("name", searchKeyword)
                        .or().like("province_name", searchKeyword)
                        .or().like("city_name", searchKeyword)
                        .or().like("county_name", searchKeyword)
                        .or().like("township_name", searchKeyword)
                        .or().like("community_name", searchKeyword)
                );
            }

            if (level != null) {
                queryWrapper.eq("level", level);
            }

            queryWrapper.orderByAsc("level", "code");
            return list(queryWrapper);
        } catch (Exception e) {
            log.error("搜索基层组织机构失败: countyId={}, keyword={}, level={}, year={}", countyId, keyword, level, year, e);
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int copyFromPreviousYear(Integer targetYear) {
        if (targetYear == null) {
            return 0;
        }
        int sourceYear = targetYear - 1;
        if (sourceYear <= 0) {
            return 0;
        }

        // 查询源年份的所有基层组织机构
        List<GrassrootsOrganization> sourceOrgs = baseMapper.selectList(
                new QueryWrapper<GrassrootsOrganization>().eq("year", sourceYear)
        );
        if (sourceOrgs == null || sourceOrgs.isEmpty()) {
            log.info("源年份 {} 没有基层组织机构数据", sourceYear);
            return 0;
        }

        // 查询目标年份已存在的基层组织机构
        List<GrassrootsOrganization> targetOrgs = baseMapper.selectList(
                new QueryWrapper<GrassrootsOrganization>().eq("year", targetYear)
        );

        Set<String> existingCodes = new HashSet<>();
        Map<String, GrassrootsOrganization> existingOrgs = new HashMap<>();
        if (targetOrgs != null) {
            for (GrassrootsOrganization org : targetOrgs) {
                if (org.getCode() != null) {
                    existingCodes.add(org.getCode());
                    existingOrgs.put(org.getCode(), org);
                }
            }
        }

        // 建立区县代码到新区县ID的映射
        Map<String, Long> countyCodeToNewId = new HashMap<>();
        for (GrassrootsOrganization src : sourceOrgs) {
            if (src.getCountyId() != null) {
                Organization oldCounty = organizationMapper.selectById(src.getCountyId());
                if (oldCounty != null && StringUtils.hasText(oldCounty.getCode())) {
                    // 查找目标年份的区县
                    QueryWrapper<Organization> countyQuery = new QueryWrapper<>();
                    countyQuery.eq("code", oldCounty.getCode());
                    countyQuery.eq("year", targetYear);
                    countyQuery.eq("level", 3);
                    Organization newCounty = organizationMapper.selectOne(countyQuery);
                    if (newCounty != null) {
                        countyCodeToNewId.put(oldCounty.getCode(), newCounty.getId());
                    }
                }
            }
        }

        // 按层级排序
        sourceOrgs.sort((a, b) -> {
            int levelCompare = Integer.compare(
                    a.getLevel() != null ? a.getLevel() : 0,
                    b.getLevel() != null ? b.getLevel() : 0
            );
            if (levelCompare != 0) {
                return levelCompare;
            }
            return Long.compare(a.getId() != null ? a.getId() : 0, b.getId() != null ? b.getId() : 0);
        });

        Map<Long, GrassrootsOrganization> oldIdToNewOrg = new HashMap<>();
        Map<String, GrassrootsOrganization> codeToNewOrg = new HashMap<>();
        int copiedCount = 0;

        for (GrassrootsOrganization src : sourceOrgs) {
            if (existingCodes.contains(src.getCode())) {
                GrassrootsOrganization existing = existingOrgs.get(src.getCode());
                if (existing != null) {
                    oldIdToNewOrg.put(src.getId(), existing);
                    codeToNewOrg.put(src.getCode(), existing);
                }
                continue;
            }

            GrassrootsOrganization targetOrg = new GrassrootsOrganization();
            targetOrg.setCode(src.getCode());
            targetOrg.setName(src.getName());
            targetOrg.setLevel(src.getLevel());
            targetOrg.setYear(targetYear);
            targetOrg.setDataSource(src.getDataSource());
            targetOrg.setProvinceName(src.getProvinceName());
            targetOrg.setCityName(src.getCityName());
            targetOrg.setCountyName(src.getCountyName());
            targetOrg.setTownshipName(src.getTownshipName());
            targetOrg.setCommunityName(src.getCommunityName());

            // 设置county_id
            if (src.getCountyId() != null) {
                Organization oldCounty = organizationMapper.selectById(src.getCountyId());
                if (oldCounty != null && StringUtils.hasText(oldCounty.getCode())) {
                    Long newCountyId = countyCodeToNewId.get(oldCounty.getCode());
                    if (newCountyId != null) {
                        targetOrg.setCountyId(newCountyId);
                    }
                }
            }

            baseMapper.insert(targetOrg);
            copiedCount++;

            if (src.getParentId() != null) {
                GrassrootsOrganization newParent = oldIdToNewOrg.get(src.getParentId());
                if (newParent != null && newParent.getId() != null) {
                    targetOrg.setParentId(newParent.getId());
                } else {
                    GrassrootsOrganization srcParent = sourceOrgs.stream()
                            .filter(o -> o.getId().equals(src.getParentId()))
                            .findFirst().orElse(null);
                    if (srcParent != null) {
                        GrassrootsOrganization newParentByCode = codeToNewOrg.get(srcParent.getCode());
                        if (newParentByCode != null) {
                            targetOrg.setParentId(newParentByCode.getId());
                        }
                    }
                }
            }

            if (targetOrg.getParentId() != null) {
                GrassrootsOrganization updateOrg = new GrassrootsOrganization();
                updateOrg.setId(targetOrg.getId());
                updateOrg.setParentId(targetOrg.getParentId());
                baseMapper.updateById(updateOrg);
            }

            oldIdToNewOrg.put(src.getId(), targetOrg);
            codeToNewOrg.put(src.getCode(), targetOrg);
        }

        log.info("从上一年复制基层组织机构：新增 {} 条（{} -> {}），跳过已存在 {} 条",
                copiedCount, sourceYear, targetYear, sourceOrgs.size() - copiedCount);

        return copiedCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> deleteYearDataByCountyId(Long countyId, Integer year) {
        Map<String, Object> result = new HashMap<>();
        int totalDeleted = 0;

        Organization county = organizationMapper.selectById(countyId);
        if (county == null) {
            throw new IllegalArgumentException("区县不存在: " + countyId);
        }

        // 查询指定年份下的所有相关组织（包括自身和所有子组织）
        // 增量存储：查询当年变更记录 OR 基准记录
        QueryWrapper<GrassrootsOrganization> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("county_id", countyId);
        if (year != null) {
            queryWrapper.and(wrapper -> wrapper
                    .and(w -> w.eq("year", year).ne("is_baseline", 1))
                    .or().eq("is_baseline", 1)
            );
        }
        queryWrapper.orderByAsc("code");
        List<GrassrootsOrganization> allOrgs = list(queryWrapper);

        if (allOrgs == null || allOrgs.isEmpty()) {
            result.put("totalDeleted", 0);
            result.put("organizationDeleted", 0);
            result.put("countyName", county.getName());
            result.put("year", year);
            result.put("organizationCount", 0);
            log.info("删除区县年度数据：区县={}，年份={}，未找到相关记录", county.getName(), year);
            return result;
        }

        // 合并基准数据和当年数据（获取实际需要删除的组织列表）
        List<GrassrootsOrganization> orgsToDelete = allOrgs;
        if (year != null) {
            orgsToDelete = mergeBaselineWithYearData(allOrgs, year);
        }

        // 处理每个组织：创建删除标记或删除年度记录
        int organizationDeleted = 0;
        for (GrassrootsOrganization targetOrg : orgsToDelete) {
            // 查找该年度的记录
            QueryWrapper<GrassrootsOrganization> yearRecordQuery = new QueryWrapper<>();
            yearRecordQuery.eq("code", targetOrg.getCode());
            yearRecordQuery.eq("year", year);
            GrassrootsOrganization yearRecord = getOne(yearRecordQuery, false);

            if (yearRecord != null) {
                // 如果该年度有记录，标记为已删除
                yearRecord.setIsDeleted(1);
                updateById(yearRecord);
                organizationDeleted++;
            } else {
                // 如果该年度没有记录，创建删除标记记录
                GrassrootsOrganization deleteMarker = new GrassrootsOrganization();
                deleteMarker.setCode(targetOrg.getCode());
                deleteMarker.setName(targetOrg.getName());
                deleteMarker.setLevel(targetOrg.getLevel());
                deleteMarker.setCountyId(targetOrg.getCountyId());
                deleteMarker.setParentId(targetOrg.getParentId());
                deleteMarker.setYear(year);
                deleteMarker.setIsBaseline(0);
                deleteMarker.setBaselineCode(targetOrg.getCode());
                deleteMarker.setIsDeleted(1); // 标记为已删除
                deleteMarker.setProvinceName(targetOrg.getProvinceName());
                deleteMarker.setCityName(targetOrg.getCityName());
                deleteMarker.setCountyName(targetOrg.getCountyName());
                deleteMarker.setTownshipName(targetOrg.getTownshipName());
                deleteMarker.setCommunityName(targetOrg.getCommunityName());
                save(deleteMarker);
                organizationDeleted++;
            }
        }
        totalDeleted += organizationDeleted;

        result.put("totalDeleted", totalDeleted);
        result.put("organizationDeleted", organizationDeleted);
        result.put("countyName", county.getName());
        result.put("year", year);
        result.put("organizationCount", orgsToDelete.size());

        log.info("删除区县年度数据完成：区县={} (含{}个子组织)，年份={}，共处理{}条记录",
                county.getName(), orgsToDelete.size() - 1, year, totalDeleted);

        return result;
    }

    @Override
    public Map<String, Object> getGrassrootsOrganizationList(Integer page, Integer size, Long countyId, String code, String name, Integer level, Long parentId, Integer year) {
        Map<String, Object> result = new HashMap<>();

        try {
            Page<GrassrootsOrganization> pageParam = new Page<>(page, size);
            QueryWrapper<GrassrootsOrganization> queryWrapper = new QueryWrapper<>();

            if (countyId != null) {
                queryWrapper.eq("county_id", countyId);
            }

            if (StringUtils.hasText(code)) {
                queryWrapper.like("code", code.trim());
            }
            if (StringUtils.hasText(name)) {
                queryWrapper.like("name", name.trim());
            }
            if (level != null) {
                queryWrapper.eq("level", level);
            }
            if (parentId != null) {
                queryWrapper.eq("parent_id", parentId);
            }
            if (year != null) {
                queryWrapper.eq("year", year);
            }

            queryWrapper.orderByAsc("level", "code");

            IPage<GrassrootsOrganization> pageResult = page(pageParam, queryWrapper);

            result.put("success", true);
            result.put("data", pageResult.getRecords());
            result.put("total", pageResult.getTotal());
            result.put("page", page);
            result.put("size", size);
            result.put("pages", pageResult.getPages());

        } catch (Exception e) {
            log.error("查询基层组织机构列表失败", e);
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public List<GrassrootsOrganization> debugGetCommunitiesByTownshipId(Long townshipId) {
        try {
            QueryWrapper<GrassrootsOrganization> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("parent_id", townshipId);
            queryWrapper.eq("level", LEVEL_COMMUNITY);
            queryWrapper.orderByAsc("code");
            List<GrassrootsOrganization> result = list(queryWrapper);
            log.info("调试查询：乡镇ID {} 下有 {} 个社区（所有年份）", townshipId, result == null ? 0 : result.size());
            if (result != null && !result.isEmpty()) {
                for (GrassrootsOrganization org : result) {
                    log.info("  - code: {}, name: {}, year: {}, is_baseline: {}, parent_id: {}",
                            org.getCode(), org.getName(), org.getYear(), org.getIsBaseline(), org.getParentId());
                }
            }
            return result;
        } catch (Exception e) {
            log.error("调试查询失败: townshipId={}", townshipId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 合并基准数据和当年变更数据
     * 当年记录优先，相同code的基准记录被覆盖
     * 如果当年记录是删除标记（is_deleted=1），则不包含该组织
     */
    private List<GrassrootsOrganization> mergeBaselineWithYearData(List<GrassrootsOrganization> organizations, Integer year) {
        Map<String, GrassrootsOrganization> mergedMap = new LinkedHashMap<>();
        // 记录被删除的code
        Set<String> deletedCodes = new HashSet<>();

        for (GrassrootsOrganization org : organizations) {
            String code = org.getCode();
            if (code == null) continue;

            GrassrootsOrganization existing = mergedMap.get(code);

            // 当年变更记录
            if (org.getYear() != null && org.getYear().equals(year) &&
                (org.getIsBaseline() == null || org.getIsBaseline() == 0)) {
                // 如果是删除标记，记录该code为已删除
                if (org.getIsDeleted() != null && org.getIsDeleted() == 1) {
                    deletedCodes.add(code);
                    // 从结果中移除该code（如果存在）
                    mergedMap.remove(code);
                } else {
                    mergedMap.put(code, org);
                }
            } else if (existing == null && !deletedCodes.contains(code)) {
                // 基准记录，只有当年没有变更且未被删除时才使用
                mergedMap.put(code, org);
            }
        }

        return new ArrayList<>(mergedMap.values());
    }

    /**
     * 构建树形结构
     */
    private List<Map<String, Object>> buildTree(List<GrassrootsOrganization> organizations, Long parentId) {
        if (organizations == null || organizations.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, List<GrassrootsOrganization>> parentMap = organizations.stream()
                .collect(Collectors.groupingBy(
                        org -> org.getParentId() != null ? org.getParentId() : 0L
                ));

        if (parentId != null) {
            return buildTreeRecursive(parentId, parentMap);
        } else {
            // 找到所有根节点（乡镇，parentId为0或null）
            List<Map<String, Object>> result = new ArrayList<>();
            List<GrassrootsOrganization> roots = parentMap.getOrDefault(0L, new ArrayList<>());

            for (GrassrootsOrganization root : roots) {
                if (root == null || root.getId() == null) {
                    continue;
                }
                Map<String, Object> node = new HashMap<>();
                node.put("id", root.getId());
                node.put("parentId", root.getParentId());
                node.put("countyId", root.getCountyId());
                node.put("code", root.getCode());
                node.put("name", root.getName());
                node.put("level", root.getLevel());
                node.put("dataSource", root.getDataSource());
                node.put("provinceName", root.getProvinceName());
                node.put("cityName", root.getCityName());
                node.put("countyName", root.getCountyName());
                node.put("townshipName", root.getTownshipName());
                node.put("communityName", root.getCommunityName());
                node.put("year", root.getYear());

                List<Map<String, Object>> childNodes = buildTreeRecursive(root.getId(), parentMap);
                if (!childNodes.isEmpty()) {
                    node.put("children", childNodes);
                }
                result.add(node);
            }

            return result;
        }
    }

    private List<Map<String, Object>> buildTreeRecursive(
            Long parentId,
            Map<Long, List<GrassrootsOrganization>> parentMap
    ) {
        if (parentId == null) {
            return new ArrayList<>();
        }
        List<GrassrootsOrganization> children = parentMap.get(parentId);
        if (children == null || children.isEmpty()) {
            return new ArrayList<>();
        }

        return children.stream().map(org -> {
            Map<String, Object> node = new HashMap<>();
            node.put("id", org.getId());
            node.put("parentId", org.getParentId());
            node.put("countyId", org.getCountyId());
            node.put("code", org.getCode());
            node.put("name", org.getName());
            node.put("level", org.getLevel());
            node.put("dataSource", org.getDataSource());
            node.put("provinceName", org.getProvinceName());
            node.put("cityName", org.getCityName());
            node.put("countyName", org.getCountyName());
            node.put("townshipName", org.getTownshipName());
            node.put("communityName", org.getCommunityName());
            node.put("year", org.getYear());

            List<Map<String, Object>> childNodes = buildTreeRecursive(org.getId(), parentMap);
            if (!childNodes.isEmpty()) {
                node.put("children", childNodes);
            }

            return node;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> fixCommunityParentIds(Integer year) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 查询所有parent_id为NULL或不匹配的社区数据
            QueryWrapper<GrassrootsOrganization> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("level", LEVEL_COMMUNITY);
            queryWrapper.eq("year", year);
            List<GrassrootsOrganization> communities = list(queryWrapper);

            int fixedCount = 0;
            int skippedCount = 0;

            for (GrassrootsOrganization community : communities) {
                // 根据township_name和county_name查找对应的乡镇
                QueryWrapper<GrassrootsOrganization> townshipQuery = new QueryWrapper<>();
                townshipQuery.eq("level", LEVEL_TOWNSHIP);
                townshipQuery.eq("year", year);
                townshipQuery.eq("name", community.getTownshipName());
                townshipQuery.eq("county_name", community.getCountyName());
                GrassrootsOrganization township = getOne(townshipQuery, false);

                if (township != null) {
                    // 检查是否需要更新parent_id
                    if (!township.getId().equals(community.getParentId())) {
                        community.setParentId(township.getId());
                        community.setCountyId(township.getCountyId());
                        updateById(community);
                        fixedCount++;
                        log.debug("修复社区parent_id: community={}, parent_id={}", community.getName(), township.getId());
                    }
                } else {
                    skippedCount++;
                    log.warn("未找到乡镇: township_name={}, county_name={}", community.getTownshipName(), community.getCountyName());
                }
            }

            result.put("totalCommunities", communities.size());
            result.put("fixedCount", fixedCount);
            result.put("skippedCount", skippedCount);
            result.put("success", true);

            log.info("修复社区parent_id完成: 总数={}, 修复={}, 跳过={}", communities.size(), fixedCount, skippedCount);
            return result;
        } catch (Exception e) {
            log.error("修复社区parent_id失败: year={}", year, e);
            result.put("success", false);
            result.put("message", e.getMessage());
            throw e;
        }
    }
}
