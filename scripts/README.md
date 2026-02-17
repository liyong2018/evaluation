# 减灾能力评估系统 - 脚本与文档

## 快速导航

- [部署指南](#部署指南) - 快速部署到生产环境
- [开发指南](#开发指南) - 本地开发环境设置
- [代码规范](#代码规范) - 编码风格与提交规范

---

## 部署指南

### 环境要求

- Docker 20.10+
- Docker Compose 2.0+
- MySQL 8.0+
- Java 17+
- Node.js 16+

### 快速部署 (Linux/Mac)

```bash
# 1. 克隆项目
git clone <repository_url>
cd evaluation

# 2. 配置环境变量（可选）
export SERVER_HOST=101.126.46.254
export SERVER_USER=root
export REMOTE_DIR=/opt/evaluation

# 3. 执行部署脚本
chmod +x scripts/deploy.sh
./scripts/deploy.sh
```

### 快速部署 (Windows)

```powershell
# 1. 进入项目目录
cd evaluation

# 2. 设置环境变量（可选）
$env:RemoteUser = "htht"
$env:RemoteHost = "172.16.43.189"

# 3. 执行部署脚本
.\scripts\deploy.ps1
```

### 手动部署步骤

#### 1. 服务器连接

```bash
ssh root@101.126.46.254
```

#### 2. 在服务器上创建部署目录

```bash
mkdir -p /opt/evaluation/{logs,uploads,backups}
cd /opt/evaluation
```

#### 3. 安装 Docker（如果未安装）

```bash
curl -fsSL https://get.docker.com | sh -s -- --mirror Aliyun
systemctl start docker
systemctl enable docker
docker --version
```

#### 4. 上传项目文件

```bash
# 方法一：使用 scp 上传压缩包
tar -czf evaluation-deploy.tar.gz --exclude='.git' --exclude='node_modules' --exclude='target' .
scp evaluation-deploy.tar.gz root@101.126.46.254:/opt/evaluation/
ssh root@101.126.46.254
cd /opt/evaluation
tar -xzf evaluation-deploy.tar.gz
rm evaluation-deploy.tar.gz
```

#### 5. 构建和启动服务

```bash
cd /opt/evaluation
docker-compose build --no-cache
docker-compose up -d
docker-compose ps
```

### Nginx 配置（SPA 路由回退）

前端使用 Vue Router 的 History 模式，需要在 Nginx 中配置 SPA 路由回退：

```nginx
location / {
    root /usr/share/nginx/html;
    try_files $uri $uri/ /index.html;  # 关键：SPA 路由回退
}

location /api/ {
    proxy_pass http://app:8087;
}
```

### 常用管理命令

```bash
cd /opt/evaluation
docker-compose ps              # 查看状态
docker-compose logs -f app      # 查看日志
docker-compose restart          # 重启服务
docker-compose down             # 停止服务
docker-compose pull && docker-compose up -d  # 更新服务
```

---

## 开发指南

### 项目结构

```
evaluation/
├── src/main/java/               # 后端源码
│   └── com/evaluate/
│       ├── config/              # 配置类
│       ├── controller/          # 控制器
│       ├── service/             # 服务层
│       ├── mapper/              # 数据访问层
│       └── entity/              # 实体类
├── frontend/                    # 前端源码
│   ├── src/
│   │   ├── api/                 # API接口
│   │   ├── components/          # 组件
│   │   ├── views/               # 页面
│   │   └── router/              # 路由
│   └── package.json
├── scripts/                     # 工具脚本
│   ├── deploy.sh                # Linux/Mac部署脚本
│   ├── deploy.ps1               # Windows部署脚本
│   ├── utils.py                 # Python工具脚本
│   └── README.md                # 本文档
└── src/main/resources/
    ├── application.yml          # 应用配置
    └── sql/                     # SQL脚本
```

### 本地开发

#### 后端开发

```bash
# 构建项目
mvn clean package -DskipTests

# 运行应用
mvn spring-boot:run

# 或直接运行 JAR
java -jar target/disaster-reduction-evaluation-1.0.0.jar
```

#### 前端开发

```bash
cd frontend

# 安装依赖
npm ci

# 开发模式
npm run dev

# 生产构建
npm run build

# 代码检查
npm run lint
npm run type-check
```

---

## 代码规范

### 编码风格

#### Java
- Java 17, UTF-8, 4空格缩进
- 包名 `com.evaluate.*` (小写)
- 类名 `PascalCase`
- 方法/字段 `camelCase`
- 常量 `UPPER_SNAKE_CASE`
- 使用 Lombok 简化代码
- Controller 保持精简，业务逻辑放在 Service 层

#### 前端 (TypeScript/Vue)
- 使用 TypeScript
- 组件命名 `PascalCase.vue`
- 优先使用命名导出
- 遵循 ESLint 规则 (`frontend/eslint.config.ts`)

### 提交规范

使用 Conventional Commits 格式：

```
feat: 新功能
fix: 修复bug
docs: 文档更新
refactor: 代码重构
test: 测试相关
chore: 构建/工具相关
```

示例：
```
feat(service): 添加社区评估模型支持
fix(deployment): 修复前端路由404问题
docs(readme): 更新部署说明
```

---

## 工具脚本说明

### deploy.sh (Linux/Mac)

完整部署脚本，支持以下功能：

```bash
# 完整部署
./scripts/deploy.sh

# 显示远程部署说明
./scripts/deploy.sh remote

# 显示帮助
./scripts/deploy.sh help
```

环境变量：
- `SERVER_HOST` - 服务器地址 (默认: 101.126.46.254)
- `SERVER_USER` - 服务器用户 (默认: root)
- `REMOTE_DIR` - 远程目录 (默认: /opt/evaluation)
- `SSH_KEY` - SSH密钥路径 (可选)

### deploy.ps1 (Windows)

Windows PowerShell 部署脚本：

```powershell
# 快速部署
.\scripts\deploy.ps1

# 设置SSH密钥
.\scripts\deploy.ps1 setup-ssh

# 显示帮助
.\scripts\deploy.ps1 help
```

### utils.py (Python)

工具脚本集合：

```bash
# 设置SSH密钥认证
python scripts/utils.py setup-ssh

# 生成消防员配置SQL
python scripts/utils.py generate-sql

# 显示帮助
python scripts/utils.py help
```

---

## 常见问题

### 1. 前端路由404

**问题**: 直接访问前端路由（如 /data-management）出现404

**解决**: 确保Nginx配置了 `try_files $uri $uri/ /index.html;`

### 2. 后端连接失败

**问题**: API请求失败

**解决**: 检查后端服务状态、端口配置、防火墙设置

### 3. 数据库连接失败

**问题**: 无法连接到MySQL

**解决**:
```bash
docker-compose logs mysql
docker-compose exec mysql mysql -uroot -p123456
```

---

**文档版本**: 1.0
**最后更新**: 2025-01-26
