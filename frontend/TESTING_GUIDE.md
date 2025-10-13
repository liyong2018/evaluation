# ResultDialog 列分组优化 - 测试和调试指南

## 快速开始

### 1. 访问应用
打开浏览器访问：`http://localhost:5173`

### 2. 打开开发者工具
按 `F12` 打开浏览器开发者工具，切换到 **Console（控制台）** 标签

### 3. 触发计算结果对话框
在应用中执行以下操作之一：
- 执行评估模型
- 执行算法步骤
- 查看历史计算结果

## 关键日志输出说明

### 日志1: 模型详情加载
```
Loading model detail for modelId: XXX
Model steps received: N
Model steps detail: [...]
```

**检查点**：
- 步骤数量是否正确
- 每个步骤的数据结构（展开数组查看详细内容）

### 日志2: 步骤数据解析
```
Processing step 1: 步骤名称
Step details: {
  description: "...",
  algorithms: [...],
  algorithmConfigs: [...]
}
```

**检查点**：
- `description` 字段是否包含 `|ALGORITHMS|` 标记
- `algorithms` 或 `algorithmConfigs` 字段是否存在
- 是否有输出参数被成功提取

### 日志3: 输出参数提取
```
Step 1 algorithms from description: N
// 或
Step 1 algorithms from algorithms field: N
// 或
Step 1 algorithms from algorithmConfigs: N

Step 1 output param: 参数名
```

**检查点**：
- 是否从某种格式中成功提取到了算法列表
- 每个步骤的输出参数是否被正确识别

### 日志4: 最终映射表
```
Step algorithm outputs map: {
  1: Set { "param1", "param2" },
  2: Set { "param3", "param4" },
  ...
}
Step order names map: {
  1: "步骤1名称",
  2: "步骤2名称",
  ...
}
```

**检查点**：
- 映射表是否为空（如果为空，说明解析失败）
- 每个步骤的输出参数数量是否合理

### 日志5: 列分组计算开始
```
=== 开始计算列分组 ===
Computing column groups with: {
  allColumnsCount: N,
  allColumnsList: [...],
  stepAlgorithmOutputs: {...},
  stepOrderNames: {...},
  isMultiStep: true/false
}
```

**检查点**：
- `allColumnsCount` 是否符合预期
- `allColumnsList` 中的列是否包含 `stepOrder` 字段
- `stepAlgorithmOutputs` 是否为空

### 日志6: 列自带 stepOrder 分组
```
✓ 列自带stepOrder: columnName -> 步骤N
```

**最理想情况**：看到这类日志，说明列数据本身就带有 stepOrder

### 日志7: 后端映射分组
```
列数据中没有stepOrder，使用后端步骤映射
步骤 1 的输出参数: [...]
  ✓ 匹配: columnName -> 步骤1
```

**正常情况**：如果列没有 stepOrder，会尝试用后端映射匹配

### 日志8: 关键字兜底分组
```
开始关键字兜底分组
未分配的列（N）: [...]
  ✓ 关键字匹配[步骤1-原始数据]: columnName (columnLabel)
  ✓ 关键字匹配[步骤2-归一化]: columnName (columnLabel)
  ...
  ? 未匹配: columnName (columnLabel)
```

**重点检查**：
- 哪些列被成功匹配到了步骤1
- 哪些列仍然未匹配（会进入"其他输出"）

### 日志9: 最终分组结果
```
组装最终分组
  + 基础信息: N列
  + 步骤1 原始数据(关键字): N列
  + 步骤2 属性向量归一化(关键字): N列
  ...
  + 其他输出: N列

Final column groups summary: [...]
```

**成功标准**：
- "其他输出"中的列数尽可能少
- 步骤1的列正确归入"步骤1"分组

## 常见问题诊断

### 问题1: 所有列都在"其他输出"中

**可能原因**：
1. 后端返回的步骤数据格式不符合预期
2. 列名不匹配任何关键字规则

**解决方法**：
1. 查看 `Model steps detail` 日志，了解实际的数据结构
2. 查看 `未匹配` 的列名，考虑添加更多关键字规则

### 问题2: 某些步骤的列没有被分组

**可能原因**：
1. 后端没有返回该步骤的输出参数
2. 列名不包含该步骤的关键字

**解决方法**：
1. 检查 `Step algorithm outputs map` 是否包含该步骤
2. 在关键字规则中添加该步骤的特征词

### 问题3: 列被错误分到了其他步骤

**可能原因**：
关键字匹配规则过于宽泛

**解决方法**：
调整 `reStepN` 正则表达式，使其更精确

## 动态调试技巧

### 在控制台中检查数据

```javascript
// 查看所有列
console.log('All columns:', allColumns.value)

// 查看分组结果
console.log('Column groups:', columnGroups.value)

// 查看可见列
console.log('Visible columns:', visibleColumns.value)
```

### 测试正则表达式

```javascript
// 测试某个列名是否匹配步骤2的规则
const testLabel = "某个指标归一化"
const reStep2 = /(归一化|标准化|normalized)/i
console.log('Matches step 2:', reStep2.test(testLabel))
```

## 需要反馈的信息

如果问题仍然存在，请提供以下信息：

1. **完整的控制台日志**（从"Loading model detail"到"Final column groups summary"）

2. **后端返回的步骤数据结构**（`Model steps detail` 日志的内容）

3. **未匹配的列清单**（"未匹配"日志中的列名和标签）

4. **预期的分组结果** vs **实际的分组结果**

5. **截图**：
   - 列显示控制面板的截图
   - 控制台日志的截图

## 下一步

完成测试后，如果：

✅ **问题已解决**：列分组正确，"其他输出"中不再有步骤1的列
- 可以进行UI优化（折叠面板、图标等）

❌ **问题仍存在**：
- 提供上述反馈信息
- 我们将根据实际数据结构进一步调整解析逻辑
