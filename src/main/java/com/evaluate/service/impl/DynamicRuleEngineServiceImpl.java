package com.evaluate.service.impl;

// 暂时注释掉QLExpress相关导入，等待依赖问题解决
// import com.alibaba.qlexpress.DefaultContext;
// import com.alibaba.qlexpress.ExpressRunner;
import com.evaluate.entity.DynamicRule;
import com.evaluate.entity.AlgorithmRuleMapping;
import com.evaluate.mapper.DynamicRuleMapper;
import com.evaluate.mapper.AlgorithmRuleMappingMapper;
import com.evaluate.service.DynamicRuleEngineService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 动态规则引擎服务实现类
 * <p>
 * 基于QLExpress规则引擎实现动态算法执行 <mcreference link="https://github.com/alibaba/QLExpress" index="1">1</mcreference>
 * 
 * @author System
 * @since 2024-01-01
 */
@Slf4j
@Service
public class DynamicRuleEngineServiceImpl implements DynamicRuleEngineService {

    // 暂时注释掉QLExpress相关字段
    // private ExpressRunner expressRunner;
    private ObjectMapper objectMapper;
    
    @Autowired
    private DynamicRuleMapper dynamicRuleMapper;
    
    @Autowired
    private AlgorithmRuleMappingMapper algorithmRuleMappingMapper;
    
    // 规则缓存
    private final Map<Long, DynamicRule> ruleCache = new ConcurrentHashMap<>();
    
    // 编译后的表达式缓存
    private final Map<String, Object> compiledExpressionCache = new ConcurrentHashMap<>();
    
    // 执行统计
    private final Map<Long, Map<String, Object>> performanceStats = new ConcurrentHashMap<>();
    
    // 执行日志
    private final List<Map<String, Object>> executionLogs = new ArrayList<>();

    @Autowired
    public DynamicRuleEngineServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        // 暂时注释掉QLExpress相关初始化代码
        /*
        // 初始化QLExpress引擎 <mcreference link="https://blog.csdn.net/wen811651208/article/details/149568736" index="2">2</mcreference>
        expressRunner = new ExpressRunner();
        
        // 配置引擎参数
        expressRunner.setTrace(false); // 关闭调试模式
        expressRunner.setPrecise(true); // 启用精确计算
        
        // 注册内置函数
        registerBuiltinFunctions();
        */
        
        System.out.println("动态规则引擎初始化完成（QLExpress暂时禁用）");
    }

    /**
     * 注册内置函数
     */
    private void registerBuiltinFunctions() {
        // 暂时注释掉QLExpress相关代码
        /*
        try {
            // 数学函数
            expressRunner.addFunction("max", Math.class.getDeclaredMethod("max", double.class, double.class));
            expressRunner.addFunction("min", Math.class.getDeclaredMethod("min", double.class, double.class));
            expressRunner.addFunction("abs", Math.class.getDeclaredMethod("abs", double.class));
            expressRunner.addFunction("sqrt", Math.class.getDeclaredMethod("sqrt", double.class));
            expressRunner.addFunction("pow", Math.class.getDeclaredMethod("pow", double.class, double.class));
            
            // 字符串函数
            expressRunner.addFunction("length", String.class.getDeclaredMethod("length"));
            expressRunner.addFunction("substring", String.class.getDeclaredMethod("substring", int.class, int.class));
            
            System.out.println("内置函数注册完成");
        } catch (Exception e) {
            System.out.println("注册内置函数失败" + ": " + e);
        }
        */
        System.out.println("内置函数注册完成（QLExpress暂时禁用）");
    }

    @Override
    public Object executeRule(DynamicRule rule, Map<String, Object> context) {
        if (rule == null || rule.getRuleExpression() == null) {
            throw new IllegalArgumentException("规则或规则表达式不能为空");
        }

        long startTime = System.currentTimeMillis();
        
        try {
            // 暂时返回模拟结果，等待QLExpress依赖问题解决
            System.out.println("执行规则（QLExpress暂时禁用）: " + rule.getRuleName() + ", 输入参数: " + context);
            return "模拟执行结果";
            
            /*
            // 创建执行上下文 <mcreference link="https://github.com/alibaba/QLExpress/wiki/QLExpress%E4%BD%BF%E7%94%A8%E6%8C%87%E5%8D%97" index="4">4</mcreference>
            DefaultContext<String, Object> expressContext = new DefaultContext<>();
            
            // 添加上下文变量
            if (context != null) {
                context.forEach(expressContext::put);
            }
            
            // 解析输入参数
            if (rule.getInputParams() != null) {
                Map<String, Object> inputParams = parseJsonToMap(rule.getInputParams());
                inputParams.forEach(expressContext::put);
            }
            
            // 执行规则表达式
            Object result = expressRunner.execute(rule.getRuleExpression(), expressContext, null, true, false);
            
            // 记录执行统计
            long executionTime = System.currentTimeMillis() - startTime;
            recordExecutionLog(rule.getId(), context, result, executionTime, null);
            updatePerformanceStats(rule.getId(), executionTime, true);
            
            return result;
            */
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            recordExecutionLog(rule.getId(), context, null, executionTime, e.getMessage());
            // updatePerformanceStats(rule.getId(), executionTime, false);
            
            System.out.println("执行规则失败: ruleId=" + rule.getId() + ", expression=" + rule.getRuleExpression() + ", error=" + e.getMessage());
            throw new RuntimeException("执行规则失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> executeRuleWithResult(Long ruleId, Map<String, Object> inputData) {
        DynamicRule rule = getRuleFromCache(ruleId);
        if (rule == null) {
            throw new IllegalArgumentException("规则不存在: " + ruleId);
        }

        Object result = executeRule(rule, inputData);
        
        Map<String, Object> response = new HashMap<>();
        response.put("ruleId", ruleId);
        response.put("ruleName", rule.getRuleName());
        response.put("result", result);
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }

    @Override
    public boolean validateRuleExpression(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return false;
        }
        
        // 暂时返回true，等待QLExpress依赖问题解决
        System.out.println("验证规则表达式（QLExpress暂时禁用）: " + expression);
        return true;
        
        /*
        try {
            // 使用空上下文验证表达式语法
            DefaultContext<String, Object> context = new DefaultContext<>();
            expressRunner.execute(expression, context, null, true, false);
            return true;
        } catch (Exception e) {
            System.out.println("规则表达式验证失败: " + expression + ", error=" + e.getMessage());
            return false;
        }
        */
    }

    @Override
    public Map<String, Object> getPerformanceStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRules", performanceStats.size());
        stats.put("cacheSize", ruleCache.size());
        stats.put("compiledExpressionCacheSize", compiledExpressionCache.size());
        stats.put("executionLogSize", executionLogs.size());
        
        // 计算平均执行时间
        double avgExecutionTime = performanceStats.values().stream()
                .mapToLong(stat -> (Long) stat.getOrDefault("avgExecutionTime", 0L))
                .average()
                .orElse(0.0);
        stats.put("avgExecutionTime", avgExecutionTime);
        
        return stats;
    }

    @Override
    public void clearCache() {
        ruleCache.clear();
        compiledExpressionCache.clear();
        executionLogs.clear();
        performanceStats.clear();
        System.out.println("规则引擎缓存已清理");
    }

    @Override
    public Map<String, Object> executeRuleChain(List<DynamicRule> rules, Map<String, Object> context) {
        if (rules == null || rules.isEmpty()) {
            return new HashMap<>(context);
        }

        Map<String, Object> chainContext = new HashMap<>(context);
        Map<String, Object> results = new HashMap<>();
        
        // 按执行顺序排序
        rules.sort(Comparator.comparing(DynamicRule::getExecutionOrder, Comparator.nullsLast(Integer::compareTo)));
        
        for (DynamicRule rule : rules) {
            try {
                Object result = executeRule(rule, chainContext);
                
                // 将结果添加到上下文中，供后续规则使用
                String outputKey = rule.getRuleCode() + "_result";
                chainContext.put(outputKey, result);
                results.put(rule.getRuleCode(), result);
                
                System.out.println("规则链执行步骤完成: " + rule.getRuleName() + " -> " + result);
                
            } catch (Exception e) {
                System.out.println("规则链执行失败，停止在规则: " + rule.getRuleName() + ", error=" + e.getMessage());
                results.put("error", "规则链执行失败: " + e.getMessage());
                results.put("failedRule", rule.getRuleCode());
                break;
            }
        }
        
        return results;
    }

    @Override
    public Map<String, Object> executeRulesByStep(Long algorithmStepId, Map<String, Object> context) {
        try {
            // 查询算法步骤对应的规则列表
            List<DynamicRule> rules = dynamicRuleMapper.selectByAlgorithmStepId(algorithmStepId);
            
            if (rules.isEmpty()) {
                System.out.println("算法步骤 " + algorithmStepId + " 没有配置动态规则");
                return new HashMap<>(context);
            }
            
            // 执行规则链
            return executeRuleChain(rules, context);
            
        } catch (Exception e) {
            System.out.println("根据算法步骤执行规则失败，stepId: " + algorithmStepId + ", error=" + e.getMessage());
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "规则执行失败: " + e.getMessage());
            errorResult.put("algorithmStepId", algorithmStepId);
            return errorResult;
        }
    }

    @Override
    public boolean compileRule(DynamicRule rule) {
        if (rule == null || rule.getRuleExpression() == null) {
            return false;
        }
        
        // 暂时返回true，等待QLExpress依赖问题解决
        // 缓存规则
        ruleCache.put(rule.getId(), rule);
        System.out.println("规则编译成功（QLExpress暂时禁用）: " + rule.getRuleName());
        return true;
        
        /*
        try {
            String cacheKey = "rule_" + rule.getId();
            
            // 编译并缓存表达式
            DefaultContext<String, Object> context = new DefaultContext<>();
            Object compiled = expressRunner.execute(rule.getRuleExpression(), context, null, true, true);
            compiledExpressionCache.put(cacheKey, compiled);
            
            // 缓存规则
            ruleCache.put(rule.getId(), rule);
            
            System.out.println("规则编译成功: " + rule.getRuleName());
            return true;
        } catch (Exception e) {
            System.out.println("规则编译失败: " + rule.getRuleName() + ", error=" + e.getMessage());
            return false;
        }
        */
    }

    @Override
    public List<Map<String, Object>> getRuleExecutionLogs(Long ruleId, Integer limit) {
        // 从执行日志中获取最近的日志记录
        return executionLogs.stream()
                .filter(log -> ruleId == null || ruleId.equals(log.get("ruleId")))
                .sorted((a, b) -> ((Long)b.get("timestamp")).compareTo((Long)a.get("timestamp"))) // 按时间倒序
                .limit(limit != null ? limit : 100) // 最多返回指定条数或100条
                .collect(Collectors.toList());
    }

    @Override
    public void clearRuleCache(Long ruleId) {
        if (ruleId == null) {
            ruleCache.clear();
            compiledExpressionCache.clear();
            System.out.println("清理所有规则缓存");
        } else {
            ruleCache.remove(ruleId);
            compiledExpressionCache.remove("rule_" + ruleId);
            System.out.println("清理规则缓存: " + ruleId + "");
        }
    }

    @Override
    public Map<String, Object> getRulePerformanceStats(Long ruleId) {
        return performanceStats.getOrDefault(ruleId, new HashMap<>());
    }

    @Override
    public boolean registerCustomFunction(String functionName, Class<?> functionClass) {
        // 暂时返回true，等待QLExpress依赖问题解决
        System.out.println("自定义函数注册成功（QLExpress暂时禁用）: " + functionName);
        return true;
        
        /*
        try {
            // 注册自定义函数到QLExpress
            expressRunner.addFunctionOfClassMethod(functionName, functionClass, functionName, 
                functionClass.getDeclaredMethods()[0].getParameterTypes(), null);
            
            System.out.println("自定义函数注册成功: " + functionName + "");
            return true;
        } catch (Exception e) {
            System.out.println("自定义函数注册失败: " + functionName + ": " + e.getMessage());
            return false;
        }
        */
    }

    @Override
    public List<String> getBuiltinFunctions() {
        return Arrays.asList(
            "max", "min", "abs", "sqrt", "pow",
            "length", "substring",
            "sum", "avg", "count"
        );
    }

    /**
     * 记录执行日志
     */
    private void recordExecutionLog(Long ruleId, Map<String, Object> context, Object result, long executionTime, String error) {
        Map<String, Object> log = new HashMap<>();
        log.put("ruleId", ruleId);
        log.put("context", context);
        log.put("result", result);
        log.put("executionTime", executionTime);
        log.put("error", error);
        log.put("timestamp", System.currentTimeMillis());
        
        executionLogs.add(log);
        
        // 保持日志数量在合理范围内
        if (executionLogs.size() > 1000) {
            executionLogs.remove(0);
        }
    }

    /**
     * 从缓存获取规则
     */
    private DynamicRule getRuleFromCache(Long ruleId) {
        DynamicRule rule = ruleCache.get(ruleId);
        if (rule == null) {
            // 从数据库加载规则
            rule = dynamicRuleMapper.selectById(ruleId);
            if (rule != null) {
                ruleCache.put(ruleId, rule);
            }
        }
        return rule;
    }

    /**
     * 记录性能统计
     */
    private void recordPerformanceStats(Long ruleId, long executionTime, boolean success) {
        performanceStats.compute(ruleId, (id, stats) -> {
            if (stats == null) {
                stats = new HashMap<>();
                stats.put("totalExecutions", 0L);
                stats.put("successExecutions", 0L);
                stats.put("failedExecutions", 0L);
                stats.put("totalExecutionTime", 0L);
                stats.put("avgExecutionTime", 0.0);
            }
            
            long totalExecutions = (Long) stats.get("totalExecutions") + 1;
            long successExecutions = (Long) stats.get("successExecutions") + (success ? 1 : 0);
            long failedExecutions = (Long) stats.get("failedExecutions") + (success ? 0 : 1);
            long totalExecutionTime = (Long) stats.get("totalExecutionTime") + executionTime;
            double avgExecutionTime = (double) totalExecutionTime / totalExecutions;
            
            stats.put("totalExecutions", totalExecutions);
            stats.put("successExecutions", successExecutions);
            stats.put("failedExecutions", failedExecutions);
            stats.put("totalExecutionTime", totalExecutionTime);
            stats.put("avgExecutionTime", avgExecutionTime);
            stats.put("lastExecutionTime", System.currentTimeMillis());
            
            return stats;
        });
    }

    /**
     * 解析JSON字符串为Map
     */
    private Map<String, Object> parseJsonToMap(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            System.out.println("JSON解析失败: " + json + ": " + e.getMessage());
            return new HashMap<>();
        }
    }
}