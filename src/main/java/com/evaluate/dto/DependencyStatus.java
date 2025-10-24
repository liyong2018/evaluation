package com.evaluate.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 依赖模型检查状态DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DependencyStatus {

    /**
     * 整体状态
     */
    private String status;

    /**
     * 问题描述
     */
    private String problemDescription;

    /**
     * 建议信息
     */
    private String suggestion;

    /**
     * 有效数据区域列表
     */
    private java.util.List<String> validDataRegions;

    /**
     * 无效数据区域列表
     */
    private java.util.List<String> invalidDataRegions;

    /**
     * 缺失数据字段列表
     */
    private java.util.List<String> missingDataFields;

    /**
     * 缺失数据区域列表
     */
    private java.util.List<String> missingDataRegions;
}