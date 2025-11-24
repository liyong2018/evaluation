#!/bin/bash

# 减灾能力评估系统备份脚本

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 配置变量
SERVER="root@101.126.46.254"
REMOTE_DIR="/opt/evaluation"
BACKUP_DIR="${REMOTE_DIR}/backups/$(date +%Y%m%d_%H%M%S)"
LOCAL_BACKUP_DIR="./backups/$(date +%Y%m%d_%H%M%S)"

echo -e "${GREEN}=== 减灾能力评估系统备份脚本 ===${NC}"
echo -e "${YELLOW}目标服务器: ${SERVER}${NC}"
echo -e "${YELLOW}备份目录: ${BACKUP_DIR}${NC}"
echo ""

# 创建本地备份目录
mkdir -p ${LOCAL_BACKUP_DIR}

# 在服务器上创建备份目录
echo -e "${GREEN}1. 创建备份目录...${NC}"
ssh ${SERVER} "mkdir -p ${BACKUP_DIR}"
echo -e "${GREEN}✓ 备份目录创建完成${NC}"

# 备份数据库
echo -e "${GREEN}2. 备份数据库...${NC}"
ssh ${SERVER} "
cd ${REMOTE_DIR} && \
docker-compose exec -T mysql mysqldump -uroot -p123456 evaluate_db | gzip > ${BACKUP_DIR}/database.sql.gz
"
echo -e "${GREEN}✓ 数据库备份完成${NC}"

# 备份Docker数据卷
echo -e "${GREEN}3. 备份Docker数据卷...${NC}"
ssh ${SERVER} "
# 备份MySQL数据
docker run --rm -v ${REMOTE_DIR}/mysql_data:/data -v ${BACKUP_DIR}:/backup alpine tar czf /backup/mysql_data.tar.gz -C /data .

# 备份Redis数据
docker run --rm -v ${REMOTE_DIR}/redis_data:/data -v ${BACKUP_DIR}:/backup alpine tar czf /backup/redis_data.tar.gz -C /data .

echo 'Docker数据卷备份完成'
"
echo -e "${GREEN}✓ Docker数据卷备份完成${NC}"

# 备份上传文件
echo -e "${GREEN}4. 备份上传文件...${NC}"
ssh ${SERVER} "
if [ -d '${REMOTE_DIR}/uploads' ]; then
    cp -r ${REMOTE_DIR}/uploads ${BACKUP_DIR}/
    echo '上传文件备份完成'
else
    echo '无上传文件需要备份'
fi
"

# 备份配置文件
echo -e "${GREEN}5. 备份配置文件...${NC}"
ssh ${SERVER} "
cp ${REMOTE_DIR}/docker-compose.yml ${BACKUP_DIR}/
cp ${REMOTE_DIR}/.env ${BACKUP_DIR}/ 2>/dev/null || true
echo '配置文件备份完成'
"
echo -e "${GREEN}✓ 配置文件备份完成${NC}"

# 备份应用日志（最近7天）
echo -e "${GREEN}6. 备份应用日志...${NC}"
ssh ${SERVER} "
if [ -d '${REMOTE_DIR}/logs' ]; then
    find ${REMOTE_DIR}/logs -name '*.log' -mtime -7 -exec cp {} ${BACKUP_DIR}/ \;
    echo '应用日志备份完成'
else
    echo '无应用日志需要备份'
fi
"
echo -e "${GREEN}✓ 应用日志备份完成${NC}"

# 生成备份清单
echo -e "${GREEN}7. 生成备份清单...${NC}"
ssh ${SERVER} "
cd ${BACKUP_DIR} && \
ls -la > backup_manifest.txt && \
echo '备份生成时间: $(date)' >> backup_manifest.txt && \
echo '服务器信息: $(uname -a)' >> backup_manifest.txt && \
echo 'Docker版本: $(docker --version)' >> backup_manifest.txt && \
echo 'Docker Compose版本: $(docker-compose --version)' >> backup_manifest.txt
"
echo -e "${GREEN}✓ 备份清单生成完成${NC}"

# 下载备份到本地
echo -e "${GREEN}8. 下载备份到本地...${NC}"
scp -r ${SERVER}:${BACKUP_DIR}/* ${LOCAL_BACKUP_DIR}/
echo -e "${GREEN}✓ 备份下载完成${NC}"

# 清理旧备份（保留最近7天）
echo -e "${GREEN}9. 清理旧备份...${NC}"
ssh ${SERVER} "
find ${REMOTE_DIR}/backups -type d -mtime +7 -exec rm -rf {} + 2>/dev/null || true
echo '旧备份清理完成'
"
echo -e "${GREEN}✓ 旧备份清理完成${NC}"

# 显示备份信息
echo ""
echo -e "${GREEN}=== 备份完成 ===${NC}"
echo -e "${YELLOW}远程备份目录: ${BACKUP_DIR}${NC}"
echo -e "${YELLOW}本地备份目录: ${LOCAL_BACKUP_DIR}${NC}"
echo ""
echo -e "${GREEN}备份内容:${NC}"
ls -la ${LOCAL_BACKUP_DIR}/
echo ""
echo -e "${GREEN}恢复命令:${NC}"
echo -e "1. 恢复数据库: gunzip -c database.sql.gz | mysql -uroot -p123456 evaluate_db"
echo -e "2. 恢复Docker数据: tar xzf mysql_data.tar.gz -C /var/lib/mysql/"
echo -e "3. 恢复上传文件: cp -r uploads/ ${REMOTE_DIR}/"
echo ""
echo -e "${GREEN}备份成功！${NC}"