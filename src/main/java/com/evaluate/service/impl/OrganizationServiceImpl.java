package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluate.entity.CommunityDisasterReductionCapacity;
import com.evaluate.entity.GrassrootsOrganization;
import com.evaluate.entity.MedicalInstitution;
import com.evaluate.entity.Organization;
import com.evaluate.entity.OrganizationBoundary;
import com.evaluate.entity.SurveyData;
import com.evaluate.mapper.GrassrootsOrganizationMapper;
import com.evaluate.mapper.MedicalInstitutionMapper;
import com.evaluate.mapper.OrganizationBoundaryMapper;
import com.evaluate.mapper.RoleMapper;
import com.evaluate.mapper.RoleOrganizationMapper;
import com.evaluate.mapper.UserOrganizationMapper;
import com.evaluate.mapper.UserMapper;
import com.evaluate.entity.Role;
import com.evaluate.entity.User;
import com.evaluate.mapper.OrganizationMapper;
import com.evaluate.service.IGrassrootsOrganizationService;
import com.evaluate.service.IOrganizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 组织机构服务实现
 */
@Slf4j
@Service
public class OrganizationServiceImpl extends ServiceImpl<OrganizationMapper, Organization>
        implements IOrganizationService {

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private RoleOrganizationMapper roleOrganizationMapper;

    @Autowired
    private UserOrganizationMapper userOrganizationMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private com.evaluate.mapper.SurveyDataMapper surveyDataMapper;

    @Autowired
    private com.evaluate.mapper.CommunityDisasterReductionCapacityMapper communityDataMapper;

    @Autowired
    private MedicalInstitutionMapper medicalInstitutionMapper;

    @Autowired
    private OrganizationBoundaryMapper organizationBoundaryMapper;

    @Autowired
    private IGrassrootsOrganizationService grassrootsOrganizationService;

    @Autowired
    private GrassrootsOrganizationMapper grassrootsOrganizationMapper;

    private static final String SOURCE_COMMUNITY = "COMMUNITY";
    private static final String SOURCE_TOWNSHIP = "TOWNSHIP";

    private static final int LEVEL_PROVINCE = 1;
    private static final int LEVEL_CITY = 2;
    private static final int LEVEL_COUNTY = 3;
    private static final int LEVEL_TOWNSHIP = 4;
    private static final int LEVEL_COMMUNITY = 5;

    /**
     * 获取当前用户有权限的组织机构编码列表
     * @return 编码列表，如果为null表示拥有所有权限，如果为空列表表示无权限
     */
    private List<String> getCurrentUserAllowedOrgCodes() {
        try {
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                return null; // 无认证信息，可能是内部调用或未登录，暂时不做限制
            }
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username = null;
            if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            } else if (principal instanceof String) {
                username = (String) principal;
            }
            
            if (!StringUtils.hasText(username) || "anonymousUser".equals(username)) {
                return null; // 匿名用户或无法识别，不做限制? 或者限制所有? 
                // 考虑到登录接口等不需要权限，这里暂时返回null(不限制)或者根据业务需求。
                // 通常只有受保护的接口才会走到这里。
                // 如果是匿名用户，理论上不应该能调到这个Service方法(如果Controller有鉴权)。
                // 暂时假设Admin才有权限查看全部。
                // 这里的逻辑：如果无法识别用户，默认不限制（保持原有逻辑）。
            }
            
            User user = userMapper.selectUserByUsername(username);
            if (user == null) return new ArrayList<>(); // User not found, no access

            // Special check for 'admin' username to ensure they always have full access
            if ("admin".equals(user.getUsername())) {
                return null;
            }

            List<Role> roles = roleMapper.selectRolesByUserId(user.getId());
            boolean isAdmin = roles.stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getRoleCode()));
            if (isAdmin) return null; // Admin has all access

            List<String> codes = new ArrayList<>();

            List<Long> roleIds = roles.stream().map(Role::getId).collect(Collectors.toList());
            if (!roleIds.isEmpty()) {
                List<String> roleCodes = roleOrganizationMapper.selectOrgCodesByRoleIds(roleIds);
                if (roleCodes != null) {
                    codes.addAll(roleCodes);
                }
            }
            
            // 获取用户直接关联的组织机构权限
            List<String> userCodes = userOrganizationMapper.selectOrgCodesByUserId(user.getId());
            if (userCodes != null) {
                codes.addAll(userCodes);
            }
            
            return codes;
        } catch (Exception e) {
            log.error("获取用户权限失败", e);
            return new ArrayList<>(); // Fail safe: no access
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncFromCommunityData(CommunityDisasterReductionCapacity community) {
        if (community == null || !StringUtils.hasText(community.getRegionCode())) {
            return;
        }

        String regionCode = community.getRegionCode().trim();
        Integer year = community.getYear();

        try {
            // 省、市、县使用 organization 表
            Organization province = ensureOrganization(
                    extractCode(regionCode, 2),
                    community.getProvinceName(),
                    LEVEL_PROVINCE,
                    SOURCE_COMMUNITY,
                    null,
                    community.getProvinceName(),
                    null,
                    null,
                    null,
                    null,
                    year
            );

            Organization city = ensureOrganization(
                    extractCode(regionCode, 4),
                    community.getCityName(),
                    LEVEL_CITY,
                    SOURCE_COMMUNITY,
                    province,
                    community.getProvinceName(),
                    community.getCityName(),
                    null,
                    null,
                    null,
                    year
            );

            Organization county = ensureOrganization(
                    extractCode(regionCode, 6),
                    community.getCountyName(),
                    LEVEL_COUNTY,
                    SOURCE_COMMUNITY,
                    city,
                    community.getProvinceName(),
                    community.getCityName(),
                    community.getCountyName(),
                    null,
                    null,
                    year
            );

            if (county == null) {
                log.warn("无法同步组织机构数据：区县不存在。regionCode={}, countyName={}, year={}",
                    regionCode, community.getCountyName(), year);
                return;
            }

            // 乡镇和社区使用 grassroots_organization 表
            GrassrootsOrganization township = ensureGrassrootsOrganization(
                    extractCode(regionCode, 9),
                    community.getTownshipName(),
                    LEVEL_TOWNSHIP,
                    SOURCE_COMMUNITY,
                    county.getId(),
                    null,
                    community.getProvinceName(),
                    community.getCityName(),
                    community.getCountyName(),
                    community.getTownshipName(),
                    null,
                    year
            );

            if (township == null) {
                log.warn("无法同步社区组织机构：乡镇不存在。regionCode={}, townshipName={}, year={}",
                    regionCode, community.getTownshipName(), year);
            }

            GrassrootsOrganization communityOrg = ensureGrassrootsOrganization(
                    regionCode,
                    community.getCommunityName(),
                    LEVEL_COMMUNITY,
                    SOURCE_COMMUNITY,
                    null,
                    township != null ? township.getId() : null,
                    community.getProvinceName(),
                    community.getCityName(),
                    community.getCountyName(),
                    community.getTownshipName(),
                    community.getCommunityName(),
                    year
            );

            if (communityOrg != null) {
                log.debug("成功同步社区组织机构：code={}, name={}, township={}, year={}",
                    regionCode, community.getCommunityName(), community.getTownshipName(), year);
            } else {
                log.warn("社区组织机构同步失败：code={}, name={}, year={}",
                    regionCode, community.getCommunityName(), year);
            }
        } catch (Exception e) {
            log.error("同步社区组织机构数据失败：regionCode={}, communityName={}, year={}",
                regionCode, community.getCommunityName(), year, e);
            // 不抛出异常，避免影响主流程
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncFromSurveyData(SurveyData surveyData) {
        if (surveyData == null || !StringUtils.hasText(surveyData.getRegionCode())) {
            return;
        }

        String regionCode = surveyData.getRegionCode().trim();
        Integer year = surveyData.getYear();

        // 省、市、县使用 organization 表
        Organization province = ensureOrganization(
                extractCode(regionCode, 2),
                surveyData.getProvince(),
                LEVEL_PROVINCE,
                SOURCE_TOWNSHIP,
                null,
                surveyData.getProvince(),
                null,
                null,
                null,
                null,
                year
        );

        Organization city = ensureOrganization(
                extractCode(regionCode, 4),
                surveyData.getCity(),
                LEVEL_CITY,
                SOURCE_TOWNSHIP,
                province,
                surveyData.getProvince(),
                surveyData.getCity(),
                null,
                null,
                null,
                year
        );

        Organization county = ensureOrganization(
                extractCode(regionCode, 6),
                surveyData.getCounty(),
                LEVEL_COUNTY,
                SOURCE_TOWNSHIP,
                city,
                surveyData.getProvince(),
                surveyData.getCity(),
                surveyData.getCounty(),
                null,
                null,
                year
        );

        // 乡镇使用 grassroots_organization 表（只插入，不更新）
        ensureGrassrootsOrganization(
                extractCode(regionCode, 9),
                surveyData.getTownship(),
                LEVEL_TOWNSHIP,
                SOURCE_TOWNSHIP,
                county != null ? county.getId() : null,
                null,
                surveyData.getProvince(),
                surveyData.getCity(),
                surveyData.getCounty(),
                surveyData.getTownship(),
                null,
                year
        );
    }

    private Organization ensureOrganization(
            String code,
            String name,
            int level,
            String source,
            Organization parent,
            String provinceName,
            String cityName,
            String countyName,
            String townshipName,
            String communityName
    ) {
        return ensureOrganization(code, name, level, source, parent, provinceName, cityName, countyName, townshipName, communityName, null);
    }

    private Organization ensureOrganization(
            String code,
            String name,
            int level,
            String source,
            Organization parent,
            String provinceName,
            String cityName,
            String countyName,
            String townshipName,
            String communityName,
            Integer year
    ) {
        if (!StringUtils.hasText(code) || !StringUtils.hasText(name)) {
            return parent;
        }

        String normalizedCode = code.trim();
        String normalizedName = name.trim();
        Long parentId = parent != null ? parent.getId() : null;

        // 增量存储逻辑
        if (year != null && year != 2020) {
            // 非基准年：先查询当年变更记录
            QueryWrapper<Organization> yearQuery = new QueryWrapper<>();
            yearQuery.eq("code", normalizedCode);
            yearQuery.eq("year", year);
            yearQuery.eq("is_baseline", 0);
            Organization yearOrg = getOne(yearQuery, false);

            // 查询基准记录
            QueryWrapper<Organization> baselineQuery = new QueryWrapper<>();
            baselineQuery.eq("code", normalizedCode);
            baselineQuery.eq("is_baseline", 1);
            Organization baselineOrg = getOne(baselineQuery, false);

            // 检查是否有业务变更
            boolean hasChange = hasBusinessChange(
                    normalizedName, provinceName, cityName, countyName, townshipName, communityName,
                    baselineOrg
            );

            if (yearOrg != null) {
                // 当年变更记录已存在，更新它
                updateOrganizationFields(yearOrg, normalizedName, parentId, source,
                        provinceName, cityName, countyName, townshipName, communityName);
                updateById(yearOrg);
                return yearOrg;
            } else if (hasChange && baselineOrg != null) {
                // 有变更且基准记录存在，创建当年变更记录
                Organization newYearOrg = new Organization();
                newYearOrg.setCode(normalizedCode);
                newYearOrg.setName(normalizedName);
                newYearOrg.setLevel(level);
                newYearOrg.setYear(year);
                newYearOrg.setIsBaseline(0);
                newYearOrg.setBaselineCode(normalizedCode);
                newYearOrg.setParentId(parentId);
                newYearOrg.setDataSource(source);
                newYearOrg.setProvinceName(StringUtils.hasText(provinceName) ? provinceName.trim() : null);
                newYearOrg.setCityName(StringUtils.hasText(cityName) ? cityName.trim() : null);
                newYearOrg.setCountyName(StringUtils.hasText(countyName) ? countyName.trim() : null);
                newYearOrg.setTownshipName(StringUtils.hasText(townshipName) ? townshipName.trim() : null);
                newYearOrg.setCommunityName(StringUtils.hasText(communityName) ? communityName.trim() : null);
                save(newYearOrg);
                log.debug("新增年度变更记录: code={}, year={}", normalizedCode, year);
                return newYearOrg;
            } else if (baselineOrg != null) {
                // 无变更，返回基准记录
                return baselineOrg;
            }
        }

        // 基准年（2020年）或无年份参数：创建/更新基准记录
        QueryWrapper<Organization> query = new QueryWrapper<>();
        query.eq("code", normalizedCode);
        if (year != null) {
            query.eq("year", year);
        }
        Organization organization = getOne(query, false);

        if (organization == null) {
            // 创建新记录（基准记录）
            organization = new Organization();
            organization.setCode(normalizedCode);
            organization.setName(normalizedName);
            organization.setLevel(level);
            organization.setYear(year);
            organization.setIsBaseline(year != null && year == 2020 ? 1 : 0);
            organization.setBaselineCode(year != null && year == 2020 ? normalizedCode : null);
            organization.setParentId(parentId);
            organization.setDataSource(source);
            organization.setProvinceName(StringUtils.hasText(provinceName) ? provinceName.trim() : null);
            organization.setCityName(StringUtils.hasText(cityName) ? cityName.trim() : null);
            organization.setCountyName(StringUtils.hasText(countyName) ? countyName.trim() : null);
            organization.setTownshipName(StringUtils.hasText(townshipName) ? townshipName.trim() : null);
            organization.setCommunityName(StringUtils.hasText(communityName) ? communityName.trim() : null);
            save(organization);
            log.debug("新增组织机构: code={}, name={}, level={}, year={}", normalizedCode, normalizedName, level, year);
        } else {
            // 更新现有记录
            boolean needUpdate = false;
            Organization updateOrg = new Organization();
            updateOrg.setId(organization.getId());

            if (normalizedCode.equals(organization.getName()) && !normalizedCode.equals(normalizedName)) {
                updateOrg.setName(normalizedName);
                needUpdate = true;
            }
            if (StringUtils.hasText(provinceName) && !StringUtils.hasText(organization.getProvinceName())) {
                updateOrg.setProvinceName(provinceName.trim());
                needUpdate = true;
            }
            if (StringUtils.hasText(cityName) && !StringUtils.hasText(organization.getCityName())) {
                updateOrg.setCityName(cityName.trim());
                needUpdate = true;
            }
            if (StringUtils.hasText(countyName) && !StringUtils.hasText(organization.getCountyName())) {
                updateOrg.setCountyName(countyName.trim());
                needUpdate = true;
            }
            if (StringUtils.hasText(townshipName) && !StringUtils.hasText(organization.getTownshipName())) {
                updateOrg.setTownshipName(townshipName.trim());
                needUpdate = true;
            }
            if (StringUtils.hasText(communityName) && !StringUtils.hasText(organization.getCommunityName())) {
                updateOrg.setCommunityName(communityName.trim());
                needUpdate = true;
            }
            if (parentId != null && !parentId.equals(organization.getParentId())) {
                updateOrg.setParentId(parentId);
                needUpdate = true;
            }

            if (needUpdate) {
                updateById(updateOrg);
                organization = getById(organization.getId());
            }
        }
        return organization;
    }

    /**
     * 检查是否有业务字段变更
     */
    private boolean hasBusinessChange(String name, String provinceName, String cityName,
                                      String countyName, String townshipName, String communityName,
                                      Organization baseline) {
        if (baseline == null) {
            return true; // 无基准记录，视为有变更
        }

        // 比较业务字段
        if (!Objects.equals(normalizeText(name), normalizeText(baseline.getName()))) {
            return true;
        }
        if (!Objects.equals(normalizeText(provinceName), normalizeText(baseline.getProvinceName()))) {
            return true;
        }
        if (!Objects.equals(normalizeText(cityName), normalizeText(baseline.getCityName()))) {
            return true;
        }
        if (!Objects.equals(normalizeText(countyName), normalizeText(baseline.getCountyName()))) {
            return true;
        }
        if (!Objects.equals(normalizeText(townshipName), normalizeText(baseline.getTownshipName()))) {
            return true;
        }
        if (!Objects.equals(normalizeText(communityName), normalizeText(baseline.getCommunityName()))) {
            return true;
        }

        return false;
    }

    /**
     * 更新组织机构字段
     */
    private void updateOrganizationFields(Organization org, String name, Long parentId,
                                          String source, String provinceName, String cityName,
                                          String countyName, String townshipName, String communityName) {
        org.setName(name);
        org.setParentId(parentId);
        org.setDataSource(source);
        org.setProvinceName(StringUtils.hasText(provinceName) ? provinceName.trim() : null);
        org.setCityName(StringUtils.hasText(cityName) ? cityName.trim() : null);
        org.setCountyName(StringUtils.hasText(countyName) ? countyName.trim() : null);
        org.setTownshipName(StringUtils.hasText(townshipName) ? townshipName.trim() : null);
        org.setCommunityName(StringUtils.hasText(communityName) ? communityName.trim() : null);
    }

    /**
     * 确保基层组织机构存在（乡镇和社区级别）
     * 只在不存在时插入，存在则直接返回（不更新）
     * @param code 机构代码
     * @param name 机构名称
     * @param level 级别（4=乡镇，5=社区）
     * @param source 数据来源
     * @param countyId 所属区县ID
     * @param parentId 父级ID
     * @param provinceName 省名称
     * @param cityName 市名称
     * @param countyName 区县名称
     * @param townshipName 乡镇名称
     * @param communityName 社区名称
     * @param year 年份
     * @return GrassrootsOrganization
     */
    private GrassrootsOrganization ensureGrassrootsOrganization(
            String code,
            String name,
            int level,
            String source,
            Long countyId,
            Long parentId,
            String provinceName,
            String cityName,
            String countyName,
            String townshipName,
            String communityName,
            Integer year
    ) {
        if (!StringUtils.hasText(code) || !StringUtils.hasText(name)) {
            return null;
        }

        String normalizedCode = code.trim();
        String normalizedName = name.trim();

        // 检查是否已存在（优先按代码和年份查询）
        QueryWrapper<GrassrootsOrganization> query = new QueryWrapper<>();
        query.eq("code", normalizedCode);
        if (year != null) {
            query.eq("year", year);
        }
        GrassrootsOrganization existing = grassrootsOrganizationMapper.selectOne(query);

        if (existing != null) {
            // 已存在，但需要更新可能缺失的关联字段
            boolean needUpdate = false;
            if (parentId != null && !parentId.equals(existing.getParentId())) {
                existing.setParentId(parentId);
                needUpdate = true;
            }
            if (countyId != null && !countyId.equals(existing.getCountyId())) {
                existing.setCountyId(countyId);
                needUpdate = true;
            }
            // 确保 is_baseline 字段正确设置
            int expectedIsBaseline = year != null ? 0 : 1;
            if (existing.getIsBaseline() == null || existing.getIsBaseline() != expectedIsBaseline) {
                existing.setIsBaseline(expectedIsBaseline);
                needUpdate = true;
            }
            if (needUpdate) {
                grassrootsOrganizationMapper.updateById(existing);
                log.debug("更新基层组织机构关联字段: code={}, parentId={}, countyId={}, isBaseline={}", normalizedCode, parentId, countyId, expectedIsBaseline);
            }
            return existing;
        }

        // 不存在，插入新记录
        GrassrootsOrganization org = new GrassrootsOrganization();
        org.setCode(normalizedCode);
        org.setName(normalizedName);
        org.setLevel(level);
        org.setYear(year);
        org.setDataSource(source);
        org.setCountyId(countyId);
        org.setParentId(parentId);
        // 设置 is_baseline：有年份则为年度数据(0)，否则为基准数据(1)
        org.setIsBaseline(year != null ? 0 : 1);
        org.setProvinceName(StringUtils.hasText(provinceName) ? provinceName.trim() : null);
        org.setCityName(StringUtils.hasText(cityName) ? cityName.trim() : null);
        org.setCountyName(StringUtils.hasText(countyName) ? countyName.trim() : null);
        org.setTownshipName(StringUtils.hasText(townshipName) ? townshipName.trim() : null);
        org.setCommunityName(StringUtils.hasText(communityName) ? communityName.trim() : null);

        try {
            grassrootsOrganizationMapper.insert(org);
            log.info("新增基层组织机构: code={}, name={}, level={}, year={}, countyId={}, parentId={}",
                normalizedCode, normalizedName, level, year, countyId, parentId);
        } catch (Exception e) {
            log.error("插入基层组织机构失败: code={}, name={}, level={}, year={}, countyId={}, parentId={}, error={}",
                normalizedCode, normalizedName, level, year, countyId, parentId, e.getMessage(), e);
            // 插入失败时，尝试重新查询（可能是并发插入）
            try {
                existing = grassrootsOrganizationMapper.selectOne(query);
                if (existing != null) {
                    log.info("插入失败但查询到已有记录: code={}", normalizedCode);
                    return existing;
                }
            } catch (Exception queryException) {
                log.error("重新查询也失败: code={}", normalizedCode, queryException);
            }
            // 返回null表示插入失败
            return null;
        }
        return org;
    }

    private String extractCode(String regionCode, int length) {
        if (!StringUtils.hasText(regionCode)) {
            return null;
        }
        String normalized = regionCode.trim();
        if (normalized.length() < length) {
            return null;
        }
        return normalized.substring(0, length);
    }

    @Override
    public Map<String, Object> getOrganizationList(Integer page, Integer size, String code, String name, Integer level, Long parentId) {
        Map<String, Object> result = new HashMap<>();

        try {
            Page<Organization> pageParam = new Page<>(page, size);
            QueryWrapper<Organization> queryWrapper = new QueryWrapper<>();

            // 权限过滤
            List<String> allowedCodes = getCurrentUserAllowedOrgCodes();
            if (allowedCodes != null) {
                if (allowedCodes.isEmpty()) {
                    result.put("success", true);
                    result.put("data", new ArrayList<>());
                    result.put("total", 0L);
                    result.put("page", page);
                    result.put("size", size);
                    result.put("pages", 0L);
                    return result;
                }
                queryWrapper.and(wrapper -> {
                    for (String allowedCode : allowedCodes) {
                        wrapper.or().likeRight("code", allowedCode);
                    }
                });
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

            queryWrapper.orderByAsc("level", "code");

            IPage<Organization> pageResult = page(pageParam, queryWrapper);

            result.put("success", true);
            result.put("data", pageResult.getRecords());
            result.put("total", pageResult.getTotal());
            result.put("page", page);
            result.put("size", size);
            result.put("pages", pageResult.getPages());

        } catch (Exception e) {
            log.error("查询组织机构列表失败", e);
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public Organization getByCode(String code) {
        if (!StringUtils.hasText(code)) {
            return null;
        }
        QueryWrapper<Organization> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("code", code.trim());
        return getOne(queryWrapper, false);
    }

    @Override
    public List<Map<String, Object>> getOrganizationTree(Long parentId, Integer maxLevel, Integer year) {
        try {
            // 增量存储查询逻辑：当年份记录 + 基准记录
            List<Organization> allOrganizations = getOrganizationsWithBaseline(parentId, maxLevel, year);

            // When parentId is null, start building from root (parent_id = 0 or null)
            // When parentId is provided, start building from that parent
            return buildTree(allOrganizations, parentId);
        } catch (Exception e) {
            log.error("获取组织机构树形结构失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取组织机构（含增量存储逻辑）
     * 查询策略：
     * 1. 如果指定了年份，从该年份开始逐级向下查找（2026->2025->2024...->2020）
     * 2. 直到找到有数据的年份或到达基准年2020
     * 3. 合并基准数据和找到的年份数据
     */
    private List<Organization> getOrganizationsWithBaseline(Long parentId, Integer maxLevel, Integer year) {
        // 查找有效的数据年份（逐级回退）
        final Integer effectiveYear = (year != null) ? findEffectiveYear(year, parentId, maxLevel) : null;
        if (year != null) {
            log.info("查询年份: {}, 实际使用数据年份: {}", year, effectiveYear);
        }

        QueryWrapper<Organization> queryWrapper = new QueryWrapper<>();

        // 权限过滤
        List<String> allowedCodes = getCurrentUserAllowedOrgCodes();
        if (allowedCodes != null) {
            if (allowedCodes.isEmpty()) {
                return new ArrayList<>();
            }
            queryWrapper.and(wrapper -> {
                for (String allowedCode : allowedCodes) {
                    wrapper.or().likeRight("code", allowedCode);
                }
            });
        }

        // 父节点过滤
        if (parentId != null) {
            Organization parent = getById(parentId);
            if (parent != null) {
                queryWrapper.likeRight("code", parent.getCode());
            }
        }

        // 层级过滤
        if (maxLevel != null) {
            queryWrapper.le("level", maxLevel);
        }

        // 年份/基准过滤逻辑
        if (effectiveYear != null) {
            // 查询找到的年份的变更记录 OR 基准记录
            queryWrapper.and(wrapper -> wrapper
                    .and(w -> w.eq("year", effectiveYear).eq("is_baseline", 0))
                    .or()
                    .eq("is_baseline", 1)
            );
        } else {
            // 没有指定年份，只返回基准记录
            queryWrapper.eq("is_baseline", 1);
        }

        queryWrapper.orderByAsc("level", "code");

        List<Organization> organizations = list(queryWrapper);

        // 如果有年份参数，需要合并数据（年份数据记录覆盖基准记录）
        if (effectiveYear != null && !organizations.isEmpty()) {
            organizations = mergeBaselineWithYearData(organizations, effectiveYear);
        }

        return organizations;
    }

    /**
     * 查找有效的数据年份（逐级回退）
     * 从指定年份开始向下查找，直到找到有数据的年份或到达基准年2020
     */
    private Integer findEffectiveYear(Integer year, Long parentId, Integer maxLevel) {
        if (year == null) {
            return null;
        }

        // 从指定年份开始向下查找
        for (int checkYear = year; checkYear >= 2020; checkYear--) {
            if (hasDataForYear(checkYear, parentId, maxLevel)) {
                return checkYear;
            }
        }

        // 如果都找不到，返回基准年
        return 2020;
    }

    /**
     * 检查指定年份是否有数据
     */
    private boolean hasDataForYear(Integer year, Long parentId, Integer maxLevel) {
        QueryWrapper<Organization> checkWrapper = new QueryWrapper<>();

        // 权限过滤
        List<String> allowedCodes = getCurrentUserAllowedOrgCodes();
        if (allowedCodes != null) {
            if (allowedCodes.isEmpty()) {
                return false;
            }
            checkWrapper.and(wrapper -> {
                for (String allowedCode : allowedCodes) {
                    wrapper.or().likeRight("code", allowedCode);
                }
            });
        }

        // 父节点过滤
        if (parentId != null) {
            Organization parent = getById(parentId);
            if (parent != null) {
                checkWrapper.likeRight("code", parent.getCode());
            }
        }

        // 层级过滤
        if (maxLevel != null) {
            checkWrapper.le("level", maxLevel);
        }

        // 检查该年份是否有数据（包括变更记录或基准记录）
        checkWrapper.and(wrapper -> wrapper
                .and(w -> w.eq("year", year).eq("is_baseline", 0))
                .or()
                .and(w -> w.eq("year", year).eq("is_baseline", 1))
        );

        long count = count(checkWrapper);
        return count > 0;
    }

    /**
     * 合并基准数据和当年变更数据
     * 当年记录优先，相同code的基准记录被覆盖
     * 如果当年记录是删除标记（is_deleted=1），则不包含该组织
     */
    private List<Organization> mergeBaselineWithYearData(List<Organization> organizations, Integer year) {
        // 按code分组，当年记录优先
        Map<String, Organization> mergedMap = new LinkedHashMap<>();
        // 记录被删除的code
        Set<String> deletedCodes = new HashSet<>();

        for (Organization org : organizations) {
            String code = org.getCode();
            if (code == null) continue;

            Organization existing = mergedMap.get(code);

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
            // 如果已有当年记录，跳过基准记录
        }

        return new ArrayList<>(mergedMap.values());
    }

    /**
     * 根据年份过滤组织机构（只保留该年份有数据的组织）
     */
    private List<Organization> filterOrganizationsByYear(List<Organization> organizations, Integer year) {
        if (organizations == null || organizations.isEmpty()) {
            return organizations;
        }

        List<Organization> deduped = deduplicateOrganizationsByCode(organizations);

        Set<String> dataCodes = new HashSet<>();
        List<Object> surveyCodes = surveyDataMapper.selectObjs(new QueryWrapper<SurveyData>()
                .select("DISTINCT region_code")
                .eq("year", year)
                .isNotNull("region_code"));
        for (Object code : surveyCodes) {
            if (code != null && StringUtils.hasText(code.toString())) {
                dataCodes.add(code.toString().trim());
            }
        }

        List<Object> communityCodes = communityDataMapper.selectObjs(new QueryWrapper<CommunityDisasterReductionCapacity>()
                .select("DISTINCT region_code")
                .eq("year", year)
                .isNotNull("region_code"));
        for (Object code : communityCodes) {
            if (code != null && StringUtils.hasText(code.toString())) {
                dataCodes.add(code.toString().trim());
            }
        }

        List<Object> medicalCodes = medicalInstitutionMapper.selectObjs(new QueryWrapper<MedicalInstitution>()
                .select("DISTINCT org_code")
                .eq("year", year)
                .isNotNull("org_code"));
        for (Object code : medicalCodes) {
            if (code != null && StringUtils.hasText(code.toString())) {
                dataCodes.add(code.toString().trim());
            }
        }

        List<Object> boundaryOrgIds = organizationBoundaryMapper.selectObjs(new QueryWrapper<OrganizationBoundary>()
                .select("DISTINCT organization_id")
                .eq("year", year)
                .isNotNull("organization_id"));
        if (boundaryOrgIds != null && !boundaryOrgIds.isEmpty()) {
            Map<Long, String> orgIdToCode = new HashMap<>();
            for (Organization org : deduped) {
                if (org == null || org.getId() == null || !StringUtils.hasText(org.getCode())) {
                    continue;
                }
                orgIdToCode.putIfAbsent(org.getId(), org.getCode().trim());
            }
            for (Object idObj : boundaryOrgIds) {
                if (idObj == null) {
                    continue;
                }
                Long orgId = null;
                if (idObj instanceof Number) {
                    orgId = ((Number) idObj).longValue();
                } else {
                    try {
                        orgId = Long.parseLong(idObj.toString());
                    } catch (Exception ignored) {
                    }
                }
                if (orgId == null) {
                    continue;
                }
                String code = orgIdToCode.get(orgId);
                if (StringUtils.hasText(code)) {
                    dataCodes.add(code.trim());
                }
            }
        }

        if (dataCodes.isEmpty()) {
            log.info("年份过滤: year={} 无任何数据组织编码", year);
            return new ArrayList<>();
        }

        Map<Long, Organization> idMap = new HashMap<>();
        for (Organization org : deduped) {
            if (org.getId() != null) {
                idMap.putIfAbsent(org.getId(), org);
            }
        }

        Set<Long> keepIds = new HashSet<>();
        for (Organization org : deduped) {
            if (org.getId() == null || !StringUtils.hasText(org.getCode())) {
                continue;
            }
            if (!dataCodes.contains(org.getCode().trim())) {
                continue;
            }
            if (!keepIds.add(org.getId())) {
                continue;
            }

            Long pid = org.getParentId();
            while (pid != null && pid != 0L) {
                Organization parent = idMap.get(pid);
                if (parent == null || parent.getId() == null) {
                    break;
                }
                if (!keepIds.add(parent.getId())) {
                    break;
                }
                pid = parent.getParentId();
            }
        }

        List<Organization> filtered = new ArrayList<>();
        for (Organization org : deduped) {
            if (org.getId() != null && keepIds.contains(org.getId())) {
                filtered.add(org);
            }
        }

        log.info("年份过滤: year={} 从 {} 个组织过滤到 {} 个组织(含祖先)", year, organizations.size(), filtered.size());
        return filtered;
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

        // 1. 查询源年份的所有组织机构
        List<Organization> sourceOrgs = baseMapper.selectList(
                new QueryWrapper<Organization>().eq("year", sourceYear)
        );
        if (sourceOrgs == null || sourceOrgs.isEmpty()) {
            log.info("源年份 {} 没有组织机构数据", sourceYear);
            return 0;
        }

        // 2. 查询目标年份已存在的组织机构
        List<Organization> targetOrgs = baseMapper.selectList(
                new QueryWrapper<Organization>().eq("year", targetYear)
        );

        Set<String> existingCodes = new HashSet<>();
        Map<String, Organization> existingOrgs = new HashMap<>();
        if (targetOrgs != null) {
            for (Organization org : targetOrgs) {
                if (org.getCode() != null) {
                    existingCodes.add(org.getCode());
                    existingOrgs.put(org.getCode(), org);
                }
            }
        }

        // 3. 按层级排序（从高到低：省->市->县->乡->村），确保父节点先处理
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

        // 4. 创建映射：源ID -> 新组织
        Map<Long, Organization> oldIdToNewOrg = new HashMap<>();
        Map<String, Organization> codeToNewOrg = new HashMap<>();
        int copiedCount = 0;

        for (Organization src : sourceOrgs) {
            // 如果目标年份已存在相同code的组织，跳过
            if (existingCodes.contains(src.getCode())) {
                Organization existing = existingOrgs.get(src.getCode());
                if (existing != null) {
                    oldIdToNewOrg.put(src.getId(), existing);
                    codeToNewOrg.put(src.getCode(), existing);
                }
                continue;
            }

            // 插入新记录
            Organization targetOrg = new Organization();
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

            baseMapper.insert(targetOrg);
            copiedCount++;

            // 处理父节点ID：需要从新复制的组织中找到对应的父节点
            if (src.getParentId() != null) {
                Organization newParent = oldIdToNewOrg.get(src.getParentId());
                if (newParent != null && newParent.getId() != null) {
                    targetOrg.setParentId(newParent.getId());
                } else {
                    // 按code查找父节点（可能在目标年份已存在）
                    Organization srcParent = sourceOrgs.stream()
                            .filter(o -> o.getId().equals(src.getParentId()))
                            .findFirst().orElse(null);
                    if (srcParent != null) {
                        Organization newParentByCode = codeToNewOrg.get(srcParent.getCode());
                        if (newParentByCode != null) {
                            targetOrg.setParentId(newParentByCode.getId());
                        }
                    }
                }
            }

            // 更新parent_id
            if (targetOrg.getParentId() != null) {
                Organization updateOrg = new Organization();
                updateOrg.setId(targetOrg.getId());
                updateOrg.setParentId(targetOrg.getParentId());
                baseMapper.updateById(updateOrg);
            }

            oldIdToNewOrg.put(src.getId(), targetOrg);
            codeToNewOrg.put(src.getCode(), targetOrg);
        }

        log.info("从上一年复制组织机构：新增 {} 条（{} -> {}），跳过已存在 {} 条",
                copiedCount, sourceYear, targetYear, sourceOrgs.size() - copiedCount);

        // 5. 复制边界配置（使用新的组织ID）
        List<OrganizationBoundary> sourceBoundaries = organizationBoundaryMapper.selectList(
                new QueryWrapper<OrganizationBoundary>().eq("year", sourceYear)
        );

        if (sourceBoundaries != null && !sourceBoundaries.isEmpty()) {
            // 建立源code到新组织ID的映射（包括新复制的和已存在的）
            Map<String, Long> sourceCodeToNewId = new HashMap<>();
            for (Organization src : sourceOrgs) {
                Organization newOrg = codeToNewOrg.get(src.getCode());
                if (newOrg != null) {
                    sourceCodeToNewId.put(src.getCode(), newOrg.getId());
                }
            }

            // 查询源年份组织的code->id映射
            Map<Long, String> sourceIdToCode = sourceOrgs.stream()
                    .collect(Collectors.toMap(Organization::getId, Organization::getCode));

            List<OrganizationBoundary> toInsert = new ArrayList<>();
            for (OrganizationBoundary src : sourceBoundaries) {
                if (src.getOrganizationId() == null) {
                    continue;
                }
                String sourceCode = sourceIdToCode.get(src.getOrganizationId());
                if (sourceCode == null) {
                    continue;
                }
                Long newOrgId = sourceCodeToNewId.get(sourceCode);
                if (newOrgId == null) {
                    continue;
                }

                // 检查目标年份是否已存在该组织的边界
                Long existing = organizationBoundaryMapper.selectCount(
                        new QueryWrapper<OrganizationBoundary>()
                                .eq("year", targetYear)
                                .eq("organization_id", newOrgId)
                );
                if (existing != null && existing > 0) {
                    continue;
                }

                OrganizationBoundary copy = new OrganizationBoundary();
                copy.setOrganizationId(newOrgId);
                copy.setYear(targetYear);
                copy.setBoundaryCoordinates(src.getBoundaryCoordinates());
                copy.setFilePath(src.getFilePath());
                toInsert.add(copy);
            }

            for (OrganizationBoundary boundary : toInsert) {
                organizationBoundaryMapper.insert(boundary);
            }
            log.info("从上一年复制边界配置：{} 条（{} -> {}）", toInsert.size(), sourceYear, targetYear);
        }

        return copiedCount;
    }

    private List<Organization> deduplicateOrganizationsByCode(List<Organization> organizations) {
        if (organizations == null || organizations.isEmpty()) {
            return organizations;
        }

        Set<Long> allIds = organizations.stream()
                .map(Organization::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, Organization> byCode = new LinkedHashMap<>();
        int dropped = 0;

        for (Organization org : organizations) {
            if (org == null || !StringUtils.hasText(org.getCode())) {
                continue;
            }
            String code = org.getCode().trim();
            Organization existing = byCode.get(code);
            if (existing == null) {
                byCode.put(code, org);
                continue;
            }

            Organization chosen = chooseBetterOrganizationCandidate(existing, org, allIds);
            if (chosen != existing) {
                byCode.put(code, chosen);
            }
            dropped++;
        }

        if (dropped > 0) {
            log.warn("组织机构去重: 输入 {} 条，去重后 {} 条", organizations.size(), byCode.size());
        }

        return new ArrayList<>(byCode.values());
    }

    private Organization chooseBetterOrganizationCandidate(Organization a, Organization b, Set<Long> allIds) {
        boolean aParentOk = a.getParentId() == null || a.getParentId() == 0L || allIds.contains(a.getParentId());
        boolean bParentOk = b.getParentId() == null || b.getParentId() == 0L || allIds.contains(b.getParentId());
        if (aParentOk != bParentOk) {
            return bParentOk ? b : a;
        }

        boolean aNameOk = StringUtils.hasText(a.getName());
        boolean bNameOk = StringUtils.hasText(b.getName());
        if (aNameOk != bNameOk) {
            return bNameOk ? b : a;
        }

        if (a.getId() == null) return b;
        if (b.getId() == null) return a;
        return b.getId() < a.getId() ? b : a;
    }

    @Override
    public List<Organization> getChildrenByParentId(Long parentId) {
        try {
            QueryWrapper<Organization> queryWrapper = new QueryWrapper<>();
            if (parentId != null) {
                queryWrapper.eq("parent_id", parentId);
            } else {
                queryWrapper.isNull("parent_id").or().eq("parent_id", 0);
            }
            queryWrapper.orderByAsc("code");
            return list(queryWrapper);
        } catch (Exception e) {
            log.error("根据父级ID获取子级组织机构失败，parentId: {}", parentId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Organization> searchOrganization(String keyword, Integer level) {
        try {
            QueryWrapper<Organization> queryWrapper = new QueryWrapper<>();

            // 权限过滤
            List<String> allowedCodes = getCurrentUserAllowedOrgCodes();
            if (allowedCodes != null) {
                if (allowedCodes.isEmpty()) {
                    return new ArrayList<>();
                }
                queryWrapper.and(wrapper -> {
                    for (String allowedCode : allowedCodes) {
                        wrapper.or().likeRight("code", allowedCode);
                    }
                });
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
            log.error("搜索组织机构失败: keyword={}, level={}", keyword, level, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Organization> getOrganizationsByLevel(Integer level) {
        try {
            QueryWrapper<Organization> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("level", level);
            queryWrapper.orderByAsc("code");
            return list(queryWrapper);
        } catch (Exception e) {
            log.error("根据级别获取组织机构列表失败: level={}", level, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Organization> getCitiesByProvinceCode(String provinceCode) {
        try {
            QueryWrapper<Organization> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("level", LEVEL_CITY);
            if (StringUtils.hasText(provinceCode)) {
                queryWrapper.likeRight("code", provinceCode.trim());
            }
            queryWrapper.orderByAsc("code");
            return list(queryWrapper);
        } catch (Exception e) {
            log.error("根据省编码获取市级组织机构列表失败: provinceCode={}", provinceCode, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Organization> getCountiesByCityCode(String cityCode) {
        try {
            QueryWrapper<Organization> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("level", LEVEL_COUNTY);
            if (StringUtils.hasText(cityCode)) {
                queryWrapper.likeRight("code", cityCode.trim());
            }
            queryWrapper.orderByAsc("code");
            return list(queryWrapper);
        } catch (Exception e) {
            log.error("根据市编码获取县级组织机构列表失败: cityCode={}", cityCode, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Organization> getTownshipsByCountyCode(String countyCode) {
        try {
            QueryWrapper<Organization> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("level", LEVEL_TOWNSHIP);
            if (StringUtils.hasText(countyCode)) {
                queryWrapper.likeRight("code", countyCode.trim());
            }
            queryWrapper.orderByAsc("code");
            return list(queryWrapper);
        } catch (Exception e) {
            log.error("根据县编码获取乡镇级组织机构列表失败: countyCode={}", countyCode, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Organization> getCommunitiesByTownshipCode(String townshipCode) {
        try {
            QueryWrapper<Organization> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("level", LEVEL_COMMUNITY);
            if (StringUtils.hasText(townshipCode)) {
                queryWrapper.likeRight("code", townshipCode.trim());
            }
            queryWrapper.orderByAsc("code");
            return list(queryWrapper);
        } catch (Exception e) {
            log.error("根据乡镇编码获取社区级组织机构列表失败: townshipCode={}", townshipCode, e);
            return new ArrayList<>();
        }
    }

    /**
     * 构建树形结构
     */
    private List<Map<String, Object>> buildTree(List<Organization> organizations, Long parentId) {
        if (organizations == null || organizations.isEmpty()) {
            return new ArrayList<>();
        }

        List<Organization> deduped = deduplicateOrganizationsByCode(organizations);

        Map<Long, List<Organization>> parentMap = deduped.stream()
                .collect(Collectors.groupingBy(
                        org -> org.getParentId() != null ? org.getParentId() : 0L
                ));

        Set<String> emittedCodes = new HashSet<>();
        Set<Long> stack = new HashSet<>();

        if (parentId != null) {
            return buildTreeRecursive(parentId, parentMap, emittedCodes, stack);
        } else {
            // 如果 parentId 为 null，说明需要构建完整的树（或者当前可见范围的树）
            // 我们需要找到所有的"根"节点
            // 根节点是那些：1. parentId 为 0 或 null 的节点
            //              2. 或者 parentId 指向的节点不在 organizations 列表中的节点（孤儿节点）
            
            Set<Long> allIds = deduped.stream().map(Organization::getId).filter(Objects::nonNull).collect(Collectors.toSet());
            List<Map<String, Object>> result = new ArrayList<>();
            Set<Long> addedRootIds = new HashSet<>();
            
            // 1. 标准根节点
            if (parentMap.containsKey(0L)) {
                List<Organization> roots = parentMap.get(0L);
                if (roots != null) {
                    for (Organization root : roots) {
                        if (root != null && root.getId() != null) {
                            addedRootIds.add(root.getId());
                        }
                    }
                }
                result.addAll(buildTreeRecursive(0L, parentMap, emittedCodes, stack));
            }
            
            // 2. 查找孤儿节点作为根节点
            for (Map.Entry<Long, List<Organization>> entry : parentMap.entrySet()) {
                Long pid = entry.getKey();
                if (pid != 0L && !allIds.contains(pid)) {
                    // 这些节点的父节点不在列表中，所以它们是当前视图的根节点
                    // 我们不能直接调用 buildTreeRecursive(pid, parentMap)，因为它是找 pid 的子节点
                    // 这里 entry.getValue() 就是我们要找的根节点列表
                    List<Organization> roots = entry.getValue();
                    for (Organization root : roots) {
                        if (root == null || root.getId() == null) {
                            continue;
                        }
                        if (addedRootIds.contains(root.getId())) {
                            continue;
                        }
                        String normalizedCode = normalizeText(root.getCode());
                        if (!StringUtils.hasText(normalizedCode) || emittedCodes.contains(normalizedCode)) {
                            continue;
                        }
                        emittedCodes.add(normalizedCode);
                        addedRootIds.add(root.getId());
                        Map<String, Object> node = new HashMap<>();
                        node.put("id", root.getId());
                        node.put("parentId", root.getParentId());
                        node.put("code", normalizedCode);
                        node.put("name", normalizeText(root.getName()));
                        node.put("level", root.getLevel());
                        node.put("dataSource", normalizeText(root.getDataSource()));
                        node.put("provinceName", normalizeText(root.getProvinceName()));
                        node.put("cityName", normalizeText(root.getCityName()));
                        node.put("countyName", normalizeText(root.getCountyName()));
                        node.put("townshipName", normalizeText(root.getTownshipName()));
                        node.put("communityName", normalizeText(root.getCommunityName()));
                        
                        List<Map<String, Object>> childNodes = buildTreeRecursive(root.getId(), parentMap, emittedCodes, stack);
                        if (!childNodes.isEmpty()) {
                            node.put("children", childNodes);
                        }
                        result.add(node);
                    }
                }
            }
            
            // 排序
            Comparator<Integer> levelComparator = Comparator.nullsLast(Integer::compareTo);
            Comparator<String> codeComparator = Comparator.nullsLast(String::compareTo);
            result.sort(Comparator.comparing((Map<String, Object> m) -> (Integer) m.get("level"), levelComparator)
                    .thenComparing(m -> (String) m.get("code"), codeComparator));
            
            return result;
        }
    }

    /**
     * 递归构建树形结构
     */
    private List<Map<String, Object>> buildTreeRecursive(
            Long parentId,
            Map<Long, List<Organization>> parentMap,
            Set<String> emittedCodes,
            Set<Long> stack
    ) {
        if (parentId == null) {
            return new ArrayList<>();
        }
        if (!stack.add(parentId)) {
            return new ArrayList<>();
        }
        List<Organization> children = parentMap.get(parentId);
        if (children == null || children.isEmpty()) {
            stack.remove(parentId);
            return new ArrayList<>();
        }

        List<Map<String, Object>> result = children.stream().map(org -> {
            String normalizedCode = normalizeText(org.getCode());
            if (!StringUtils.hasText(normalizedCode) || emittedCodes.contains(normalizedCode)) {
                return null;
            }
            emittedCodes.add(normalizedCode);

            Map<String, Object> node = new HashMap<>();
            node.put("id", org.getId());
            node.put("parentId", org.getParentId());
            node.put("code", normalizedCode);
            node.put("name", normalizeText(org.getName()));
            node.put("level", org.getLevel());
            node.put("dataSource", normalizeText(org.getDataSource()));
            node.put("provinceName", normalizeText(org.getProvinceName()));
            node.put("cityName", normalizeText(org.getCityName()));
            node.put("countyName", normalizeText(org.getCountyName()));
            node.put("townshipName", normalizeText(org.getTownshipName()));
            node.put("communityName", normalizeText(org.getCommunityName()));

            List<Map<String, Object>> childNodes = buildTreeRecursive(org.getId(), parentMap, emittedCodes, stack);
            if (!childNodes.isEmpty()) {
                node.put("children", childNodes);
            }

            return node;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        stack.remove(parentId);
        return result;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createOrganization(Organization organization) {
        try {
            // 检查编码是否已存在
            QueryWrapper<Organization> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("code", organization.getCode());
            Organization existing = baseMapper.selectOne(queryWrapper);
            if (existing != null) {
                throw new RuntimeException("组织机构编码已存在: " + organization.getCode());
            }

            // 设置默认值
            if (organization.getLevel() == null) {
                organization.setLevel(1);
            }

            return save(organization);
        } catch (Exception e) {
            log.error("创建组织机构失败: {}", organization.getCode(), e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrganization(Organization organization) {
        try {
            // 检查是否存在
            Organization existing = getById(organization.getId());
            if (existing == null) {
                throw new RuntimeException("组织机构不存在: " + organization.getId());
            }

            // 检查是否是基准记录
            boolean isBaseline = existing.getIsBaseline() != null && existing.getIsBaseline() == 1;
            Integer updateYear = organization.getYear();

            // 如果更新基准记录（2020年或其他基准年），允许直接更新
            if (updateYear == null || (isBaseline && updateYear.equals(existing.getYear()))) {
                // 如果修改了编码，检查新编码是否已被其他记录使用
                if (!existing.getCode().equals(organization.getCode())) {
                    QueryWrapper<Organization> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("code", organization.getCode());
                    queryWrapper.ne("id", organization.getId());
                    Organization codeExisting = baseMapper.selectOne(queryWrapper);
                    if (codeExisting != null) {
                        throw new RuntimeException("组织机构编码已存在: " + organization.getCode());
                    }
                }
                return updateById(organization);
            }

            // 更新非基准年数据：创建或更新年度变更记录，不修改基准记录
            if (updateYear != null && !updateYear.equals(existing.getYear())) {
                // 检查是否已存在该年度的变更记录
                QueryWrapper<Organization> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("code", existing.getCode());
                queryWrapper.eq("year", updateYear);
                Organization yearRecord = baseMapper.selectOne(queryWrapper);

                if (yearRecord != null) {
                    // 更新现有年度记录
                    organization.setId(yearRecord.getId());
                    organization.setIsBaseline(0);
                    organization.setBaselineCode(existing.getCode());
                    return updateById(organization);
                } else {
                    // 创建新的年度变更记录
                    Organization newYearOrg = new Organization();
                    newYearOrg.setCode(existing.getCode());
                    newYearOrg.setName(organization.getName() != null ? organization.getName() : existing.getName());
                    newYearOrg.setLevel(existing.getLevel());
                    newYearOrg.setParentId(organization.getParentId() != null ? organization.getParentId() : existing.getParentId());
                    newYearOrg.setYear(updateYear);
                    newYearOrg.setIsBaseline(0);
                    newYearOrg.setBaselineCode(existing.getCode());
                    newYearOrg.setDataSource(organization.getDataSource() != null ? organization.getDataSource() : existing.getDataSource());
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
            log.error("更新组织机构失败: {}", organization.getId(), e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteOrganization(Long id) {
        try {
            return cascadeDeleteOrganizations(Collections.singletonList(id));
        } catch (Exception e) {
            log.error("删除组织机构失败: {}", id, e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteOrganizations(List<Long> ids) {
        try {
            return cascadeDeleteOrganizations(ids);
        } catch (Exception e) {
            log.error("批量删除组织机构失败: {}", ids, e);
            throw e;
        }
    }

    private boolean cascadeDeleteOrganizations(List<Long> rootIds) {
        if (rootIds == null || rootIds.isEmpty()) {
            return true;
        }

        List<Organization> roots = listByIds(rootIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (roots.isEmpty()) {
            return true;
        }

        List<String> prefixes = roots.stream()
                .map(Organization::getCode)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());

        if (prefixes.isEmpty()) {
            removeByIds(rootIds);
            return true;
        }

        QueryWrapper<Organization> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(wrapper -> {
            for (String prefix : prefixes) {
                wrapper.or().likeRight("code", prefix);
            }
        });

        List<Organization> organizations = list(queryWrapper);
        if (organizations == null || organizations.isEmpty()) {
            return true;
        }

        Set<Long> deleteIds = organizations.stream()
                .map(Organization::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (deleteIds.isEmpty()) {
            return true;
        }

        List<Long> deleteIdList = new ArrayList<>(deleteIds);

        roleOrganizationMapper.deleteByOrgIds(deleteIdList);
        userOrganizationMapper.deleteByOrgIds(deleteIdList);
        organizationBoundaryMapper.delete(new QueryWrapper<OrganizationBoundary>().in("organization_id", deleteIdList));

        Map<Integer, List<Long>> byLevel = new HashMap<>();
        int maxLevel = 0;
        for (Organization org : organizations) {
            if (org == null || org.getId() == null) {
                continue;
            }
            int level = org.getLevel() == null ? 0 : org.getLevel();
            maxLevel = Math.max(maxLevel, level);
            byLevel.computeIfAbsent(level, k -> new ArrayList<>()).add(org.getId());
        }

        for (int level = maxLevel; level >= 0; level--) {
            List<Long> idsAtLevel = byLevel.get(level);
            if (idsAtLevel == null || idsAtLevel.isEmpty()) {
                continue;
            }
            removeByIds(idsAtLevel);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int importFromExcel(List<com.evaluate.dto.OrganizationImportDTO> importList) {
        if (importList == null || importList.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (com.evaluate.dto.OrganizationImportDTO dto : importList) {
            try {
                String address = dto.getAddress();
                String regionCode = dto.getRegionCode();

                if (!StringUtils.hasText(regionCode)) {
                    log.warn("行政区划代码为空，跳过: {}", address);
                    continue;
                }

                // 从地址中解析省、市、县、乡镇、社区
                String provinceName = null;
                String cityName = null;
                String countyName = null;
                String townshipName = null;
                String communityName = null;

                if (StringUtils.hasText(address)) {
                    // 地址格式：四川省眉山市仁寿县慈航镇观音社区振兴大道西路129号
                    String[] parts = address.split("[省市区县镇村社区]");
                    for (String part : parts) {
                        if (part.contains("自治")) {
                            // 处理自治区等特殊情况
                            continue;
                        }
                    }

                    // 简单解析：按省、市、县、镇、社区关键词分割
                    if (address.contains("省")) {
                        provinceName = address.substring(0, address.indexOf("省") + 1);
                        address = address.substring(address.indexOf("省") + 1);
                    }
                    if (address.contains("市")) {
                        cityName = address.substring(0, address.indexOf("市") + 1);
                        address = address.substring(address.indexOf("市") + 1);
                    }
                    if (address.contains("县")) {
                        countyName = address.substring(0, address.indexOf("县") + 1);
                        address = address.substring(address.indexOf("县") + 1);
                    }
                    if (address.contains("镇")) {
                        townshipName = address.substring(0, address.indexOf("镇") + 1);
                        address = address.substring(address.indexOf("镇") + 1);
                    }
                    // 社区名称可能在地址中，也可能从完整地址中截取
                    if (dto.getCommunityName() != null) {
                        communityName = dto.getCommunityName();
                    } else if (address.contains("社区") || address.contains("村")) {
                        int communityEnd = address.indexOf("社区") > 0 ? address.indexOf("社区") : address.indexOf("村");
                        if (communityEnd > 0) {
                            // 找到社区/村前面的起始位置
                            String temp = address.substring(0, communityEnd + 2);
                            // 社区/村名称通常是最后一个词
                            int lastSpace = temp.lastIndexOf(" ");
                            if (lastSpace < 0) {
                                lastSpace = temp.lastIndexOf("、");
                            }
                            if (lastSpace >= 0) {
                                communityName = temp.substring(lastSpace + 1);
                            } else {
                                communityName = temp;
                            }
                        }
                    }
                }

                // 确保行政区划代码规范化
                regionCode = regionCode.trim();

                // 根据行政区划代码长度构建各级组织
                // 12位代码：51（省）+5114（市）+511421（县）+511421109（乡镇）+511421109003（社区）

                // 省级（2位）
                String provinceCode = regionCode.length() >= 2 ? regionCode.substring(0, 2) : null;
                // 市级（4位）
                String cityCode = regionCode.length() >= 4 ? regionCode.substring(0, 4) : null;
                // 县级（6位）
                String countyCode = regionCode.length() >= 6 ? regionCode.substring(0, 6) : null;
                // 乡镇级（9位）
                String townshipCode = regionCode.length() >= 9 ? regionCode.substring(0, 9) : null;
                // 社区级（12位）
                String communityCode = regionCode;

                // 创建或获取省级组织
                Organization province = null;
                if (provinceCode != null && StringUtils.hasText(provinceName)) {
                    province = ensureOrganization(
                            provinceCode,
                            provinceName,
                            LEVEL_PROVINCE,
                            "IMPORT",
                            null,
                            provinceName,
                            null,
                            null,
                            null,
                            null
                    );
                }

                // 创建或获取市级组织
                Organization city = null;
                if (cityCode != null) {
                    if (StringUtils.hasText(cityName) && province != null) {
                        city = ensureOrganization(
                                cityCode,
                                cityName,
                                LEVEL_CITY,
                                "IMPORT",
                                province,
                                provinceName,
                                cityName,
                                null,
                                null,
                                null
                        );
                    } else {
                        // 如果没有市名称或province为null，尝试通过编码获取或创建
                        QueryWrapper<Organization> query = new QueryWrapper<>();
                        query.eq("code", cityCode);
                        city = getOne(query, false);
                        if (city == null && province != null) {
                            // 创建一个默认的市级组织
                            city = ensureOrganization(
                                    cityCode,
                                    cityCode, // 使用编码作为名称
                                    LEVEL_CITY,
                                    "IMPORT",
                                    province,
                                    provinceName,
                                    null,
                                    null,
                                    null,
                                    null
                            );
                        }
                    }
                }

                // 创建或获取县级组织
                Organization county = null;
                if (countyCode != null) {
                    if (StringUtils.hasText(countyName) && city != null) {
                        county = ensureOrganization(
                                countyCode,
                                countyName,
                                LEVEL_COUNTY,
                                "IMPORT",
                                city,
                                provinceName,
                                cityName,
                                countyName,
                                null,
                                null
                        );
                    } else {
                        // 如果没有县名称或city为null，尝试通过编码获取或创建
                        QueryWrapper<Organization> query = new QueryWrapper<>();
                        query.eq("code", countyCode);
                        county = getOne(query, false);
                        if (county == null && city != null) {
                            // 创建一个默认的县级组织
                            county = ensureOrganization(
                                    countyCode,
                                    countyCode, // 使用编码作为名称
                                    LEVEL_COUNTY,
                                    "IMPORT",
                                    city,
                                    provinceName,
                                    cityName,
                                    null,
                                    null,
                                    null
                            );
                        }
                    }
                }

                // 创建或获取乡镇级组织
                Organization township = null;
                if (townshipCode != null) {
                    if (StringUtils.hasText(townshipName) && county != null) {
                        township = ensureOrganization(
                                townshipCode,
                                townshipName,
                                LEVEL_TOWNSHIP,
                                "IMPORT",
                                county,
                                provinceName,
                                cityName,
                                countyName,
                                townshipName,
                                null
                        );
                    } else {
                        // 如果没有乡镇名称或county为null，尝试通过编码获取或创建
                        QueryWrapper<Organization> query = new QueryWrapper<>();
                        query.eq("code", townshipCode);
                        township = getOne(query, false);
                        if (township == null && county != null) {
                            // 创建一个默认的乡镇级组织
                            township = ensureOrganization(
                                    townshipCode,
                                    townshipCode, // 使用编码作为名称
                                    LEVEL_TOWNSHIP,
                                    "IMPORT",
                                    county,
                                    provinceName,
                                    cityName,
                                    countyName,
                                    null,
                                    null
                            );
                        }
                    }
                }

                // 创建或获取社区级组织
                if (regionCode != null) {
                    if (StringUtils.hasText(communityName) && township != null) {
                        ensureOrganization(
                                regionCode,
                                communityName,
                                LEVEL_COMMUNITY,
                                "IMPORT",
                                township,
                                provinceName,
                                cityName,
                                countyName,
                                townshipName,
                                communityName
                        );
                        count++;
                    } else {
                        // 如果没有社区名称或township为null，尝试通过编码获取或创建
                        QueryWrapper<Organization> query = new QueryWrapper<>();
                        query.eq("code", regionCode);
                        Organization existing = getOne(query, false);
                        if (existing == null && township != null) {
                            ensureOrganization(
                                    regionCode,
                                    regionCode, // 使用编码作为名称
                                    LEVEL_COMMUNITY,
                                    "IMPORT",
                                    township,
                                    provinceName,
                                    cityName,
                                    countyName,
                                    townshipName,
                                    null
                            );
                            count++;
                        } else if (existing != null) {
                            // 已存在，跳过
                        }
                    }
                }

            } catch (Exception e) {
                log.error("导入组织机构失败: address={}, regionCode={}", dto.getAddress(), dto.getRegionCode(), e);
            }
        }

        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int importFromExcel(List<com.evaluate.dto.OrganizationImportDTO> importList, Integer year) {
        if (year == null) {
            throw new IllegalArgumentException("年份参数不能为空");
        }

        int count = 0;
        for (com.evaluate.dto.OrganizationImportDTO dto : importList) {
            try {
                String address = dto.getAddress();
                String regionCode = dto.getRegionCode();

                if (!StringUtils.hasText(regionCode)) {
                    log.warn("行政区划代码为空，跳过: {}", address);
                    continue;
                }

                // 从地址中解析省、市、县、乡镇、社区
                String provinceName = null;
                String cityName = null;
                String countyName = null;
                String townshipName = null;
                String communityName = null;

                if (StringUtils.hasText(address)) {
                    // 解析省
                    int provIdx = address.indexOf("省");
                    if (provIdx >= 0) {
                        provinceName = address.substring(0, provIdx + 1);
                        address = address.substring(provIdx + 1);
                    }

                    // 解析市（地级市）
                    int cityIdx = address.indexOf("市");
                    if (cityIdx >= 0) {
                        cityName = address.substring(0, cityIdx + 1);
                        address = address.substring(cityIdx + 1);
                    }

                    // 解析区县（优先解析区，再解析县）
                    int districtIdx = address.indexOf("区");
                    int countyIdx = address.indexOf("县");
                    if (districtIdx >= 0 && (countyIdx < 0 || districtIdx < countyIdx)) {
                        countyName = address.substring(0, districtIdx + 1);
                        address = address.substring(districtIdx + 1);
                    } else if (countyIdx >= 0) {
                        countyName = address.substring(0, countyIdx + 1);
                        address = address.substring(countyIdx + 1);
                    }

                    // 解析乡镇街道（优先解析街道，再解析镇）
                    int streetIdx = address.indexOf("街道");
                    int townIdx = address.indexOf("镇");
                    if (streetIdx >= 0 && (townIdx < 0 || streetIdx < townIdx)) {
                        townshipName = address.substring(0, streetIdx + 2);
                        address = address.substring(streetIdx + 2);
                    } else if (townIdx >= 0) {
                        townshipName = address.substring(0, townIdx + 1);
                        address = address.substring(townIdx + 1);
                    }

                    // 解析社区村（优先解析社区，再解析村）
                    if (dto.getCommunityName() != null) {
                        communityName = dto.getCommunityName();
                    } else {
                        int commIdx = address.indexOf("社区");
                        int villIdx = address.indexOf("村");
                        if (commIdx >= 0 && (villIdx < 0 || commIdx < villIdx)) {
                            communityName = address.substring(0, commIdx + 2);
                        } else if (villIdx >= 0) {
                            communityName = address.substring(0, villIdx + 1);
                        }
                    }
                }

                // 确保行政区划代码规范化
                regionCode = regionCode.trim();

                // 根据行政区划代码长度构建各级组织（带年份）
                String provinceCode = regionCode.length() >= 2 ? regionCode.substring(0, 2) : null;
                String cityCode = regionCode.length() >= 4 ? regionCode.substring(0, 4) : null;
                String countyCode = regionCode.length() >= 6 ? regionCode.substring(0, 6) : null;
                String townshipCode = regionCode.length() >= 9 ? regionCode.substring(0, 9) : null;
                String communityCode = regionCode;

                // 创建或获取省级组织（带年份）
                Organization province = null;
                if (provinceCode != null && StringUtils.hasText(provinceName)) {
                    province = ensureOrganization(
                            provinceCode,
                            provinceName,
                            LEVEL_PROVINCE,
                            "IMPORT",
                            null,
                            provinceName,
                            null,
                            null,
                            null,
                            null,
                            year
                    );
                }

                // 创建或获取市级组织（带年份）
                Organization city = null;
                if (cityCode != null) {
                    if (StringUtils.hasText(cityName) && province != null) {
                        city = ensureOrganization(
                                cityCode,
                                cityName,
                                LEVEL_CITY,
                                "IMPORT",
                                province,
                                provinceName,
                                cityName,
                                null,
                                null,
                                null,
                                year
                        );
                    } else {
                        QueryWrapper<Organization> query = new QueryWrapper<>();
                        query.eq("code", cityCode).eq("year", year);
                        city = getOne(query, false);
                        if (city == null && province != null) {
                            city = ensureOrganization(
                                    cityCode,
                                    cityCode,
                                    LEVEL_CITY,
                                    "IMPORT",
                                    province,
                                    provinceName,
                                    null,
                                    null,
                                    null,
                                    null,
                                    year
                            );
                        }
                    }
                }

                // 创建或获取县级组织（带年份）
                Organization county = null;
                if (countyCode != null) {
                    if (StringUtils.hasText(countyName) && city != null) {
                        county = ensureOrganization(
                                countyCode,
                                countyName,
                                LEVEL_COUNTY,
                                "IMPORT",
                                city,
                                provinceName,
                                cityName,
                                countyName,
                                null,
                                null,
                                year
                        );
                    } else {
                        QueryWrapper<Organization> query = new QueryWrapper<>();
                        query.eq("code", countyCode).eq("year", year);
                        county = getOne(query, false);
                        if (county == null && city != null) {
                            county = ensureOrganization(
                                    countyCode,
                                    countyCode,
                                    LEVEL_COUNTY,
                                    "IMPORT",
                                    city,
                                    provinceName,
                                    cityName,
                                    null,
                                    null,
                                    null,
                                    year
                            );
                        }
                    }
                }

                // 创建或获取乡镇级组织（带年份）
                Organization township = null;
                if (townshipCode != null) {
                    if (StringUtils.hasText(townshipName) && county != null) {
                        township = ensureOrganization(
                                townshipCode,
                                townshipName,
                                LEVEL_TOWNSHIP,
                                "IMPORT",
                                county,
                                provinceName,
                                cityName,
                                countyName,
                                townshipName,
                                null,
                                year
                        );
                    } else {
                        QueryWrapper<Organization> query = new QueryWrapper<>();
                        query.eq("code", townshipCode).eq("year", year);
                        township = getOne(query, false);
                        if (township == null && county != null) {
                            township = ensureOrganization(
                                    townshipCode,
                                    townshipCode,
                                    LEVEL_TOWNSHIP,
                                    "IMPORT",
                                    county,
                                    provinceName,
                                    cityName,
                                    countyName,
                                    null,
                                    null,
                                    year
                            );
                        }
                    }
                }

                // 创建或获取社区级组织（带年份）
                if (regionCode != null) {
                    if (StringUtils.hasText(communityName) && township != null) {
                        ensureOrganization(
                                regionCode,
                                communityName,
                                LEVEL_COMMUNITY,
                                "IMPORT",
                                township,
                                provinceName,
                                cityName,
                                countyName,
                                townshipName,
                                communityName,
                                year
                        );
                        count++;
                    } else {
                        QueryWrapper<Organization> query = new QueryWrapper<>();
                        query.eq("code", regionCode).eq("year", year);
                        Organization existing = getOne(query, false);
                        if (existing == null && township != null) {
                            ensureOrganization(
                                    regionCode,
                                    regionCode,
                                    LEVEL_COMMUNITY,
                                    "IMPORT",
                                    township,
                                    provinceName,
                                    cityName,
                                    countyName,
                                    townshipName,
                                    null,
                                    year
                            );
                            count++;
                        }
                    }
                }

            } catch (Exception e) {
                log.error("导入组织机构失败: address={}, regionCode={}", dto.getAddress(), dto.getRegionCode(), e);
            }
        }

        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> deleteOrganizationYearData(Long organizationId, Integer year) {
        Map<String, Object> result = new HashMap<>();
        int totalDeleted = 0;

        // 获取组织机构
        Organization org = getById(organizationId);
        if (org == null) {
            throw new IllegalArgumentException("组织机构不存在: " + organizationId);
        }

        // 查询指定年份下的所有相关组织（包括自身和所有子组织）
        // 增量存储：查询当年变更记录 OR 基准记录
        QueryWrapper<Organization> queryWrapper = new QueryWrapper<>();
        queryWrapper.likeRight("code", org.getCode());
        if (year != null) {
            queryWrapper.and(wrapper -> wrapper
                    .and(w -> w.eq("year", year).ne("is_baseline", 1))
                    .or().eq("is_baseline", 1)
            );
        }
        queryWrapper.orderByAsc("code");
        List<Organization> allOrgs = list(queryWrapper);

        if (allOrgs == null || allOrgs.isEmpty()) {
            result.put("totalDeleted", 0);
            result.put("organizationDeleted", 0);
            result.put("boundaryDeleted", 0);
            result.put("organizationName", org.getName());
            result.put("year", year);
            result.put("organizationCount", 0);
            log.info("删除组织机构年度数据：组织={}，年份={}，未找到相关记录", org.getName(), year);
            return result;
        }

        // 合并基准数据和当年数据（获取实际需要删除的组织列表）
        List<Organization> orgsToDelete = allOrgs;
        if (year != null) {
            orgsToDelete = mergeBaselineWithYearData(allOrgs, year);
        }

        Set<Long> yearOrgIds = new HashSet<>();

        // 处理每个组织：创建删除标记或删除年度记录
        int organizationDeleted = 0;
        for (Organization targetOrg : orgsToDelete) {
            // 查找该年度的记录
            QueryWrapper<Organization> yearRecordQuery = new QueryWrapper<>();
            yearRecordQuery.eq("code", targetOrg.getCode());
            yearRecordQuery.eq("year", year);
            Organization yearRecord = baseMapper.selectOne(yearRecordQuery);

            if (yearRecord != null) {
                // 如果该年度有记录，标记为已删除
                yearRecord.setIsDeleted(1);
                updateById(yearRecord);
                yearOrgIds.add(yearRecord.getId());
                organizationDeleted++;
            } else {
                // 如果该年度没有记录，创建删除标记记录
                Organization deleteMarker = new Organization();
                deleteMarker.setCode(targetOrg.getCode());
                deleteMarker.setName(targetOrg.getName());
                deleteMarker.setLevel(targetOrg.getLevel());
                deleteMarker.setParentId(targetOrg.getParentId());
                deleteMarker.setYear(year);
                deleteMarker.setIsBaseline(0);
                deleteMarker.setBaselineCode(targetOrg.getCode());
                deleteMarker.setIsDeleted(1); // 标记为已删除
                deleteMarker.setDataSource(targetOrg.getDataSource());
                deleteMarker.setProvinceName(targetOrg.getProvinceName());
                deleteMarker.setCityName(targetOrg.getCityName());
                deleteMarker.setCountyName(targetOrg.getCountyName());
                deleteMarker.setTownshipName(targetOrg.getTownshipName());
                deleteMarker.setCommunityName(targetOrg.getCommunityName());
                save(deleteMarker);
                yearOrgIds.add(deleteMarker.getId());
                organizationDeleted++;
            }
        }
        totalDeleted += organizationDeleted;

        // 删除边界配置
        if (!yearOrgIds.isEmpty()) {
            QueryWrapper<OrganizationBoundary> boundaryQuery = new QueryWrapper<>();
            boundaryQuery.in("organization_id", yearOrgIds).eq("year", year);
            int boundaryDeleted = organizationBoundaryMapper.delete(boundaryQuery);
            totalDeleted += boundaryDeleted;
            result.put("boundaryDeleted", boundaryDeleted);
        } else {
            result.put("boundaryDeleted", 0);
        }

        result.put("totalDeleted", totalDeleted);
        result.put("organizationDeleted", organizationDeleted);
        result.put("organizationName", org.getName());
        result.put("year", year);
        result.put("organizationCount", orgsToDelete.size());

        log.info("删除组织机构年度数据完成：组织={} (含{}个子组织)，年份={}，共处理{}条记录（其中组织记录{}条，边界配置{}条）",
                org.getName(), orgsToDelete.size() - 1, year, totalDeleted, organizationDeleted, result.get("boundaryDeleted"));

        return result;
    }
}
