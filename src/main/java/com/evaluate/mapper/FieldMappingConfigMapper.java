package com.evaluate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evaluate.entity.FieldMappingConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 字段映射配置Mapper接口
 *
 * @author system
 * @since 2025-10-29
 */
@Mapper
public interface FieldMappingConfigMapper extends BaseMapper<FieldMappingConfig> {

    /**
     * 根据模型ID获取字段映射配置
     *
     * @param modelId 模型ID
     * @return 字段映射配置列表
     */
    List<FieldMappingConfig> selectByModelId(@Param("modelId") Long modelId);

    /**
     * 根据模型ID和字段类型获取字段映射配置
     *
     * @param modelId 模型ID
     * @param fieldType 字段类型
     * @return 字段映射配置列表
     */
    List<FieldMappingConfig> selectByModelIdAndFieldType(@Param("modelId") Long modelId,
                                                       @Param("fieldType") String fieldType);
}