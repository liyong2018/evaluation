#!/bin/bash

# 减灾能力评估系统Docker部署脚本（SCP版本）
# 使用SCP进行文件传输

set -e

# 配置变量
SSH_KEY="$HOME/.ssh/id_rsa_evaluation"
SERVER="root@101.126.46.254"
REMOTE_DIR="/opt/evaluation"
TEMP_DIR="/tmp/evaluation_deploy_$(date +%s)"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== 减灾能力评估系统Docker部署脚本 ===${NC}"
echo -e "${YELLOW}目标服务器: ${SERVER}${NC}"
echo -e "${YELLOW}部署目录: ${REMOTE_DIR}${NC}"
echo ""

# 1. 检查SSH密钥
echo -e "${GREEN}1. 检查SSH密钥...${NC}"
if [ ! -f "$SSH_KEY" ]; then
    echo -e "${RED}错误: SSH密钥不存在: ${SSH_KEY}${NC}"
    echo "请先运行: ssh-keygen -t rsa -b 4096 -f ~/.ssh/id_rsa_evaluation"
    exit 1
fi

# 2. 检查本地文件
echo -e "${GREEN}2. 检查本地部署文件...${NC}"
if [ ! -f "docker-compose.yml" ]; then
    echo -e "${RED}错误: docker-compose.yml 文件不存在${NC}"
    exit 1
fi

if [ ! -f "Dockerfile" ]; then
    echo -e "${RED}错误: Dockerfile 文件不存在${NC}"
    exit 1
fi

echo -e "${GREEN}✓ 本地文件检查完成${NC}"

# 3. 测试SSH连接
echo -e "${GREEN}3. 测试SSH连接...${NC}"
if ! ssh -i "$SSH_KEY" -o StrictHostKeyChecking=no ${SERVER} "echo 'SSH连接成功'" > /dev/null 2>&1; then
    echo -e "${RED}错误: SSH连接失败${NC}"
    echo "请检查SSH密钥配置"
    exit 1
fi
echo -e "${GREEN}✓ SSH连接正常${NC}"

# 4. 检查Docker环境
echo -e "${GREEN}4. 检查服务器Docker环境...${NC}"
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

# 5. 创建临时目录并打包
echo -e "${GREEN}5. 准备部署文件...${NC}"
mkdir -p "$TEMP_DIR"
tar -czf "$TEMP_DIR/evaluation.tar.gz" \
    --exclude='.git' \
    --exclude='node_modules' \
    --exclude='target' \
    --exclude='logs/*' \
    --exclude='uploads/*' \
    --exclude='*.log' \
    .
echo -e "${GREEN}✓ 文件打包完成${NC}"

# 6. 上传压缩包
echo -e "${GREEN}6. 上传部署文件...${NC}"
scp -i "$SSH_KEY" -o StrictHostKeyChecking=no "$TEMP_DIR/evaluation.tar.gz" ${SERVER}:/tmp/
echo -e "${GREEN}✓ 文件上传完成${NC}"

# 7. 在服务器上解压和部署
echo -e "${GREEN}7. 服务器端部署...${NC}"
ssh -i "$SSH_KEY" ${SERVER} "
# 创建部署目录
mkdir -p ${REMOTE_DIR}/{logs,uploads,backups}

# 停止现有服务
cd ${REMOTE_DIR} && \
if [ -f 'docker-compose.yml' ]; then
    docker-compose down --remove-orphans || true
fi

# 解压新文件
cd /tmp && \
tar -xzf evaluation.tar.gz -C ${REMOTE_DIR}/ && \
rm evaluation.tar.gz

# 构建和启动服务
cd ${REMOTE_DIR} && \
docker-compose build --no-cache && \
docker-compose up -d
"
echo -e "${GREEN}✓ 服务器端部署完成${NC}"

# 8. 清理临时文件
echo -e "${GREEN}8. 清理临时文件...${NC}"
rm -rf "$TEMP_DIR"
echo -e "${GREEN}✓ 临时文件清理完成${NC}"

# 9. 等待服务启动
echo -e "${GREEN}9. 等待服务启动...${NC}"
sleep 30

# 10. 检查服务状态
echo -e "${GREEN}10. 检查服务状态...${NC}"
ssh -i "$SSH_KEY" ${SERVER} "
cd ${REMOTE_DIR} && \
echo '=== Docker容器状态 ===' && \
docker-compose ps && \
echo '' && \
echo '=== 服务健康检查 ===' && \
for i in {1..12}; do
    if curl -f http://localhost:8087/actuator/health >/dev/null 2>&1; then
        echo '应用服务正常 ✓'
        break
    else
        echo -n '.'
        sleep 5
    fi
    if [ \$i -eq 12 ]; then
        echo ''
        echo '应用服务可能未正常启动，请查看日志'
        echo '查看日志: docker-compose logs app'
    fi
done
"

# 11. 显示部署信息
echo ""
echo -e "${GREEN}=== 部署完成 ===${NC}"
echo -e "${YELLOW}应用访问地址: http://101.126.46.254:8087${NC}"
echo ""
echo -e "${GREEN}常用命令:${NC}"
echo -e "查看日志: ssh -i ${SSH_KEY} ${SERVER} 'cd ${REMOTE_DIR} && docker-compose logs -f'"
echo -e "重启服务: ssh -i ${SSH_KEY} ${SERVER} 'cd ${REMOTE_DIR} && docker-compose restart'"
echo -e "停止服务: ssh -i ${SSH_KEY} ${SERVER} 'cd ${REMOTE_DIR} && docker-compose down'"
echo -e "服务状态: ssh -i ${SSH_KEY} ${SERVER} 'cd ${REMOTE_DIR} && docker-compose ps'"
echo ""

# 12. 创建服务器管理脚本
echo -e "${GREEN}11. 创建服务器管理脚本...${NC}"
ssh -i "$SSH_KEY" ${SERVER} "
cat > ${REMOTE_DIR}/manage.sh << 'EOF'
#!/bin/bash
# 服务器管理脚本

cd /opt/evaluation

case \"\$1\" in
    logs)
        docker-compose logs -f app
        ;;
    logs-all)
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
        echo '=== 应用健康检查 ==='
        if curl -f http://localhost:8087/actuator/health >/dev/null 2>&1; then
            echo '应用服务: 正常 ✓'
            curl -s http://localhost:8087/actuator/health | jq '.'
        else
            echo '应用服务: 异常 ✗'
        fi
        echo ''
        echo '=== 数据库连接检查 ==='
        if docker-compose exec mysql mysqladmin ping -h localhost -uroot -p123456 >/dev/null 2>&1; then
            echo '数据库连接: 正常 ✓'
        else
            echo '数据库连接: 异常 ✗'
        fi
        ;;
    backup)
        echo '开始备份数据...'
        BACKUP_DIR=\"${REMOTE_DIR}/backups/$(date +%Y%m%d_%H%M%S)\"
        mkdir -p \"\$BACKUP_DIR\"

        # 备份数据库
        docker-compose exec mysql mysqldump -uroot -p123456 evaluate_db | gzip > \"\$BACKUP_DIR/database.sql.gz\"
        echo '数据库备份完成'

        # 备份上传文件
        if [ -d 'uploads' ]; then
            cp -r uploads \"\$BACKUP_DIR/\"
            echo '上传文件备份完成'
        fi

        echo \"备份完成: \$BACKUP_DIR\"
        ;;
    update)
        echo '更新功能需要从本地执行更新脚本'
        echo '本地命令: ./scripts/update.sh'
        ;;
    *)
        echo '用法: ./manage.sh {logs|logs-all|restart|stop|start|status|health|backup|update}'
        echo '  logs      - 查看应用日志'
        echo '  logs-all  - 查看所有服务日志'
        echo '  restart   - 重启服务'
        echo '  stop      - 停止服务'
        echo '  start     - 启动服务'
        echo '  status    - 查看服务状态'
        echo '  health    - 健康检查'
        echo '  backup    - 数据备份'
        echo '  update    - 更新应用'
        exit 1
        ;;
esac
EOF

chmod +x ${REMOTE_DIR}/manage.sh

echo '管理脚本创建完成'
echo '使用方法: ./manage.sh [command]'
"
echo -e "${GREEN}✓ 管理脚本创建完成${NC}"
echo ""
echo -e "${YELLOW}服务器管理命令:${NC}"
echo -e "ssh -i ${SSH_KEY} ${SERVER} '${REMOTE_DIR}/manage.sh [logs|restart|stop|start|status|health|backup]'"
echo ""
echo -e "${GREEN}🎉 部署成功！${NC}"