package com.evaluate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.evaluate.entity.EvaluationModel;
import com.evaluate.entity.ModelStep;
import com.evaluate.entity.StepAlgorithm;

import java.util.List;

/**
 * 评估模型服务接口
 * 
 * @author System
 * @since 2025-01-01
 */
public interface EvaluationModelService extends IService<EvaluationModel> {

    /**
     * 获取默认模型
     */
    EvaluationModel getDefaultModel();

    /**
     * 设置默认模型
     */
    boolean setDefaultModel(Long modelId);

    /**
     * 获取模型的所有步骤
     */
    List<ModelStep> getModelSteps(Long modelId);

    /**
     * 获取步骤的所有算法
     */
    List<StepAlgorithm> getStepAlgorithms(Long stepId);

    /**
     * 复制模型
     */
    EvaluationModel copyModel(Long sourceModelId, String newModelName, String newModelCode);

    /**
     * 验证模型配置
     */
    boolean validateModel(Long modelId);

    /**
     * 启用/禁用模型
     */
    boolean toggleModelStatus(Long modelId, Integer status);
}