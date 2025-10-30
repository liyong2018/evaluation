package com.evaluate.service;

import java.util.List;
import java.util.Map;

/**
 * TOPSIS配置迁移服务接口
 * 
 * 负责分析现有TOPSIS算法配置并迁移到新格式
 * 
 * @author System
 * @since 2025-01-01
 */
public interface TOPSISConfigMigrationService {
    
    /**
     * 分析现有TOPSIS算法配置
     * 
     * @return 配置分析报告
     */
    Map<String, Object> analyzeExistingConfigurations();
    
    /**
     * 迁移指定模型的TOPSIS配置
     * 
     * @param modelId 模型ID
     * @param stepCode 步骤代码
     * @param indicators 新的指标列表
     * @return 迁移结果
     */
    Map<String, Object> migrateConfiguration(Long modelId, String stepCode, List<String> indicators);
    
    /**
     * 批量迁移所有TOPSIS配置
     * 
     * @return 批量迁移结果
     */
    Map<String, Object> migrateAllConfigurations();
    
    /**
     * 验证迁移后的配置
     * 
     * @param modelId 模型ID
     * @param stepCode 步骤代码
     * @return 验证结果
     */
    Map<String, Object> validateMigratedConfiguration(Long modelId, String stepCode);
    
    /**
     * 修复配置错误
     * 
     * @param modelId 模型ID
     * @param stepCode 步骤代码
     * @param issues 发现的问题列表
     * @return 修复结果
     */
    Map<String, Object> repairConfiguration(Long modelId, String stepCode, List<String> issues);
    
    /**
     * 回滚配置迁移
     * 
     * @param modelId 模型ID
     * @param stepCode 步骤代码
     * @return 回滚结果
     */
    Map<String, Object> rollbackMigration(Long modelId, String stepCode);
    
    /**
     * 获取迁移历史记录
     * 
     * @return 迁移历史
     */
    List<Map<String, Object>> getMigrationHistory();
    
    /**
     * 分析迁移
     * 
     * @param modelId 模型ID
     * @return 迁移分析结果
     */
    Map<String, Object> analyzeMigration(Long modelId);
    
    /**
     * 执行迁移
     * 
     * @param modelId 模型ID
     * @param dryRun 是否为试运行
     * @return 迁移执行结果
     */
    Map<String, Object> executeMigration(Long modelId, boolean dryRun);
}