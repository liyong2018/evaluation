package com.evaluate.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.evaluate.service.QLExpressService;
import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * QLExpress规则引擎服务实现类
 * 
 * @author System
 * @since 2025-01-01
 */
@Slf4j
@Service
public class QLExpressServiceImpl implements QLExpressService {

    private ExpressRunner runner;

    @PostConstruct
    public void init() {
        runner = new ExpressRunner();
        // 添加自定义函数
        addCustomFunctions();
    }

    @Override
    public Object execute(String expression, Map<String, Object> context) throws Exception {
        log.debug("执行QLExpress表达式: {}, 上下文: {}", expression, context);

        DefaultContext<String, Object> expressContext = new DefaultContext<>();
        if (context != null) {
            // 将上下文中的所有整数类型转换为Double，避免整数除法精度丢失
            context.forEach((key, value) -> {
                if (value instanceof Integer) {
                    expressContext.put(key, ((Integer) value).doubleValue());
                } else if (value instanceof Long) {
                    expressContext.put(key, ((Long) value).doubleValue());
                } else if (value instanceof Float) {
                    expressContext.put(key, ((Float) value).doubleValue());
                } else if (value instanceof BigDecimal) {
                    expressContext.put(key, ((BigDecimal) value).doubleValue());
                } else {
                    expressContext.put(key, value);
                }
            });
        }

        Object result = runner.execute(expression, expressContext, null, true, false);
        log.debug("表达式执行结果: {}", result);

        return result;
    }

    @Override
    public boolean validate(String expression) {
        try {
            DefaultContext<String, Object> context = new DefaultContext<>();
            // 添加一些测试变量
            context.put("test", 1.0);
            context.put("management_staff", 10);
            context.put("population", 1000);
            
            runner.execute(expression, context, null, true, true);
            return true;
        } catch (Exception e) {
            log.warn("表达式验证失败: {}, 错误: {}", expression, e.getMessage());
            return false;
        }
    }

    @Override
    public String getErrorMessage(String expression, Map<String, Object> context) {
        try {
            execute(expression, context);
            return null;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @Override
    public void addFunction(String functionName, Class<?> functionClass) {
        try {
            // QLExpress需要用实例而不是类
            runner.addFunctionOfClassMethod(functionName, functionClass, functionName, 
                new Class[]{Object[].class}, null);
            log.info("添加自定义函数: {} -> {}", functionName, functionClass.getName());
        } catch (Exception e) {
            log.error("添加自定义函数失败: {}", functionName, e);
        }
    }

    @Override
    public void addOperator(String operatorName, Class<?> operatorClass) {
        try {
            // 这里暂时不实现自定义运算符
            log.info("添加自定义运算符: {} -> {}", operatorName, operatorClass.getName());
        } catch (Exception e) {
            log.error("添加自定义运算符失败: {}", operatorName, e);
        }
    }

    /**
     * 添加自定义函数
     */
    private void addCustomFunctions() {
        try {
            // 添加数学函数 - 使用addFunctionOfClassMethod
            runner.addFunctionOfClassMethod("SQRT", Math.class, "sqrt", new Class[]{double.class}, null);
            runner.addFunctionOfClassMethod("POW", Math.class, "pow", new Class[]{double.class, double.class}, null);
            runner.addFunctionOfClassMethod("ABS", Math.class, "abs", new Class[]{double.class}, null);
            runner.addFunctionOfClassMethod("MAX", Math.class, "max", new Class[]{double.class, double.class}, null);
            runner.addFunctionOfClassMethod("MIN", Math.class, "min", new Class[]{double.class, double.class}, null);
            
            // 添加统计函数
            runner.addFunctionOfClassMethod("AVERAGE", MathFunctions.class, "AVERAGE", new Class[]{Object[].class}, null);
            runner.addFunctionOfClassMethod("STDEV", MathFunctions.class, "STDEV", new Class[]{Object[].class}, null);
            runner.addFunctionOfClassMethod("SUMSQ", MathFunctions.class, "SUMSQ", new Class[]{Object[].class}, null);
            runner.addFunctionOfClassMethod("SUM", MathFunctions.class, "SUM", new Class[]{Object[].class}, null);
            
            // 添加条件函数 - 支持Object类型的条件，会自动转换为布尔值
            runner.addFunctionOfClassMethod("IF", MathFunctions.class, "IF", new Class[]{Object.class, Object.class, Object.class}, null);
            
            log.info("自定义函数初始化完成");
        } catch (Exception e) {
            log.error("自定义函数初始化失败", e);
        }
    }

    /**
     * 自定义数学函数类
     */
    public static class MathFunctions {
        
        /**
         * 计算平均值
         */
        public static double AVERAGE(Object... values) {
            if (values == null || values.length == 0) {
                return 0.0;
            }
            
            double sum = 0.0;
            int count = 0;
            
            for (Object value : values) {
                if (value instanceof Number) {
                    sum += ((Number) value).doubleValue();
                    count++;
                } else if (value instanceof List) {
                    List<?> list = (List<?>) value;
                    for (Object item : list) {
                        if (item instanceof Number) {
                            sum += ((Number) item).doubleValue();
                            count++;
                        }
                    }
                } else if (value instanceof JSONArray) {
                    JSONArray array = (JSONArray) value;
                    for (Object item : array) {
                        if (item instanceof Number) {
                            sum += ((Number) item).doubleValue();
                            count++;
                        }
                    }
                }
            }
            
            return count > 0 ? sum / count : 0.0;
        }
        
        /**
         * 计算标准差
         */
        public static double STDEV(Object... values) {
            if (values == null || values.length == 0) {
                return 0.0;
            }
            
            double mean = AVERAGE(values);
            double sumSquaredDiff = 0.0;
            int count = 0;
            
            for (Object value : values) {
                if (value instanceof Number) {
                    double diff = ((Number) value).doubleValue() - mean;
                    sumSquaredDiff += diff * diff;
                    count++;
                } else if (value instanceof List) {
                    List<?> list = (List<?>) value;
                    for (Object item : list) {
                        if (item instanceof Number) {
                            double diff = ((Number) item).doubleValue() - mean;
                            sumSquaredDiff += diff * diff;
                            count++;
                        }
                    }
                } else if (value instanceof JSONArray) {
                    JSONArray array = (JSONArray) value;
                    for (Object item : array) {
                        if (item instanceof Number) {
                            double diff = ((Number) item).doubleValue() - mean;
                            sumSquaredDiff += diff * diff;
                            count++;
                        }
                    }
                }
            }
            
            return count > 1 ? Math.sqrt(sumSquaredDiff / (count - 1)) : 0.0;
        }
        
        /**
         * 计算平方和
         */
        public static double SUMSQ(Object... values) {
            if (values == null || values.length == 0) {
                return 0.0;
            }
            
            double sumSquared = 0.0;
            
            for (Object value : values) {
                if (value instanceof Number) {
                    double val = ((Number) value).doubleValue();
                    sumSquared += val * val;
                } else if (value instanceof List) {
                    List<?> list = (List<?>) value;
                    for (Object item : list) {
                        if (item instanceof Number) {
                            double val = ((Number) item).doubleValue();
                            sumSquared += val * val;
                        }
                    }
                } else if (value instanceof JSONArray) {
                    JSONArray array = (JSONArray) value;
                    for (Object item : array) {
                        if (item instanceof Number) {
                            double val = ((Number) item).doubleValue();
                            sumSquared += val * val;
                        }
                    }
                }
            }
            
            return sumSquared;
        }
        
        /**
         * 计算求和
         */
        public static double SUM(Object... values) {
            if (values == null || values.length == 0) {
                return 0.0;
            }
            
            double sum = 0.0;
            
            for (Object value : values) {
                if (value instanceof Number) {
                    sum += ((Number) value).doubleValue();
                } else if (value instanceof List) {
                    List<?> list = (List<?>) value;
                    for (Object item : list) {
                        if (item instanceof Number) {
                            sum += ((Number) item).doubleValue();
                        }
                    }
                } else if (value instanceof JSONArray) {
                    JSONArray array = (JSONArray) value;
                    for (Object item : array) {
                        if (item instanceof Number) {
                            sum += ((Number) item).doubleValue();
                        }
                    }
                }
            }
            
            return sum;
        }
        
        /**
         * IF条件函数
         * 用法: IF(条件, 真值, 假值)
         * 示例: IF(a > 0, a, 0) 或 IF(risk_assessment == "是", 1, 0)
         * 
         * @param condition 条件表达式（可以是布尔值、数字或字符串）
         * @param trueValue 条件为真时返回的值
         * @param falseValue 条件为假时返回的值
         * @return 根据条件返回相应的值
         */
        public static Object IF(Object condition, Object trueValue, Object falseValue) {
            // 将条件转换为布尔值
            boolean boolCondition = toBoolean(condition);
            return boolCondition ? trueValue : falseValue;
        }
        
        /**
         * 将任意类型的值转换为布尔值
         */
        private static boolean toBoolean(Object value) {
            if (value == null) {
                return false;
            }
            if (value instanceof Boolean) {
                return (Boolean) value;
            }
            if (value instanceof Number) {
                return ((Number) value).doubleValue() != 0;
            }
            if (value instanceof String) {
                String str = (String) value;
                // 空字符串认为false
                if (str.isEmpty()) {
                    return false;
                }
                // "true", "yes", "1", "是" 等认为true
                str = str.toLowerCase().trim();
                return str.equals("true") || str.equals("yes") || str.equals("1") || str.equals("是");
            }
            // 其他类型，非空则为true
            return true;
        }
    }
}
