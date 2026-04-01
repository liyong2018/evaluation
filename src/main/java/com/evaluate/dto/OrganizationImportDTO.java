package com.evaluate.dto;

import lombok.Data;

/**
 * 组织机构导入DTO
 */
@Data
public class OrganizationImportDTO {
    /**
     * 地址（如：四川省眉山市仁寿县慈航镇观音社区振兴大道西路129号）
     */
    private String address;

    /**
     * 行政区划代码（如：511421109003）
     */
    private String regionCode;

    /**
     * 从地址解析出的省名称
     */
    private String provinceName;

    /**
     * 从地址解析出的市名称
     */
    private String cityName;

    /**
     * 从地址解析出的县名称
     */
    private String countyName;

    /**
     * 从地址解析出的乡镇名称
     */
    private String townshipName;

    /**
     * 从地址解析出的社区名称
     */
    private String communityName;
}
