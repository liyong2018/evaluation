# 统一TOPSIS优劣解算系统需求文档

## 介绍

当前系统中社区-行政村能力评估模型的第四步骤优劣解算数据全部为0，而乡镇减灾能力评估模型的优劣解算数据正确。需要创建一个统一的TOPSIS优劣解算函数，支持前端配置列名，避免将列名固化到程序中。

## 术语表

- **TOPSIS算法**: Technique for Order Preference by Similarity to Ideal Solution，基于与理想解相似性的排序技术
- **正理想解**: 所有指标的最优值组合
- **负理想解**: 所有指标的最差值组合
- **优劣解算**: TOPSIS算法中计算到正理想解和负理想解距离的过程
- **统一TOPSIS计算器**: 不依赖固定列名的通用TOPSIS计算服务
- **列配置**: 前端可配置的用于TOPSIS计算的指标列名列表

## 需求

### Requirement 1: 问题诊断与分析

**用户故事:** 作为系统管理员，我希望能够诊断当前社区-行政村模型TOPSIS计算问题的根本原因，以便制定正确的解决方案

#### 验收标准

1. WHEN 执行社区-行政村模型第四步骤时 THEN 系统 SHALL 记录详细的TOPSIS计算过程日志
2. WHEN 对比乡镇模型和社区模型的TOPSIS配置时 THEN 系统 SHALL 识别配置差异
3. WHEN 分析TOPSIS计算结果为0的原因时 THEN 系统 SHALL 提供具体的错误诊断信息
4. WHEN 检查输入数据时 THEN 系统 SHALL 验证定权数据的完整性和有效性

### Requirement 2: 统一TOPSIS计算服务

**用户故事:** 作为开发人员，我希望有一个统一的TOPSIS计算服务，能够处理任意配置的指标列，以便支持不同的评估模型

#### 验收标准

1. WHEN 创建统一TOPSIS计算器时 THEN 系统 SHALL 接受动态配置的指标列名列表作为输入参数
2. WHEN 计算正理想解时 THEN 系统 SHALL 对指定列计算最大值（效益型指标）
3. WHEN 计算负理想解时 THEN 系统 SHALL 对指定列计算最小值（效益型指标）
4. WHEN 计算欧几里得距离时 THEN 系统 SHALL 使用标准的TOPSIS距离公式
5. WHEN 处理单区域情况时 THEN 系统 SHALL 使用合理的理论基准值进行计算

### Requirement 3: 前端列配置管理

**用户故事:** 作为系统配置员，我希望能够在前端界面配置TOPSIS计算使用的指标列，以便灵活支持不同的评估场景

#### 验收标准

1. WHEN 访问TOPSIS配置页面时 THEN 系统 SHALL 显示当前模型的所有可用指标列
2. WHEN 选择TOPSIS计算列时 THEN 系统 SHALL 允许多选指标列并保存配置
3. WHEN 保存列配置时 THEN 系统 SHALL 验证所选列的数据类型和有效性
4. WHEN 应用新配置时 THEN 系统 SHALL 更新step_algorithm表中对应记录的ql_expression字段

### Requirement 4: 算法参数动态化

**用户故事:** 作为系统架构师，我希望TOPSIS算法参数能够动态配置，而不是硬编码在程序中，以便提高系统的灵活性和可维护性

#### 验收标准

1. WHEN 执行TOPSIS算法时 THEN 系统 SHALL 从step_algorithm表的ql_expression字段读取指标列名参数
2. WHEN 更新算法配置时 THEN 系统 SHALL 通过修改step_algorithm表实现配置更新
3. WHEN 验证算法参数时 THEN 系统 SHALL 检查ql_expression中指定的列是否存在于数据中
4. WHEN 处理不同模型时 THEN 系统 SHALL 根据step_id从step_algorithm表加载对应的列配置

### Requirement 5: 计算结果验证与修复

**用户故事:** 作为数据分析师，我希望系统能够验证TOPSIS计算结果的正确性，并自动修复异常情况，以便确保评估结果的可靠性

#### 验收标准

1. WHEN TOPSIS计算完成时 THEN 系统 SHALL 验证距离值不为负数且不为NaN
2. WHEN 检测到计算结果异常时 THEN 系统 SHALL 记录详细的错误信息和输入数据
3. WHEN 所有距离值为0时 THEN 系统 SHALL 提供替代计算方法或默认值
4. WHEN 计算综合能力值时 THEN 系统 SHALL 确保结果在[0,1]范围内
5. WHEN 验证失败时 THEN 系统 SHALL 提供具体的修复建议和操作指导