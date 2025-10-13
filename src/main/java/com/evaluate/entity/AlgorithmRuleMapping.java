package com.evaluate.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 算法规则映射表
 * 
 * @author System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("algorithm_rule_mapping")
public class AlgorithmRuleMapping implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 算法配置ID
     */
    @TableField("algorithm_config_id")
    private Long algorithmConfigId;

    /**
     * 算法步骤ID
     */
    @TableField("algorithm_step_id")
    private Long algorithmStepId;

    /**
     * 动态规则ID
     */
    @TableField("dynamic_rule_id")
    private Long dynamicRuleId;

    /**
     * 映射顺序
     */
    @TableField("mapping_order")
    private Integer mappingOrder;

    /**
     * 参数映射配置（JSON格式）
     */
    @TableField("param_mapping")
    private String paramMapping;

    /**
     * 条件表达式（可选，用于条件执行）
     */
    @TableField("condition_expression")
    private String conditionExpression;

    /**
     * 是否启用（1-启用，0-禁用）
     */
    @TableField("status")
    private Integer status;

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

    // 手动添加getter/setter方法以解决Lombok问题
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAlgorithmConfigId() {
        return algorithmConfigId;
    }

    public void setAlgorithmConfigId(Long algorithmConfigId) {
        this.algorithmConfigId = algorithmConfigId;
    }

    public Long getAlgorithmStepId() {
        return algorithmStepId;
    }

    public void setAlgorithmStepId(Long algorithmStepId) {
        this.algorithmStepId = algorithmStepId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public Integer getMappingOrder() {
        return mappingOrder;
    }

    public void setMappingOrder(Integer mappingOrder) {
        this.mappingOrder = mappingOrder;
    }

    public Long getDynamicRuleId() {
        return dynamicRuleId;
    }

    public void setDynamicRuleId(Long dynamicRuleId) {
        this.dynamicRuleId = dynamicRuleId;
    }

    public String getParamMapping() {
        return paramMapping;
    }

    public void setParamMapping(String paramMapping) {
        this.paramMapping = paramMapping;
    }

    public String getConditionExpression() {
        return conditionExpression;
    }

    public void setConditionExpression(String conditionExpression) {
        this.conditionExpression = conditionExpression;
    }

    // 添加缺少的方法
    public String getParameterMapping() {
        return this.paramMapping;
    }

    public void setParameterMapping(String paramMapping) {
        this.paramMapping = paramMapping;
    }
}