package com.evaluate.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 导入结果DTO
 */
@Data
public class ImportResultDTO {
    /** 是否成功 */
    private boolean success;
    /** 总记录数 */
    private int totalCount;
    /** 成功导入数 */
    private int successCount;
    /** 更新记录数 */
    private int updateCount;
    /** 新增记录数 */
    private int insertCount;
    /** 警告信息列表 */
    private List<String> warnings = new ArrayList<>();
    /** 错误信息列表 */
    private List<String> errors = new ArrayList<>();

    public void addWarning(String warning) {
        if (this.warnings == null) {
            this.warnings = new ArrayList<>();
        }
        this.warnings.add(warning);
    }

    public void addError(String error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
    }

    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }

    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    public String getWarningsMessage() {
        if (!hasWarnings()) {
            return null;
        }
        if (warnings.size() <= 5) {
            return String.join("; ", warnings);
        }
        // 最多显示5条警告
        return String.join("; ", warnings.subList(0, 5)) + "; ... 等" + warnings.size() + "条";
    }

    public String getErrorsMessage() {
        if (!hasErrors()) {
            return null;
        }
        if (errors.size() <= 5) {
            return String.join("; ", errors);
        }
        // 最多显示5条错误
        return String.join("; ", errors.subList(0, 5)) + "; ... 等" + errors.size() + "条";
    }
}
