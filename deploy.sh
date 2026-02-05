#!/bin/bash

# 减灾能力评估工具Docker部署脚本
# 目标服务器: root@101.126.46.254

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 配置变量
SERVER_HOST="${SERVER_HOST:-101.126.46.254}"
SERVER_USER="${SERVER_USER:-root}"
SERVER="${SERVER_USER}@${SERVER_HOST}"
SSH_KEY="${SSH_KEY:-}"
REMOTE_DIR="${REMOTE_DIR:-/opt/evaluation}"
COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.prod.yml}"

SSH_OPTS="-o ConnectTimeout=10"
SCP_OPTS=""
if [ -n "$SSH_KEY" ]; then
    SSH_OPTS="$SSH_OPTS -i $SSH_KEY -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null"
    SCP_OPTS="-i $SSH_KEY -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null"
fi

echo -e "${GREEN}=== 减灾能力评估工具Docker部署脚本 ===${NC}"
echo -e "${YELLOW}目标服务器: ${SERVER}${NC}"
echo -e "${YELLOW}部署目录: ${REMOTE_DIR}${NC}"
echo ""

# 检查本地文件
echo -e "${GREEN}1. 检查本地部署文件...${NC}"
if [ ! -f "${COMPOSE_FILE}" ]; then
    COMPOSE_FILE="docker-compose.yml"
fi
if [ ! -f "${COMPOSE_FILE}" ]; then
    echo -e "${RED}错误: docker-compose.prod.yml 或 docker-compose.yml 文件不存在${NC}"
    exit 1
fi
echo -e "${YELLOW}使用编排文件: ${COMPOSE_FILE}${NC}"

if [ ! -f "Dockerfile" ]; then
    echo -e "${RED}错误: Dockerfile 文件不存在${NC}"
    exit 1
fi

echo -e "${GREEN}✓ 本地文件检查完成${NC}"

# 检查SSH连接
echo -e "${GREEN}2. 检查SSH连接...${NC}"
if ! ssh ${SSH_OPTS} ${SERVER} "echo 'SSH连接成功'" > /dev/null 2>&1; then
    echo -e "${RED}错误: 无法连接到服务器 ${SERVER}${NC}"
    echo "请检查："
    echo "1. 服务器IP地址是否正确"
    echo "2. SSH服务是否运行"
    echo "3. SSH密钥是否配置正确"
    echo "4. 网络连接是否正常"
    exit 1
fi
echo -e "${GREEN}✓ SSH连接正常${NC}"

# 在服务器上创建部署目录
echo -e "${GREEN}3. 在服务器上创建部署目录...${NC}"
ssh ${SSH_OPTS} ${SERVER} "mkdir -p ${REMOTE_DIR}/{logs,uploads/thematic-maps,backups} && chown -R 999:999 ${REMOTE_DIR}/logs ${REMOTE_DIR}/uploads || true"
echo -e "${GREEN}✓ 部署目录创建完成${NC}"

# 检查Docker环境
echo -e "${GREEN}4. 检查服务器Docker环境...${NC}"
DOCKER_VERSION=$(ssh ${SSH_OPTS} ${SERVER} "docker --version 2>/dev/null" || echo "")
if [ -z "$DOCKER_VERSION" ]; then
    echo -e "${YELLOW}警告: Docker未安装，正在安装Docker...${NC}"
    ssh ${SSH_OPTS} ${SERVER} "curl -fsSL https://get.docker.com | sh -s -- --mirror Aliyun && \
                  systemctl start docker && \
                  systemctl enable docker"
fi

COMPOSE_VERSION=$(ssh ${SSH_OPTS} ${SERVER} "docker-compose --version 2>/dev/null" || echo "")
if [ -z "$COMPOSE_VERSION" ]; then
    echo -e "${YELLOW}警告: Docker Compose未安装，正在安装...${NC}"
    ssh ${SSH_OPTS} ${SERVER} "curl -L 'https://github.com/docker/compose/releases/download/v2.20.2/docker-compose-$(uname -s)-$(uname -m)' -o /usr/local/bin/docker-compose && \
                  chmod +x /usr/local/bin/docker-compose"
fi
echo -e "${GREEN}✓ Docker环境检查完成${NC}"

# 备份现有数据
echo -e "${GREEN}5. 备份现有数据...${NC}"
BACKUP_DIR="${REMOTE_DIR}/backups/$(date +%Y%m%d_%H%M%S)"
ssh ${SSH_OPTS} ${SERVER} "
if [ -d '${REMOTE_DIR}/mysql_data' ]; then
    mkdir -p ${BACKUP_DIR}
    docker run --rm -v ${REMOTE_DIR}/mysql_data:/data -v ${BACKUP_DIR}:/backup alpine tar czf /backup/mysql_data.tar.gz -C /data .
    echo '数据库备份完成'
fi
"
echo -e "${GREEN}✓ 数据备份完成${NC}"

# 停止现有服务
echo -e "${GREEN}6. 停止现有服务...${NC}"
ssh ${SSH_OPTS} ${SERVER} "
cd ${REMOTE_DIR} && \
if [ -f '${COMPOSE_FILE}' ]; then
    docker-compose -f ${COMPOSE_FILE} down --remove-orphans || true
fi
"
echo -e "${GREEN}✓ 现有服务已停止${NC}"

# 上传文件
echo -e "${GREEN}7. 上传项目文件...${NC}"
if command -v rsync >/dev/null 2>&1; then
    rsync -avz --delete \
        -e "ssh ${SSH_OPTS}" \
        --exclude='.git' \
        --exclude='node_modules' \
        --exclude='target' \
        --exclude='logs/*' \
        --exclude='uploads/*' \
        --exclude='*.log' \
        --exclude='backups' \
        ./ ${SERVER}:${REMOTE_DIR}/
else
    TEMP_DIR="$(mktemp -d 2>/dev/null || mktemp -d -t evaluation_deploy)"
    tar -czf "$TEMP_DIR/evaluation-deploy.tar.gz" \
        --exclude='.git' \
        --exclude='node_modules' \
        --exclude='target' \
        --exclude='logs/*' \
        --exclude='uploads/*' \
        --exclude='*.log' \
        --exclude='backups' \
        .
    scp ${SCP_OPTS} "$TEMP_DIR/evaluation-deploy.tar.gz" ${SERVER}:/tmp/
    ssh ${SSH_OPTS} ${SERVER} "cd /tmp && tar -xzf evaluation-deploy.tar.gz -C ${REMOTE_DIR} && rm evaluation-deploy.tar.gz"
    rm -rf "$TEMP_DIR"
fi
echo -e "${GREEN}✓ 文件上传完成${NC}"

# 构建和启动服务
echo -e "${GREEN}8. 构建和启动服务...${NC}"
ssh ${SSH_OPTS} ${SERVER} "
cd ${REMOTE_DIR} && \
if [ '${COMPOSE_FILE}' = 'docker-compose.prod.yml' ]; then
    docker-compose -f ${COMPOSE_FILE} pull || true && \
    docker-compose -f ${COMPOSE_FILE} up -d --remove-orphans
else
    docker-compose -f ${COMPOSE_FILE} build --no-cache && \
    docker-compose -f ${COMPOSE_FILE} up -d --remove-orphans
fi
"
echo -e "${GREEN}✓ 服务启动完成${NC}"

# 等待服务启动
echo -e "${GREEN}9. 等待服务启动...${NC}"
sleep 30

# 检查服务状态
echo -e "${GREEN}10. 检查服务状态...${NC}"
ssh ${SSH_OPTS} ${SERVER} "
cd ${REMOTE_DIR} && \
echo '=== Docker容器状态 ===' && \
docker-compose -f ${COMPOSE_FILE} ps && \
echo '' && \
echo '=== 服务健康检查 ===' && \
for i in {1..10}; do
    if curl -f http://localhost:8081/actuator/health >/dev/null 2>&1 || \
       curl -f http://localhost:8082/actuator/health >/dev/null 2>&1 || \
       curl -f http://localhost:8087/actuator/health >/dev/null 2>&1; then
        echo '应用服务正常 ✓'
        break
    fi
    echo -n '.'
    sleep 5
    if [ \$i -eq 10 ]; then
        echo ''
        echo '应用服务可能未正常启动，请检查日志'
    fi
done
"

# 显示部署信息
echo ""
echo -e "${GREEN}=== 部署完成 ===${NC}"
if [ "${COMPOSE_FILE}" = "docker-compose.prod.yml" ]; then
    echo -e "${YELLOW}前端访问地址: http://${SERVER_HOST}${NC}"
    echo -e "${YELLOW}后端访问地址: http://${SERVER_HOST}:8081${NC}"
else
    echo -e "${YELLOW}前端访问地址: http://${SERVER_HOST}:8088${NC}"
    echo -e "${YELLOW}后端访问地址: http://${SERVER_HOST}:8082${NC}"
fi
echo ""
echo -e "${GREEN}常用命令:${NC}"
echo -e "查看日志: ssh ${SSH_OPTS} ${SERVER} 'cd ${REMOTE_DIR} && docker-compose -f ${COMPOSE_FILE} logs -f'"
echo -e "重启服务: ssh ${SSH_OPTS} ${SERVER} 'cd ${REMOTE_DIR} && docker-compose -f ${COMPOSE_FILE} restart'"
echo -e "停止服务: ssh ${SSH_OPTS} ${SERVER} 'cd ${REMOTE_DIR} && docker-compose -f ${COMPOSE_FILE} down'"
echo -e "更新服务: ssh ${SSH_OPTS} ${SERVER} 'cd ${REMOTE_DIR} && docker-compose -f ${COMPOSE_FILE} pull && docker-compose -f ${COMPOSE_FILE} up -d'"
echo ""
echo -e "${GREEN}部署成功！${NC}"
