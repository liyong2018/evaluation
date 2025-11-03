package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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

import java.util.Objects;

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
            boolean updated = false;
            if (StringUtils.hasText(normalizedName) && !Objects.equals(organization.getName(), normalizedName)) {
                organization.setName(normalizedName);
                updated = true;
            }
            if (!Objects.equals(organization.getLevel(), level)) {
                organization.setLevel(level);
                updated = true;
            }
            if (!Objects.equals(organization.getParentId(), parentId)) {
                organization.setParentId(parentId);
                updated = true;
            }
            if (StringUtils.hasText(source) && !Objects.equals(organization.getDataSource(), source)) {
                organization.setDataSource(source);
                updated = true;
            }
            updated |= updateIfChanged(organization::setProvinceName, organization.getProvinceName(), provinceName);
            updated |= updateIfChanged(organization::setCityName, organization.getCityName(), cityName);
            updated |= updateIfChanged(organization::setCountyName, organization.getCountyName(), countyName);
            updated |= updateIfChanged(organization::setTownshipName, organization.getTownshipName(), townshipName);
            updated |= updateIfChanged(organization::setCommunityName, organization.getCommunityName(), communityName);

            if (updated) {
                updateById(organization);
                log.debug("更新组织机构: code={}, name={}, level={}", normalizedCode, normalizedName, level);
            }
        }
        return organization;
    }

    private boolean updateIfChanged(java.util.function.Consumer<String> setter, String currentValue, String newValue) {
        if (!StringUtils.hasText(newValue)) {
            return false;
        }
        String normalized = newValue.trim();
        if (!Objects.equals(currentValue, normalized)) {
            setter.accept(normalized);
            return true;
        }
        return false;
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
}
