package com.evaluate.dto;

import lombok.Data;

@Data
public class EvaluationRequest {
    private Long surveyId;
    private Long algorithmId;
    private Long weightConfigId;
}