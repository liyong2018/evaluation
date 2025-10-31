# 下一步操作指南

## 📌 当前问题概览

执行社区-乡镇评估模型时遇到两个问题：
1. **步骤1**：13个社区指标列没有显示
2. **步骤2**：乡镇聚合结果全部为0

这两个问题相关联：步骤2依赖步骤1的输出，如果步骤1没有产生数据，步骤2自然得不到正确结果。

---

## ✅ 已完成的工作

### 代码修复
- ✅ 修复了乡镇聚合逻辑，添加详细日志（commit `ffd1691`）
- ✅ 修复了字段提取逻辑，使用`inputParams`而非`qlExpression`
- ✅ 添加了调试日志，显示可用字段和缺失字段

### 诊断工具
- ✅ 创建了乡镇聚合诊断SQL脚本 (`diagnose_township_aggregation.sql`)
- ✅ 创建了步骤1诊断SQL脚本 (`quick_diagnose_step1.sql`)
- ✅ 编写了详细的问题分析文档
  - `TOWNSHIP_AGGREGATION_ISSUE_ANALYSIS.md` - 乡镇聚合问题分析
  - `STEP1_MISSING_COLUMNS_DIAGNOSTIC.md` - 步骤1缺失列诊断

---

## 🚀 立即执行（按顺序）

### 第1步：重启应用程序 ⭐ 最重要！

**为什么必须重启？**
代码已经修复并提交，但应用程序仍在运行旧代码。必须重启才能加载新代码。

**如何重启？**

#### 方法A：使用IDEA（推荐）
1. 找到IDEA底部的"运行"面板
2. 点击红色"停止"按钮 ⏹️
3. 等待服务完全停止（3-5秒）
4. 点击绿色"运行"按钮 ▶️

#### 方法B：使用命令行
```bash
# 在运行服务的窗口中按 Ctrl+C 停止服务
# 然后重新启动
mvn spring-boot:run
```

**验证重启成功：**
查看日志，应该看到：
```
Starting DisasterReductionEvaluationApplication...
...
Tomcat started on port(s): 8080 (http)
Started DisasterReductionEvaluationApplication in 8.234 seconds
```

---

### 第2步：运行诊断SQL检查配置

在重启之前或之后，都应该运行诊断SQL，确保数据库配置正确。

#### 诊断步骤1的配置
```bash
cd /home/user/evaluation/docs
mysql -h192.168.15.203 -P30314 -uroot -p123456 evaluate_db < quick_diagnose_step1.sql > step1_diagnosis.txt
cat step1_diagnosis.txt
```

**查看输出，关注以下内容：**

1. **社区数据是否存在？**
   ```
   total_communities: 58  ← 应该 > 0
   ```
   如果为0，说明需要导入社区数据。

2. **步骤1算法配置是否完整？**
   ```
   total_algorithms: 13         ← 应该是13
   with_output_param: 13        ← 应该是13
   enabled: 13                  ← 应该是13
   ```
   如果不是13，说明算法配置有问题。

3. **算法详细列表：**
   ```
   检查结果列应该全部显示: ✓ 配置正常
   ```
   如果有 ❌ 或 ⚠，说明该算法有问题。

4. **问题总结：**
   ```
   诊断结果: ✓ 配置看起来正常
   ```
   如果不是这个，按照"建议操作"执行。

#### 诊断乡镇聚合配置
```bash
mysql -h192.168.15.203 -P30314 -uroot -p123456 evaluate_db < diagnose_township_aggregation.sql > township_diagnosis.txt
cat township_diagnosis.txt
```

**特别关注"字段映射检查"部分：**
```
step1_output        step2_input         match_status
MGMT_CAPABILITY     MGMT_CAPABILITY     ✓ 匹配
PREP_CAPABILITY     PREP_CAPABILITY     ✓ 匹配
```

如果有"✗ 不匹配"，需要修复字段名。

---

### 第3步：执行评估模型并观察日志

重启完成后，执行模型：

```bash
# 通过API或前端界面执行模型
POST /api/evaluation/execute
{
  "modelId": 8,
  "regionCodes": ["330402001001", "330402001002", ...],
  "weightConfigId": 1
}
```

**关键：观察日志输出**

#### 期望看到的日志（步骤1）
```
INFO - 执行步骤: STEP_1 - 社区指标计算, order=1
INFO - 为地区 330402001001 执行非GRADE算法
DEBUG - 执行算法: CALC_MGMT_1 - 灾害管理机构设立
DEBUG - 算法 CALC_MGMT_1 执行结果: 1.00000000
...
INFO - 步骤 STEP_1 执行完成
INFO - 步骤1 的输出参数: [灾害管理机构设立, 灾害管理预案编制, ...]
```

**如果看到ERROR：**
```
ERROR - 算法 CALC_MGMT_1 执行失败: 变量 disaster_mgmt_org 未定义
```
说明算法表达式中的变量名与数据库字段不匹配，需要修复。

#### 期望看到的日志（步骤2）
```
INFO - 开始执行乡镇聚合, stepId=51, regionCodes.size=58
INFO - 按乡镇分组完成，共 7 个乡镇
INFO - 处理乡镇: 青竹街道, 社区数量: 10
INFO - 第一个社区的所有字段: [currentRegionCode, MGMT_CAPABILITY, PREP_CAPABILITY, ...]
INFO - 算法: 灾害管理能力聚合, inputParams=MGMT_CAPABILITY, qlExpression=, outputParam=TOWNSHIP_MGMT
INFO - 将从社区数据中提取字段: MGMT_CAPABILITY
DEBUG - 社区 330402001001 的 MGMT_CAPABILITY = 0.75634521
...
INFO - 乡镇聚合完成，共 7 个乡镇
```

**如果看到WARN：**
```
WARN - 社区数据中未找到字段: MGMT_CAPABILITY, 可用字段: [currentRegionCode, ...]
```
说明步骤1没有产生预期的输出，或者字段名不匹配。

---

### 第4步：检查结果

#### 检查前端表格
表格应该显示：
- 社区行（58行）：有13列指标数据，不是0
- 乡镇行（7行）：有聚合后的指标数据，不是0

#### 检查数据库
```sql
-- 查看最新的评估结果
SELECT
    region_code,
    region_name,
    management_capability_score,
    support_capability_score,
    self_rescue_capability_score,
    created_at
FROM evaluation_result
WHERE evaluation_model_id = 8
ORDER BY id DESC
LIMIT 10;
```

分值应该不是NULL或0。

---

## 🔧 常见问题和解决方案

### 问题1：重启后日志仍然没有显示新的调试信息

**可能原因：**
- 日志级别设置为WARN或ERROR，INFO日志被过滤

**解决方案：**
检查 `application.properties` 或 `application.yml`：
```properties
# 确保日志级别是DEBUG或INFO
logging.level.com.evaluate=DEBUG
```

修改后重启应用。

---

### 问题2：诊断SQL显示社区数据为0

**解决方案：**
```bash
# 导入社区数据
mysql -h192.168.15.203 -P30314 -uroot -p123456 evaluate_db < sql/archive/community_disaster_reduction_capacity.sql
```

---

### 问题3：步骤1算法缺少output_param

**解决方案：**
```sql
-- 查看哪些算法缺少output_param
SELECT algorithm_code, algorithm_name
FROM step_algorithm
WHERE step_id = (SELECT id FROM model_step WHERE model_id = 8 AND step_order = 1)
  AND (output_param IS NULL OR output_param = '');

-- 为每个算法添加output_param（示例）
UPDATE step_algorithm
SET output_param = 'MGMT_ORG_SCORE'
WHERE step_id = (SELECT id FROM model_step WHERE model_id = 8 AND step_order = 1)
  AND algorithm_code = 'CALC_MGMT_1';
```

---

### 问题4：字段名不匹配

**诊断：**
运行 `diagnose_township_aggregation.sql`，查看"字段映射检查"部分。

**解决方案：**
```sql
-- 方案A：修改步骤2的input_params以匹配步骤1
UPDATE step_algorithm
SET input_params = 'CORRECT_FIELD_NAME'
WHERE step_id = (SELECT id FROM model_step WHERE model_id = 8 AND step_order = 2)
  AND algorithm_code = 'TOWNSHIP_AGG_XXX';

-- 方案B：修改步骤1的output_param以匹配步骤2
UPDATE step_algorithm
SET output_param = 'CORRECT_FIELD_NAME'
WHERE step_id = (SELECT id FROM model_step WHERE model_id = 8 AND step_order = 1)
  AND algorithm_code = 'CALC_XXX';
```

---

### 问题5：算法表达式引用的变量不存在

**诊断：**
1. 查看 `quick_diagnose_step1.sql` 的输出，特别是"Community表字段列表"
2. 对比算法的 `ql_expression` 中使用的变量名

**解决方案：**
```sql
-- 查看实际的数据库字段名
SHOW COLUMNS FROM community_disaster_reduction_capacity;

-- 修改算法表达式以使用正确的字段名
UPDATE step_algorithm
SET ql_expression = 'IF(disaster_mgmt_org == "是", 1, 0)'  -- 使用正确的字段名
WHERE step_id = (SELECT id FROM model_step WHERE model_id = 8 AND step_order = 1)
  AND algorithm_code = 'CALC_MGMT_1';
```

---

## 📋 检查清单

请按顺序完成以下步骤：

### 准备工作
- [ ] 阅读 `TOWNSHIP_AGGREGATION_ISSUE_ANALYSIS.md`
- [ ] 阅读 `STEP1_MISSING_COLUMNS_DIAGNOSTIC.md`

### 诊断数据库配置
- [ ] 运行 `quick_diagnose_step1.sql`
- [ ] 检查社区数据是否存在
- [ ] 检查步骤1算法配置是否完整
- [ ] 运行 `diagnose_township_aggregation.sql`
- [ ] 检查步骤1和步骤2的字段映射

### 修复配置问题（如果有）
- [ ] 修复缺少的 output_param
- [ ] 修复字段名不匹配
- [ ] 修复被禁用的算法
- [ ] 修复算法表达式中的变量名

### 重启应用
- [ ] 停止当前运行的应用
- [ ] 重新启动应用
- [ ] 确认应用启动成功
- [ ] 确认日志中出现新的调试信息

### 测试验证
- [ ] 执行社区-乡镇评估模型
- [ ] 观察日志，确认步骤1执行成功
- [ ] 观察日志，确认步骤2执行成功
- [ ] 检查前端表格，确认数据不是0
- [ ] 检查数据库，确认结果正确保存

### 如果仍有问题
- [ ] 收集完整的后端日志
- [ ] 收集诊断SQL的输出
- [ ] 截图前端显示的表格
- [ ] 提供以上信息以便进一步分析

---

## 📞 获取更多帮助

如果完成上述所有步骤后仍有问题，请提供：

1. **诊断SQL输出**
   - `step1_diagnosis.txt`
   - `township_diagnosis.txt`

2. **完整的后端日志**
   - 从"执行步骤: STEP_1"开始
   - 到"评估模型执行完成"结束

3. **前端表格截图**
   - 显示列名和前几行数据

4. **使用的regionCodes**
   - 执行模型时使用的地区代码列表

5. **模型配置信息**
   ```sql
   SELECT * FROM evaluation_model WHERE id = 8;
   SELECT * FROM model_step WHERE model_id = 8 ORDER BY step_order;
   ```

---

## 🎯 预期时间线

| 步骤 | 预计时间 |
|------|---------|
| 运行诊断SQL | 2分钟 |
| 修复配置问题（如有） | 5-10分钟 |
| 重启应用 | 1分钟 |
| 执行模型并验证 | 2分钟 |
| **总计** | **10-15分钟** |

---

## 📝 相关文档

### 问题分析
- [乡镇聚合问题分析](./TOWNSHIP_AGGREGATION_ISSUE_ANALYSIS.md)
- [步骤1缺失列诊断](./STEP1_MISSING_COLUMNS_DIAGNOSTIC.md)

### 诊断工具
- [诊断乡镇聚合SQL](./diagnose_township_aggregation.sql)
- [快速诊断步骤1SQL](./quick_diagnose_step1.sql)

### 操作指南
- [如何重启后端服务](./archive/如何重启后端服务.md)

### 技术文档
- [实现总结](./IMPLEMENTATION_SUMMARY.md)
- [综合模型使用指南](./comprehensive_model_guide.md)

---

**更新时间：** 2025-10-31
**文档版本：** 1.0
**作者：** Claude Code

**最后提醒：最重要的是重启应用程序！** 🚀
