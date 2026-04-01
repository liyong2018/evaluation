package com.evaluate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.evaluate.dto.GpkgFieldValidationResult;
import com.evaluate.entity.CommunityDisasterReductionCapacity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 社区行政村减灾能力服务接口
 *
 * @author System
 * @since 2025-01-01
 */
public interface ICommunityDisasterReductionCapacityService extends IService<CommunityDisasterReductionCapacity> {

    /**
     * 批量导入社区行政村减灾能力数据
     *
     * @param file Excel文件
     * @param year 数据所属年份
     * @return 导入结果
     */
    Map<String, Object> importCommunityCapacityData(MultipartFile file, Integer year);

    /**
     * 分页查询社区行政村减灾能力数据
     *
     * @param page 页码
     * @param size 每页大小
     * @param regionCode 行政区代码
     * @param communityName 社区名称
     * @param year 数据年份
     * @return 查询结果
     */
    Map<String, Object> getCommunityCapacityList(Integer page, Integer size, String regionCode, String communityName, Integer year);

    /**
     * 根据ID删除社区行政村减灾能力数据
     *
     * @param id 主键ID
     * @return 删除结果
     */
    boolean deleteById(Long id);

    /**
     * 批量删除社区行政村减灾能力数据
     *
     * @param ids 主键ID列表
     * @return 删除结果
     */
    boolean deleteByIds(List<Long> ids);

    /**
     * 根据行政区代码和社区名称获取数据
     *
     * @param regionCode 行政区代码
     * @param communityName 社区名称
     * @return 社区行政村减灾能力数据
     */
    CommunityDisasterReductionCapacity getByRegionAndCommunity(String regionCode, String communityName);

    /**
     * 搜索社区行政村减灾能力数据
     *
     * @param keyword 关键词（社区名称、乡镇名称等）
     * @param regionCode 行政区代码
     * @param communityName 社区名称
     * @param year 数据年份
     * @return 搜索结果列表
     */
    List<CommunityDisasterReductionCapacity> searchCommunityCapacity(String keyword, String regionCode, String communityName, Integer year);

    /**
     * 验证GPKG文件字段
     * 检查GPKG文件是否包含社区减灾能力数据所需的必要字段
     *
     * @param file     GPKG文件
     * @param dataType 数据类型 (township/community/medical)
     * @return 验证结果
     */
    GpkgFieldValidationResult validateGpkgFields(MultipartFile file, String dataType, Integer year);

    /**
     * 从GPKG文件导入社区减灾能力数据
     *
     * @param file GPKG文件
     * @param year 数据所属年份
     * @return 导入结果
     */
    Map<String, Object> importFromGpkg(MultipartFile file, Integer year);
}
