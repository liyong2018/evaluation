# 乡镇聚合问题分析报告

## 📋 问题描述

执行社区-乡镇评估模型（modelId=8）时：
1. **步骤1**（社区指标计算）应该显示13列数据，但数据缺失或为0
2. **步骤2**（乡镇聚合）将社区数据按乡镇名称分组并求平均值，但所有结果都是0

### 用户反馈
```
缺少步骤1的13列数据的显示，步骤2都是0，说明根据社区分组到乡镇了解不对
```

### 输出示例
```
regionCode    regionName    townshipName    [13个指标字段都是0]
511425001     青竹街道      青竹街道        0.00  0.00  0.00  ...
511425102     瑞峰镇        瑞峰镇          0.00  0.00  0.00  ...
...
```

---

## 🔍 根本原因分析

### 1. 应用未重启
最关键的问题是：**代码修改后，应用程序没有重启**。

#### 证据
- 提交了修复代码（commit `ffd1691`），添加了详细的日志输出
- 但用户提供的日志中**没有出现**这些新添加的日志：
  - ❌ 没有看到 "开始执行乡镇聚合"
  - ❌ 没有看到 "处理乡镇: xxx, 社区数量: xxx"
  - ❌ 没有看到 "第一个社区的所有字段: xxx"
  - ❌ 没有看到 "将从社区数据中提取字段: xxx"
  - ❌ 没有看到 "社区数据中未找到字段: xxx"

#### 位置
这些日志在 `ModelExecutionServiceImpl.java` 的以下行：
- 第1395行: `log.info("开始执行乡镇聚合, stepId={}, regionCodes.size={}", stepId, regionCodes.size());`
- 第1480行: `log.info("处理乡镇: {}, 社区数量: {}", townshipName, communityCount);`
- 第1485行: `log.info("第一个社区的所有字段: {}", firstCommunity.keySet());`
- 第1496行: `log.info("算法: {}, inputParams={}, qlExpression={}, outputParam={}", ...);`
- 第1514行: `log.info("将从社区数据中提取字段: {}", inputField);`
- 第1529行: `log.warn("社区数据中未找到字段: {}, 可用字段: {}", inputField, community.keySet());`

### 2. 字段名称不匹配（潜在问题）
即使重启后，仍可能存在字段名称不匹配的问题。

#### 工作原理
乡镇聚合的数据流：
```
步骤1（社区指标计算）
  → 每个算法执行后，将结果保存为 output_param
  → 例如：output_param = "MANAGEMENT_CAPABILITY"

步骤2（乡镇聚合）
  → 读取 input_params，指定要聚合哪个字段
  → 例如：input_params = "MANAGEMENT_CAPABILITY"
  → 从社区数据中获取该字段的值：community.get("MANAGEMENT_CAPABILITY")

如果字段名不匹配：
  → community.get(inputField) 返回 null
  → 聚合值 = 0
```

#### 可能的不匹配情况
| 步骤1的output_param | 步骤2的input_params | 结果 |
|-------------------|---------------------|------|
| `MGMT_CAPABILITY` | `MANAGEMENT_CAPABILITY` | ✗ 不匹配 → 0值 |
| `management_capability` | `MANAGEMENT_CAPABILITY` | ✗ 不匹配 → 0值 |
| `MANAGEMENT_CAPABILITY` | `MANAGEMENT_CAPABILITY` | ✓ 匹配 → 正常 |

---

## 🛠️ 解决方案

### 解决方案1：重启应用程序（必需）

#### 使用IDEA重启
1. 找到IDEA底部的"运行"面板
2. 点击红色"停止"按钮 ⏹️
3. 等待服务完全停止
4. 点击绿色"运行"按钮 ▶️

#### 使用命令行重启
```bash
# 1. 停止当前运行的服务（Ctrl+C）

# 2. 重新启动
mvn spring-boot:run
```

#### 验证重启成功
重启后，日志中应该看到：
```
INFO - 开始执行乡镇聚合, stepId=51, regionCodes.size=58
INFO - 按乡镇分组完成，共 7 个乡镇
INFO - 处理乡镇: 青竹街道, 社区数量: 10
INFO - 第一个社区的所有字段: [currentRegionCode, MGMT_CAPABILITY, ...]
INFO - 算法: 灾害管理能力聚合, inputParams=MGMT_CAPABILITY, ...
INFO - 将从社区数据中提取字段: MGMT_CAPABILITY
```

如果看到这些日志，说明新代码已生效！

### 解决方案2：检查字段名称匹配（如果重启后仍有问题）

#### 步骤A：运行诊断SQL
执行提供的诊断脚本：
```bash
cd /home/user/evaluation/docs
mysql -h192.168.15.203 -P30314 -uroot -p123456 evaluate_db < diagnose_township_aggregation.sql > diagnosis_result.txt
cat diagnosis_result.txt
```

#### 步骤B：检查输出
查找"字段映射检查"部分，应该看到类似：
```
step1_output           step2_input            step2_algorithm         match_status
MGMT_CAPABILITY        MGMT_CAPABILITY        灾害管理能力聚合        ✓ 匹配
PREP_CAPABILITY        PREP_CAPABILITY        备灾能力聚合            ✓ 匹配
...
```

如果看到"✗ 不匹配"，需要修复配置。

#### 步骤C：修复不匹配的字段
如果发现不匹配，更新step_algorithm表：
```sql
-- 示例：假设步骤1输出 MGMT_CAPABILITY，但步骤2期望 MANAGEMENT_CAPABILITY
UPDATE step_algorithm
SET input_params = 'MGMT_CAPABILITY'  -- 改为与步骤1一致
WHERE step_id = (SELECT id FROM model_step WHERE model_id = 8 AND step_order = 2)
  AND algorithm_code = 'TOWNSHIP_AGG_MGMT';
```

---

## 📊 预期结果

### 重启并修复后的正常输出

#### 日志输出
```
2025-10-31 16:00:00 INFO  - 开始执行乡镇聚合, stepId=51, regionCodes.size=58
2025-10-31 16:00:00 INFO  - 按乡镇分组完成，共 7 个乡镇
2025-10-31 16:00:00 INFO  - 处理乡镇: 青竹街道, 社区数量: 10
2025-10-31 16:00:00 INFO  - 第一个社区的所有字段: [currentRegionCode, MGMT_CAPABILITY, PREP_CAPABILITY, ...]
2025-10-31 16:00:00 INFO  - 算法: 灾害管理能力聚合, inputParams=MGMT_CAPABILITY, qlExpression=, outputParam=TOWNSHIP_MGMT
2025-10-31 16:00:00 INFO  - 将从社区数据中提取字段: MGMT_CAPABILITY
2025-10-31 16:00:00 DEBUG - 社区 330402001001 的 MGMT_CAPABILITY = 0.75634521
2025-10-31 16:00:00 DEBUG - 社区 330402001002 的 MGMT_CAPABILITY = 0.82341256
...
2025-10-31 16:00:00 DEBUG - 乡镇 青竹街道 的 TOWNSHIP_MGMT 聚合结果: sum=7.23, count=10, avg=0.72300000
2025-10-31 16:00:00 INFO  - 乡镇聚合完成，共 7 个乡镇
```

#### 表格输出
```
regionCode          regionName    townshipName    [13个指标字段有实际值]
TOWNSHIP_青竹街道   青竹街道      青竹街道        0.72300000  0.65432100  ...
TOWNSHIP_瑞峰镇     瑞峰镇        瑞峰镇          0.81234567  0.73456789  ...
...
```

---

## 🐛 调试技巧

### 如果重启后仍然是0值

#### 1. 检查日志中的警告
查找这条日志：
```
WARN - 社区数据中未找到字段: xxx, 可用字段: [...]
```

这会明确告诉你：
- 步骤2在找什么字段（`xxx`）
- 步骤1实际提供了哪些字段（`[...]`）

#### 2. 检查步骤1是否执行成功
查找：
```
INFO - 步骤 STEP_1 执行完成
INFO - 步骤1 的输出参数: [算法1, 算法2, ...]
```

如果步骤1没有执行或失败，步骤2自然得不到数据。

#### 3. 检查社区数据是否存在
```sql
SELECT COUNT(*) FROM community_disaster_reduction_capacity
WHERE region_code IN ('330402001001', '330402001002', ...);
```

如果返回0，说明数据库中没有社区数据。

---

## 📁 相关文件

### 关键代码文件
- `src/main/java/com/evaluate/service/impl/ModelExecutionServiceImpl.java`
  - 第1394-1568行：`executeTownshipAggregation` 方法
  - 第114-157行：步骤执行循环
  - 第198-411行：`executeStep` 方法

### 诊断工具
- `docs/diagnose_township_aggregation.sql` - SQL诊断脚本
- `docs/archive/如何重启后端服务.md` - 重启指南

### Git提交记录
- `ffd1691` - 修复乡镇聚合逻辑并添加详细日志
- `c088cb0` - 添加实现总结文档
- `937215f` - 添加综合模型使用指南

---

## ✅ 检查清单

请按顺序完成以下检查：

- [ ] 1. **重启应用程序**
  - [ ] 停止当前服务
  - [ ] 重新启动服务
  - [ ] 确认服务启动成功（看到"Started DisasterReductionEvaluationApplication"）

- [ ] 2. **执行模型并检查日志**
  - [ ] 执行社区-乡镇评估模型
  - [ ] 查看后端日志
  - [ ] 确认看到"开始执行乡镇聚合"等新日志

- [ ] 3. **检查结果**
  - [ ] 查看表格数据
  - [ ] 确认步骤2的值不再是0

- [ ] 4. **如果仍有问题**
  - [ ] 运行 `diagnose_township_aggregation.sql`
  - [ ] 检查字段映射是否匹配
  - [ ] 检查日志中的WARN消息
  - [ ] 提供完整的后端日志用于进一步分析

---

## 🆘 寻求帮助

如果按照上述步骤仍无法解决问题，请提供：

1. **完整的后端日志**（从"执行步骤: step_1"开始到"乡镇聚合完成"）
2. **诊断SQL的输出**（diagnosis_result.txt）
3. **前端显示的表格数据截图**
4. **使用的regionCodes列表**

---

## 📝 总结

### 问题根源
**应用程序未重启，导致新的修复代码未生效**

### 立即行动
**重启应用程序，然后重新执行模型**

### 预期时间
- 重启应用：30秒
- 执行模型：10秒
- 验证结果：5秒
- **总计：约1分钟**

---

**更新时间：** 2025-10-31
**文档版本：** 1.0
**作者：** Claude Code
