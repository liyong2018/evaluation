# 综合减灾能力计算验证指南

## 问题定位

根据您提供的数据，系统计算的综合减灾能力优劣解值与手工计算不一致。

### 手工计算结果（正确）
| 乡镇名称 | 最优距离 | 最劣距离 |
|---------|---------|---------|
| 青竹街道 | 0.24959091 | 0.106251 |
| 汉阳镇 | 0.19743867 | 0.1094355 |
| 瑞峰镇 | 0.12194812 | 0.2146261 |
| 西龙镇 | 0.25254899 | 0.0276086 |
| 高台镇 | 0.23341046 | 0.0883564 |
| 白果乡 | 0.24344524 | 0.0367315 |
| 罗波乡 | 0.18121838 | 0.1454442 |

### 系统计算结果（错误）
| 乡镇名称 | 最优距离 | 最劣距离 |
|---------|---------|---------|
| 青竹街道 | 0.289425003935 | 0.011637250691 |
| 汉阳镇 | 0.236012464280 | 0.106705642492 |
| 瑞峰镇 | 0.181836740017 | 0.211622702921 |
| 西龙镇 | 0.229273979968 | 0.156764172237 |
| 高台镇 | 0.261344631217 | 0.088156717484 |
| 白果乡 | 0.272273372247 | 0.034683795088 |
| 罗波乡 | 0.221060548420 | 0.144130958086 |

## 可能的原因

### 1. 定权数据不正确
检查步骤3（二级指标定权）的输出数据是否正确：

应该使用的数据（综合定权：归一化值 × 一级权重 × 二级权重）：

| 乡镇 | 队伍管理 | 风险评估 | 财政投入 | 物资储备 | 医疗保障 | 自救互救 | 公众避险 | 转移安置 |
|-----|---------|---------|---------|---------|---------|---------|---------|---------|
| 青竹街道 | 0.00036933 | 0.03866577 | 0.00095138 | 0.00512831 | 0.12587605 | 0.01395918 | 0.00123692 | 0.00101204 |
| 汉阳镇 | 0.00596862 | 0.03866577 | 0.05381293 | 0.02762595 | 0.04431071 | 0.05648262 | 0.06425255 | 0.03925304 |
| 瑞峰镇 | 0.11949577 | 0.03866577 | 0.03729360 | 0.14181789 | 0.05583334 | 0.05169684 | 0.09114598 | 0.01964680 |
| 西龙镇 | 0.00269100 | 0.03866577 | 0.00693199 | 0.02906252 | 0.01997782 | 0.01036923 | 0.01030000 | 0.00737397 |
| 高台镇 | 0.00548545 | 0.03866577 | 0.03285337 | 0.00846319 | 0.02591507 | 0.08159547 | 0.02204578 | 0.02254715 |
| 白果乡 | 0.00279607 | 0.03866577 | 0.00720265 | 0.03451115 | 0.03208030 | 0.00530783 | 0.01070216 | 0.01532377 |
| 罗波乡 | 0.02341494 | 0.03866577 | 0.07539587 | 0.06020930 | 0.03950708 | 0.02211394 | 0.01400351 | 0.10693742 |

### 2. 最大最小值计算错误
验证最大值和最小值是否正确：

| 指标 | 最大值 | 最小值 |
|-----|--------|--------|
| 队伍管理能力 | 0.119495775 | 0.000369326 |
| 风险评估能力 | 0.038665766 | 0.038665766 |
| 财政投入能力 | 0.075395865 | 0.000951381 |
| 物资储备能力 | 0.141817886 | 0.005128308 |
| 医疗保障能力 | 0.12587605 | 0.019977818 |
| 自救互救能力 | 0.081595469 | 0.005307827 |
| 公众避险能力 | 0.091145977 | 0.00123692 |
| 转移安置能力 | 0.106937416 | 0.00101204 |

## 调试步骤

### 步骤1：检查后端日志
1. 重启后端服务
2. 运行评估算法
3. 查看日志中以下内容：
   - 定权值计算日志
   - TOPSIS计算日志
   - 最大最小值日志

### 步骤2：在Java代码中添加调试日志

在 `AlgorithmExecutionServiceImpl.java` 的 `calculateComprehensiveTOPSIS` 方法（约2001行）中添加日志：

```java
private double calculateComprehensiveTOPSIS(Map<String, Double> currentWeightedValues, 
                                           Map<String, Map<String, Double>> allWeightedValues) {
    
    // 添加调试日志
    log.info("[综合TOPSIS调试] currentWeightedValues: {}", currentWeightedValues);
    log.info("[综合TOPSIS调试] allWeightedValues size: {}", allWeightedValues.size());
    
    // 计算各指标的最大值和最小值
    Map<String, Double> maxValues = new HashMap<>();
    Map<String, Double> minValues = new HashMap<>();
    
    String[] indicators = {"teamManagement", "riskAssessment", "financialInput", 
                          "materialReserve", "medicalSupport", "selfRescue", 
                          "publicAvoidance", "relocationCapacity"};
    
    for (String indicator : indicators) {
        double max = allWeightedValues.values().stream()
            .mapToDouble(values -> values.getOrDefault(indicator, 0.0))
            .max().orElse(0.0);
        double min = allWeightedValues.values().stream()
            .mapToDouble(values -> values.getOrDefault(indicator, 0.0))
            .min().orElse(0.0);
        
        maxValues.put(indicator, max);
        minValues.put(indicator, min);
        
        // 添加调试日志
        log.info("[综合TOPSIS调试] {}: max={}, min={}, current={}", 
                indicator, max, min, currentWeightedValues.getOrDefault(indicator, 0.0));
    }
    
    // ... 继续计算
}
```

### 步骤3：验证定权值计算

在 `calculateCurrentWeightedValues` 方法（约1809行）中添加日志：

```java
private Map<String, Double> calculateCurrentWeightedValues(SurveyData surveyData, List<String> regionIds) {
    Map<String, Double> currentWeightedValues = new HashMap<>();
    
    // ... 计算过程 ...
    
    // 添加调试日志
    log.info("[定权计算调试] 地区: {}", surveyData.getTownship());
    log.info("[定权计算调试] teamManagementWeighted: {}", teamManagementWeighted);
    log.info("[定权计算调试] riskAssessmentWeighted: {}", riskAssessmentWeighted);
    log.info("[定权计算调试] financialInputWeighted: {}", financialInputWeighted);
    log.info("[定权计算调试] materialReserveWeighted: {}", materialReserveWeighted);
    log.info("[定权计算调试] medicalSupportWeighted: {}", medicalSupportWeighted);
    log.info("[定权计算调试] selfRescueWeighted: {}", selfRescueWeighted);
    log.info("[定权计算调试] publicAvoidanceWeighted: {}", publicAvoidanceWeighted);
    log.info("[定权计算调试] relocationCapacityWeighted: {}", relocationCapacityWeighted);
    
    return currentWeightedValues;
}
```

## 下一步操作

1. **重启后端服务**：确保数据库修改生效
2. **清理缓存**：如果有Redis或其他缓存，清理它
3. **重新运行评估**：对所有乡镇重新执行评估
4. **查看日志**：检查调试日志，对比计算值与预期值
5. **提供日志**：如果问题仍存在，请提供后端日志以便进一步诊断

## 快速检查清单

- [ ] 数据库配置已更新（ID 61, 62的ql_expression已改为xxxWeighted）
- [ ] 后端服务已重启
- [ ] 缓存已清理
- [ ] 重新执行了评估算法
- [ ] 检查了后端日志
- [ ] 验证了步骤3的定权数据是否正确

---

*创建时间：2025-01-24*