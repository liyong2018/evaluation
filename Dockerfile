# 多阶段构建 - 后端构建阶段
FROM maven:3.8-openjdk-11 AS backend-builder

WORKDIR /app

# 复制pom.xml并下载依赖（利用Docker缓存层）
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# 复制源代码并构建
COPY src ./src
RUN mvn clean package -DskipTests || mvn clean package -DskipTests -Dmaven.test.skip=true

# 前端构建阶段
FROM node:18-alpine AS frontend-builder

WORKDIR /app/frontend

# 复制package.json并安装依赖
COPY frontend/package.json frontend/package-lock.json* ./
RUN npm ci --only=production || npm install --production

# 复制前端源代码并构建
COPY frontend/ .
RUN npm run build || npm run build:only

# 运行时镜像
FROM openjdk:11-jre-slim

# 安装必要工具
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# 创建应用用户
RUN groupadd -r appuser && useradd -r -g appuser appuser

WORKDIR /app

# 复制后端JAR文件
COPY --from=backend-builder /app/target/*.jar app.jar

# 复制前端构建产物
COPY --from=frontend-builder /app/frontend/dist ./static

# 创建日志目录
RUN mkdir -p /app/logs && chown -R appuser:appuser /app

# 切换到非root用户
USER appuser

# 暴露端口
EXPOSE 8087

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8087/actuator/health || exit 1

# 启动命令
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]