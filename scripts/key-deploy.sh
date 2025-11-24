#!/bin/bash

# 减灾能力评估系统Docker部署脚本（SSH密钥版本）
# 使用SSH密钥进行认证

set -e

# 配置变量
SSH_KEY="$HOME/.ssh/id_rsa_evaluation"
SERVER="root@101.126.46.254"
REMOTE_DIR="/opt/evaluation"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== 减灾能力评估系统Docker部署脚本 ===${NC}"
echo -e "${YELLOW}目标服务器: ${SERVER}${NC}"
echo -e "${YELLOW}部署目录: ${REMOTE_DIR}${NC}"
echo ""

# 检查SSH密钥
if [ ! -f "$SSH_KEY" ]; then
    echo -e "${RED}错误: SSH密钥不存在: ${SSH_KEY}${NC}"
    echo "请先运行: ssh-keygen -t rsa -b 4096 -f ~/.ssh/id_rsa_evaluation"
    exit 1
fi

# 1. 检查本地文件
echo -e "${GREEN}1. 检查本地部署文件...${NC}"
if [ ! -f "docker-compose.yml" ]; then
    echo -e "${RED}错误: docker-compose.yml 文件不存在${NC}"
    exit 1
fi

if [ ! -f "Dockerfile" ]; then
    echo -e "${RED}错误: Dockerfile 文件不存在${NC}"
    exit 1
fi

echo -e "${GREEN}✓ 本地文件检查完成${NC}"

# 2. 测试SSH连接
echo -e "${GREEN}2. 测试SSH连接...${NC}"
if ! ssh -i "$SSH_KEY" -o StrictHostKeyChecking=no ${SERVER} "echo 'SSH连接成功'" > /dev/null 2>&1; then
    echo -e "${RED}错误: SSH连接失败${NC}"
    echo "请检查SSH密钥配置"
    exit 1
fi
echo -e "${GREEN}✓ SSH连接正常${NC}"

# 3. 检查Docker环境
echo -e "${GREEN}3. 检查服务器Docker环境...${NC}"
DOCKER_VERSION=$(ssh -i "$SSH_KEY" ${SERVER} "docker --version 2>/dev/null" || echo "")
if [ -z "$DOCKER_VERSION" ]; then
    echo -e "${YELLOW}警告: Docker未安装，正在安装Docker...${NC}"
    ssh -i "$SSH_KEY" ${SERVER} "curl -fsSL https://get.docker.com | sh -s -- --mirror Aliyun && \
                  systemctl start docker && \
                  systemctl enable docker"
fi

COMPOSE_VERSION=$(ssh -i "$SSH_KEY" ${SERVER} "docker-compose --version 2>/dev/null" || echo "")
if [ -z "$COMPOSE_VERSION" ]; then
    echo -e "${YELLOW}警告: Docker Compose未安装，正在安装...${NC}"
    ssh -i "$SSH_KEY" ${SERVER} "curl -L 'https://github.com/docker/compose/releases/download/v2.20.2/docker-compose-$(uname -s)-$(uname -m)' -o /usr/local/bin/docker-compose && \
                  chmod +x /usr/local/bin/docker-compose"
fi
echo -e "${GREEN}✓ Docker环境检查完成${NC}"

# 4. 创建部署目录
echo -e "${GREEN}4. 创建部署目录...${NC}"
ssh -i "$SSH_KEY" ${SERVER} "mkdir -p ${REMOTE_DIR}/{logs,uploads,backups}"
echo -e "${GREEN}✓ 部署目录创建完成${NC}"

# 5. 停止现有服务
echo -e "${GREEN}5. 停止现有服务...${NC}"
ssh -i "$SSH_KEY" ${SERVER} "
cd ${REMOTE_DIR} && \
if [ -f 'docker-compose.yml' ]; then
    docker-compose down --remove-orphans || true
fi
"
echo -e "${GREEN}✓ 现有服务已停止${NC}"

# 6. 上传文件
echo -e "${GREEN}6. 上传项目文件...${NC}"
rsync -avz -e "ssh -i $SSH_KEY -o StrictHostKeyChecking=no" \
    --exclude='.git' \
    --exclude='node_modules' \
    --exclude='target' \
    --exclude='logs/*' \
    --exclude='uploads/*' \
    --exclude='*.log' \
    ./ ${SERVER}:${REMOTE_DIR}/
echo -e "${GREEN}✓ 文件上传完成${NC}"

# 7. 构建和启动服务
echo -e "${GREEN}7. 构建和启动服务...${NC}"
ssh -i "$SSH_KEY" ${SERVER} "
cd ${REMOTE_DIR} && \
docker-compose build --no-cache && \
docker-compose up -d
"
echo -e "${GREEN}✓ 服务启动完成${NC}"

# 8. 等待服务启动
echo -e "${GREEN}8. 等待服务启动...${NC}"
sleep 30

# 9. 检查服务状态
echo -e "${GREEN}9. 检查服务状态...${NC}"
ssh -i "$SSH_KEY" ${SERVER} "
cd ${REMOTE_DIR} && \
echo '=== Docker容器状态 ===' && \
docker-compose ps && \
echo '' && \
echo '=== 服务健康检查 ===' && \
for i in {1..10}; do
    if curl -f http://localhost:8087/actuator/health >/dev/null 2>&1; then
        echo '应用服务正常 ✓'
        break
    else
        echo -n '.'
        sleep 5
    fi
    if [ \$i -eq 10 ]; then
        echo ''
        echo '应用服务可能未正常启动，请检查日志'
    fi
done
"

# 10. 显示部署信息
echo ""
echo -e "${GREEN}=== 部署完成 ===${NC}"
echo -e "${YELLOW}应用访问地址: http://101.126.46.254:8087${NC}"
echo ""
echo -e "${GREEN}常用命令:${NC}"
echo -e "查看日志: ssh -i ${SSH_KEY} ${SERVER} 'cd ${REMOTE_DIR} && docker-compose logs -f'"
echo -e "重启服务: ssh -i ${SSH_KEY} ${SERVER} 'cd ${REMOTE_DIR} && docker-compose restart'"
echo -e "停止服务: ssh -i ${SSH_KEY} ${SERVER} 'cd ${REMOTE_DIR} && docker-compose down'"
echo ""
echo -e "${GREEN}部署成功！${NC}"

# 11. 创建管理脚本
echo -e "${GREEN}10. 创建服务器管理脚本...${NC}"
ssh -i "$SSH_KEY" ${SERVER} "
cat > ${REMOTE_DIR}/manage.sh << 'EOF'
#!/bin/bash
# 服务器管理脚本

cd /opt/evaluation

case \"\$1\" in
    logs)
        docker-compose logs -f
        ;;
    restart)
        docker-compose restart
        ;;
    stop)
        docker-compose down
        ;;
    start)
        docker-compose up -d
        ;;
    status)
        docker-compose ps
        ;;
    health)
        curl -f http://localhost:8087/actuator/health
        ;;
    update)
        echo '更新功能需要从本地执行更新脚本'
        ;;
    *)
        echo '用法: ./manage.sh {logs|restart|stop|start|status|health}'
        echo '  logs   - 查看应用日志'
        echo '  restart- 重启服务'
        echo '  stop   - 停止服务'
        echo '  start  - 启动服务'
        echo '  status - 查看服务状态'
        echo '  health - 健康检查'
        exit 1
        ;;
esac
EOF

chmod +x ${REMOTE_DIR}/manage.sh
"
echo -e "${GREEN}✓ 管理脚本创建完成${NC}"
echo -e "${YELLOW}服务器管理命令: ssh -i ${SSH_KEY} ${SERVER} '${REMOTE_DIR}/manage.sh [logs|restart|stop|start|status|health]'${NC}"