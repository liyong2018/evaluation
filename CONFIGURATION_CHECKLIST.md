# Supabase 配置检查清单

## ✅ 已完成的配置

### 1. 依赖配置
- [x] **PostgreSQL驱动** (42.7.3) - 已添加到 `pom.xml`
- [x] **MySQL驱动** - 保留用于迁移

### 2. 数据库配置
- [x] **Supabase连接信息** - 已配置在 `application.yml`
  ```yaml
  supabase:
    jdbc:
      url: jdbc:postgresql://postgres.olcdeeonmpjijxtvolum:[YOUR-PASSWORD]@aws-1-ap-southeast-1.pooler.supabase.com:6543/postgres
      user: postgres
      password: [YOUR-PASSWORD]
  ```

- [x] **Supabase专用配置** - `application-supabase.yml`
- [x] **MySQL配置** - 保留作为默认配置

### 3. 迁移配置
- [x] **迁移开关** - `migration.enabled: false` (默认关闭)
- [x] **迁移运行器** - `SupabaseMigrationRunner.java` 已存在
- [x] **迁移条件控制** - `SupabaseMigrationRunnerEnabledCondition.java`

### 4. 数据库适配
- [x] **数据库配置类** - `DatabaseConfig.java` 自动检测数据库类型
- [x] **MyBatis配置** - 支持MySQL和PostgreSQL方言
- [x] **连接测试** - `SupabaseTestConfig.java` 和 `DatabaseConnectionTester.java`

## 🔧 需要您完成的配置

### ✅ 密码配置
**已完成**: Supabase密码已配置为 `Htht@1234`

已更新的文件：
1. ✅ `src/main/resources/application.yml`
2. ✅ `src/main/resources/application-supabase.yml`

## 🚀 使用步骤

### 步骤1: ✅ 配置密码
**已完成**: 密码已配置为 `Htht@1234`

### 步骤2: 测试连接（可选）
```bash
mvn spring-boot:run -Dtest-connection=true
```

### 步骤3: 执行迁移
```bash
# 启用迁移
sed -i 's/enabled: false/enabled: true/' src/main/resources/application.yml

# 执行迁移
mvn spring-boot:run

# 迁移完成后禁用
sed -i 's/enabled: true/enabled: false/' src/main/resources/application.yml
```

### 步骤4: 切换到Supabase
```bash
# 方式1: 修改默认配置
# 编辑 application.yml 将数据源改为PostgreSQL

# 方式2: 使用Profile
mvn spring-boot:run -Dspring.profiles.active=supabase
```

## 📁 新增文件列表

### 配置文件
- `src/main/resources/application-supabase.yml`
- `docs/SUPABASE_MIGRATION_GUIDE.md`
- `SUPABASE_QUICK_START.md`
- `CONFIGURATION_CHECKLIST.md`

### Java类
- `src/main/java/com/evaluate/config/DatabaseConfig.java`
- `src/main/java/com/evaluate/config/SupabaseTestConfig.java`
- `src/main/java/com/evaluate/util/DatabaseConnectionTester.java`

## 🔍 验证清单

迁移完成后请验证：

- [ ] 数据迁移成功
- [ ] 应用连接Supabase正常
- [ ] API功能正常
- [ ] 数据查询正确
- [ ] 日志无错误

## 🆘 故障排除

### 连接失败
```bash
# 检查配置
mvn spring-boot:run -Dtest-connection=true
```

### 迁移失败
1. 检查MySQL连接
2. 检查Supabase连接
3. 查看应用日志 `logs/evaluate.log`

### 编译错误
```bash
mvn clean compile -DskipTests
```

## 📞 支持

如遇到问题，请检查：
1. 应用日志：`logs/evaluate.log`
2. Supabase Dashboard状态
3. 网络连接和防火墙设置