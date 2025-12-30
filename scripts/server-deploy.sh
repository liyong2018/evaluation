#!/bin/bash

# 服务器端部署脚本
# 在服务器上执行的部署脚本
# 使用方法：将文件上传到服务器后执行此脚本

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 配置变量
DEPLOY_DIR="/opt/evaluation"

echo -e "${GREEN}=== 服务器端部署脚本 ===${NC}"
echo -e "${YELLOW}部署目录: ${DEPLOY_DIR}${NC}"
echo ""

# 检查是否在正确的目录
if [ "$(pwd)" != "${DEPLOY_DIR}" ]; then
    echo -e "${RED}错误: 请在 ${DEPLOY_DIR} 目录下执行此脚本${NC}"
    echo -e "执行: cd ${DEPLOY_DIR}"
    exit 1
fi

# 检查必要文件
echo -e "${GREEN}1. 检查部署文件...${NC}"
if [ ! -f "docker-compose.yml" ]; then
    echo -e "${RED}错误: docker-compose.yml 文件不存在${NC}"
    exit 1
fi

if [ ! -f "Dockerfile" ]; then
    echo -e "${RED}错误: Dockerfile 文件不存在${NC}"
    exit 1
fi

echo -e "${GREEN}✓ 文件检查完成${NC}"

# 检查Docker环境
echo -e "${GREEN}2. 检查Docker环境...${NC}"
if ! command -v docker &> /dev/null; then
    echo -e "${RED}错误: Docker未安装${NC}"
    echo "请先安装Docker: curl -fsSL https://get.docker.com | sh"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}错误: Docker Compose未安装${NC}"
    echo "请先安装Docker Compose"
    exit 1
fi

echo -e "${GREEN}✓ Docker环境检查完成${NC}"

# 停止现有服务
echo -e "${GREEN}3. 停止现有服务...${NC}"
if [ -f "docker-compose.yml" ]; then
    docker-compose down --remove-orphans || true
    echo -e "${GREEN}✓ 现有服务已停止${NC}"
fi

# 清理旧镜像（可选）
echo -e "${GREEN}4. 清理旧镜像...${NC}"
docker system prune -f
echo -e "${GREEN}✓ 镜像清理完成${NC}"

# 创建必要的目录
echo -e "${GREEN}5. 创建数据目录...${NC}"
mkdir -p logs uploads backups mysql_data redis_data
echo -e "${GREEN}✓ 目录创建完成${NC}"

# 构建镜像
echo -e "${GREEN}6. 构建Docker镜像...${NC}"
docker-compose build --no-cache
echo -e "${GREEN}✓ 镜像构建完成${NC}"

# 启动服务
echo -e "${GREEN}7. 启动服务...${NC}"
docker-compose up -d
echo -e "${GREEN}✓ 服务启动完成${NC}"

# 等待服务启动
echo -e "${GREEN}8. 等待服务启动...${NC}"
sleep 30

# 检查服务状态
echo -e "${GREEN}9. 检查服务状态...${NC}"
echo "=== Docker容器状态 ==="
docker-compose ps

echo ""
echo "=== 服务健康检查 ==="

# 检查应用服务
for i in {1..12}; do
    if curl -f http://localhost:8087/actuator/health >/dev/null 2>&1; then
        echo -e "${GREEN}应用服务正常 ✓${NC}"
        break
    else
        echo -n "."
        sleep 5
    fi
    if [ $i -eq 12 ]; then
        echo ""
        echo -e "${YELLOW}应用服务可能未完全启动，请查看日志${NC}"
        echo "查看日志命令: docker-compose logs -f app"
    fi
done

# 检查数据库连接
echo ""
echo "=== 数据库连接检查 ==="
if docker-compose exec mysql mysqladmin ping -h localhost -uroot -p123456 >/dev/null 2>&1; then
    echo -e "${GREEN}数据库连接正常 ✓${NC}"
else
    echo -e "${YELLOW}数据库连接可能有问题${NC}"
fi

# 显示服务信息
echo ""
echo -e "${GREEN}=== 部署完成 ===${NC}"
echo -e "${YELLOW}服务访问地址:${NC}"
echo -e "应用服务: http://$(curl -s ifconfig.me):8087"
echo -e "本地访问: http://localhost:8087"
echo -e "数据库: $(curl -s ifconfig.me):3306"
echo -e "Redis: $(curl -s ifconfig.me):6379"
echo ""
echo -e "${GREEN}常用命令:${NC}"
echo -e "查看日志: docker-compose logs -f"
echo -e "重启服务: docker-compose restart"
echo -e "停止服务: docker-compose down"
echo -e "查看状态: docker-compose ps"
echo ""
echo -e "${GREEN}部署成功！${NC}"

# 显示日志位置
echo -e "${YELLOW}日志文件位置:${NC}"
echo -e "- Docker日志: docker-compose logs [service]"
echo -e "- 应用日志: ./logs/"
echo -e "- 工具日志: /var/log/"
echo ""

echo -e "${GREEN}如需帮助，请查看 DEPLOY.md 文档${NC}"