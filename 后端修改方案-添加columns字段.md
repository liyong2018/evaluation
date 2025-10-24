# 后端修改方案：添加 columns 字段（包含 stepOrder）

## 问题分析

当前后端接口返回的数据结构如下：
```json
{
  "stepId": 1,
  "stepName": "评估指标赋值",
  "stepOrder": 1,
  "tableData": [
    {
      "regionCode": "110000",
      "regionName": "北京市",
      "队伍管理能力计算": 0.8,
      "风险评估能力计算": 0.7
    }
  ]
}
```

**问题：缺少 `columns` 字段**，前端无法知道每一列属于哪个步骤。

前端需要的数据结构：
```json
{
  "stepId": 1,
  "stepName": "评估指标赋值",
  "stepOrder": 1,
  "columns": [
    {"prop": "regionCode", "label": "地区代码", "width": 150},
    {"prop": "regionName", "label": "地区名称", "width": 120},
    {"prop": "队伍管理能力计算", "label": "队伍管理能力计算", "width": 120, "stepOrder": 1},
    {"prop": "风险评估能力计算", "label": "风险评估能力计算", "width": 120, "stepOrder": 1}
  ],
  "tableData": [...]
}
```

## 解决方案

### 方案 1：在 executeAlgorithmStep 方法中添加 columns 字段（推荐）

修改文件：`ModelExecutionServiceImpl.java`

在 `executeAlgorithmStep` 方法的末尾，生成 columns 数组：

```java
@Override
public Map<String, Object> executeAlgorithmStep(Long algorithmId, Integer stepOrder, List<String> regionCodes, Long weightConfigId) {
    log.info("执行算法步骤, algorithmId={}, stepOrder={}, regionCodes.size={}", algorithmId, stepOrder, regionCodes.size());

    try {
        // ... 现有的执行逻辑 ...
        
        // 5. 生成该步骤的2D表格数据
        List<Map<String, Object>> tableData = generateStepResultTable(stepExecutionResult, regionCodes);

        // ==================== 新增：生成 columns 数组 ====================
        List<Map<String, Object>> columns = generateColumnsWithStepOrder(tableData, stepOrder);
        // =================================================================

        // 6. 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("stepId", targetStep.getId());
        result.put("stepName", targetStep.getStepName());
        result.put("stepOrder", stepOrder);
        result.put("stepCode", targetStep.getStepCode());
        result.put("description", targetStep.getStepDescription());
        result.put("executionResult", stepExecutionResult);
        result.put("tableData", tableData);
        result.put("columns", columns);  // 新增
        result.put("success", true);
        result.put("executionTime", new Date());

        log.info("算法步骤 {} 执行完成，生成 {} 行表格数据", stepOrder, tableData.size());
        return result;

    } catch (Exception e) {
        log.error("执行算法步骤失败", e);
        throw new RuntimeException("执行算法步骤失败: " + e.getMessage(), e);
    }
}

/**
 * 从表格数据生成 columns 数组，并为非基础列添加 stepOrder
 */
private List<Map<String, Object>> generateColumnsWithStepOrder(
        List<Map<String, Object>> tableData, Integer stepOrder) {
    
    List<Map<String, Object>> columns = new ArrayList<>();
    
    if (tableData == null || tableData.isEmpty()) {
        return columns;
    }
    
    // 从第一行数据提取所有列名
    Map<String, Object> firstRow = tableData.get(0);
    Set<String> baseColumns = new HashSet<>(Arrays.asList("regionCode", "regionName", "region"));
    
    for (String columnName : firstRow.keySet()) {
        Map<String, Object> column = new LinkedHashMap<>();
        column.put("prop", columnName);
        column.put("label", columnName);  // 使用中文名称作为 label
        
        // 设置列宽
        if ("regionCode".equals(columnName)) {
            column.put("width", 150);
        } else if ("regionName".equals(columnName) || "region".equals(columnName)) {
            column.put("width", 120);
        } else {
            column.put("width", 120);
            // 非基础列添加 stepOrder
            column.put("stepOrder", stepOrder);
        }
        
        columns.add(column);
        log.debug("生成列: prop={}, label={}, stepOrder={}", 
                columnName, columnName, baseColumns.contains(columnName) ? "null" : stepOrder);
    }
    
    return columns;
}
```

### 方案 2：在 executeModel 方法中添加完整的 columns 数组（含所有步骤）

修改文件：`ModelExecutionServiceImpl.java`

在 `executeModel` 方法中，需要生成包含所有步骤输出的 columns 数组：

```java
@Override
@Transactional(rollbackFor = Exception.class)
public Map<String, Object> executeModel(Long modelId, List<String> regionCodes, Long weightConfigId) {
    log.info("开始执行评估模型, modelId={}, regionCodes={}, weightConfigId={}", 
            modelId, regionCodes, weightConfigId);

    // ... 现有的执行逻辑 ...
    
    // 5. 按顺序执行每个步骤
    Map<String, Object> stepResults = new HashMap<>();
    Map<Integer, List<String>> stepOutputParams = new LinkedHashMap<>();  // 新增：记录每个步骤的输出参数
    
    for (ModelStep step : steps) {
        log.info("执行步骤: {} - {}, order={}", step.getStepCode(), step.getStepName(), step.getStepOrder());
        
        try {
            // 执行单个步骤
            Map<String, Object> stepResult = executeStep(step.getId(), regionCodes, globalContext);
            stepResults.put(step.getStepCode(), stepResult);
            
            // 记录该步骤的输出参数
            Map<String, String> outputToAlgorithmName = 
                    (Map<String, String>) stepResult.get("outputToAlgorithmName");
            if (outputToAlgorithmName != null) {
                stepOutputParams.put(step.getStepOrder(), new ArrayList<>(outputToAlgorithmName.values()));
            }
            
            // 将步骤结果合并到全局上下文（供后续步骤使用）
            globalContext.put("step_" + step.getStepCode(), stepResult);
            
            log.info("步骤 {} 执行完成", step.getStepCode());
        } catch (Exception e) {
            log.error("步骤 {} 执行失败: {}", step.getStepCode(), e.getMessage(), e);
            throw new RuntimeException("步骤 " + step.getStepName() + " 执行失败: " + e.getMessage(), e);
        }
    }

    // 生成二维表数据
    List<Map<String, Object>> tableData = generateResultTable(
            Collections.singletonMap("stepResults", stepResults));
    
    // ==================== 新增：生成 columns 数组 ====================
    List<Map<String, Object>> columns = generateColumnsWithAllSteps(tableData, stepOutputParams);
    // =================================================================

    // 6. 构建最终结果
    Map<String, Object> result = new HashMap<>();
    result.put("modelId", modelId);
    result.put("modelName", model.getModelName());
    result.put("executionTime", new Date());
    result.put("stepResults", stepResults);
    result.put("tableData", tableData);  // 新增
    result.put("columns", columns);      // 新增
    result.put("success", true);

    log.info("评估模型执行完成");
    return result;
}

/**
 * 从表格数据和步骤输出参数生成 columns 数组，每列标记所属步骤
 */
private List<Map<String, Object>> generateColumnsWithAllSteps(
        List<Map<String, Object>> tableData, 
        Map<Integer, List<String>> stepOutputParams) {
    
    List<Map<String, Object>> columns = new ArrayList<>();
    
    if (tableData == null || tableData.isEmpty()) {
        return columns;
    }
    
    // 从第一行数据提取所有列名
    Map<String, Object> firstRow = tableData.get(0);
    Set<String> baseColumns = new HashSet<>(Arrays.asList("regionCode", "regionName", "region"));
    
    // 创建反向映射：列名 -> 步骤序号
    Map<String, Integer> columnToStepOrder = new HashMap<>();
    for (Map.Entry<Integer, List<String>> entry : stepOutputParams.entrySet()) {
        Integer stepOrder = entry.getKey();
        List<String> outputNames = entry.getValue();
        for (String outputName : outputNames) {
            columnToStepOrder.put(outputName, stepOrder);
        }
    }
    
    for (String columnName : firstRow.keySet()) {
        Map<String, Object> column = new LinkedHashMap<>();
        column.put("prop", columnName);
        column.put("label", columnName);
        
        // 设置列宽
        if ("regionCode".equals(columnName)) {
            column.put("width", 150);
        } else if ("regionName".equals(columnName) || "region".equals(columnName)) {
            column.put("width", 120);
        } else {
            column.put("width", 120);
            // 非基础列添加 stepOrder
            Integer stepOrder = columnToStepOrder.get(columnName);
            if (stepOrder != null) {
                column.put("stepOrder", stepOrder);
                log.debug("列 {} 属于步骤 {}", columnName, stepOrder);
            }
        }
        
        columns.add(column);
    }
    
    return columns;
}
```

## 实施步骤

### 第一步：修改单步骤执行接口（executeAlgorithmStep）

1. 打开文件：`src/main/java/com/evaluate/service/impl/ModelExecutionServiceImpl.java`

2. 在 `executeAlgorithmStep` 方法中，找到构建返回结果的部分（约第535行）

3. 在 `result.put("tableData", tableData);` 之后添加：
   ```java
   // 生成 columns 数组
   List<Map<String, Object>> columns = generateColumnsWithStepOrder(tableData, stepOrder);
   result.put("columns", columns);
   ```

4. 在类的末尾（约第887行之前）添加辅助方法 `generateColumnsWithStepOrder`

### 第二步：修改模型执行接口（executeModel）

1. 在 `executeModel` 方法中，找到执行步骤的循环（约第99-115行）

2. 在循环开始前添加：
   ```java
   Map<Integer, List<String>> stepOutputParams = new LinkedHashMap<>();
   ```

3. 在循环内部，记录每个步骤的输出参数（在第108行附近）：
   ```java
   Map<String, String> outputToAlgorithmName = 
           (Map<String, String>) stepResult.get("outputToAlgorithmName");
   if (outputToAlgorithmName != null) {
       stepOutputParams.put(step.getStepOrder(), new ArrayList<>(outputToAlgorithmName.values()));
   }
   ```

4. 在构建最终结果之前（第117行之前），生成表格数据和列：
   ```java
   List<Map<String, Object>> tableData = generateResultTable(
           Collections.singletonMap("stepResults", stepResults));
   List<Map<String, Object>> columns = generateColumnsWithAllSteps(tableData, stepOutputParams);
   ```

5. 在结果中添加 tableData 和 columns：
   ```java
   result.put("tableData", tableData);
   result.put("columns", columns);
   ```

6. 在类的末尾添加辅助方法 `generateColumnsWithAllSteps`

### 第三步：测试验证

1. 重新编译后端代码
2. 启动后端服务
3. 在前端执行算法步骤
4. 查看浏览器控制台日志，确认：
   - `allColumnsList` 中每个列对象包含 `stepOrder` 字段
   - `检测到列数据中包含stepOrder字段，使用该字段进行分组`
   - 各步骤的列数量正确
   - 不再出现"其他输出"分组（或该分组为空）

## 预期效果

修改完成后，后端返回的数据将包含明确的 `columns` 数组，每个非基础列都标记了 `stepOrder`：

```json
{
  "stepOrder": 1,
  "stepName": "评估指标赋值",
  "columns": [
    {"prop": "regionCode", "label": "地区代码", "width": 150},
    {"prop": "regionName", "label": "地区名称", "width": 120},
    {"prop": "队伍管理能力计算", "label": "队伍管理能力计算", "width": 120, "stepOrder": 1},
    {"prop": "风险评估能力计算", "label": "风险评估能力计算", "width": 120, "stepOrder": 1},
    {"prop": "财政投入能力计算", "label": "财政投入能力计算", "width": 120, "stepOrder": 1}
  ],
  "tableData": [...]
}
```

前端将能够：
1. 识别 `columns` 数组中的 `stepOrder` 字段
2. 正确地将列分组到对应的步骤
3. 显示准确的步骤名称和列数量
4. 不再依赖关键字匹配的临时方案

## 注意事项

1. **导入语句**：确保添加了必要的导入：
   ```java
   import java.util.Arrays;
   import java.util.Collections;
   ```

2. **向后兼容**：如果有其他地方调用这些方法，确保它们能处理新增的 `columns` 字段（通常前端会忽略不需要的字段）

3. **步骤顺序**：确保 `stepOrder` 从 1 开始，并且连续

4. **列名一致性**：确保 `prop` 和 `label` 使用相同的名称（中文算法名称），这样前端能正确显示

5. **日志调试**：建议在生成 columns 时添加详细的日志，便于调试
