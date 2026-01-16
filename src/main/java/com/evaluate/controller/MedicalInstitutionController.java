package com.evaluate.controller;

import com.evaluate.entity.MedicalInstitution;
import com.evaluate.service.IMedicalInstitutionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
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

    /**
     * 检查服务是否可用
     */
    private boolean isServiceAvailable() {
        return medicalInstitutionService != null;
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

            boolean importResult = medicalInstitutionService.importMedicalInstitutionData(file, year);

            if (importResult) {
                result.put("success", true);
                result.put("message", "医疗卫生机构数据导入成功");
            } else {
                result.put("success", false);
                result.put("message", "导入失败，请检查文件格式和数据");
            }

        } catch (Exception e) {
            log.error("导入医疗卫生机构数据失败", e);
            result.put("success", false);
            result.put("message", "导入失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 根据年份获取医疗卫生机构数据列表
     */
    @GetMapping("/list")
    public Map<String, Object> getMedicalInstitutionList(@RequestParam Integer year) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (!isServiceAvailable()) {
                result.put("success", false);
                result.put("data", null);
                result.put("message", "服务暂时不可用，请稍后重试");
                log.warn("医疗卫生机构服务未注入，无法获取数据列表");
                return result;
            }
            List<MedicalInstitution> list = medicalInstitutionService.getMedicalInstitutionByYear(year);
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
}