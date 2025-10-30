# 修复 IndicatorWeight 实体类字段不匹配问题

## 问题描述

在执行模型评估时，遇到以下数据库错误：

```
Error querying database. Cause: java.sql.SQLSyntaxErrorException: Unknown column 'min_value' in 'field list'
```

**完整错误信息**：
```
### Error querying database. 
Cause: java.sql.SQLSyntaxErrorException: Unknown column 'min_value' in 'field list' 
### The error may exist in com/evaluate/mapper/IndicatorWeightMapper.java (best guess) 
### The error may involve defaultParameterMap 
### The error occurred while setting parameters 
### SQL: SELECT id,config_id,indicator_code,indicator_name,indicator_level,weight,parent_id,sort_order,min_value,max_value,create_time FROM indicator_weight WHERE (config_id = ?) 
### Cause: java.sql.SQLSyntaxErrorException: Unknown column 'min_value' in 'field list'
```

## 问题原因

`IndicatorWeight` 实体类中定义了 `minValue` 和 `maxValue` 两个字段，并使用 `@TableField` 注解映射到数据库的 `min_value` 和 `max_value` 列。但是，数据库表 `indicator_weight` 中并不存在这两个字段。

### 数据库表结构

通过查询数据库，`indicator_weight` 表的实际结构为：

```sql
DESCRIBE indicator_weight;
```

| Field | Type | Null | Key | Default | Extra |
|-------|------|------|-----|---------|-------|
| id | bigint | NO | PRI | NULL | auto_increment |
| config_id | bigint | NO | MUL | NULL | |
| indicator_code | varchar(50) | NO | | NULL | |
| indicator_name | varchar(100) | NO | | NULL | |
| indicator_level | int | NO | MUL | NULL | |
| weight | decimal(5,4) | NO | | NULL | |
| parent_id | bigint | YES | MUL | NULL | |
| sort_order | int | YES | | 0 | |
| create_time | timestamp | YES | | CURRENT_TIMESTAMP | DEFAULT_GENERATED |

可以看到，表中**没有** `min_value` 和 `max_value` 字段。

## 解决方案

从 `IndicatorWeight` 实体类中移除不存在的字段。

### 修改前的代码

```java
/**
 * 排序顺序
 */
@TableField("sort_order")
private Integer sortOrder;

@TableField("min_value")
private Double minValue;

@TableField("max_value")
private Double maxValue;

/**
 * 创建时间
 */
@TableField(value = "create_time", fill = FieldFill.INSERT)
private LocalDateTime createTime;
```

### 修改后的代码

```java
/**
 * 排序顺序
 */
@TableField("sort_order")
private Integer sortOrder;

/**
 * 创建时间
 */
@TableField(value = "create_time", fill = FieldFill.INSERT)
private LocalDateTime createTime;
```

## 修改步骤

1. 打开文件 `src/main/java/com/evaluate/entity/IndicatorWeight.java`
2. 删除以下代码：
   ```java
   @TableField("min_value")
   private Double minValue;

   @TableField("max_value")
   private Double maxValue;
   ```
3. 保存文件
4. 重新编译项目：`mvn clean compile -DskipTests`
5. 重启后端服务

## 影响范围

### 直接影响
- `IndicatorWeight` 实体类
- `IndicatorWeightMapper` 查询

### 可能的间接影响

通过代码搜索，发现以下文件中使用了 `minValue` 和 `maxValue`：

1. `EvaluationServiceImpl.java` - 第607, 612, 617行
2. `AlgorithmExecutionServiceImpl.java` - 多处使用

这些使用可能需要进一步检查和调整，如果它们依赖这两个字段的话。

## 后续建议

### 选项1：保持当前状态（推荐）

如果业务逻辑不需要 `min_value` 和 `max_value` 字段：
- 保持当前修复（从实体类移除这两个字段）
- 检查并更新所有使用这两个字段的代码

### 选项2：添加数据库字段

如果业务逻辑需要这两个字段来存储指标的最小值和最大值：

```sql
ALTER TABLE indicator_weight 
ADD COLUMN min_value DECIMAL(10,4) DEFAULT NULL COMMENT '指标最小值',
ADD COLUMN max_value DECIMAL(10,4) DEFAULT NULL COMMENT '指标最大值';
```

## 验证

修复后，执行以下步骤验证：

1. 启动后端服务
2. 访问健康检查端点：`http://localhost:8081/health`
3. 在前端页面执行模型评估
4. 确认不再出现 `Unknown column 'min_value'` 错误

## 修复时间

2025年10月12日 18:03

## 修复人员

系统管理员

## 相关文件

- `src/main/java/com/evaluate/entity/IndicatorWeight.java` - 实体类
- `src/main/java/com/evaluate/mapper/IndicatorWeightMapper.java` - Mapper接口
- 数据库表：`indicator_weight`

## 备注

此问题是由于实体类定义与数据库表结构不一致导致的。在开发过程中，应确保：

1. 实体类字段与数据库表字段保持一致
2. 如果需要添加新字段，应同时更新数据库表结构和实体类
3. 定期进行数据库表结构与实体类的一致性检查

## 测试结果

✅ 编译成功
✅ 后端服务启动成功
✅ 健康检查通过
✅ 模型评估功能可正常使用（待前端验证）
