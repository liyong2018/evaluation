package com.evaluate.controller;

import com.evaluate.common.Result;
import com.evaluate.dto.OrganizationImportDTO;
import com.evaluate.entity.Organization;
import com.evaluate.service.IOrganizationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 组织机构控制器
 *
 * @author System
 * @since 2025-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/organization")
public class OrganizationController {

    @Autowired
    private IOrganizationService organizationService;

    /**
     * 分页查询组织机构列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getOrganizationList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) Long parentId) {
        log.info("查询组织机构列表，页码: {}, 每页大小: {}, 机构编码: {}, 机构名称: {}, 级别: {}, 父级ID: {}",
                page, size, code, name, level, parentId);
        try {
            Map<String, Object> result = organizationService.getOrganizationList(page, size, code, name, level, parentId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询组织机构列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取组织机构
     */
    @GetMapping("/{id}")
    public Result<Organization> getById(@PathVariable Long id) {
        log.info("根据ID获取组织机构，ID: {}", id);
        try {
            Organization organization = organizationService.getById(id);
            if (organization != null) {
                return Result.success(organization);
            } else {
                return Result.error("组织机构不存在");
            }
        } catch (Exception e) {
            log.error("根据ID获取组织机构失败，ID: {}", id, e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据编码获取组织机构
     */
    @GetMapping("/code/{code}")
    public Result<Organization> getByCode(@PathVariable String code) {
        log.info("根据编码获取组织机构，编码: {}", code);
        try {
            Organization organization = organizationService.getByCode(code);
            if (organization != null) {
                return Result.success(organization);
            } else {
                return Result.error("组织机构不存在");
            }
        } catch (Exception e) {
            log.error("根据编码获取组织机构失败，编码: {}", code, e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取组织机构树形结构
     */
    @GetMapping("/tree")
    public Result<List<Map<String, Object>>> getOrganizationTree(
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) Integer maxLevel,
            @RequestParam(required = false) Integer year) {
        log.info("获取组织机构树形结构，父级ID: {}, 最大层级: {}, 年份: {}", parentId, maxLevel, year);
        try {
            List<Map<String, Object>> tree = organizationService.getOrganizationTree(parentId, maxLevel, year);
            return Result.success(tree);
        } catch (Exception e) {
            log.error("获取组织机构树形结构失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据父级ID获取子级组织机构
     */
    @GetMapping("/children/{parentId}")
    public Result<List<Organization>> getChildrenByParentId(@PathVariable Long parentId) {
        log.info("根据父级ID获取子级组织机构，父级ID: {}", parentId);
        try {
            List<Organization> children = organizationService.getChildrenByParentId(parentId);
            return Result.success(children);
        } catch (Exception e) {
            log.error("根据父级ID获取子级组织机构失败，父级ID: {}", parentId, e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 搜索组织机构
     */
    @GetMapping("/search")
    public Result<List<Organization>> searchOrganization(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer level) {
        log.info("搜索组织机构，关键词: {}, 级别: {}", keyword, level);
        try {
            List<Organization> result = organizationService.searchOrganization(keyword, level);
            return Result.success(result);
        } catch (Exception e) {
            log.error("搜索组织机构失败", e);
            return Result.error("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 获取省级组织机构列表
     */
    @GetMapping("/provinces")
    public Result<List<Organization>> getProvinces() {
        log.info("获取省级组织机构列表");
        try {
            List<Organization> provinces = organizationService.getOrganizationsByLevel(1);
            return Result.success(provinces);
        } catch (Exception e) {
            log.error("获取省级组织机构列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取市级组织机构列表
     */
    @GetMapping("/cities")
    public Result<List<Organization>> getCities(@RequestParam(required = false) String provinceCode) {
        log.info("获取市级组织机构列表，省编码: {}", provinceCode);
        try {
            List<Organization> cities = organizationService.getCitiesByProvinceCode(provinceCode);
            return Result.success(cities);
        } catch (Exception e) {
            log.error("获取市级组织机构列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取县级组织机构列表
     */
    @GetMapping("/counties")
    public Result<List<Organization>> getCounties(@RequestParam(required = false) String cityCode) {
        log.info("获取县级组织机构列表，市编码: {}", cityCode);
        try {
            List<Organization> counties = organizationService.getCountiesByCityCode(cityCode);
            return Result.success(counties);
        } catch (Exception e) {
            log.error("获取县级组织机构列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取乡镇级组织机构列表
     */
    @GetMapping("/townships")
    public Result<List<Organization>> getTownships(@RequestParam(required = false) String countyCode) {
        log.info("获取乡镇级组织机构列表，县编码: {}", countyCode);
        try {
            List<Organization> townships = organizationService.getTownshipsByCountyCode(countyCode);
            return Result.success(townships);
        } catch (Exception e) {
            log.error("获取乡镇级组织机构列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取社区级组织机构列表
     */
    @GetMapping("/communities")
    public Result<List<Organization>> getCommunities(@RequestParam(required = false) String townshipCode) {
        log.info("获取社区级组织机构列表，乡镇编码: {}", townshipCode);
        try {
            List<Organization> communities = organizationService.getCommunitiesByTownshipCode(townshipCode);
            return Result.success(communities);
        } catch (Exception e) {
            log.error("获取社区级组织机构列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 创建组织机构
     */
    @PostMapping
    public Result<Boolean> create(@RequestBody Organization organization) {
        log.info("创建组织机构，编码: {}, 名称: {}", organization.getCode(), organization.getName());
        try {
            boolean success = organizationService.createOrganization(organization);
            return success ? Result.success(true) : Result.error("创建失败");
        } catch (Exception e) {
            log.error("创建组织机构失败", e);
            return Result.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新组织机构
     */
    @PutMapping
    public Result<Boolean> update(@RequestBody Organization organization) {
        log.info("更新组织机构，ID: {}", organization.getId());
        try {
            boolean success = organizationService.updateOrganization(organization);
            return success ? Result.success(true) : Result.error("更新失败");
        } catch (Exception e) {
            log.error("更新组织机构失败", e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除组织机构
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        log.info("删除组织机构，ID: {}", id);
        try {
            boolean success = organizationService.deleteOrganization(id);
            return success ? Result.success(true) : Result.error("删除失败");
        } catch (Exception e) {
            log.error("删除组织机构失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除组织机构
     */
    @DeleteMapping("/batch")
    public Result<Boolean> batchDelete(@RequestBody List<Long> ids) {
        log.info("批量删除组织机构，IDs: {}", ids);
        try {
            boolean success = organizationService.batchDeleteOrganizations(ids);
            return success ? Result.success(true) : Result.error("批量删除失败");
        } catch (Exception e) {
            log.error("批量删除组织机构失败", e);
            return Result.error("批量删除失败: " + e.getMessage());
        }
    }

    /**
     * 导入组织机构Excel
     */
    @PostMapping("/import")
    public Result<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file) {
        log.info("导入组织机构Excel，文件名: {}", file.getOriginalFilename());
        try {
            // 验证文件
            if (file.isEmpty()) {
                return Result.error("文件为空");
            }

            String filename = file.getOriginalFilename();
            if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
                return Result.error("文件格式错误，请上传Excel文件(.xlsx或.xls)");
            }

            // 解析Excel文件
            List<OrganizationImportDTO> importList = parseExcelFile(file);

            if (importList.isEmpty()) {
                return Result.error("Excel文件中没有有效数据");
            }

            // 调用服务层导入
            int count = organizationService.importFromExcel(importList);

            Map<String, Object> result = new HashMap<>();
            result.put("count", count);
            result.put("total", importList.size());

            return Result.success(result);
        } catch (Exception e) {
            log.error("导入组织机构Excel失败", e);
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 解析Excel文件
     */
    private List<OrganizationImportDTO> parseExcelFile(MultipartFile file) throws Exception {
        List<OrganizationImportDTO> importList = new ArrayList<>();

        try (InputStream is = file.getInputStream()) {
            Workbook workbook;
            if (file.getOriginalFilename().endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(is);
            } else {
                workbook = new HSSFWorkbook(is);
            }

            Sheet sheet = workbook.getSheetAt(0);

            // 跳过标题行，从第二行开始
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                // 第一列：地址
                String address = getCellValueAsString(row.getCell(0));
                // 第二列：行政区划代码
                String regionCode = getCellValueAsString(row.getCell(1));

                // 跳过空行
                if (!org.springframework.util.StringUtils.hasText(address) && !org.springframework.util.StringUtils.hasText(regionCode)) {
                    continue;
                }

                OrganizationImportDTO dto = new OrganizationImportDTO();
                dto.setAddress(address.trim());
                dto.setRegionCode(regionCode.trim());

                importList.add(dto);
            }

            workbook.close();
        }

        return importList;
    }

    /**
     * 获取单元格值作为字符串
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                // 数字类型（如行政区划代码）需要转为字符串
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                // 处理数字，防止科学计数法
                double numericValue = cell.getNumericCellValue();
                if (numericValue == (long) numericValue) {
                    return String.valueOf((long) numericValue);
                } else {
                    return String.valueOf(numericValue);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    return cell.getStringCellValue();
                }
            default:
                return "";
        }
    }
}
