package com.evaluate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.evaluate.entity.MedicalInstitution;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 医疗卫生机构服务接口
 *
 * @author system
 * @since 2024-11-24
 */
public interface IMedicalInstitutionService extends IService<MedicalInstitution> {

    /**
     * 导入医疗卫生机构数据
     *
     * @param file 上传的文件
     * @param year 数据年份
     * @return 导入结果
     */
    boolean importMedicalInstitutionData(MultipartFile file, Integer year);

    /**
     * 根据年份获取医疗卫生机构数据列表
     *
     * @param year 年份
     * @return 数据列表
     */
    List<MedicalInstitution> getMedicalInstitutionByYear(Integer year);

    /**
     * 根据机构名称搜索医疗卫生机构数据
     *
     * @param institutionName 机构名称
     * @return 数据列表
     */
    List<MedicalInstitution> searchByInstitutionName(String institutionName);

    /**
     * 批量删除医疗卫生机构数据
     *
     * @param ids ID列表
     * @return 删除结果
     */
    boolean batchDelete(List<Long> ids);

    /**
     * 导出医疗卫生机构数据
     *
     * @param year 年份
     * @param response HTTP响应
     */
    void exportMedicalInstitutionData(Integer year, HttpServletResponse response);

    /**
     * 下载导入模板
     *
     * @param response HTTP响应
     */
    void downloadImportTemplate(HttpServletResponse response);

    /**
     * 根据乡镇地址统计实有住院床位数总和
     *
     * @param townshipAddress 乡镇地址
     * @param year 数据年份
     * @return 实有住院床位数总和
     */
    Integer sumActualHospitalBedsByTownship(String townshipAddress, Integer year);

    /**
     * 检查指定年份是否有任何医疗设施数据
     *
     * @param year 数据年份
     * @return 如果有数据返回true，否则返回false
     */
    boolean hasAnyDataForYear(Integer year);
}