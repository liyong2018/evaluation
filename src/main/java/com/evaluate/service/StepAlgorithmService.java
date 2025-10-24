package com.evaluate.service;

import com.evaluate.entity.StepAlgorithm;
import java.util.List;

/**
 * 步骤算法服务接口
 */
public interface StepAlgorithmService {

    /**
     * 根据步骤ID获取算法
     */
    StepAlgorithm getByStepId(Long stepId);

    /**
     * 根据ID获取算法
     */
    StepAlgorithm getById(Long id);
    
    /**
     * 根据模型ID和步骤代码获取算法
     */
    StepAlgorithm getByModelIdAndStepCode(long modelId, String stepCode);
    
    /**
     * 更新QL表达式
     */
    boolean updateQlExpression(long id, String qlExpression);
    
    /**
     * 根据模型ID和算法代码获取算法
     */
    StepAlgorithm getByModelIdAndAlgorithmCode(long modelId, String algorithmCode);
    
    /**
     * 创建算法
     */
    boolean create(StepAlgorithm stepAlgorithm);

    /**
     * 获取所有算法
     */
    List<StepAlgorithm> getAll();

    /**
     * 保存算法
     */
    boolean save(StepAlgorithm stepAlgorithm);

    /**
     * 更新算法
     */
    boolean update(StepAlgorithm stepAlgorithm);

    /**
     * 删除算法
     */
    boolean deleteById(Long id);
}