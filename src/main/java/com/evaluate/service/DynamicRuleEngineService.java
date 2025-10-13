package com.evaluate.service;

import com.evaluate.entity.DynamicRule;
import com.evaluate.entity.AlgorithmRuleMapping;

import java.util.List;
import java.util.Map;

/**
 * 动态规则引擎服务接口
 * 
 * @author System
 * @since 2024-01-01
 */
public interface DynamicRuleEngineService {

    /**
     * 执行单个动态规则
     * 
     * @param rule 动态规则
     * @param context 执行上下文
     * @return 执行结果
     */
    Object executeRule(DynamicRule rule, Map<String, Object> context);

    /**
     * 批量执行规则链
     * 
     * @param rules 规则列表
     * @param context 执行上下文
     * @return 执行结果
     */
    Map<String, Object> executeRuleChain(List<DynamicRule> rules, Map<String, Object> context);

    /**
     * 根据算法步骤执行对应的动态规则
     * 
     * @param algorithmStepId 算法步骤ID
     * @param context 执行上下文
     * @return 执行结果
     */
    Map<String, Object> executeRulesByStep(Long algorithmStepId, Map<String, Object> context);

    /**
     * 验证规则表达式语法
     * 
     * @param expression 规则表达式
     * @return 验证结果
     */
    boolean validateRuleExpression(String expression);

    /**
     * 编译规则表达式
     * 
     * @param rule 动态规则
     * @return 编译结果
     */
    boolean compileRule(DynamicRule rule);

    /**
     * 获取规则执行日志
     * 
     * @param ruleId 规则ID
     * @param limit 限制条数
     * @return 执行日志
     */
    List<Map<String, Object>> getRuleExecutionLogs(Long ruleId, Integer limit);

    /**
     * 清理规则缓存
     * 
     * @param ruleId 规则ID，为null时清理所有缓存
     */
    void clearRuleCache(Long ruleId);

    /**
     * 获取规则性能统计
     * 
     * @param ruleId 规则ID
     * @return 性能统计信息
     */
    Map<String, Object> getRulePerformanceStats(Long ruleId);

    /**
     * 动态注册自定义函数
     * 
     * @param functionName 函数名称
     * @param functionClass 函数实现类
     * @return 注册结果
     */
    boolean registerCustomFunction(String functionName, Class<?> functionClass);

    /**
     * 获取可用的内置函数列表
     * 
     * @return 内置函数列表
     */
    List<String> getBuiltinFunctions();

    /**
     * 获取性能统计信息
     * 
     * @return 性能统计信息
     */
    Map<String, Object> getPerformanceStats();

    /**
     * 清理缓存
     */
    void clearCache();

    /**
     * 执行规则并返回详细结果
     * 
     * @param ruleId 规则ID
     * @param inputData 输入数据
     * @return 执行结果
     */
    Map<String, Object> executeRuleWithResult(Long ruleId, Map<String, Object> inputData);
}