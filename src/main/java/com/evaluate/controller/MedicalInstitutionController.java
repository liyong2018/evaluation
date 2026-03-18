package com.evaluate.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.evaluate.common.Result;
import com.evaluate.dto.GpkgFieldValidationResult;
import com.evaluate.entity.MedicalInstitution;
import com.evaluate.entity.Organization;
import com.evaluate.entity.GrassrootsOrganization;
import com.evaluate.service.IMedicalInstitutionService;
import com.evaluate.service.IGrassrootsOrganizationService;
import com.evaluate.service.IOrganizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.lang.Integer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 医疗卫生机构数据管理控制器
 *
 * @author system
 * @since 2024-11-24
 */
@Slf4j
@RestController
@RequestMapping("/api/medical-institution")
@CrossOrigin(origins = "*")
public class MedicalInstitutionController {

    @Autowired(required = false)
    private IMedicalInstitutionService medicalInstitutionService;

    @Autowired(required = false)
    private IOrganizationService organizationService;

    @Autowired(required = false)
    private IGrassrootsOrganizationService grassrootsOrganizationService;

    /**
     * 检查服务是否可用
     */
    private boolean isServiceAvailable() {
        return medicalInstitutionService != null;
    }

    private Organization findOrganization(Integer year, String orgCode) {
        if (!StringUtils.hasText(orgCode) || organizationService == null) {
            return null;
        }
        String trimmedCode = orgCode.trim();
        if (year != null) {
            Organization org = organizationService.lambdaQuery()
                    .eq(Organization::getCode, trimmedCode)
                    .eq(Organization::getYear, year)
                    .one();
            if (org != null) {
                return org;
            }
        }
        return organizationService.getByCode(trimmedCode);
    }

    private GrassrootsOrganization findGrassrootsOrganization(Integer year, String orgCode) {
        if (!StringUtils.hasText(orgCode) || grassrootsOrganizationService == null) {
            return null;
        }
        String trimmedCode = orgCode.trim();
        if (year != null) {
            GrassrootsOrganization org = grassrootsOrganizationService.lambdaQuery()
                    .eq(GrassrootsOrganization::getCode, trimmedCode)
                    .eq(GrassrootsOrganization::getYear, year)
                    .one();
            if (org != null) {
                return org;
            }
        }
        return grassrootsOrganizationService.getByCode(trimmedCode, year);
    }

    private GrassrootsOrganization findGrassrootsOrganizationByPrefix(Integer year, String orgCodePrefix) {
        if (!StringUtils.hasText(orgCodePrefix) || grassrootsOrganizationService == null) {
            return null;
        }
        String trimmedCode = orgCodePrefix.trim();
        return grassrootsOrganizationService.lambdaQuery()
                .likeRight(GrassrootsOrganization::getCode, trimmedCode)
                .eq(year != null, GrassrootsOrganization::getYear, year)
                .orderByAsc(GrassrootsOrganization::getLevel)
                .last("limit 1")
                .one();
    }

    private String resolveAddressKeyword(Integer year, String orgCode) {
        Organization organization = findOrganization(year, orgCode);
        String keyword = resolveAddressKeyword(organization);
        if (StringUtils.hasText(keyword)) {
            return keyword;
        }

        GrassrootsOrganization grassrootsOrganization = findGrassrootsOrganization(year, orgCode);
        if (grassrootsOrganization == null) {
            grassrootsOrganization = findGrassrootsOrganizationByPrefix(year, orgCode);
        }
        return resolveAddressKeyword(grassrootsOrganization);
    }

    private void applyOrganizationNames(MedicalInstitution item, Organization org) {
        if (item == null || org == null) {
            return;
        }
        if (!StringUtils.hasText(item.getProvinceName())) {
            item.setProvinceName(org.getProvinceName());
        }
        if (!StringUtils.hasText(item.getCityName())) {
            item.setCityName(org.getCityName());
        }
        if (!StringUtils.hasText(item.getCountyName())) {
            item.setCountyName(org.getCountyName());
        }

        if (!StringUtils.hasText(item.getTownshipName())) {
            if (StringUtils.hasText(org.getTownshipName())) {
                item.setTownshipName(org.getTownshipName());
            } else if (org.getLevel() != null && org.getLevel() == 4) {
                item.setTownshipName(org.getName());
            }
        }

        if (!StringUtils.hasText(item.getCommunityName())) {
            if (StringUtils.hasText(org.getCommunityName())) {
                item.setCommunityName(org.getCommunityName());
            } else if (org.getLevel() != null && org.getLevel() == 5) {
                item.setCommunityName(org.getName());
            }
        }
    }

    private String normalizeForMatch(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return text.replaceAll("\\s+", "");
    }

    private String toCountyPrefix(String orgCode) {
        if (!StringUtils.hasText(orgCode)) {
            return null;
        }
        String trimmedCode = orgCode.trim();
        if (trimmedCode.length() >= 6) {
            return trimmedCode.substring(0, 6);
        }
        return trimmedCode;
    }

    private Map<String, String> loadTownshipVariants(Integer year, String orgCode) {
        if (!StringUtils.hasText(orgCode) || grassrootsOrganizationService == null) {
            return new HashMap<>();
        }
        String countyPrefix = toCountyPrefix(orgCode);
        if (!StringUtils.hasText(countyPrefix)) {
            return new HashMap<>();
        }

        QueryWrapper<GrassrootsOrganization> queryWrapper = new QueryWrapper<>();
        queryWrapper.likeRight("code", countyPrefix);
        queryWrapper.eq("level", 4);
        if (year != null) {
            queryWrapper.and(wrapper -> wrapper
                    .and(w -> w.eq("year", year).ne("is_baseline", 1))
                    .or().eq("is_baseline", 1)
            );
        }
        queryWrapper.and(wrapper -> wrapper
                .isNull("is_deleted")
                .or().eq("is_deleted", 0)
        );
        queryWrapper.orderByAsc("code");

        List<GrassrootsOrganization> townships = grassrootsOrganizationService.list(queryWrapper);
        if (townships == null || townships.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, String> fullNameToOriginal = new HashMap<>();
        for (GrassrootsOrganization township : townships) {
            if (township == null) {
                continue;
            }
            String fullName = StringUtils.hasText(township.getTownshipName()) ? township.getTownshipName().trim() : null;
            if (!StringUtils.hasText(fullName) && StringUtils.hasText(township.getName())) {
                fullName = township.getName().trim();
            }
            if (!StringUtils.hasText(fullName)) {
                continue;
            }
            fullNameToOriginal.put(normalizeForMatch(fullName), fullName);
        }

        return fullNameToOriginal;
    }

    private void applyTownshipFromAddress(MedicalInstitution item, List<String> townshipVariants, Map<String, String> normalizedVariantToFullName) {
        if (item == null) {
            return;
        }
        if (StringUtils.hasText(item.getTownshipName())) {
            return;
        }
        if (townshipVariants == null || townshipVariants.isEmpty()
                || normalizedVariantToFullName == null || normalizedVariantToFullName.isEmpty()) {
            return;
        }
        String address = normalizeForMatch(item.getInstitutionAddress());
        if (!StringUtils.hasText(address)) {
            return;
        }

        for (String variant : townshipVariants) {
            if (!StringUtils.hasText(variant)) {
                continue;
            }
            if (address.contains(variant)) {
                String fullName = normalizedVariantToFullName.get(variant);
                if (StringUtils.hasText(fullName)) {
                    item.setTownshipName(fullName);
                    log.debug("精确匹配街道/乡镇: {} -> {}", variant, fullName);
                    return;
                }
            }
        }
    }

    private void applyNamesFromAddress(MedicalInstitution item) {
        if (item == null) {
            return;
        }
        String address = normalizeForMatch(item.getInstitutionAddress());
        if (!StringUtils.hasText(address)) {
            return;
        }

        String provinceName = null;
        String cityName = null;
        String countyName = null;

        int provinceIdx = address.indexOf("省");
        if (provinceIdx >= 0) {
            provinceName = address.substring(0, provinceIdx + 1);
            address = address.substring(provinceIdx + 1);
        }

        int cityIdx = address.indexOf("市");
        if (cityIdx >= 0) {
            cityName = address.substring(0, cityIdx + 1);
            address = address.substring(cityIdx + 1);
        }

        int districtIdx = address.indexOf("区");
        int countyIdx = address.indexOf("县");
        if (districtIdx >= 0 && (countyIdx < 0 || districtIdx < countyIdx)) {
            countyName = address.substring(0, districtIdx + 1);
        } else if (countyIdx >= 0) {
            countyName = address.substring(0, countyIdx + 1);
        }

        if (!StringUtils.hasText(item.getProvinceName()) && StringUtils.hasText(provinceName)) {
            item.setProvinceName(provinceName);
        }
        if (!StringUtils.hasText(item.getCityName()) && StringUtils.hasText(cityName)) {
            item.setCityName(cityName);
        }
        if (!StringUtils.hasText(item.getCountyName()) && StringUtils.hasText(countyName)) {
            item.setCountyName(countyName);
        }
    }

    private void applyCommunityFromAddress(MedicalInstitution item) {
        if (item == null) {
            return;
        }
        if (StringUtils.hasText(item.getCommunityName())) {
            return;
        }
        String address = normalizeForMatch(item.getInstitutionAddress());
        if (!StringUtils.hasText(address)) {
            return;
        }

        String suffixGroup = "(社区居民委员会|社区居委会|居民委员会|居委会|村民委员会|村委会|行政村|社区|村)";

        String base = null;
        String suffix = null;

        java.util.regex.Pattern p = java.util.regex.Pattern.compile("([\\u4e00-\\u9fff]{2,20})" + suffixGroup);
        java.util.regex.Matcher m = p.matcher(address);
        while (m.find()) {
            base = m.group(1);
            suffix = m.group(2);
        }

        if (!StringUtils.hasText(base) || !StringUtils.hasText(suffix)) {
            return;
        }

        String normalized = normalizeCommunityOrVillageName(base, suffix);
        if (StringUtils.hasText(normalized)) {
            item.setCommunityName(normalized);
        }
    }

    private String normalizeCommunityOrVillageName(String base, String suffix) {
        if (!StringUtils.hasText(base) || !StringUtils.hasText(suffix)) {
            return null;
        }

        String trimmedBase = base.trim();
        String[] separators = new String[] { "办事处", "街道", "镇", "乡", "区", "县", "市", "省" };
        for (String sep : separators) {
            int idx = trimmedBase.lastIndexOf(sep);
            if (idx >= 0) {
                trimmedBase = trimmedBase.substring(idx + sep.length()).trim();
            }
        }

        if (!StringUtils.hasText(trimmedBase)) {
            return null;
        }

        if ("社区居民委员会".equals(suffix) || "社区居委会".equals(suffix) || "居民委员会".equals(suffix) || "居委会".equals(suffix)) {
            return trimmedBase.endsWith("社区") ? trimmedBase : trimmedBase + "社区";
        }

        if ("村民委员会".equals(suffix) || "村委会".equals(suffix)) {
            return trimmedBase.endsWith("村") ? trimmedBase : trimmedBase + "村";
        }

        if ("行政村".equals(suffix)) {
            return trimmedBase.endsWith("村") ? trimmedBase : trimmedBase + "村";
        }

        if ("社区".equals(suffix)) {
            return trimmedBase.endsWith("社区") ? trimmedBase : trimmedBase + "社区";
        }

        if ("村".equals(suffix)) {
            return trimmedBase.endsWith("村") ? trimmedBase : trimmedBase + "村";
        }

        return trimmedBase + suffix;
    }

    private String resolveAddressKeyword(Organization org) {
        if (org == null) {
            return null;
        }
        if (StringUtils.hasText(org.getCommunityName())) {
            return org.getCommunityName().trim();
        }
        if (StringUtils.hasText(org.getTownshipName())) {
            return org.getTownshipName().trim();
        }
        if (StringUtils.hasText(org.getCountyName())) {
            return org.getCountyName().trim();
        }
        if (StringUtils.hasText(org.getCityName())) {
            return org.getCityName().trim();
        }
        if (StringUtils.hasText(org.getProvinceName())) {
            return org.getProvinceName().trim();
        }
        return StringUtils.hasText(org.getName()) ? org.getName().trim() : null;
    }

    private String resolveAddressKeyword(GrassrootsOrganization org) {
        if (org == null) {
            return null;
        }
        if (StringUtils.hasText(org.getCommunityName())) {
            return org.getCommunityName().trim();
        }
        if (StringUtils.hasText(org.getTownshipName())) {
            return org.getTownshipName().trim();
        }
        if (StringUtils.hasText(org.getCountyName())) {
            return org.getCountyName().trim();
        }
        if (StringUtils.hasText(org.getCityName())) {
            return org.getCityName().trim();
        }
        if (StringUtils.hasText(org.getProvinceName())) {
            return org.getProvinceName().trim();
        }
        return StringUtils.hasText(org.getName()) ? org.getName().trim() : null;
    }

    private void applyGrassrootsNames(MedicalInstitution item, GrassrootsOrganization org) {
        if (item == null || org == null) {
            return;
        }
        if (!StringUtils.hasText(item.getProvinceName()) && StringUtils.hasText(org.getProvinceName())) {
            item.setProvinceName(org.getProvinceName());
        }
        if (!StringUtils.hasText(item.getCityName()) && StringUtils.hasText(org.getCityName())) {
            item.setCityName(org.getCityName());
        }
        if (!StringUtils.hasText(item.getCountyName()) && StringUtils.hasText(org.getCountyName())) {
            item.setCountyName(org.getCountyName());
        }
        if (!StringUtils.hasText(item.getTownshipName()) && StringUtils.hasText(org.getTownshipName())) {
            item.setTownshipName(org.getTownshipName());
        }
        if (!StringUtils.hasText(item.getCommunityName()) && StringUtils.hasText(org.getCommunityName())) {
            item.setCommunityName(org.getCommunityName());
        }
    }

    private void applyGrassrootsNamesUpToCounty(MedicalInstitution item, GrassrootsOrganization org) {
        if (item == null || org == null) {
            return;
        }
        if (!StringUtils.hasText(item.getProvinceName()) && StringUtils.hasText(org.getProvinceName())) {
            item.setProvinceName(org.getProvinceName());
        }
        if (!StringUtils.hasText(item.getCityName()) && StringUtils.hasText(org.getCityName())) {
            item.setCityName(org.getCityName());
        }
        if (!StringUtils.hasText(item.getCountyName()) && StringUtils.hasText(org.getCountyName())) {
            item.setCountyName(org.getCountyName());
        }
    }

    /**
     * 导入医疗卫生机构数据
     */
    @PostMapping("/import")
    public Map<String, Object> importMedicalInstitutionData(
            @RequestParam("file") MultipartFile file,
            @RequestParam("year") Integer year) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (file.isEmpty()) {
                result.put("success", false);
                result.put("message", "请选择要导入的文件");
                return result;
            }

            if (!isServiceAvailable()) {
                result.put("success", false);
                result.put("message", "服务暂时不可用，请稍后重试");
                log.warn("医疗卫生机构服务未注入，无法导入数据");
                return result;
            }

            com.evaluate.dto.ImportResultDTO importResult = medicalInstitutionService.importMedicalInstitutionDataWithResult(file, year);

            result.put("success", importResult.isSuccess());
            result.put("totalCount", importResult.getTotalCount());
            result.put("successCount", importResult.getSuccessCount());
            result.put("insertCount", importResult.getInsertCount());
            result.put("updateCount", importResult.getUpdateCount());

            StringBuilder message = new StringBuilder();

            // 优先处理错误信息
            if (importResult.hasErrors()) {
                result.put("errors", importResult.getErrors());
                result.put("errorsMessage", importResult.getErrorsMessage());
                // message 包含完整的错误详情
                message.append("导入失败：存在地址验证错误\n\n");
                message.append(importResult.getErrorsMessage());
            } else if (importResult.isSuccess()) {
                message.append("导入成功！共处理").append(importResult.getTotalCount()).append("条数据");
                if (importResult.getInsertCount() > 0) {
                    message.append("，新增").append(importResult.getInsertCount()).append("条");
                }
                if (importResult.getUpdateCount() > 0) {
                    message.append("，更新").append(importResult.getUpdateCount()).append("条");
                }
            } else {
                message.append("导入完成，但部分数据处理失败");
            }

            if (importResult.hasWarnings()) {
                result.put("warnings", importResult.getWarnings());
                result.put("warningsMessage", importResult.getWarningsMessage());
            }

            result.put("message", message.toString());

        } catch (Exception e) {
            log.error("导入医疗卫生机构数据失败", e);
            result.put("success", false);
            result.put("message", "导入失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 根据年份和组织机构代码获取医疗卫生机构数据列表
     * （已临时简化以提高性能）
     */
    @GetMapping("/list")
    public Map<String, Object> getMedicalInstitutionList(
            @RequestParam Integer year,
            @RequestParam(required = false) String orgCode) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (!isServiceAvailable()) {
                result.put("success", false);
                result.put("data", null);
                result.put("message", "服务暂时不可用，请稍后重试");
                log.warn("医疗卫生机构服务未注入，无法获取数据列表");
                return result;
            }
            List<MedicalInstitution> list = medicalInstitutionService.getMedicalInstitutionByYear(year, orgCode);

            // 临时：禁用循环中的额外处理以提高性能
            // TODO: 后续需要批量优化这些查询
            /*
            Organization filterOrg = findOrganization(year, orgCode);
            GrassrootsOrganization filterGrassrootsOrg = findGrassrootsOrganization(year, orgCode);
            if (filterGrassrootsOrg == null) {
                filterGrassrootsOrg = findGrassrootsOrganizationByPrefix(year, orgCode);
            }
            Map<String, String> townshipVariantToFullName = loadTownshipVariants(year, orgCode);
            List<String> townshipVariants = new ArrayList<>(townshipVariantToFullName.keySet());
            townshipVariants.sort(Comparator.comparingInt(String::length).reversed());

            if ((list == null || list.isEmpty()) && StringUtils.hasText(orgCode)) {
                String keyword = resolveAddressKeyword(year, orgCode);
                if (StringUtils.hasText(keyword)) {
                    list = medicalInstitutionService.lambdaQuery()
                            .eq(MedicalInstitution::getYear, year)
                            .like(MedicalInstitution::getInstitutionAddress, keyword)
                            .list();
                }
            }

            if (list != null && !list.isEmpty()) {
                for (MedicalInstitution item : list) {
                    applyNamesFromAddress(item);
                    applyCommunityFromAddress(item);

                    Organization org = findOrganization(year, item.getOrgCode());
                    if (org != null) {
                        applyOrganizationNames(item, org);
                    } else if (filterGrassrootsOrg != null) {
                        boolean isExactMatch = StringUtils.hasText(orgCode)
                                && StringUtils.hasText(filterGrassrootsOrg.getCode())
                                && filterGrassrootsOrg.getCode().trim().equals(orgCode.trim());
                        if (isExactMatch) {
                            applyGrassrootsNames(item, filterGrassrootsOrg);
                        } else {
                            applyGrassrootsNamesUpToCounty(item, filterGrassrootsOrg);
                        }
                    } else if (filterOrg != null) {
                        applyOrganizationNames(item, filterOrg);
                    }
                    applyTownshipFromAddress(item, townshipVariants, townshipVariantToFullName);
                }
            }
            */

            result.put("success", true);
            result.put("data", list);
            result.put("message", "获取数据成功");
        } catch (Exception e) {
            log.error("获取医疗卫生机构数据失败", e);
            result.put("success", false);
            result.put("message", "获取数据失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 分页查询医疗卫生机构数据（简化版，支持按组织机构过滤）
     */
    @GetMapping("/page")
    public Result<Map<String, Object>> getMedicalInstitutionPage(
            @RequestParam Integer year,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false) String orgCode,
            @RequestParam(required = false) String keyword) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (!isServiceAvailable()) {
                result.put("success", false);
                result.put("message", "服务暂时不可用");
                return Result.error("服务暂时不可用");
            }

            // 构建分页查询
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<MedicalInstitution> pageParam =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);

            QueryWrapper<MedicalInstitution> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("year", year);
            queryWrapper.orderByDesc("create_time");

            // 添加组织机构代码过滤
            if (StringUtils.hasText(orgCode)) {
                queryWrapper.likeRight("org_code", orgCode.trim());
            }

            if (StringUtils.hasText(keyword)) {
                queryWrapper.and(wrapper -> wrapper
                    .like("institution_name", keyword)
                    .or().like("institution_address", keyword)
                    .or().like("province", keyword)
                    .or().like("city", keyword)
                    .or().like("county", keyword)
                    .or().like("township", keyword)
                );
            }

            com.baomidou.mybatisplus.extension.plugins.pagination.Page<MedicalInstitution> pageResult =
                medicalInstitutionService.page(pageParam, queryWrapper);

            // 返回与乡镇数据一致的分页格式
            result.put("records", pageResult.getRecords());
            result.put("total", pageResult.getTotal());
            result.put("current", pageResult.getCurrent());
            result.put("pages", pageResult.getPages());
            result.put("size", pageResult.getSize());

            return Result.success(result);
        } catch (Exception e) {
            log.error("分页查询医疗卫生机构数据失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 根据机构名称搜索医疗卫生机构数据
     */
    @GetMapping("/search")
    public Map<String, Object> searchMedicalInstitution(@RequestParam String institutionName) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (!isServiceAvailable()) {
                result.put("success", false);
                result.put("data", null);
                result.put("message", "服务暂时不可用，请稍后重试");
                log.warn("医疗卫生机构服务未注入，无法搜索数据");
                return result;
            }
            List<MedicalInstitution> list = medicalInstitutionService.searchByInstitutionName(institutionName);
            result.put("success", true);
            result.put("data", list);
            result.put("message", "搜索数据成功");
        } catch (Exception e) {
            log.error("搜索医疗卫生机构数据失败", e);
            result.put("success", false);
            result.put("message", "搜索数据失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 更新医疗卫生机构数据
     */
    @PutMapping("/update")
    public Map<String, Object> updateMedicalInstitution(@RequestBody MedicalInstitution medicalInstitution) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (!isServiceAvailable()) {
                result.put("success", false);
                result.put("message", "服务暂时不可用，请稍后重试");
                log.warn("医疗卫生机构服务未注入，无法更新数据");
                return result;
            }

            boolean updateResult = medicalInstitutionService.updateMedicalInstitution(medicalInstitution);

            if (updateResult) {
                result.put("success", true);
                result.put("message", "更新成功");
            } else {
                result.put("success", false);
                result.put("message", "更新失败");
            }

        } catch (Exception e) {
            log.error("更新医疗卫生机构数据失败", e);
            result.put("success", false);
            result.put("message", "更新失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 批量删除医疗卫生机构数据
     */
    @DeleteMapping("/batch")
    public Map<String, Object> batchDeleteMedicalInstitution(@RequestBody List<Long> ids) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (ids == null || ids.isEmpty()) {
                result.put("success", false);
                result.put("message", "请选择要删除的数据");
                return result;
            }

            if (!isServiceAvailable()) {
                result.put("success", false);
                result.put("message", "服务暂时不可用，请稍后重试");
                log.warn("医疗卫生机构服务未注入，无法批量删除数据");
                return result;
            }

            boolean deleteResult = medicalInstitutionService.batchDelete(ids);

            if (deleteResult) {
                result.put("success", true);
                result.put("message", "批量删除成功");
            } else {
                result.put("success", false);
                result.put("message", "批量删除失败");
            }

        } catch (Exception e) {
            log.error("批量删除医疗卫生机构数据失败", e);
            result.put("success", false);
            result.put("message", "批量删除失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 根据年份和组织机构删除所有医疗卫生机构数据
     */
    @DeleteMapping("/delete-by-year-org")
    public Result<Long> deleteByYearAndOrg(
            @RequestParam Integer year,
            @RequestParam(required = false) String orgCode) {
        try {
            log.info("删除医疗卫生机构数据 - year: {}, orgCode: {}", year, orgCode);
            if (!isServiceAvailable()) {
                return Result.error("服务暂时不可用");
            }
            QueryWrapper<MedicalInstitution> wrapper = new QueryWrapper<>();
            wrapper.eq("year", year);
            if (StringUtils.hasText(orgCode)) {
                wrapper.likeRight("org_code", orgCode.trim());
            }
            long count = medicalInstitutionService.count(wrapper);
            boolean result = medicalInstitutionService.remove(wrapper);
            return result ? Result.success(count) : Result.error("删除失败");
        } catch (Exception e) {
            log.error("删除医疗卫生机构数据失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 导出医疗卫生机构数据
     */
    @GetMapping("/export")
    public void exportMedicalInstitutionData(
            @RequestParam Integer year,
            HttpServletResponse response) {
        try {
            if (!isServiceAvailable()) {
                log.warn("医疗卫生机构服务未注入，无法导出数据");
                throw new RuntimeException("服务暂时不可用，请稍后重试");
            }
            medicalInstitutionService.exportMedicalInstitutionData(year, response);
        } catch (Exception e) {
            log.error("导出医疗卫生机构数据失败", e);
            throw new RuntimeException("导出失败：" + e.getMessage());
        }
    }

    /**
     * 下载导入模板
     */
    @GetMapping("/template")
    public void downloadImportTemplate(HttpServletResponse response) {
        try {
            if (!isServiceAvailable()) {
                log.warn("医疗卫生机构服务未注入，无法下载模板");
                throw new RuntimeException("服务暂时不可用，请稍后重试");
            }
            medicalInstitutionService.downloadImportTemplate(response);
        } catch (Exception e) {
            log.error("下载导入模板失败", e);
            throw new RuntimeException("下载模板失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取医疗卫生机构数据
     */
    @GetMapping("/{id}")
    public Map<String, Object> getMedicalInstitutionById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (!isServiceAvailable()) {
                result.put("success", false);
                result.put("data", null);
                result.put("message", "服务暂时不可用，请稍后重试");
                log.warn("医疗卫生机构服务未注入，无法获取数据");
                return result;
            }
            MedicalInstitution medicalInstitution = medicalInstitutionService.getById(id);
            if (medicalInstitution != null) {
                result.put("success", true);
                result.put("data", medicalInstitution);
                result.put("message", "获取数据成功");
            } else {
                result.put("success", false);
                result.put("message", "数据不存在");
            }
        } catch (Exception e) {
            log.error("获取医疗卫生机构数据失败", e);
            result.put("success", false);
            result.put("message", "获取数据失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 新增医疗卫生机构数据
     */
    @PostMapping
    public Map<String, Object> createMedicalInstitution(@RequestBody MedicalInstitution medicalInstitution) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (!isServiceAvailable()) {
                result.put("success", false);
                result.put("message", "服务暂时不可用，请稍后重试");
                log.warn("医疗卫生机构服务未注入，无法创建数据");
                return result;
            }
            boolean saveResult = medicalInstitutionService.save(medicalInstitution);

            if (saveResult) {
                result.put("success", true);
                result.put("message", "创建成功");
                result.put("data", medicalInstitution);
            } else {
                result.put("success", false);
                result.put("message", "创建失败");
            }

        } catch (Exception e) {
            log.error("创建医疗卫生机构数据失败", e);
            result.put("success", false);
            result.put("message", "创建失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 删除医疗卫生机构数据
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteMedicalInstitution(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (!isServiceAvailable()) {
                result.put("success", false);
                result.put("message", "服务暂时不可用，请稍后重试");
                log.warn("医疗卫生机构服务未注入，无法删除数据");
                return result;
            }
            boolean deleteResult = medicalInstitutionService.removeById(id);

            if (deleteResult) {
                result.put("success", true);
                result.put("message", "删除成功");
            } else {
                result.put("success", false);
                result.put("message", "删除失败");
            }

        } catch (Exception e) {
            log.error("删除医疗卫生机构数据失败", e);
            result.put("success", false);
            result.put("message", "删除失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 修复数据库唯一约束，支持多年度数据
     */
    @PostMapping("/fix-constraint")
    public Map<String, Object> fixUniqueConstraint() {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean fixResult = ((com.evaluate.service.impl.MedicalInstitutionServiceImpl) medicalInstitutionService).fixUniqueConstraint();

            if (fixResult) {
                result.put("success", true);
                result.put("message", "数据库唯一约束修复成功，现在支持同一医疗机构在不同年份的数据");
            } else {
                result.put("success", false);
                result.put("message", "数据库唯一约束修复失败");
            }

        } catch (Exception e) {
            log.error("修复数据库唯一约束失败", e);
            result.put("success", false);
            result.put("message", "修复失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 验证GPKG文件字段
     * 检查GPKG文件是否包含医疗卫生机构数据所需的必要字段
     */
    @PostMapping("/validate-gpkg")
    public Map<String, Object> validateGpkgFile(@RequestParam("file") MultipartFile file,
                                                 @RequestParam(value = "year", required = false) Integer year) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (!isServiceAvailable()) {
                result.put("success", false);
                result.put("message", "服务暂时不可用，请稍后重试");
                log.warn("医疗卫生机构服务未注入，无法验证GPKG文件");
                return result;
            }

            GpkgFieldValidationResult validationResult = medicalInstitutionService.validateGpkgFields(file, "medical", year);
            result.put("success", true);
            result.put("data", validationResult);
            result.put("message", validationResult.getMessage());

        } catch (Exception e) {
            log.error("验证GPKG文件失败", e);
            result.put("success", false);
            result.put("message", "验证失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 从GPKG文件导入医疗卫生机构数据
     */
    @PostMapping("/import-gpkg")
    public Map<String, Object> importFromGpkg(
            @RequestParam("file") MultipartFile file,
            @RequestParam("year") Integer year) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (file.isEmpty()) {
                result.put("success", false);
                result.put("message", "请选择要导入的文件");
                return result;
            }

            if (!isServiceAvailable()) {
                result.put("success", false);
                result.put("message", "服务暂时不可用，请稍后重试");
                log.warn("医疗卫生机构服务未注入，无法导入GPKG数据");
                return result;
            }

            com.evaluate.dto.ImportResultDTO importResult = medicalInstitutionService.importFromGpkg(file, year);

            result.put("success", importResult.isSuccess());
            result.put("totalCount", importResult.getTotalCount());
            result.put("successCount", importResult.getSuccessCount());
            result.put("insertCount", importResult.getInsertCount());
            result.put("updateCount", importResult.getUpdateCount());

            StringBuilder message = new StringBuilder();

            if (importResult.hasErrors()) {
                result.put("errors", importResult.getErrors());
                result.put("errorsMessage", importResult.getErrorsMessage());
                message.append("导入失败：存在数据验证错误\n\n");
                message.append(importResult.getErrorsMessage());
            } else if (importResult.isSuccess()) {
                message.append("导入成功！共处理").append(importResult.getTotalCount()).append("条数据");
                if (importResult.getInsertCount() > 0) {
                    message.append("，新增").append(importResult.getInsertCount()).append("条");
                }
                if (importResult.getUpdateCount() > 0) {
                    message.append("，更新").append(importResult.getUpdateCount()).append("条");
                }
            } else {
                message.append("导入完成，但部分数据处理失败");
            }

            if (importResult.hasWarnings()) {
                result.put("warnings", importResult.getWarnings());
                result.put("warningsMessage", importResult.getWarningsMessage());
            }

            result.put("message", message.toString());

        } catch (Exception e) {
            log.error("导入GPKG文件失败", e);
            result.put("success", false);
            result.put("message", "导入失败：" + e.getMessage());
        }

        return result;
    }
}
