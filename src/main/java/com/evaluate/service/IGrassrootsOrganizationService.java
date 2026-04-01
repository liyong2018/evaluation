package com.evaluate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.evaluate.entity.GrassrootsOrganization;

import java.util.List;
import java.util.Map;

/**
 * 基层组织机构服务（乡镇和社区）
 */
public interface IGrassrootsOrganizationService extends IService<GrassrootsOrganization> {

    /**
     * 根据区县ID获取乡镇列表
     *
     * @param countyId 区县ID
     * @param year 年份（可选）
     * @return 乡镇列表
     */
    List<GrassrootsOrganization> getTownshipsByCountyId(Long countyId, Integer year);

    /**
     * 根据区县代码获取乡镇列表
     *
     * @param countyCode 区县代码
     * @param year 年份（可选）
     * @return 乡镇列表
     */
    List<GrassrootsOrganization> getTownshipsByCountyCode(String countyCode, Integer year);

    /**
     * 根据乡镇ID获取社区列表
     *
     * @param townshipId 乡镇ID
     * @param year 年份（可选）
     * @return 社区列表
     */
    List<GrassrootsOrganization> getCommunitiesByTownshipId(Long townshipId, Integer year);

    /**
     * 根据乡镇代码获取社区列表
     *
     * @param townshipCode 乡镇代码
     * @param year 年份（可选）
     * @return 社区列表
     */
    List<GrassrootsOrganization> getCommunitiesByTownshipCode(String townshipCode, Integer year);

    /**
     * 根据区县ID获取所有下级组织（乡镇和社区）
     *
     * @param countyId 区县ID
     * @param year 年份（可选）
     * @return 树形结构
     */
    List<Map<String, Object>> getTreeByCountyId(Long countyId, Integer year);

    /**
     * 根据区县代码获取所有下级组织（乡镇和社区）
     *
     * @param countyCode 区县代码
     * @param year 年份（可选）
     * @return 树形结构
     */
    List<Map<String, Object>> getTreeByCountyCode(String countyCode, Integer year);

    /**
     * 根据编码获取基层组织机构
     *
     * @param code 机构编码
     * @param year 年份（可选）
     * @return 基层组织机构
     */
    GrassrootsOrganization getByCode(String code, Integer year);

    /**
     * 创建基层组织机构
     *
     * @param organization 基层组织机构
     * @return 是否创建成功
     */
    boolean createGrassrootsOrganization(GrassrootsOrganization organization);

    /**
     * 更新基层组织机构
     *
     * @param organization 基层组织机构
     * @return 是否更新成功
     */
    boolean updateGrassrootsOrganization(GrassrootsOrganization organization);

    /**
     * 删除基层组织机构（级联删除子节点）
     *
     * @param id 基层组织机构ID
     * @return 是否删除成功
     */
    boolean deleteGrassrootsOrganization(Long id);

    /**
     * 删除基层组织机构（支持按年份删除）
     * 如果指定年份，使用删除标记；否则直接删除记录
     *
     * @param id 基层组织机构ID
     * @param year 年份（可选）
     * @return 是否删除成功
     */
    boolean deleteGrassrootsOrganization(Long id, Integer year);

    /**
     * 批量删除基层组织机构
     *
     * @param ids 基层组织机构ID列表
     * @return 是否删除成功
     */
    boolean batchDeleteGrassrootsOrganizations(List<Long> ids);

    /**
     * 搜索基层组织机构
     *
     * @param countyId 区县ID（可选，限制搜索范围）
     * @param keyword 关键词
     * @param level 级别（可选）
     * @param year 年份（可选）
     * @return 基层组织机构列表
     */
    List<GrassrootsOrganization> searchGrassrootsOrganizations(Long countyId, String keyword, Integer level, Integer year);

    /**
     * 从上一年复制年度配置
     *
     * @param targetYear 目标年份
     * @return 复制的记录数
     */
    int copyFromPreviousYear(Integer targetYear);

    /**
     * 删除区县下的年度数据
     *
     * @param countyId 区县ID
     * @param year 年份
     * @return 删除结果统计
     */
    Map<String, Object> deleteYearDataByCountyId(Long countyId, Integer year);

    /**
     * 调试方法：获取乡镇下的所有社区（不受年份限制）
     *
     * @param townshipId 乡镇ID
     * @return 社区列表
     */
    List<GrassrootsOrganization> debugGetCommunitiesByTownshipId(Long townshipId);

    /**
     * 分页查询基层组织机构列表
     *
     * @param page 页码
     * @param size 每页大小
     * @param countyId 区县ID（可选）
     * @param code 机构编码
     * @param name 机构名称
     * @param level 级别
     * @param parentId 父级ID
     * @param year 年份
     * @return 查询结果
     */
    Map<String, Object> getGrassrootsOrganizationList(Integer page, Integer size, Long countyId, String code, String name, Integer level, Long parentId, Integer year);

    /**
     * 修复社区数据的parent_id（根据township_name匹配）
     *
     * @param year 年份
     * @return 修复结果统计
     */
    Map<String, Object> fixCommunityParentIds(Integer year);
}
