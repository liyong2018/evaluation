# 部署说明（前端 SPA + 后端 API）

本项目前端使用 Vue Router 的 History 模式（`createWebHistory`），生产环境需要在反向代理（Nginx）中开启 **SPA 路由回退**（`try_files ... /index.html`），否则直接访问前端路由（如 `/data-management`）将返回后端 404（Whitelabel Error Page）。

## 1. 前端构建与部署

- 在前端目录执行构建：
  - `cd frontend`
  - `npm ci && npm run build`
- 将生成的 `frontend/dist` 内容复制到 Nginx 静态目录：
  - 容器内路径：`/usr/share/nginx/html`
  - 或根据你的环境指定的静态根目录

## 2. Nginx 配置（关键：SPA 回退）

`nginx/conf.d/default.conf` 已更新为：

- `location /`：
  - `root /usr/share/nginx/html;`
  - `try_files $uri $uri/ /index.html;`
  - 负责前端静态资源与 SPA 路由回退
- `location /api/`：
  - 反向代理到后端 Spring Boot（容器服务 `app:8087`）
- `location /ws/`：
  - WebSocket 代理（如后端需要）

注意端口：

- 该配置 `listen 80`，如果你的宿主机对外端口是 `8088`，请在部署层（容器映射或上游反向代理）将 `8088 -> 80` 映射；或者改为 `listen 8088;` 并重载 Nginx。

## 3. 后端服务

- Spring Boot 运行在容器内 `app:8087` 或宿主机端口（例如 `8081/8088`），请保证 `/api/**` 路径可访问。
- 数据库配置（MySQL 8.0）：`mysql://127.0.0.1:30314/evaluate_db`，用户名/密码：`root/123456`

## 4. 验证步骤

1. 访问首页：`http://<host>:8088/`（或对应端口）应加载前端 `index.html`
2. 直接访问前端路由：`http://<host>:8088/data-management` 应不再出现 Whitelabel 404
3. 打开浏览器控制台：
   - 静态资源（`*.js`, `*.css`）应从 `/` 路径加载成功
   - API 请求（`/api/...`）应成功代理到后端并返回数据
4. 如果仍出现 404：
   - 检查 `default.conf` 是否已生效（`nginx -t && nginx -s reload`）
   - 检查前端构建产物已复制到 `root` 目录
   - 确认上游端口映射（`8088 -> 80`）或修改 `listen` 后重载

## 5. 备用方案（无法立即改 Nginx）

- 临时改为 Hash 路由：在 `frontend/src/router/index.ts` 中使用 `createWebHashHistory()`，避免服务端对深链接的依赖。
- 后端回退控制器：在 Spring Boot 添加控制器，将非 `/api/**` 请求 `forward` 到 `index.html`（不推荐作为长期方案，最佳实践是由 Nginx 处理）。

## 6. 手动部署（无法自动化时）

由于 SSH 认证需要密码/环境受限时，可以按以下步骤手动部署。

### 6.1 服务器连接

```bash
ssh root@101.126.46.254
```

### 6.2 在服务器上创建部署目录

```bash
mkdir -p /opt/evaluation/{logs,uploads,backups}
cd /opt/evaluation
```

### 6.3 安装 Docker（如果未安装）

```bash
curl -fsSL https://get.docker.com | sh -s -- --mirror Aliyun
systemctl start docker
systemctl enable docker
docker --version
```

### 6.4 安装 Docker Compose（如果未安装）

```bash
curl -L 'https://github.com/docker/compose/releases/download/v2.20.2/docker-compose-$(uname -s)-$(uname -m)' -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose
docker-compose --version
```

### 6.5 上传项目文件

方法一：使用 scp 上传压缩包

```bash
tar -czf evaluation-deploy.tar.gz --exclude='.git' --exclude='node_modules' --exclude='target' --exclude='logs' --exclude='uploads' .
scp evaluation-deploy.tar.gz root@101.126.46.254:/opt/evaluation/
ssh root@101.126.46.254
cd /opt/evaluation
tar -xzf evaluation-deploy.tar.gz
rm evaluation-deploy.tar.gz
```

方法二：使用 rsync 上传

```bash
rsync -avz --exclude='.git' --exclude='node_modules' --exclude='target' --exclude='logs' --exclude='uploads' ./ root@101.126.46.254:/opt/evaluation/
```

### 6.6 构建和启动服务

```bash
cd /opt/evaluation
docker-compose build --no-cache
docker-compose up -d
docker-compose ps
```

### 6.7 验证部署

```bash
curl -f http://localhost:8087/actuator/health
docker-compose logs -f app
docker-compose ps
```

### 6.8 访问地址

- 应用直接访问: http://101.126.46.254:8087
- Nginx 反向代理（docker-compose 默认映射）: http://101.126.46.254:8088

### 6.9 常用管理命令

```bash
cd /opt/evaluation
docker-compose ps
docker-compose logs
docker-compose logs -f app
docker-compose restart
docker-compose down
docker-compose pull
docker-compose build --no-cache
docker-compose up -d
```

### 6.10 数据备份

```bash
docker-compose exec mysql mysqldump -uroot -p123456 evaluate_db > backup.sql
cp -r uploads/ backup_uploads/
docker run --rm -v /opt/evaluation/mysql_data:/data -v $(pwd):/backup alpine tar czf /backup/mysql_data.tar.gz -C /data .
```

### 6.11 故障排查

应用无法启动

```bash
docker-compose logs app
docker-compose exec app ping mysql
docker-compose exec app sh
```

数据库问题

```bash
docker-compose logs mysql
docker-compose exec mysql mysql -uroot -p123456
```

端口问题

```bash
netstat -tulpn | grep :8087
systemctl status firewalld
firewall-cmd --add-port=8087/tcp --permanent
firewall-cmd --reload
```

### 6.12 安全配置

修改数据库密码

```bash
docker-compose exec mysql mysql -uroot -p123456
```

```sql
ALTER USER 'root'@'%' IDENTIFIED BY '新密码';
FLUSH PRIVILEGES;
```

配置防火墙

```bash
apt-get update
apt-get install ufw
ufw allow 22/tcp
ufw allow 80/tcp
ufw allow 443/tcp
ufw allow 8087/tcp
ufw enable
```

### 6.13 性能优化

调整 JVM 参数（编辑 `docker-compose.yml`）

```yaml
environment:
  - JAVA_OPTS=-Xms1g -Xmx2g -XX:+UseG1GC
```

优化 MySQL 配置（编辑 `mysql/conf.d/my.cnf`）

```ini
innodb_buffer_pool_size = 512M
max_connections = 500
query_cache_size = 128M
```

### 6.14 监控

基础监控

```bash
top
htop
df -h
docker stats
du -sh /opt/evaluation/*
```

日志监控

```bash
tail -f /opt/evaluation/logs/application.log
docker-compose logs -f --tail=100
```
