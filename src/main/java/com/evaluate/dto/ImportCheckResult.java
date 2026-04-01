package com.evaluate.dto;

import java.util.List;

/**
 * 导入前置条件检查结果DTO
 *
 * @author System
 * @since 2025-12-30
 */
public class ImportCheckResult {

    /**
     * 是否可以导入
     */
    private boolean canImport;

    /**
     * 检查结果消息
     */
    private String message;

    /**
     * 是否有医疗设施数据
     */
    private boolean hasMedicalData;

    /**
     * 是否有消防员配置数据
     */
    private boolean hasFirefighterData;

    /**
     * 缺少医疗设施数据的区域列表
     */
    private List<String> missingMedicalRegions;

    /**
     * 缺少消防员配置数据的区域列表
     */
    private List<String> missingFirefighterRegions;

    public ImportCheckResult() {}

    public ImportCheckResult(boolean canImport, String message) {
        this.canImport = canImport;
        this.message = message;
    }

    public boolean isCanImport() {
        return canImport;
    }

    public void setCanImport(boolean canImport) {
        this.canImport = canImport;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isHasMedicalData() {
        return hasMedicalData;
    }

    public void setHasMedicalData(boolean hasMedicalData) {
        this.hasMedicalData = hasMedicalData;
    }

    public boolean isHasFirefighterData() {
        return hasFirefighterData;
    }

    public void setHasFirefighterData(boolean hasFirefighterData) {
        this.hasFirefighterData = hasFirefighterData;
    }

    public List<String> getMissingMedicalRegions() {
        return missingMedicalRegions;
    }

    public void setMissingMedicalRegions(List<String> missingMedicalRegions) {
        this.missingMedicalRegions = missingMedicalRegions;
    }

    public List<String> getMissingFirefighterRegions() {
        return missingFirefighterRegions;
    }

    public void setMissingFirefighterRegions(List<String> missingFirefighterRegions) {
        this.missingFirefighterRegions = missingFirefighterRegions;
    }
}
