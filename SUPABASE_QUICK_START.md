# Supabase 快速迁移指南

## 🚀 快速开始

### 1. 配置Supabase连接

您的Supabase连接信息已配置在 `src/main/resources/application.yml` 中：

```yaml
# Supabase配置
supabase:
  jdbc:
    url: jdbc:postgresql://aws-1-ap-southeast-1.pooler.supabase.com:6543/postgres
    user: postgres.olcdeeonmpjijxtvolum
    password: Htht@1234
```

✅ **Supabase连接已配置完成！**

### 2. 测试连接（可选）

在配置密码后，您可以先测试连接：

```bash
mvn spring-boot:run -Dtest-connection=true
```

这会测试MySQL和Supabase的连接状态并显示数据库信息。

### 3. 执行数据迁移

启用迁移配置：

```yaml
migration:
  enabled: true
```

启动应用执行迁移：

```bash
mvn spring-boot:run
```

### 4. 切换到Supabase

迁移完成后，修改数据源配置：

```yaml
spring:
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://aws-1-ap-southeast-1.pooler.supabase.com:6543/postgres
    username: postgres.olcdeeonmpjijxtvolum
    password: Htht@1234
```

禁用迁移：

```yaml
migration:
  enabled: false
```

重启应用：

```bash
mvn spring-boot:run
```

## ✅ 完成！

您的应用现在已经切换到使用Supabase数据库。

## 🔧 详细配置

查看 [docs/SUPABASE_MIGRATION_GUIDE.md](docs/SUPABASE_MIGRATION_GUIDE.md) 获取详细配置说明。

## 🛠️ 配置选项

- **MySQL → Supabase迁移**：自动处理数据类型转换
- **双数据库支持**：可在MySQL和Supabase间切换
- **连接池优化**：针对云数据库优化
- **自动表创建**：迁移时自动创建PostgreSQL表结构

## ⚠️ 注意事项

1. 迁移前请备份MySQL数据
2. 确保网络可访问Supabase
3. 首次迁移可能需要较长时间