package com.evaluate.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 组织机构数据变更 DTO
 */
@Data
public class OrganizationChangeDTO {

    /**
     * 数据年份
     */
    private Integer year;

    /**
     * 数据来源类型（township/community）
     */
    private String dataType;

    /**
     * 新增的组织机构列表
     */
    private List<OrganizationRecord> added = new ArrayList<>();

    /**
     * 删除的组织机构列表
     */
    private List<OrganizationRecord> removed = new ArrayList<>();

    /**
     * 变更的组织机构列表
     */
    private List<OrganizationChange> changed = new ArrayList<>();

    /**
     * 组织机构记录
     */
    @Data
    public static class OrganizationRecord {
        private String code;
        private String name;
        private Integer level;
        private Long parentId;
        private String provinceName;
        private String cityName;
        private String countyName;
        private String townshipName;
        private String communityName;
    }

    /**
     * 组织机构变更记录
     */
    @Data
    public static class OrganizationChange {
        private String code;
        private String oldName;
        private String newName;
        private Long oldParentId;
        private Long newParentId;
        private String changeType; // "name" | "parent" | "both"
    }

    /**
     * 获取变更统计信息
     */
    public String getSummary() {
        return String.format("年份：%d, 类型：%s, 新增：%d, 删除：%d, 变更：%d",
                year, dataType, added.size(), removed.size(), changed.size());
    }
}
