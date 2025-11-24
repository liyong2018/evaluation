# 减灾能力评估系统 Docker 部署指南

## 部署环境

- **目标服务器**: root@101.126.46.254
- **部署方式**: Docker + Docker Compose
- **包含服务**:
  - Spring Boot 应用 (端口 8087)
  - MySQL 8.0 (端口 3306)
  - Redis 7 (端口 6379)
  - Nginx 反向代理 (端口 80/443)

## 快速部署

### 1. 准备工作

确保本地已安装：
- Git
- SSH 客户端
- rsync

### 2. 执行部署

```bash
# 克隆项目（如果还没有）
git clone <repository-url>
cd evaluation

# 执行一键部署脚本
./scripts/deploy.sh
```

### 3. 访问应用

- **应用直接访问**: http://101.126.46.254:8087
- **通过Nginx**: http://101.126.46.254

## 详细部署步骤

### 1. 服务器环境要求

- 操作系统: Linux (推荐 Ubuntu 20.04+)
- 内存: 至少 2GB RAM
- 存储: 至少 20GB 可用空间
- 网络: 允许访问外部网络下载Docker镜像

### 2. 手动部署（可选）

如果自动部署脚本无法使用，可以手动执行以下步骤：

```bash
# 2.1 连接服务器
ssh root@101.126.46.254

# 2.2 创建部署目录
mkdir -p /opt/evaluation/{logs,uploads,backups}

# 2.3 安装Docker（如果未安装）
curl -fsSL https://get.docker.com | sh -s -- --mirror Aliyun
systemctl start docker
systemctl enable docker

# 2.4 安装Docker Compose（如果未安装）
curl -L 'https://github.com/docker/compose/releases/download/v2.20.2/docker-compose-$(uname -s)-$(uname -m)' -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# 2.5 退出SSH，上传项目文件
# 在本地执行
rsync -avz --exclude='.git' --exclude='node_modules' --exclude='target' ./ root@101.126.46.254:/opt/evaluation/

# 2.6 在服务器上构建和启动
ssh root@101.126.46.254
cd /opt/evaluation
docker-compose build --no-cache
docker-compose up -d
```

## 配置说明

### 1. 环境变量配置

创建 `.env` 文件（可选）：

```env
# 数据库配置
MYSQL_ROOT_PASSWORD=123456
MYSQL_DATABASE=evaluate_db

# Redis配置
REDIS_PASSWORD=redis123

# 应用配置
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8087
```

### 2. 数据库配置

数据库配置文件：`mysql/conf.d/my.cnf`

主要配置：
- 字符集：utf8mb4
- 连接数：1000
- 缓冲区：256MB
- 查询缓存：64MB

### 3. Nginx配置

Nginx配置文件：`nginx/conf.d/default.conf`

功能：
- 反向代理到后端应用
- WebSocket支持
- Gzip压缩
- 静态文件服务

## 服务管理

### 1. 查看服务状态

```bash
# SSH连接到服务器
ssh root@101.126.46.254
cd /opt/evaluation

# 查看所有容器状态
docker-compose ps

# 查看应用日志
docker-compose logs -f app

# 查看数据库日志
docker-compose logs -f mysql
```

### 2. 重启服务

```bash
# 重启所有服务
docker-compose restart

# 重启特定服务
docker-compose restart app
docker-compose restart mysql
```

### 3. 停止服务

```bash
# 停止所有服务
docker-compose down

# 停止并删除数据卷（谨慎使用）
docker-compose down -v
```

### 4. 更新服务

```bash
# 使用更新脚本
./scripts/update.sh

# 或手动更新
ssh root@101.126.46.254
cd /opt/evaluation
docker-compose down
docker-compose pull
docker-compose build --no-cache
docker-compose up -d
```

## 数据备份和恢复

### 1. 自动备份

```bash
# 执行备份脚本
./scripts/backup.sh
```

备份内容：
- 数据库数据
- Docker数据卷
- 上传文件
- 配置文件
- 应用日志

### 2. 手动备份

```bash
# 备份数据库
ssh root@101.126.46.254
cd /opt/evaluation
docker-compose exec mysql mysqldump -uroot -p123456 evaluate_db > backup.sql

# 备份数据卷
docker run --rm -v /opt/evaluation/mysql_data:/data -v $(pwd):/backup alpine tar czf /backup/mysql_data.tar.gz -C /data .
```

### 3. 数据恢复

```bash
# 恢复数据库
ssh root@101.126.46.254
cd /opt/evaluation
docker-compose exec -T mysql mysql -uroot -p123456 evaluate_db < backup.sql

# 恢复数据卷
docker-compose down
docker run --rm -v /opt/evaluation/mysql_data:/data -v $(pwd):/backup alpine tar xzf mysql_data.tar.gz -C /data/
docker-compose up -d
```

## 监控和维护

### 1. 健康检查

应用提供健康检查端点：
- http://101.126.46.254:8087/actuator/health

### 2. 日志管理

日志文件位置：
- 应用日志：`/opt/evaluation/logs/`
- Docker日志：`docker-compose logs`
- Nginx日志：容器内 `/var/log/nginx/`

### 3. 性能监控

建议配置以下监控：
- CPU、内存、磁盘使用率
- 数据库连接数和查询性能
- 应用响应时间
- 错误日志统计

## 安全配置

### 1. 防火墙设置

```bash
# 配置UFW防火墙
ufw allow 22/tcp    # SSH
ufw allow 80/tcp    # HTTP
ufw allow 443/tcp   # HTTPS
ufw allow 8087/tcp  # 应用端口（可选）
ufw enable
```

### 2. SSL证书配置

如需配置HTTPS：

```bash
# 创建SSL目录
mkdir -p nginx/ssl

# 放置证书文件
cp your-cert.pem nginx/ssl/cert.pem
cp your-key.pem nginx/ssl/key.pem

# 修改nginx配置启用HTTPS
# 编辑 nginx/conf.d/default.conf
```

### 3. 数据库安全

- 修改默认的数据库密码
- 限制数据库访问IP
- 定期备份数据库
- 监控数据库访问日志

## 故障排查

### 1. 常见问题

#### 应用无法启动
```bash
# 查看应用日志
docker-compose logs app

# 检查数据库连接
docker-compose exec app ping mysql
```

#### 数据库连接失败
```bash
# 检查数据库状态
docker-compose logs mysql

# 连接数据库测试
docker-compose exec mysql mysql -uroot -p123456
```

#### 端口被占用
```bash
# 检查端口占用
netstat -tulpn | grep :8087

# 停止占用端口的服务
docker-compose down
```

### 2. 性能优化

- 调整JVM内存参数
- 优化数据库配置
- 配置Redis持久化
- 启用Nginx缓存

### 3. 扩容配置

如需扩容，可以：
- 增加服务器资源配置
- 配置数据库主从复制
- 使用负载均衡器
- 配置Redis集群

## 联系支持

如遇部署问题，请联系：
- 技术支持：[联系方式]
- 文档参考：[文档链接]
- 问题反馈：[反馈渠道]

---

**部署成功后，请及时修改默认密码并配置安全策略！**