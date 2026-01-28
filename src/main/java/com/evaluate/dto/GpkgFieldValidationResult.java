package com.evaluate.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * GPKG文件字段验证结果DTO
 *
 * @author System
 * @since 2025-01-26
 */
@Data
public class GpkgFieldValidationResult {

    /**
     * 是否通过验证
     */
    private boolean valid;

    /**
     * 数据类型 (township/community/medical)
     */
    private String dataType;

    /**
     * 图层名称
     */
    private String layerName;

    /**
     * 要素数量
     */
    private int featureCount;

    /**
     * 缺少的必要字段列表
     */
    private List<String> missingFields = new ArrayList<>();

    /**
     * 存在的可选字段列表
     */
    private List<String> presentFields = new ArrayList<>();

    /**
     * 警告信息列表
     */
    private List<String> warnings = new ArrayList<>();

    /**
     * 错误信息列表
     */
    private List<String> errors = new ArrayList<>();

    /**
     * 添加警告信息
     */
    public void addWarning(String warning) {
        if (this.warnings == null) {
            this.warnings = new ArrayList<>();
        }
        this.warnings.add(warning);
    }

    /**
     * 添加错误信息
     */
    public void addError(String error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
    }

    /**
     * 添加缺少字段
     */
    public void addMissingField(String field) {
        if (this.missingFields == null) {
            this.missingFields = new ArrayList<>();
        }
        this.missingFields.add(field);
    }

    /**
     * 添加存在字段
     */
    public void addPresentField(String field) {
        if (this.presentFields == null) {
            this.presentFields = new ArrayList<>();
        }
        this.presentFields.add(field);
    }

    /**
     * 是否有警告
     */
    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }

    /**
     * 是否有错误
     */
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    /**
     * 是否有缺少字段
     */
    public boolean hasMissingFields() {
        return missingFields != null && !missingFields.isEmpty();
    }

    /**
     * 获取友好的验证结果消息
     */
    public String getMessage() {
        StringBuilder sb = new StringBuilder();

        if (valid) {
            sb.append("GPKG文件验证通过！\n");
            sb.append("数据类型: ").append(getDataTypeName()).append("\n");
            sb.append("图层名称: ").append(layerName).append("\n");
            sb.append("要素数量: ").append(featureCount).append("条\n");

            if (hasWarnings()) {
                sb.append("\n警告信息:\n");
                for (String warning : warnings) {
                    sb.append("  - ").append(warning).append("\n");
                }
            }
        } else {
            sb.append("GPKG文件验证失败！\n");
            sb.append("数据类型: ").append(getDataTypeName()).append("\n");
            sb.append("图层名称: ").append(layerName).append("\n");

            if (hasMissingFields()) {
                sb.append("\n缺少的必要字段:\n");
                for (String field : missingFields) {
                    sb.append("  - ").append(field).append("\n");
                }
            }

            if (hasErrors()) {
                sb.append("\n错误信息:\n");
                for (String error : errors) {
                    sb.append("  - ").append(error).append("\n");
                }
            }
        }

        return sb.toString();
    }

    /**
     * 获取数据类型名称
     */
    private String getDataTypeName() {
        switch (dataType) {
            case "township":
                return "乡镇评估数据";
            case "community":
                return "社区减灾能力数据";
            case "medical":
                return "医疗卫生机构数据";
            default:
                return "未知数据类型";
        }
    }
}
