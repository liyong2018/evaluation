package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.evaluate.entity.CommunityDisasterReductionCapacity;
import com.evaluate.service.ICommunityDisasterReductionCapacityService;
import com.evaluate.service.IVolunteerMilitiaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 志愿者和民兵预备役统计服务实现类
 *
 * @author System
 * @since 2025-01-01
 */
@Slf4j
@Service
public class VolunteerMilitiaServiceImpl implements IVolunteerMilitiaService {

    @Autowired
    private ICommunityDisasterReductionCapacityService communityService;

    @Override
    public Integer sumVolunteersByRegion(String regionCode, Integer year) {
        if (!StringUtils.hasText(regionCode) || year == null) {
            return 0;
        }

        try {
            // 获取该行政区划下的所有社区代码
            List<String> communityCodes = getCommunityCodesByRegion(regionCode);
            if (communityCodes.isEmpty()) {
                log.warn("未找到行政区划代码 {} 对应的社区", regionCode);
                return 0;
            }

            // 查询对应年份的社区数据
            QueryWrapper<CommunityDisasterReductionCapacity> wrapper = new QueryWrapper<>();
            wrapper.eq("year", year)
                    .in("region_code", communityCodes)
                    .isNotNull("registered_volunteer_count");

            List<CommunityDisasterReductionCapacity> communityData = communityService.list(wrapper);

            // 求和登记注册志愿者人数
            int totalVolunteers = communityData.stream()
                    .mapToInt(data -> data.getRegisteredVolunteerCount() != null ? data.getRegisteredVolunteerCount() : 0)
                    .sum();

            log.debug("行政区划 {} 在年份 {} 的志愿者总人数: {}", regionCode, year, totalVolunteers);
            return totalVolunteers;

        } catch (Exception e) {
            log.error("统计志愿者人数失败，行政区划: {}, 年份: {}", regionCode, year, e);
            return 0;
        }
    }

    @Override
    public Integer sumMilitiaReserveByRegion(String regionCode, Integer year) {
        if (!StringUtils.hasText(regionCode) || year == null) {
            return 0;
        }

        try {
            // 获取该行政区划下的所有社区代码
            List<String> communityCodes = getCommunityCodesByRegion(regionCode);
            if (communityCodes.isEmpty()) {
                log.warn("未找到行政区划代码 {} 对应的社区", regionCode);
                return 0;
            }

            // 查询对应年份的社区数据
            QueryWrapper<CommunityDisasterReductionCapacity> wrapper = new QueryWrapper<>();
            wrapper.eq("year", year)
                    .in("region_code", communityCodes)
                    .isNotNull("militia_reserve_count");

            List<CommunityDisasterReductionCapacity> communityData = communityService.list(wrapper);

            // 求和民兵预备役人数
            int totalMilitiaReserve = communityData.stream()
                    .mapToInt(data -> data.getMilitiaReserveCount() != null ? data.getMilitiaReserveCount() : 0)
                    .sum();

            log.debug("行政区划 {} 在年份 {} 的民兵预备役总人数: {}", regionCode, year, totalMilitiaReserve);
            return totalMilitiaReserve;

        } catch (Exception e) {
            log.error("统计民兵预备役人数失败，行政区划: {}, 年份: {}", regionCode, year, e);
            return 0;
        }
    }

    @Override
    public Integer sumVolunteersByCounty(String countyName, Integer year) {
        if (!StringUtils.hasText(countyName) || year == null) {
            return 0;
        }

        try {
            // 获取该县下的所有社区代码
            List<String> communityCodes = getCommunityCodesByCounty(countyName);
            if (communityCodes.isEmpty()) {
                log.warn("未找到县 {} 对应的社区", countyName);
                return 0;
            }

            // 查询对应年份的社区数据
            QueryWrapper<CommunityDisasterReductionCapacity> wrapper = new QueryWrapper<>();
            wrapper.eq("year", year)
                    .in("region_code", communityCodes)
                    .isNotNull("registered_volunteer_count");

            List<CommunityDisasterReductionCapacity> communityData = communityService.list(wrapper);

            // 求和登记注册志愿者人数
            int totalVolunteers = communityData.stream()
                    .mapToInt(data -> data.getRegisteredVolunteerCount() != null ? data.getRegisteredVolunteerCount() : 0)
                    .sum();

            log.debug("县 {} 在年份 {} 的志愿者总人数: {}", countyName, year, totalVolunteers);
            return totalVolunteers;

        } catch (Exception e) {
            log.error("统计县级志愿者人数失败，县名: {}, 年份: {}", countyName, year, e);
            return 0;
        }
    }

    @Override
    public Integer sumMilitiaReserveByCounty(String countyName, Integer year) {
        if (!StringUtils.hasText(countyName) || year == null) {
            return 0;
        }

        try {
            // 获取该县下的所有社区代码
            List<String> communityCodes = getCommunityCodesByCounty(countyName);
            if (communityCodes.isEmpty()) {
                log.warn("未找到县 {} 对应的社区", countyName);
                return 0;
            }

            // 查询对应年份的社区数据
            QueryWrapper<CommunityDisasterReductionCapacity> wrapper = new QueryWrapper<>();
            wrapper.eq("year", year)
                    .in("region_code", communityCodes)
                    .isNotNull("militia_reserve_count");

            List<CommunityDisasterReductionCapacity> communityData = communityService.list(wrapper);

            // 求和民兵预备役人数
            int totalMilitiaReserve = communityData.stream()
                    .mapToInt(data -> data.getMilitiaReserveCount() != null ? data.getMilitiaReserveCount() : 0)
                    .sum();

            log.debug("县 {} 在年份 {} 的民兵预备役总人数: {}", countyName, year, totalMilitiaReserve);
            return totalMilitiaReserve;

        } catch (Exception e) {
            log.error("统计县级民兵预备役人数失败，县名: {}, 年份: {}", countyName, year, e);
            return 0;
        }
    }

    @Override
    public Map<String, int[]> batchSumVolunteerMilitia(List<String> regionCodes, Integer year) {
        Map<String, int[]> result = new HashMap<>();

        if (regionCodes == null || regionCodes.isEmpty() || year == null) {
            return result;
        }

        try {
            for (String regionCode : regionCodes) {
                int volunteers = sumVolunteersByRegion(regionCode, year);
                int militiaReserve = sumMilitiaReserveByRegion(regionCode, year);
                result.put(regionCode, new int[]{volunteers, militiaReserve});
            }

            log.debug("批量统计完成，行政区划数量: {}, 年份: {}", regionCodes.size(), year);
        } catch (Exception e) {
            log.error("批量统计志愿者和民兵预备役人数失败", e);
        }

        return result;
    }

    @Override
    public List<String> getCommunityCodesByRegion(String regionCode) {
        if (!StringUtils.hasText(regionCode)) {
            return new ArrayList<>();
        }

        try {
            // 获取所有社区数据，然后匹配乡镇代码
            QueryWrapper<CommunityDisasterReductionCapacity> wrapper = new QueryWrapper<>();
            wrapper.isNotNull("region_code");

            List<CommunityDisasterReductionCapacity> allCommunities = communityService.list(wrapper);

            // 提取匹配的社区行政区划代码（去掉最后3位后匹配乡镇代码）
            List<String> communityCodes = allCommunities.stream()
                    .map(CommunityDisasterReductionCapacity::getRegionCode)
                    .filter(StringUtils::hasText)
                    .filter(code -> code.length() >= 3)
                    .filter(code -> regionCode.equals(code.substring(0, code.length() - 3)))
                    .distinct()
                    .collect(Collectors.toList());

            log.debug("乡镇代码 {} 对应的社区数量: {}", regionCode, communityCodes.size());
            return communityCodes;

        } catch (Exception e) {
            log.error("查询乡镇代码 {} 对应的社区失败", regionCode, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<String> getCommunityCodesByCounty(String countyName) {
        if (!StringUtils.hasText(countyName)) {
            return new ArrayList<>();
        }

        try {
            // 根据县名称查询社区防灾减灾能力数据
            QueryWrapper<CommunityDisasterReductionCapacity> wrapper = new QueryWrapper<>();
            wrapper.eq("county_name", countyName);

            List<CommunityDisasterReductionCapacity> communities = communityService.list(wrapper);

            // 提取社区的行政区划代码
            List<String> communityCodes = communities.stream()
                    .map(CommunityDisasterReductionCapacity::getRegionCode)
                    .distinct()
                    .collect(Collectors.toList());

            log.debug("县 {} 对应的社区数量: {}", countyName, communityCodes.size());
            return communityCodes;

        } catch (Exception e) {
            log.error("查询县 {} 对应的社区失败", countyName, e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean isValidRegionCode(String regionCode) {
        if (!StringUtils.hasText(regionCode)) {
            return false;
        }

        try {
            // 获取所有社区数据，然后验证乡镇代码
            QueryWrapper<CommunityDisasterReductionCapacity> wrapper = new QueryWrapper<>();
            wrapper.isNotNull("region_code");

            List<CommunityDisasterReductionCapacity> allCommunities = communityService.list(wrapper);

            // 检查是否有匹配的社区（去掉最后3位后匹配乡镇代码）
            boolean hasMatchingCommunity = allCommunities.stream()
                    .map(CommunityDisasterReductionCapacity::getRegionCode)
                    .filter(StringUtils::hasText)
                    .filter(code -> code.length() >= 3)
                    .anyMatch(code -> regionCode.equals(code.substring(0, code.length() - 3)));

            return hasMatchingCommunity;
        } catch (Exception e) {
            log.error("验证行政区划代码 {} 有效性失败", regionCode, e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getStatisticsSummary(Integer year) {
        Map<String, Object> summary = new HashMap<>();

        try {
            if (year == null) {
                year = java.time.LocalDate.now().getYear();
            }

            // 查询指定年份的所有社区数据
            QueryWrapper<CommunityDisasterReductionCapacity> wrapper = new QueryWrapper<>();
            wrapper.eq("year", year);

            List<CommunityDisasterReductionCapacity> allData = communityService.list(wrapper);

            // 统计数据
            int totalCommunities = allData.size();
            int totalVolunteers = allData.stream()
                    .mapToInt(data -> data.getRegisteredVolunteerCount() != null ? data.getRegisteredVolunteerCount() : 0)
                    .sum();
            int totalMilitiaReserve = allData.stream()
                    .mapToInt(data -> data.getMilitiaReserveCount() != null ? data.getMilitiaReserveCount() : 0)
                    .sum();

            // 获取唯一的地级市和县名称
            Set<String> cities = new HashSet<>();
            Set<String> counties = new HashSet<>();
            for (CommunityDisasterReductionCapacity data : allData) {
                if (StringUtils.hasText(data.getCityName())) {
                    cities.add(data.getCityName());
                }
                if (StringUtils.hasText(data.getCountyName())) {
                    counties.add(data.getCountyName());
                }
            }

            // 构建摘要信息
            summary.put("year", year);
            summary.put("totalCommunities", totalCommunities);
            summary.put("totalVolunteers", totalVolunteers);
            summary.put("totalMilitiaReserve", totalMilitiaReserve);
            summary.put("totalCities", cities.size());
            summary.put("totalCounties", counties.size());
            summary.put("cities", cities);
            summary.put("counties", counties);

            log.info("获取年份 {} 的统计数据摘要完成", year);

        } catch (Exception e) {
            log.error("获取统计数据摘要失败，年份: {}", year, e);
        }

        return summary;
    }
}