@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

:: 减灾能力评估工具Docker部署脚本 (Windows版本)
:: 目标服务器: root@101.126.46.254

echo === 减灾能力评估工具Docker部署脚本 ===
echo 目标服务器: 101.126.46.254
echo 部署目录: /opt/evaluation
echo.

:: 1. 检查本地文件
echo 1. 检查本地部署文件...
if not exist "docker-compose.yml" (
    echo 错误: docker-compose.yml 文件不存在
    pause
    exit /b 1
)

if not exist "Dockerfile" (
    echo 错误: Dockerfile 文件不存在
    pause
    exit /b 1
)

echo ✓ 本地文件检查完成

:: 2. 测试SSH连接
echo 2. 测试SSH连接...
echo 请在弹出的窗口中输入密码: Htht@12#$

:: 使用plink (PuTTY) 进行SSH连接
plink -batch -pw "Htht@12#$" root@101.126.46.254 "echo 'SSH连接成功'" >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: SSH连接失败
    echo 请确保:
    echo 1. PuTTY已安装 (需要plink.exe)
    echo 2. 服务器IP地址正确
    echo 3. SSH服务运行正常
    pause
    exit /b 1
)

echo ✓ SSH连接正常

:: 3. 在服务器上创建部署目录
echo 3. 在服务器上创建部署目录...
plink -batch -pw "Htht@12#$" root@101.126.46.254 "mkdir -p /opt/evaluation/{logs,uploads,backups}"
if %errorlevel% neq 0 (
    echo 错误: 创建目录失败
    pause
    exit /b 1
)
echo ✓ 部署目录创建完成

:: 4. 检查Docker环境
echo 4. 检查服务器Docker环境...
plink -batch -pw "Htht@12#$" root@101.126.46.254 "docker --version" >nul 2>&1
if %errorlevel% neq 0 (
    echo 警告: Docker未安装，正在安装...
    plink -batch -pw "Htht@12#$" root@101.126.46.254 "curl -fsSL https://get.docker.com | sh"
    plink -batch -pw "Htht@12#$" root@101.126.46.254 "systemctl start docker && systemctl enable docker"
) else (
    echo ✓ Docker已安装
)

plink -batch -pw "Htht@12#$" root@101.126.46.254 "docker-compose --version" >nul 2>&1
if %errorlevel% neq 0 (
    echo 警告: Docker Compose未安装，正在安装...
    plink -batch -pw "Htht@12#$" root@101.126.46.254 "curl -L 'https://github.com/docker/compose/releases/download/v2.20.2/docker-compose-$(uname -s)-$(uname -m)' -o /usr/local/bin/docker-compose && chmod +x /usr/local/bin/docker-compose"
) else (
    echo ✓ Docker Compose已安装
)

echo ✓ Docker环境检查完成

:: 5. 停止现有服务
echo 5. 停止现有服务...
plink -batch -pw "Htht@12#$" root@101.126.46.254 "cd /opt/evaluation && if [ -f 'docker-compose.yml' ]; then docker-compose down --remove-orphans || true; fi"
echo ✓ 现有服务已停止

:: 6. 上传文件
echo 6. 上传项目文件...
echo 正在上传文件，请稍候...

:: 使用pscp上传文件
pscp -batch -pw "Htht@12#$" -r -exclude ".git" -exclude "node_modules" -exclude "target" -exclude "logs" -exclude "uploads" -exclude "*.log" . root@101.126.46.254:/opt/evaluation/
if %errorlevel% neq 0 (
    echo 错误: 文件上传失败
    pause
    exit /b 1
)

echo ✓ 文件上传完成

:: 7. 构建和启动服务
echo 7. 构建和启动服务...
plink -batch -pw "Htht@12#$" root@101.126.46.254 "cd /opt/evaluation && docker-compose build --no-cache && docker-compose up -d"
if %errorlevel% neq 0 (
    echo 错误: 服务启动失败
    pause
    exit /b 1
)

echo ✓ 服务启动完成

:: 8. 等待服务启动
echo 8. 等待服务启动...
timeout /t 30 /nobreak >nul

:: 9. 检查服务状态
echo 9. 检查服务状态...
echo.
echo === Docker容器状态 ===
plink -batch -pw "Htht@12#$" root@101.126.46.254 "cd /opt/evaluation && docker-compose ps"

echo.
echo === 服务健康检查 ===
plink -batch -pw "Htht@12#$" root@101.126.46.254 "for i in {1..10}; do if curl -f http://localhost:8087/actuator/health >/dev/null 2>&1; then echo '应用服务正常 ✓'; break; else echo -n '.'; sleep 5; fi; if [ $i -eq 10 ]; then echo ''; echo '应用服务可能未正常启动，请检查日志'; fi; done"

:: 显示部署信息
echo.
echo === 部署完成 ===
echo 应用访问地址: http://101.126.46.254:8087
echo.
echo 常用命令:
echo 查看日志: plink -batch -pw "Htht@12#$" root@101.126.46.254 "cd /opt/evaluation && docker-compose logs -f"
echo 重启服务: plink -batch -pw "Htht@12#$" root@101.126.46.254 "cd /opt/evaluation && docker-compose restart"
echo 停止服务: plink -batch -pw "Htht@12#$" root@101.126.46.254 "cd /opt/evaluation && docker-compose down"
echo.
echo 部署成功！
pause