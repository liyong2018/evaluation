package com.evaluate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evaluate.entity.EvaluationResult;
import com.evaluate.entity.FieldMappingConfig;
import com.evaluate.mapper.FieldMappingConfigMapper;
import com.evaluate.service.FieldMappingConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 字段映射配置服务实现类
 *
 * @author system
 * @since 2025-10-29
 */
@Slf4j
@Service
public class FieldMappingConfigServiceImpl extends ServiceImpl<FieldMappingConfigMapper, FieldMappingConfig>
        implements FieldMappingConfigService {

    @Autowired
    private FieldMappingConfigMapper fieldMappingConfigMapper;

    // 使用内存缓存提高性能
    private final Map<Long, Map<String, String>> fieldMappingCache = new ConcurrentHashMap<>();

    @Override
    public List<FieldMappingConfig> getByModelId(Long modelId) {
        QueryWrapper<FieldMappingConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("model_id", modelId)
                   .eq("is_active", true)
                   .eq("is_deleted", false)
                   .orderByAsc("sort_order");

        return this.list(queryWrapper);
    }

    @Override
    @Cacheable(value = "fieldMapping", key = "#modelId")
    public Map<String, String> getFieldMappingByModelId(Long modelId) {
        log.debug("获取模型 {} 的字段映射配置", modelId);

        // 先检查内存缓存
        Map<String, String> cachedMapping = fieldMappingCache.get(modelId);
        if (cachedMapping != null) {
            log.debug("从内存缓存中获取到模型 {} 的字段映射，共 {} 个映射", modelId, cachedMapping.size());
            return cachedMapping;
        }

        List<FieldMappingConfig> configs = getByModelId(modelId);
        Map<String, String> mapping = new HashMap<>();

        for (FieldMappingConfig config : configs) {
            mapping.put(config.getAlgorithmOutputField(), config.getDatabaseField());
        }

        // 存入内存缓存
        fieldMappingCache.put(modelId, mapping);

        log.debug("为模型 {} 创建了字段映射，共 {} 个映射", modelId, mapping.size());
        return mapping;
    }

    @Override
    public List<FieldMappingConfig> getByModelIdAndFieldType(Long modelId, String fieldType) {
        return fieldMappingConfigMapper.selectByModelIdAndFieldType(modelId, fieldType);
    }

    @Override
    public void mapFieldsUsingDatabaseConfig(Long modelId, Map<String, Object> algorithmData,
                                            EvaluationResult evaluationResult) {
        log.debug("开始使用数据库配置为模型 {} 映射字段", modelId);

        Map<String, String> fieldMapping = getFieldMappingByModelId(modelId);

        if (fieldMapping.isEmpty()) {
            log.warn("模型 {} 没有找到字段映射配置，使用默认映射", modelId);
            // 使用默认映射作为回退方案
            applyDefaultMapping(algorithmData, evaluationResult);
            return;
        }

        int mappedFields = 0;

        // 使用反射动态设置字段值
        for (Map.Entry<String, String> entry : fieldMapping.entrySet()) {
            String algorithmField = entry.getKey();
            String databaseField = entry.getValue();

            if (algorithmData.containsKey(algorithmField)) {
                Object value = algorithmData.get(algorithmField);

                try {
                    // 将数据库字段名转换为setter方法名
                    String setterMethodName = convertToSetterMethodName(databaseField);

                    // 根据字段类型和值类型选择合适的setter方法
                    Method setterMethod = findSuitableSetterMethod(evaluationResult.getClass(), setterMethodName, value);

                    if (setterMethod != null) {
                        // 转换值类型
                        Object convertedValue = convertValue(value, setterMethod.getParameterTypes()[0]);
                        setterMethod.invoke(evaluationResult, convertedValue);

                        log.debug("成功映射字段: {} -> {} = {}", algorithmField, databaseField, convertedValue);
                        mappedFields++;
                    } else {
                        log.warn("找不到字段 {} 的setter方法: {}", databaseField, setterMethodName);
                    }

                } catch (Exception e) {
                    log.error("映射字段 {} -> {} 时发生错误: {}", algorithmField, databaseField, e.getMessage(), e);
                }
            } else {
                log.debug("算法输出数据中不包含字段: {}", algorithmField);
            }
        }

        log.info("模型 {} 字段映射完成，成功映射 {} 个字段", modelId, mappedFields);
    }

    @Override
    @CacheEvict(value = "fieldMapping", key = "#modelId")
    public void refreshFieldMappingCache(Long modelId) {
        // 清除内存缓存
        fieldMappingCache.remove(modelId);
        log.info("已刷新模型 {} 的字段映射缓存", modelId);
    }

    /**
     * 应用默认映射（作为回退方案）
     */
    private void applyDefaultMapping(Map<String, Object> algorithmData, EvaluationResult evaluationResult) {
        log.info("使用默认字段映射方案");

        // 默认映射关系（保持与原有代码一致）
        Map<String, String> defaultMapping = new HashMap<>();
        defaultMapping.put("disasterMgmtScore", "managementCapabilityScore");
        defaultMapping.put("disasterPrepScore", "supportCapabilityScore");
        defaultMapping.put("selfRescueScore", "selfRescueCapabilityScore");
        defaultMapping.put("comprehensiveScore", "comprehensiveCapabilityScore");
        defaultMapping.put("disasterMgmtGrade", "managementCapabilityLevel");
        defaultMapping.put("disasterPrepGrade", "supportCapabilityLevel");
        defaultMapping.put("selfRescueGrade", "selfRescueCapabilityLevel");
        defaultMapping.put("comprehensiveGrade", "comprehensiveCapabilityLevel");

        for (Map.Entry<String, String> entry : defaultMapping.entrySet()) {
            String algorithmField = entry.getKey();
            String databaseField = entry.getValue();

            if (algorithmData.containsKey(algorithmField)) {
                Object value = algorithmData.get(algorithmField);

                try {
                    String setterMethodName = convertToSetterMethodName(databaseField);
                    Method setterMethod = findSuitableSetterMethod(evaluationResult.getClass(), setterMethodName, value);

                    if (setterMethod != null) {
                        Object convertedValue = convertValue(value, setterMethod.getParameterTypes()[0]);
                        setterMethod.invoke(evaluationResult, convertedValue);
                        log.debug("默认映射成功: {} -> {} = {}", algorithmField, databaseField, convertedValue);
                    }
                } catch (Exception e) {
                    log.error("默认映射字段 {} -> {} 时发生错误: {}", algorithmField, databaseField, e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 将数据库字段名（下划线命名）转换为setter方法名（驼峰命名）
     */
    private String convertToSetterMethodName(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            return fieldName;
        }

        // 将下划线命名转换为驼峰命名，例如: management_capability_level -> managementCapabilityLevel
        StringBuilder camel = new StringBuilder();
        boolean nextUpper = false;

        for (int i = 0; i < fieldName.length(); i++) {
            char currentChar = fieldName.charAt(i);

            if (currentChar == '_') {
                nextUpper = true;
            } else if (nextUpper) {
                camel.append(Character.toUpperCase(currentChar));
                nextUpper = false;
            } else {
                camel.append(currentChar);
            }
        }

        // Java Bean Setter 需要首字母大写: setManagementCapabilityLevel
        if (camel.length() == 0) {
            return "set"; // 退化情况
        }
        char first = Character.toUpperCase(camel.charAt(0));
        String methodName = "set" + first + camel.substring(1);
        return methodName;
    }

    /**
     * 查找合适的setter方法
     */
    private Method findSuitableSetterMethod(Class<?> clazz, String setterMethodName, Object value) {
        try {
            // 首先尝试精确匹配类型的方法
            Class<?> valueType = value != null ? value.getClass() : String.class;
            return clazz.getMethod(setterMethodName, valueType);
        } catch (NoSuchMethodException e) {
            // 如果找不到精确匹配的方法，尝试其他可能的类型
            try {
                return clazz.getMethod(setterMethodName, BigDecimal.class);
            } catch (NoSuchMethodException e2) {
                try {
                    return clazz.getMethod(setterMethodName, String.class);
                } catch (NoSuchMethodException e3) {
                    try {
                        return clazz.getMethod(setterMethodName, Object.class);
                    } catch (NoSuchMethodException e4) {
                        return null;
                    }
                }
            }
        }
    }

    /**
     * 转换值类型
     */
    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }

        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }

        // 转换为BigDecimal
        if (targetType == BigDecimal.class) {
            if (value instanceof Number) {
                return BigDecimal.valueOf(((Number) value).doubleValue());
            } else if (value instanceof String) {
                try {
                    return new BigDecimal((String) value);
                } catch (NumberFormatException e) {
                    log.warn("无法将字符串 '{}' 转换为BigDecimal: {}", value, e.getMessage());
                    return null;
                }
            }
        }

        // 转换为String
        if (targetType == String.class) {
            return value.toString();
        }

        // 其他类型转换
        return value;
    }
}
