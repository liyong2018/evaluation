package com.evaluate.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 调查数据表
 * 
 * @author System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("survey_data")
public class SurveyData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 行政区代码
     */
    @TableField("region_code")
    private String regionCode;

    /**
     * 省名称
     */
    @TableField("province")
    private String province;

    /**
     * 市名称
     */
    @TableField("city")
    private String city;

    /**
     * 县名称
     */
    @TableField("county")
    private String county;

    /**
     * 乡镇名称
     */
    @TableField("township")
    private String township;

    /**
     * 常住人口数量
     */
    @TableField("population")
    private Long population;

    /**
     * 本级灾害管理工作人员总数
     */
    @TableField("management_staff")
    private Integer managementStaff;

    /**
     * 是否开展风险评估
     */
    @TableField("risk_assessment")
    private String riskAssessment;

    /**
     * 防灾减灾救灾资金投入总金额(万元)
     */
    @TableField("funding_amount")
    private Double fundingAmount;

    /**
     * 现有储备物资装备折合金额(万元)
     */
    @TableField("material_value")
    private Double materialValue;

    /**
     * 实有住院床位数
     */
    @TableField("hospital_beds")
    private Integer hospitalBeds;

    /**
     * 消防员数量
     */
    @TableField("firefighters")
    private Integer firefighters;

    /**
     * 志愿者人数
     */
    @TableField("volunteers")
    private Integer volunteers;

    /**
     * 民兵预备役人数
     */
    @TableField("militia_reserve")
    private Integer militiaReserve;

    /**
     * 应急管理培训和演练参与人次
     */
    @TableField("training_participants")
    private Integer trainingParticipants;

    /**
     * 本级灾害应急避难场所容量
     */
    @TableField("shelter_capacity")
    private Integer shelterCapacity;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 是否删除(0-未删除，1-已删除)
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}