# 修复步骤间数据传递问题

## 问题分析

虽然我们已经修复了权重指标代码不匹配的问题，但评估模型仍然失败，错误相同：

```
算法 队伍管理能力定权 执行失败: run QlExpress Exception
NullPointerException: Cannot invoke "Object.getClass()" because "op1" is null
```

### 根本原因

这次的根本原因是**步骤间数据传递机制不完整**。

步骤3的表达式需要使用步骤2的输出：
```
teamManagementNorm * weight_L2_MANAGEMENT_CAPABILITY
```

但是步骤2的输出（`teamManagementNorm`）没有被正确传递到步骤3的执行上下文中。

### 代码分析

在 `ModelExecutionServiceImpl.java` 中：

1. **步骤2执行后**，结果存储在 `globalContext` 中：
   ```java
   globalContext.put("step_INDICATOR_NORMALIZATION", stepResult);
   ```

2. **步骤3执行时**，为每个地区创建 `regionContext`：
   ```java
   Map<String, Object> regionContext = new HashMap<>(inputData);
   ```

3. **问题**：`regionContext` 复制了 `globalContext`，但步骤2的结果是嵌套结构：
   ```
   globalContext {
       "step_INDICATOR_NORMALIZATION": {
           "regionResults": {
               "110101": {
                   "teamManagementNorm": 0.5,
                   "riskAssessmentNorm": 0.6,
                   ...
               }
           }
       }
   }
   ```

4. **缺失的逻辑**：需要从嵌套结构中提取当前区域的输出值，并将它们扁平化到 `regionContext` 中。

## 解决方案

在 `ModelExecutionServiceImpl.java` 中添加了新方法 `loadPreviousStepOutputs()`，用于从前面步骤的结果中提取当前区域的输出值。

### 修改1：调用新方法

在第170行添加调用：

```java
// 加载前面步骤的输出结果到当前区域上下文
loadPreviousStepOutputs(regionContext, regionCode, inputData);
```

### 修改2：添加新方法

在第362-387行添加方法：

```java
/**
 * 加载前面步骤的输出结果到当前区域上下文
 * 从 globalContext 中提取前面步骤的 regionResults，并将当前区域的输出值添加到上下文
 */
private void loadPreviousStepOutputs(Map<String, Object> regionContext, String regionCode, Map<String, Object> globalContext) {
    // 遍历 globalContext 中所有以 "step_" 开头的条目
    for (Map.Entry<String, Object> entry : globalContext.entrySet()) {
        if (entry.getKey().startsWith("step_") && entry.getValue() instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> stepResult = (Map<String, Object>) entry.getValue();
            
            // 获取该步骤的 regionResults
            Object regionResultsObj = stepResult.get("regionResults");
            if (regionResultsObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Map<String, Object>> regionResults = (Map<String, Map<String, Object>>) regionResultsObj;
                
                // 获取当前区域的输出
                Map<String, Object> currentRegionOutputs = regionResults.get(regionCode);
                if (currentRegionOutputs != null) {
                    // 将当前区域的所有输出变量添加到上下文
                    for (Map.Entry<String, Object> output : currentRegionOutputs.entrySet()) {
                        regionContext.put(output.getKey(), output.getValue());
                        log.debug("从前面步骤加载变量: {}={}", output.getKey(), output.getValue());
                    }
                }
            }
        }
    }
}
```

## 应用修复

### 方法1：重启后端应用（推荐）

1. **停止当前运行的Spring Boot应用**
   - 如果在IDE中运行，停止运行配置
   - 如果在命令行运行，按 `Ctrl+C`
   - 或者找到Java进程并终止：
     ```powershell
     # 查找Spring Boot进程
     Get-Process -Name java | Where-Object {$_.StartTime -gt (Get-Date).AddHours(-1)}
     
     # 停止进程（替换 <PID> 为实际进程ID）
     Stop-Process -Id <PID>
     ```

2. **重新编译**
   ```powershell
   mvn clean compile -f C:\Users\Administrator\Development\evaluation\pom.xml
   ```

3. **重新启动应用**
   ```powershell
   mvn spring-boot:run -f C:\Users\Administrator\Development\evaluation\pom.xml
   ```
   或者在IDE中重新运行

### 方法2：热加载（如果IDE支持）

如果使用IntelliJ IDEA或Eclipse并启用了热加载/自动重新加载：
1. IDE会自动检测文件更改
2. 可能需要手动触发"Build Project"或"Reload"

## 验证

重启后端后，重新执行评估模型：

1. 刷新前端页面
2. 选择评估配置
3. 点击"开始评估"

应该能看到：
- ✅ 步骤1（计算指标值）成功
- ✅ 步骤2（属性向量归一化）成功
- ✅ 步骤3（二级指标定权）成功 - 之前失败的步骤
- ✅ 后续步骤继续执行...

## 日志验证

在后端日志中应该能看到类似的调试信息：

```
从前面步骤加载变量: teamManagementNorm=0.5234
从前面步骤加载变量: riskAssessmentNorm=0.6127
...
执行QLExpress表达式: teamManagementNorm * weight_L2_MANAGEMENT_CAPABILITY
表达式执行结果: 0.1937
```

## 总结

这个问题有两层：

1. **第一层（已修复）**：权重指标代码不匹配
   - 表达式使用 `weight_TEAM_MANAGEMENT`
   - 数据库使用 `L2_MANAGEMENT_CAPABILITY`
   - **解决**：更新算法表达式使用正确的代码

2. **第二层（本次修复）**：步骤间数据传递不完整
   - 步骤2的输出没有传递到步骤3
   - **解决**：添加 `loadPreviousStepOutputs()` 方法提取前面步骤的区域输出

两个问题都需要修复才能使评估模型正常工作。

## 文件修改

- ✅ `fix_algorithm_weight_codes.sql` - 已执行
- ✅ `ModelExecutionServiceImpl.java` - 已修改，需要重启应用
