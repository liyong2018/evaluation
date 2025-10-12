package com.evaluate.service;

import java.util.Map;

/**
 * QLExpress规则引擎服务接口
 * 
 * @author System
 * @since 2025-01-01
 */
public interface QLExpressService {

    /**
     * 执行QLExpress表达式
     * 
     * @param expression QLExpress表达式
     * @param context 执行上下文参数
     * @return 执行结果
     * @throws Exception 执行异常
     */
    Object execute(String expression, Map<String, Object> context) throws Exception;

    /**
     * 验证QLExpress表达式语法
     * 
     * @param expression QLExpress表达式
     * @return 验证结果
     */
    boolean validate(String expression);

    /**
     * 获取表达式执行错误信息
     * 
     * @param expression QLExpress表达式
     * @param context 执行上下文参数
     * @return 错误信息，如果没有错误返回null
     */
    String getErrorMessage(String expression, Map<String, Object> context);

    /**
     * 添加自定义函数
     * 
     * @param functionName 函数名
     * @param functionClass 函数类
     */
    void addFunction(String functionName, Class<?> functionClass);

    /**
     * 添加自定义运算符
     * 
     * @param operatorName 运算符名
     * @param operatorClass 运算符类
     */
    void addOperator(String operatorName, Class<?> operatorClass);
}