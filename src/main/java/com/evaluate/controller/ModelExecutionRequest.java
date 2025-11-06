package com.evaluate.controller;

import java.util.List;
import lombok.Data;

@Data
public class ModelExecutionRequest {
    private Long modelId;
    private List<String> regionCodes;
    private Long weightConfigId;
    private Integer year;
    // 所属机构代码（区县或单位代码）
    private String orgCode;
}
