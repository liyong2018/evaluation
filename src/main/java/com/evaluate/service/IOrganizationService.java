package com.evaluate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.evaluate.entity.CommunityDisasterReductionCapacity;
import com.evaluate.entity.Organization;
import com.evaluate.entity.SurveyData;

import java.util.List;
import java.util.Map;

/**
 * 组织机构服务
 */
public interface IOrganizationService extends IService<Organization> {

    /**
     * 根据社区减灾能力数据同步组织机构
     *
     * @param community 社区减灾能力数据
     */
    void syncFromCommunityData(CommunityDisasterReductionCapacity community);

    /**
     * 根据乡镇调查数据同步组织机构
     *
     * @param surveyData 调查数据
     */
    void syncFromSurveyData(SurveyData surveyData);

    /**
     * 分页查询组织机构列表
     *
     * @param page 页码
     * @param size 每页大小
     * @param code 机构编码
     * @param name 机构名称
     * @param level 级别
     * @param parentId 父级ID
     * @return 查询结果
     */
    Map<String, Object> getOrganizationList(Integer page, Integer size, String code, String name, Integer level, Long parentId);

    /**
     * 根据编码获取组织机构
     *
     * @param code 机构编码
     * @return 组织机构
     */
    Organization getByCode(String code);

    /**
     * 获取组织机构树形结构
     *
     * @param parentId 父级ID
     * @param maxLevel 最大层级
     * @param year 年份（可选，用于过滤该年份有数据的组织）
     * @return 树形结构
     */
    List<Map<String, Object>> getOrganizationTree(Long parentId, Integer maxLevel, Integer year);

    /**
     * 根据父级ID获取子级组织机构
     *
     * @param parentId 父级ID
     * @return 子级组织机构列表
     */
    List<Organization> getChildrenByParentId(Long parentId);

    /**
     * 搜索组织机构
     *
     * @param keyword 关键词
     * @param level 级别
     * @return 组织机构列表
     */
    List<Organization> searchOrganization(String keyword, Integer level);

    /**
     * 根据级别获取组织机构列表
     *
     * @param level 级别
     * @return 组织机构列表
     */
    List<Organization> getOrganizationsByLevel(Integer level);

    /**
     * 根据省编码获取市级组织机构列表
     *
     * @param provinceCode 省编码
     * @return 市级组织机构列表
     */
    List<Organization> getCitiesByProvinceCode(String provinceCode);

    /**
     * 根据市编码获取县级组织机构列表
     *
     * @param cityCode 市编码
     * @return 县级组织机构列表
     */
    List<Organization> getCountiesByCityCode(String cityCode);

    /**
     * 根据县编码获取乡镇级组织机构列表
     *
     * @param countyCode 县编码
     * @return 乡镇级组织机构列表
     */
    List<Organization> getTownshipsByCountyCode(String countyCode);

    /**
     * 根据乡镇编码获取社区级组织机构列表
     *
     * @param townshipCode 乡镇编码
     * @return 社区级组织机构列表
     */
    List<Organization> getCommunitiesByTownshipCode(String townshipCode);

    /**
     * 创建组织机构
     *
     * @param organization 组织机构
     * @return 是否创建成功
     */
    boolean createOrganization(Organization organization);

    /**
     * 更新组织机构
     *
     * @param organization 组织机构
     * @return 是否更新成功
     */
    boolean updateOrganization(Organization organization);

    /**
     * 删除组织机构
     *
     * @param id 组织机构ID
     * @return 是否删除成功
     */
    boolean deleteOrganization(Long id);

    /**
     * 批量删除组织机构
     *
     * @param ids 组织机构ID列表
     * @return 是否删除成功
     */
    boolean batchDeleteOrganizations(List<Long> ids);

    /**
     * 从Excel导入组织机构（旧版本，兼容性保留）
     *
     * @param importList 导入数据列表
     * @return 导入的记录数
     * @deprecated 请使用带年份参数的版本
     */
    @Deprecated
    int importFromExcel(List<com.evaluate.dto.OrganizationImportDTO> importList);

    /**
     * 从Excel导入组织机构（带年份）
     *
     * @param importList 导入数据列表
     * @param year 数据所属年份
     * @return 导入的记录数
     */
    int importFromExcel(List<com.evaluate.dto.OrganizationImportDTO> importList, Integer year);

    int copyFromPreviousYear(Integer targetYear);

    /**
     * 删除组织机构的年度数据
     *
     * @param organizationId 组织机构ID
     * @param year 年份
     * @return 删除结果统计
     */
    Map<String, Object> deleteOrganizationYearData(Long organizationId, Integer year);
}
