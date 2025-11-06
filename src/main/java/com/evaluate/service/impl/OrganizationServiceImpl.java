package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluate.entity.CommunityDisasterReductionCapacity;
import com.evaluate.entity.Organization;
import com.evaluate.entity.SurveyData;
import com.evaluate.mapper.OrganizationMapper;
import com.evaluate.service.IOrganizationService;
import lombok.extern.slf4j.Slf4j;
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

    private static final String SOURCE_COMMUNITY = "COMMUNITY";
    private static final String SOURCE_TOWNSHIP = "TOWNSHIP";

    private static final int LEVEL_PROVINCE = 1;
    private static final int LEVEL_CITY = 2;
    private static final int LEVEL_COUNTY = 3;
    private static final int LEVEL_TOWNSHIP = 4;
    private static final int LEVEL_COMMUNITY = 5;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncFromCommunityData(CommunityDisasterReductionCapacity community) {
        if (community == null || !StringUtils.hasText(community.getRegionCode())) {
            return;
        }

        String regionCode = community.getRegionCode().trim();
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
                null
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
                null
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
                null
        );

        Organization township = ensureOrganization(
                extractCode(regionCode, 9),
                community.getTownshipName(),
                LEVEL_TOWNSHIP,
                SOURCE_COMMUNITY,
                county,
                community.getProvinceName(),
                community.getCityName(),
                community.getCountyName(),
                community.getTownshipName(),
                null
        );

        ensureOrganization(
                regionCode,
                community.getCommunityName(),
                LEVEL_COMMUNITY,
                SOURCE_COMMUNITY,
                township,
                community.getProvinceName(),
                community.getCityName(),
                community.getCountyName(),
                community.getTownshipName(),
                community.getCommunityName()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncFromSurveyData(SurveyData surveyData) {
        if (surveyData == null || !StringUtils.hasText(surveyData.getRegionCode())) {
            return;
        }

        String regionCode = surveyData.getRegionCode().trim();
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
                null
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
                null
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
                null
        );

        ensureOrganization(
                extractCode(regionCode, 9),
                surveyData.getTownship(),
                LEVEL_TOWNSHIP,
                SOURCE_TOWNSHIP,
                county,
                surveyData.getProvince(),
                surveyData.getCity(),
                surveyData.getCounty(),
                surveyData.getTownship(),
                null
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
        if (!StringUtils.hasText(code) || !StringUtils.hasText(name)) {
            return parent;
        }

        String normalizedCode = code.trim();
        String normalizedName = name.trim();

        QueryWrapper<Organization> query = new QueryWrapper<>();
        query.eq("code", normalizedCode);
        Organization organization = getOne(query, false);

        Long parentId = parent != null ? parent.getId() : null;
        if (organization == null) {
            // 组织机构不存在，创建新记录
            organization = new Organization();
            organization.setCode(normalizedCode);
            organization.setName(normalizedName);
            organization.setLevel(level);
            organization.setParentId(parentId);
            organization.setDataSource(source);
            organization.setProvinceName(StringUtils.hasText(provinceName) ? provinceName.trim() : null);
            organization.setCityName(StringUtils.hasText(cityName) ? cityName.trim() : null);
            organization.setCountyName(StringUtils.hasText(countyName) ? countyName.trim() : null);
            organization.setTownshipName(StringUtils.hasText(townshipName) ? townshipName.trim() : null);
            organization.setCommunityName(StringUtils.hasText(communityName) ? communityName.trim() : null);
            save(organization);
            log.debug("新增组织机构: code={}, name={}, level={}", normalizedCode, normalizedName, level);
        } else {
            // 组织机构已存在，跳过不做任何更新
            log.debug("组织机构已存在，跳过: code={}, name={}, level={}", normalizedCode, normalizedName, level);
        }
        return organization;
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
    public List<Map<String, Object>> getOrganizationTree(Long parentId, Integer maxLevel) {
        try {
            QueryWrapper<Organization> queryWrapper = new QueryWrapper<>();
            if (parentId != null) {
                queryWrapper.eq("parent_id", parentId);
            } else {
                queryWrapper.isNull("parent_id").or().eq("parent_id", 0);
            }
            if (maxLevel != null) {
                queryWrapper.le("level", maxLevel);
            }
            queryWrapper.orderByAsc("level", "code");

            List<Organization> allOrganizations = list(queryWrapper);
            return buildTree(allOrganizations, parentId);
        } catch (Exception e) {
            log.error("获取组织机构树形结构失败", e);
            return new ArrayList<>();
        }
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

        Map<Long, List<Organization>> parentMap = organizations.stream()
                .collect(Collectors.groupingBy(
                        org -> org.getParentId() != null ? org.getParentId() : 0L
                ));

        return buildTreeRecursive(parentId != null ? parentId : 0L, parentMap);
    }

    /**
     * 递归构建树形结构
     */
    private List<Map<String, Object>> buildTreeRecursive(Long parentId, Map<Long, List<Organization>> parentMap) {
        List<Organization> children = parentMap.get(parentId);
        if (children == null || children.isEmpty()) {
            return new ArrayList<>();
        }

        return children.stream().map(org -> {
            Map<String, Object> node = new HashMap<>();
            node.put("id", org.getId());
            node.put("parentId", org.getParentId());
            node.put("code", org.getCode());
            node.put("name", org.getName());
            node.put("level", org.getLevel());
            node.put("dataSource", org.getDataSource());
            node.put("provinceName", org.getProvinceName());
            node.put("cityName", org.getCityName());
            node.put("countyName", org.getCountyName());
            node.put("townshipName", org.getTownshipName());
            node.put("communityName", org.getCommunityName());

            List<Map<String, Object>> childNodes = buildTreeRecursive(org.getId(), parentMap);
            if (!childNodes.isEmpty()) {
                node.put("children", childNodes);
            }

            return node;
        }).collect(Collectors.toList());
    }
}
