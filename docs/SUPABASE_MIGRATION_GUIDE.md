# MySQL 到 Supabase 迁移指南

本指南将帮助您将现有的MySQL数据库迁移到Supabase（PostgreSQL）。

## 迁移步骤

### 1. 准备Supabase项目

1. 登录 [Supabase Dashboard](https://app.supabase.com/)
2. 创建新项目或使用现有项目
3. 获取数据库连接信息：
   - Project URL
   - Database Password
   - API Key（如果需要）

### 2. 配置应用

#### 2.1 更新 `application.yml`

```yaml
# 替换您的实际Supabase配置
supabase:
  jdbc:
    url: jdbc:postgresql://your-project-ref.supabase.co:5432/postgres
    user: postgres
    password: your-actual-supabase-password
```

#### 2.2 运行迁移

启用迁移配置：

```yaml
migration:
  enabled: true
  truncate: false  # 设为true会清空目标表
  exitOnFinish: false # 迁移完成后退出应用
```

然后启动应用：

```bash
mvn spring-boot:run
```

应用会自动：
1. 连接MySQL源数据库
2. 连接Supabase目标数据库
3. 创建必要的表结构
4. 迁移所有数据
5. 更新序列值

### 3. 切换到Supabase

迁移完成后，有两种方式切换到Supabase：

#### 方式一：修改配置文件

更新 `application.yml` 中的数据源配置：

```yaml
spring:
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://your-project-ref.supabase.co:5432/postgres
    username: postgres
    password: your-actual-supabase-password
```

#### 方式二：使用Profile

使用Supabase配置启动：

```bash
mvn spring-boot:run -Dspring.profiles.active=supabase
```

或修改 `application-supabase.yml` 并设置默认profile。

### 4. 禁用迁移

切换完成后，禁用迁移配置：

```yaml
migration:
  enabled: false
```

## 配置说明

### Supabase配置参数

```yaml
supabase:
  jdbc:
    url: jdbc:postgresql://[project-ref].supabase.co:5432/postgres
    user: postgres
    password: [your-password]
```

### 迁移配置参数

```yaml
migration:
  enabled: true/false        # 是否启用迁移
  truncate: true/false       # 是否清空目标表
  exitOnFinish: true/false   # 迁移完成后是否退出应用
```

## 注意事项

1. **备份数据**：迁移前请备份MySQL数据
2. **网络连接**：确保应用能同时访问MySQL和Supabase
3. **表结构**：迁移器会自动创建PostgreSQL版本的表
4. **数据类型**：自动处理MySQL到PostgreSQL的数据类型转换
5. **性能**：大数据量迁移可能需要较长时间

## 迁移的表

当前迁移器会迁移以下表：

- `evaluation_result` - 评估结果
- `model_execution_record` - 模型执行记录

## 故障排除

### 连接失败

检查Supabase配置是否正确：

```bash
# 测试连接
psql "postgresql://postgres:password@your-project-ref.supabase.co:5432/postgres"
```

### 迁移失败

查看应用日志，常见原因：

- 网络连接问题
- 权限不足
- 数据类型冲突

### 性能优化

对于大量数据，可以：

1. 调整批处理大小（修改代码中的batch size）
2. 使用更快的网络连接
3. 分批迁移特定表

## 回滚计划

如需回滚到MySQL：

1. 停止应用
2. 修改配置文件恢复MySQL连接
3. 确保MySQL数据是最新的
4. 重启应用

## 验证迁移

迁移完成后验证：

1. 检查记录数量是否一致
2. 验证关键字段数据
3. 测试应用功能
4. 检查日志是否有错误

## 联系支持

如遇到问题，请检查：

1. 应用日志：`logs/evaluate.log`
2. Supabase Dashboard中的数据库状态
3. 网络连接和防火墙设置