#!/bin/bash

# 减灾能力评估系统更新脚本

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 配置变量
SERVER="root@101.126.46.254"
REMOTE_DIR="/opt/evaluation"

echo -e "${GREEN}=== 减灾能力评估系统更新脚本 ===${NC}"
echo -e "${YELLOW}目标服务器: ${SERVER}${NC}"
echo -e "${YELLOW}部署目录: ${REMOTE_DIR}${NC}"
echo ""

# 备份数据
echo -e "${GREEN}1. 备份当前数据...${NC}"
BACKUP_DIR="${REMOTE_DIR}/backups/$(date +%Y%m%d_%H%M%S)"
ssh ${SERVER} "
mkdir -p ${BACKUP_DIR}
cd ${REMOTE_DIR}

# 备份数据库
if docker-compose exec mysql mysqldump -uroot -p123456 evaluate_db > ${BACKUP_DIR}/database_$(date +%Y%m%d_%H%M%S).sql; then
    echo '数据库备份完成 ✓'
else
    echo '数据库备份失败，继续更新...'
fi

# 备份上传文件
if [ -d 'uploads' ]; then
    cp -r uploads ${BACKUP_DIR}/
    echo '上传文件备份完成 ✓'
fi
"
echo -e "${GREEN}✓ 数据备份完成${NC}"

# 上传更新文件
echo -e "${GREEN}2. 上传更新文件...${NC}"
rsync -avz --delete \
    --exclude='.git' \
    --exclude='node_modules' \
    --exclude='target' \
    --exclude='logs/*' \
    --exclude='uploads/*' \
    --exclude='*.log' \
    --exclude='mysql_data' \
    --exclude='redis_data' \
    ./ ${SERVER}:${REMOTE_DIR}/
echo -e "${GREEN}✓ 文件上传完成${NC}"

# 重新构建和启动服务
echo -e "${GREEN}3. 重新构建和启动服务...${NC}"
ssh ${SERVER} "
cd ${REMOTE_DIR} && \
docker-compose down && \
docker-compose build --no-cache && \
docker-compose up -d
"
echo -e "${GREEN}✓ 服务更新完成${NC}"

# 等待服务启动
echo -e "${GREEN}4. 等待服务启动...${NC}"
sleep 20

# 健康检查
echo -e "${GREEN}5. 健康检查...${NC}"
ssh ${SERVER} "
cd ${REMOTE_DIR} && \
echo '=== 检查容器状态 ===' && \
docker-compose ps && \
echo '' && \
echo '=== 检查应用健康状态 ===' && \
for i in {1..12}; do
    if curl -f http://localhost:8087/actuator/health >/dev/null 2>&1; then
        echo '应用服务正常 ✓'
        exit 0
    else
        echo -n '.'
        sleep 5
    fi
    if [ \$i -eq 12 ]; then
        echo ''
        echo '应用服务可能未正常启动'
        echo '请查看日志: docker-compose logs app'
        exit 1
    fi
done
"

echo ""
echo -e "${GREEN}=== 更新完成 ===${NC}"
echo -e "${YELLOW}应用访问地址: http://101.126.46.254:8087${NC}"
echo ""
echo -e "${GREEN}如遇问题，可使用备份恢复:${NC}"
echo -e "恢复数据库: ssh ${SERVER} 'mysql -uroot -p123456 evaluate_db < ${BACKUP_DIR}/database_*.sql'"
echo -e "查看日志: ssh ${SERVER} 'cd ${REMOTE_DIR} && docker-compose logs -f'"