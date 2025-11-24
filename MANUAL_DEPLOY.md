# 手动部署指南

由于SSH认证需要密码，需要手动执行部署步骤。

## 服务器连接

首先连接到服务器：
```bash
ssh root@101.126.46.254
# 输入密码进行连接
```

## 部署步骤

### 1. 在服务器上创建部署目录

```bash
# 创建部署目录
mkdir -p /opt/evaluation/{logs,uploads,backups}
cd /opt/evaluation
```

### 2. 安装Docker（如果未安装）

```bash
# 安装Docker
curl -fsSL https://get.docker.com | sh -s -- --mirror Aliyun

# 启动Docker服务
systemctl start docker
systemctl enable docker

# 验证Docker安装
docker --version
```

### 3. 安装Docker Compose（如果未安装）

```bash
# 下载Docker Compose
curl -L 'https://github.com/docker/compose/releases/download/v2.20.2/docker-compose-$(uname -s)-$(uname -m)' -o /usr/local/bin/docker-compose

# 添加执行权限
chmod +x /usr/local/bin/docker-compose

# 验证安装
docker-compose --version
```

### 4. 上传项目文件

**方法一：使用scp上传**

在本地机器执行：
```bash
# 将项目文件压缩
tar -czf evaluation-deploy.tar.gz --exclude='.git' --exclude='node_modules' --exclude='target' --exclude='logs' --exclude='uploads' .

# 上传到服务器
scp evaluation-deploy.tar.gz root@101.126.46.254:/opt/evaluation/

# 在服务器上解压
ssh root@101.126.46.254
cd /opt/evaluation
tar -xzf evaluation-deploy.tar.gz
rm evaluation-deploy.tar.gz
```

**方法二：使用rsync上传**

在本地机器执行：
```bash
rsync -avz --exclude='.git' --exclude='node_modules' --exclude='target' --exclude='logs' --exclude='uploads' ./ root@101.126.46.254:/opt/evaluation/
```

### 5. 构建和启动服务

在服务器上执行：
```bash
cd /opt/evaluation

# 构建镜像
docker-compose build --no-cache

# 启动服务
docker-compose up -d

# 查看服务状态
docker-compose ps
```

### 6. 验证部署

```bash
# 检查应用健康状态
curl -f http://localhost:8087/actuator/health

# 查看应用日志
docker-compose logs -f app

# 检查所有服务
docker-compose ps
```

## 访问应用

部署成功后，可以通过以下地址访问：

- **应用直接访问**: http://101.126.46.254:8087
- **通过Nginx代理**: http://101.126.46.254

## 常用管理命令

```bash
# 进入部署目录
cd /opt/evaluation

# 查看服务状态
docker-compose ps

# 查看所有日志
docker-compose logs

# 查看应用日志
docker-compose logs -f app

# 重启服务
docker-compose restart

# 停止服务
docker-compose down

# 更新服务
docker-compose pull
docker-compose build --no-cache
docker-compose up -d
```

## 数据备份

```bash
# 备份数据库
docker-compose exec mysql mysqldump -uroot -p123456 evaluate_db > backup.sql

# 备份上传文件
cp -r uploads/ backup_uploads/

# 备份Docker数据
docker run --rm -v /opt/evaluation/mysql_data:/data -v $(pwd):/backup alpine tar czf /backup/mysql_data.tar.gz -C /data .
```

## 故障排查

### 应用无法启动
```bash
# 查看应用日志
docker-compose logs app

# 检查数据库连接
docker-compose exec app ping mysql

# 进入应用容器
docker-compose exec app sh
```

### 数据库问题
```bash
# 查看数据库日志
docker-compose logs mysql

# 连接数据库
docker-compose exec mysql mysql -uroot -p123456

# 检查数据库
SHOW DATABASES;
USE evaluate_db;
SHOW TABLES;
```

### 端口问题
```bash
# 检查端口占用
netstat -tulpn | grep :8087

# 检查防火墙状态
systemctl status firewalld

# 开放端口（如需要）
firewall-cmd --add-port=8087/tcp --permanent
firewall-cmd --reload
```

## 安全配置

### 修改数据库密码
```bash
# 进入MySQL
docker-compose exec mysql mysql -uroot -p123456

# 修改root密码
ALTER USER 'root'@'%' IDENTIFIED BY '新密码';
FLUSH PRIVILEGES;
```

### 配置防火墙
```bash
# 安装ufw
apt-get update
apt-get install ufw

# 配置防火墙
ufw allow 22/tcp    # SSH
ufw allow 80/tcp    # HTTP
ufw allow 443/tcp   # HTTPS
ufw allow 8087/tcp  # 应用端口
ufw enable
```

## 性能优化

### 调整JVM参数
编辑 `docker-compose.yml`，在应用服务的environment中添加：
```yaml
environment:
  - JAVA_OPTS=-Xms1g -Xmx2g -XX:+UseG1GC
```

### 优化MySQL配置
编辑 `mysql/conf.d/my.cnf`：
```ini
innodb_buffer_pool_size = 512M
max_connections = 500
query_cache_size = 128M
```

## 监控

### 基础监控
```bash
# 查看系统资源
top
htop
df -h

# 查看Docker资源使用
docker stats

# 查看磁盘使用
du -sh /opt/evaluation/*
```

### 日志监控
```bash
# 实时查看应用日志
tail -f /opt/evaluation/logs/application.log

# 查看Docker日志
docker-compose logs -f --tail=100
```

---

**部署完成后，请：**
1. 修改默认数据库密码
2. 配置防火墙规则
3. 设置定期备份
4. 监控系统资源使用情况