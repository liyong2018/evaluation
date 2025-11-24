#!/bin/bash

# 减灾能力评估系统Docker部署脚本（修复版本）
# 修复Docker Compose兼容性问题

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

# 4. 修复Docker环境
echo -e "${GREEN}4. 修复Docker环境...${NC}"
ssh -i "$SSH_KEY" ${SERVER} "
# 卸载可能有问题的docker-compose
rm -f /usr/local/bin/docker-compose
pip uninstall -y docker-compose 2>/dev/null || true

# 安装兼容的Docker Compose版本
curl -L 'https://github.com/docker/compose/releases/download/v2.21.0/docker-compose-$(uname -s)-$(uname -m)' -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# 确保Docker服务运行
systemctl start docker
systemctl enable docker

echo 'Docker环境修复完成'
"
echo -e "${GREEN}✓ Docker环境修复完成${NC}"

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
    --exclude='backups' \
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

# 清理旧容器和镜像
docker system prune -f

# 解压新文件
cd /tmp && \
tar -xzf evaluation.tar.gz -C ${REMOTE_DIR}/ && \
rm evaluation.tar.gz

# 构建和启动服务
cd ${REMOTE_DIR} && \
docker-compose build --no-cache && \
docker-compose up -d

# 等待容器启动
sleep 20

# 检查容器状态
echo '=== 容器启动状态 ==='
docker-compose ps
"
echo -e "${GREEN}✓ 服务器端部署完成${NC}"

# 8. 清理临时文件
echo -e "${GREEN}8. 清理临时文件...${NC}"
rm -rf "$TEMP_DIR"
echo -e "${GREEN}✓ 临时文件清理完成${NC}"

# 9. 等待服务启动
echo -e "${GREEN}9. 等待应用启动...${NC}"
sleep 40

# 10. 检查服务状态
echo -e "${GREEN}10. 检查服务状态...${NC}"
ssh -i "$SSH_KEY" ${SERVER} "
cd ${REMOTE_DIR}

echo '=== Docker容器状态 ==='
docker-compose ps

echo ''
echo '=== 服务健康检查 ==='

# 检查应用服务
APP_HEALTH=0
for i in {1..20}; do
    if curl -f http://localhost:8087/actuator/health >/dev/null 2>&1; then
        echo '应用服务: 正常 ✓'
        APP_HEALTH=1
        break
    else
        echo -n '.'
        sleep 3
    fi
done

if [ \$APP_HEALTH -eq 0 ]; then
    echo ''
    echo '应用服务: 启动中或异常 ⚠️'
    echo '查看应用日志: docker-compose logs app'
fi

# 检查数据库
echo ''
echo '=== 数据库连接检查 ==='
if docker-compose exec -T mysql mysqladmin ping -h localhost -uroot -p123456 >/dev/null 2>&1; then
    echo '数据库连接: 正常 ✓'

    # 检查数据库和表
    echo '数据库表检查:'
    docker-compose exec -T mysql mysql -uroot -p123456 -e \"SHOW DATABASES;\" evaluate_db 2>/dev/null | head -10 || echo '数据库检查跳过'
else
    echo '数据库连接: 异常 ✗'
    echo '查看数据库日志: docker-compose logs mysql'
fi

# 检查Redis
echo ''
echo '=== Redis连接检查 ==='
if docker-compose exec -T redis redis-cli ping >/dev/null 2>&1; then
    echo 'Redis连接: 正常 ✓'
else
    echo 'Redis连接: 异常 ✗'
fi
"

# 11. 显示部署信息
echo ""
echo -e "${GREEN}=== 部署完成 ===${NC}"
echo -e "${YELLOW}应用访问地址:${NC}"
echo -e "  - 直接访问: http://101.126.46.254:8087"
echo -e "  - 健康检查: http://101.126.46.254:8087/actuator/health"
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
        echo '查看应用日志:'
        docker-compose logs -f app
        ;;
    logs-all)
        echo '查看所有服务日志:'
        docker-compose logs -f
        ;;
    logs-mysql)
        echo '查看数据库日志:'
        docker-compose logs -f mysql
        ;;
    restart)
        echo '重启所有服务:'
        docker-compose restart
        ;;
    restart-app)
        echo '重启应用服务:'
        docker-compose restart app
        ;;
    stop)
        echo '停止所有服务:'
        docker-compose down
        ;;
    start)
        echo '启动所有服务:'
        docker-compose up -d
        ;;
    status)
        echo '=== 服务状态 ==='
        docker-compose ps
        ;;
    health)
        echo '=== 健康检查 ==='

        # 应用服务检查
        echo '应用服务:'
        if curl -s http://localhost:8087/actuator/health >/dev/null 2>&1; then
            echo '  状态: 正常 ✓'
            curl -s http://localhost:8087/actuator/health 2>/dev/null | head -3 || echo '  详情: 获取失败'
        else
            echo '  状态: 异常 ✗'
        fi

        # 数据库检查
        echo '数据库:'
        if docker-compose exec -T mysql mysqladmin ping -h localhost -uroot -p123456 >/dev/null 2>&1; then
            echo '  状态: 正常 ✓'
        else
            echo '  状态: 异常 ✗'
        fi

        # Redis检查
        echo 'Redis:'
        if docker-compose exec -T redis redis-cli ping >/dev/null 2>&1; then
            echo '  状态: 正常 ✓'
        else
            echo '  状态: 异常 ✗'
        fi
        ;;
    backup)
        echo '开始数据备份...'
        BACKUP_DIR=\"${REMOTE_DIR}/backups/$(date +%Y%m%d_%H%M%S)\"
        mkdir -p \"\$BACKUP_DIR\"

        echo '备份数据库...'
        docker-compose exec -T mysql mysqldump -uroot -p123456 evaluate_db | gzip > \"\$BACKUP_DIR/database.sql.gz\"

        if [ -d 'uploads' ]; then
            echo '备份上传文件...'
            cp -r uploads \"\$BACKUP_DIR/\" 2>/dev/null || true
        fi

        echo '备份配置文件...'
        cp docker-compose.yml \"\$BACKUP_DIR/\" 2>/dev/null || true

        echo \"备份完成: \$BACKUP_DIR\"
        ls -la \"\$BACKUP_DIR\"
        ;;
    clean)
        echo '清理系统资源...'
        docker system prune -f
        echo '清理完成'
        ;;
    update)
        echo '更新应用需要从本地执行:'
        echo './scripts/update.sh'
        ;;
    *)
        echo '用法: ./manage.sh {logs|logs-all|logs-mysql|restart|restart-app|stop|start|status|health|backup|clean|update}'
        echo '  logs       - 查看应用日志'
        echo '  logs-all   - 查看所有服务日志'
        echo '  logs-mysql - 查看数据库日志'
        echo '  restart    - 重启所有服务'
        echo '  restart-app- 重启应用服务'
        echo '  stop       - 停止所有服务'
        echo '  start      - 启动所有服务'
        echo '  status     - 查看服务状态'
        echo '  health     - 健康检查'
        echo '  backup     - 数据备份'
        echo '  clean      - 清理系统资源'
        echo '  update     - 更新应用'
        exit 1
        ;;
esac
EOF

chmod +x ${REMOTE_DIR}/manage.sh
"
echo -e "${GREEN}✓ 管理脚本创建完成${NC}"
echo ""
echo -e "${YELLOW}服务器管理:${NC}"
echo -e "ssh -i ${SSH_KEY} ${SERVER} '${REMOTE_DIR}/manage.sh [command]'"
echo ""
echo -e "${GREEN}🎉 部署完成！请访问 http://101.126.46.254:8087 查看应用${NC}"