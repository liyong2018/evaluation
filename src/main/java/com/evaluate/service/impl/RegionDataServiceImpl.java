package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.evaluate.mapper.SurveyDataMapper;
import com.evaluate.mapper.CommunityDisasterReductionCapacityMapper;
import com.evaluate.service.IRegionDataService;
import com.evaluate.entity.SurveyData;
import com.evaluate.entity.CommunityDisasterReductionCapacity;
import com.evaluate.entity.User;
import com.evaluate.entity.Role;
import com.evaluate.entity.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RegionDataServiceImpl implements IRegionDataService {

    @Autowired
    private SurveyDataMapper surveyDataMapper;

    @Autowired
    private CommunityDisasterReductionCapacityMapper communityCapacityMapper;

    @Autowired
    private com.evaluate.mapper.UserMapper userMapper;

    @Autowired
    private com.evaluate.mapper.RoleMapper roleMapper;

    @Autowired
    private com.evaluate.mapper.RoleOrganizationMapper roleOrganizationMapper;

    @Autowired
    private com.evaluate.mapper.UserOrganizationMapper userOrganizationMapper;

    @Autowired
    private com.evaluate.mapper.OrganizationMapper organizationMapper;

    /**
     * 获取当前用户有权限的组织机构列表
     * @return 组织机构列表，如果是管理员返回null
     */
    private List<Organization> getCurrentUserAllowedOrgs() {
        try {
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                return null;
            }
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username = null;
            if (principal instanceof UserDetails) {
                username = ((UserDetails) principal).getUsername();
            } else if (principal instanceof String) {
                username = (String) principal;
            }

            if (username == null) return new ArrayList<>();

            User user = userMapper.selectUserByUsername(username);
            if (user == null) return new ArrayList<>();

            // Special check for 'admin' username to ensure they always have full access
            if ("admin".equals(user.getUsername())) {
                return null;
            }

            List<Role> roles = roleMapper.selectRolesByUserId(user.getId());
            boolean isAdmin = roles.stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getRoleCode()));
            if (isAdmin) return null;

            List<String> codes = new ArrayList<>();

            List<Long> roleIds = roles.stream().map(Role::getId).collect(Collectors.toList());
            if (!roleIds.isEmpty()) {
                List<String> roleCodes = roleOrganizationMapper.selectOrgCodesByRoleIds(roleIds);
                if (roleCodes != null) {
                    codes.addAll(roleCodes);
                }
            }

            // User direct permissions
            List<String> userCodes = userOrganizationMapper.selectOrgCodesByUserId(user.getId());
            if (userCodes != null) {
                codes.addAll(userCodes);
            }

            if (codes.isEmpty()) return new ArrayList<>();

            QueryWrapper<Organization> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("code", codes);
            return organizationMapper.selectList(queryWrapper);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getProvincesByDataType(String dataType, Integer year) {
        List<Map<String, Object>> result;
        if ("community".equals(dataType)) {
            // 从社区数据表获取省份
            QueryWrapper<CommunityDisasterReductionCapacity> wrapper = new QueryWrapper<>();
            wrapper.select("DISTINCT province_name as name, province_name as code");
            wrapper.isNotNull("province_name");
            wrapper.ne("province_name", "");
            if (year != null) {
                wrapper.eq("year", year);
            }

            List<Map<String, Object>> rawResult = communityCapacityMapper.selectMaps(wrapper);
            result = rawResult.stream()
                .map(map -> {
                    Map<String, Object> province = new LinkedHashMap<>();
                    province.put("name", map.get("name"));
                    province.put("code", map.get("code"));
                    return province;
                })
                .collect(Collectors.toList());
        } else {
            // 从调查数据表获取省份
            QueryWrapper<SurveyData> wrapper = new QueryWrapper<>();
            wrapper.select("DISTINCT province as name, province as code");
            wrapper.isNotNull("province");
            wrapper.ne("province", "");
            if (year != null) {
                wrapper.eq("year", year);
            }

            List<Map<String, Object>> rawResult = surveyDataMapper.selectMaps(wrapper);
            result = rawResult.stream()
                .map(map -> {
                    Map<String, Object> province = new LinkedHashMap<>();
                    province.put("name", map.get("name"));
                    province.put("code", map.get("code"));
                    return province;
                })
                .collect(Collectors.toList());
        }

        // 权限过滤
        List<Organization> allowedOrgs = getCurrentUserAllowedOrgs();
        if (allowedOrgs != null) {
            Set<String> visibleProvinces = allowedOrgs.stream()
                .map(Organization::getProvinceName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
            
            return result.stream()
                .filter(m -> visibleProvinces.contains(m.get("name")))
                .collect(Collectors.toList());
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getCitiesByProvince(String dataType, String provinceName, Integer year) {
        List<Map<String, Object>> result;
        if ("community".equals(dataType)) {
            // 从社区数据表获取城市
            QueryWrapper<CommunityDisasterReductionCapacity> wrapper = new QueryWrapper<>();
            wrapper.select("DISTINCT city_name as name, city_name as code");
            wrapper.eq("province_name", provinceName);
            wrapper.isNotNull("city_name");
            wrapper.ne("city_name", "");
            if (year != null) {
                wrapper.eq("year", year);
            }

            List<Map<String, Object>> rawResult = communityCapacityMapper.selectMaps(wrapper);
            result = rawResult.stream()
                .map(map -> {
                    Map<String, Object> city = new LinkedHashMap<>();
                    city.put("name", map.get("name"));
                    city.put("code", map.get("code"));
                    return city;
                })
                .collect(Collectors.toList());
        } else {
            // 从调查数据表获取城市
            QueryWrapper<SurveyData> wrapper = new QueryWrapper<>();
            wrapper.select("DISTINCT city as name, city as code");
            wrapper.eq("province", provinceName);
            wrapper.isNotNull("city");
            wrapper.ne("city", "");
            if (year != null) {
                wrapper.eq("year", year);
            }

            List<Map<String, Object>> rawResult = surveyDataMapper.selectMaps(wrapper);
            result = rawResult.stream()
                .map(map -> {
                    Map<String, Object> city = new LinkedHashMap<>();
                    city.put("name", map.get("name"));
                    city.put("code", map.get("code"));
                    return city;
                })
                .collect(Collectors.toList());
        }

        // 权限过滤
        List<Organization> allowedOrgs = getCurrentUserAllowedOrgs();
        if (allowedOrgs != null) {
            Set<String> visibleCities = new HashSet<>();
            for (Organization org : allowedOrgs) {
                // 只关心当前省份下的权限
                if (!Objects.equals(org.getProvinceName(), provinceName)) continue;
                
                if (org.getLevel() == 1) { // Province level - can see all cities
                    return result;
                } else if (org.getLevel() >= 2) { // City/County/Township
                    // If my org is City, I see this city.
                    // If my org is County, I see its parent city.
                    visibleCities.add(org.getCityName());
                }
            }
            
            return result.stream()
                .filter(m -> visibleCities.contains(m.get("name")))
                .collect(Collectors.toList());
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getCountiesByCity(String dataType, String provinceName, String cityName, Integer year) {
        List<Map<String, Object>> result;
        if ("community".equals(dataType)) {
            // 从社区数据表获取区县
            QueryWrapper<CommunityDisasterReductionCapacity> wrapper = new QueryWrapper<>();
            wrapper.select("DISTINCT county_name as name, county_name as code");
            wrapper.eq("province_name", provinceName);
            wrapper.eq("city_name", cityName);
            wrapper.isNotNull("county_name");
            wrapper.ne("county_name", "");
            if (year != null) {
                wrapper.eq("year", year);
            }

            List<Map<String, Object>> rawResult = communityCapacityMapper.selectMaps(wrapper);
            result = rawResult.stream()
                .map(map -> {
                    Map<String, Object> county = new LinkedHashMap<>();
                    county.put("name", map.get("name"));
                    county.put("code", map.get("code"));
                    return county;
                })
                .collect(Collectors.toList());
        } else {
            // 从调查数据表获取区县
            QueryWrapper<SurveyData> wrapper = new QueryWrapper<>();
            wrapper.select("DISTINCT county as name, county as code");
            wrapper.eq("province", provinceName);
            wrapper.eq("city", cityName);
            wrapper.isNotNull("county");
            wrapper.ne("county", "");
            if (year != null) {
                wrapper.eq("year", year);
            }

            List<Map<String, Object>> rawResult = surveyDataMapper.selectMaps(wrapper);
            result = rawResult.stream()
                .map(map -> {
                    Map<String, Object> county = new LinkedHashMap<>();
                    county.put("name", map.get("name"));
                    county.put("code", map.get("code"));
                    return county;
                })
                .collect(Collectors.toList());
        }

        // 权限过滤
        List<Organization> allowedOrgs = getCurrentUserAllowedOrgs();
        if (allowedOrgs != null) {
            Set<String> visibleCounties = new HashSet<>();
            for (Organization org : allowedOrgs) {
                if (!Objects.equals(org.getProvinceName(), provinceName)) continue;
                if (!Objects.equals(org.getCityName(), cityName)) continue;
                
                if (org.getLevel() <= 2) { // Province or City - can see all counties
                    return result;
                } else if (org.getLevel() >= 3) { // County/Township
                    visibleCounties.add(org.getCountyName());
                }
            }
            
            return result.stream()
                .filter(m -> visibleCounties.contains(m.get("name")))
                .collect(Collectors.toList());
        }
        return result;
    }

    @Override
    public List<?> getDataByCounty(String dataType, String provinceName, String cityName, String countyName, Integer year) {
        if ("community".equals(dataType)) {
            // 从社区数据表获取数据，兼容名称和代码两种格式
            QueryWrapper<CommunityDisasterReductionCapacity> wrapper = new QueryWrapper<>();
            wrapper.and(w -> w.eq("province_name", provinceName).or().eq("province_name", resolveRegionCode(provinceName)));
            wrapper.and(w -> w.eq("city_name", cityName).or().eq("city_name", resolveRegionCode(cityName)));
            wrapper.eq("county_name", countyName);
            if (year != null) {
                wrapper.eq("year", year);
            }
            wrapper.orderByAsc("id");

            return communityCapacityMapper.selectList(wrapper);
        } else {
            // 从调查数据表获取数据，兼容名称和代码两种格式
            QueryWrapper<SurveyData> wrapper = new QueryWrapper<>();
            wrapper.and(w -> w.eq("province", provinceName).or().eq("province", resolveRegionCode(provinceName)));
            wrapper.and(w -> w.eq("city", cityName).or().eq("city", resolveRegionCode(cityName)));
            wrapper.eq("county", countyName);
            if (year != null) {
                wrapper.eq("year", year);
            }
            wrapper.orderByAsc("id");

            return surveyDataMapper.selectList(wrapper);
        }
    }

    /**
     * 将行政区划名称转换为对应的代码（如 四川省->510000, 乐山市->511100）
     * 如果输入已经是代码格式则直接返回
     */
    private String resolveRegionCode(String nameOrCode) {
        if (nameOrCode == null || nameOrCode.matches("^\\d+$")) {
            return nameOrCode;
        }
        // 从 organization 表查找代码
        try {
            QueryWrapper<Organization> query = new QueryWrapper<>();
            query.eq("name", nameOrCode);
            query.select("code");
            query.last("LIMIT 1");
            List<Map<String, Object>> rows = organizationMapper.selectMaps(query);
            if (rows != null && !rows.isEmpty()) {
                Object code = rows.get(0).get("code");
                if (code != null) {
                    return String.valueOf(code);
                }
            }
        } catch (Exception e) {
            // fallback: return original value
        }
        return nameOrCode;
    }
}
