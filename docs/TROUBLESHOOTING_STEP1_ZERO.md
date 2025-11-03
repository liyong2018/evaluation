# 排查综合减灾模型第一步显示0的问题

## 问题现象

执行综合减灾能力评估模型时，第一步"数据融合"返回的结果：
- `columns: []`
- `tableData: []`
- 或者所有值都是0

## 排查步骤

### 步骤1：确认您选择的区域代码

**操作**：
1. 打开前端页面
2. 查看您在"选择区域"时选择了哪些区域
3. 记录下这些区域的**区域代码**（例如：511425108）

**示例**：
```
您选择的区域：
- 511425108 (瑞峰镇)
- 511425109 (西龙镇)
- 511425110 (高台镇)
```

### 步骤2：检查这些区域是否有评估数据

**操作**：
1. 打开 `sql/check_your_regions.sql` 文件
2. 修改第7行，将区域代码替换为您选择的区域代码：
   ```sql
   SET @selected_regions = '511425108,511425109,511425110';  -- 👈 改成您的区域代码
   ```
3. 执行SQL：
   ```bash
   mysql -h192.168.15.203 -P30314 -uroot -p123456 evaluate_db < sql/check_your_regions.sql
   ```

**查看结果**：

#### 结果1：模型3和模型8都有数据 ✅
```
区域代码 | 模型3状态 | 模型8状态 | 建议
---------|----------|----------|------
511425108| ✓ 有数据 | ✓ 有数据 | ✅ 数据完整，可以运行综合减灾模型
```

如果是这个结果，说明数据是完整的，**跳到步骤3**。

#### 结果2：缺少模型3或模型8的数据 ❌
```
区域代码 | 模型3状态 | 模型8状态 | 建议
---------|----------|----------|------
511425108| ✗ 无数据 | ✓ 有数据 | ❌ 需要先运行乡镇评估模型
```

**解决方案**：
- 如果缺少模型3的数据，先运行**乡镇减灾能力评估模型**
- 如果缺少模型8的数据，先运行**社区-乡镇减灾能力评估模型**
- 两个模型都要对**相同的区域**进行评估
- 评估完成后，再运行综合减灾能力评估模型

**运行完成后，再次执行步骤2的SQL确认数据是否存在**。

### 步骤3：检查算法配置

如果数据完整但仍然显示0，可能是算法配置有问题。

**操作**：
```bash
mysql -h192.168.15.203 -P30314 -uroot -p123456 evaluate_db < sql/deep_check_comprehensive_model.sql
```

**查看"4. 综合减灾模型第一步算法配置"部分**：

应该看到6个算法，每个都包含 `@LOAD_EVAL_RESULT`：
```
顺序 | 算法名称               | QL表达式
-----|----------------------|------------------------------------------
1    | 提取乡镇灾害管理能力    | @LOAD_EVAL_RESULT:modelId=3,field=management_capability_score
2    | 提取乡镇灾害备灾能力    | @LOAD_EVAL_RESULT:modelId=3,field=support_capability_score
3    | 提取乡镇自救转移能力    | @LOAD_EVAL_RESULT:modelId=3,field=self_rescue_capability_score
4    | 提取社区-乡镇灾害管理能力| @LOAD_EVAL_RESULT:modelId=8,field=management_capability_score
5    | 提取社区-乡镇灾害备灾能力| @LOAD_EVAL_RESULT:modelId=8,field=support_capability_score
6    | 提取社区-乡镇自救转移能力| @LOAD_EVAL_RESULT:modelId=8,field=self_rescue_capability_score
```

**如果配置不正确**：
```bash
# 运行修复脚本
mysql -h192.168.15.203 -P30314 -uroot -p123456 evaluate_db < sql/fix_comprehensive_model.sql
```

### 步骤4：查看后端日志

**操作**：
1. 重新运行综合减灾能力评估模型
2. 查看后端日志（通常在项目的 `logs/` 目录）
3. 搜索关键字：`[LOAD_EVAL_RESULT]`

**正常的日志应该是**：
```
[LOAD_EVAL_RESULT] 加载评估结果: params=modelId=3,field=management_capability_score, region=511425108
[LOAD_EVAL_RESULT] 加载成功: modelId=3, region=511425108, field=management_capability_score, value=0.7657
```

**如果看到警告**：
```
[LOAD_EVAL_RESULT] 未找到评估结果: modelId=3, regionCode=511425108
```

这说明：
- evaluation_result 表中没有这个区域的数据
- 或者区域代码不匹配
- 回到**步骤2**重新检查数据

### 步骤5：检查区域代码格式

有时候区域代码的格式可能不一致（例如有前导0，或者长度不同）。

**操作**：
```sql
-- 查看evaluation_result表中的所有区域代码
SELECT DISTINCT
    region_code AS 区域代码,
    LENGTH(region_code) AS 代码长度,
    region_name AS 区域名称
FROM evaluation_result
WHERE evaluation_model_id IN (3, 8)
ORDER BY region_code;
```

**对比**：
- 您在前端选择的区域代码
- evaluation_result 表中的区域代码

**确保它们完全一致**（包括长度和格式）。

## 常见问题

### Q1: 我已经运行了模型3和模型8，为什么还是没有数据？

**A**: 检查以下几点：
1. 模型3和8是否**成功完成**（状态是SUCCESS）
2. 是否对**相同的区域**进行评估
3. 查询一下execution_record表，确认最近的执行状态：
   ```sql
   SELECT
       em.model_name,
       mer.execution_code,
       mer.execution_status,
       mer.start_time,
       mer.error_message
   FROM model_execution_record mer
   LEFT JOIN evaluation_model em ON mer.model_id = em.id
   WHERE em.id IN (3, 8)
   ORDER BY mer.start_time DESC
   LIMIT 10;
   ```

### Q2: 后端日志中看不到 [LOAD_EVAL_RESULT] 的日志？

**A**: 可能原因：
1. 日志级别设置太高（改为INFO或DEBUG）
2. 算法配置不正确（回到步骤3检查）
3. 第一步根本没有执行（检查执行记录表）

### Q3: 数据存在，但是加载的值全是0？

**A**: 检查 evaluation_result 表中的数据：
```sql
SELECT
    region_code,
    management_capability_score,
    support_capability_score,
    self_rescue_capability_score
FROM evaluation_result
WHERE evaluation_model_id = 3
  AND region_code = '您的区域代码'
ORDER BY create_time DESC
LIMIT 1;
```

如果这些分值本身就是0或NULL，说明：
- 模型3和8的评估结果本身有问题
- 需要重新运行模型3和8，确保计算出正确的分值

### Q4: 区域代码匹配，但是 @LOAD_EVAL_RESULT 返回0？

**A**: 可能是字段名不匹配。检查：
1. evaluation_result 表的字段名
2. @LOAD_EVAL_RESULT 中的 field 参数

应该是：
- `management_capability_score` （灾害管理能力）
- `support_capability_score` （灾害备灾能力）
- `self_rescue_capability_score` （自救转移能力）

## 解决方案总结

| 问题 | 解决方案 |
|------|---------|
| 缺少模型3数据 | 先运行乡镇减灾能力评估模型 |
| 缺少模型8数据 | 先运行社区-乡镇减灾能力评估模型 |
| 算法配置错误 | 运行 `fix_comprehensive_model.sql` |
| 区域代码不匹配 | 确认前端选择的区域代码与数据库中的一致 |
| 评估结果本身是0 | 重新运行模型3和8，确保计算出正确的分值 |
| 日志中无 [LOAD_EVAL_RESULT] | 检查算法配置和日志级别 |

## 联系支持

如果按照上述步骤仍然无法解决问题，请提供：
1. `check_your_regions.sql` 的输出结果
2. `deep_check_comprehensive_model.sql` 的输出结果
3. 后端日志中包含 `[LOAD_EVAL_RESULT]` 的部分
4. 您选择的区域代码列表

这将帮助我们更快地定位问题。
