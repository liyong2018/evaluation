@echo off
setlocal enabledelayedexpansion

echo ========================================
echo   灾害评估系统部署脚本
echo   服务器: 172.16.43.189
echo ========================================
echo.
echo 此脚本将分步引导您完成部署
echo 每步都会提示输入密码
echo.

pause

echo.
echo [步骤 1/5] 停止 Docker 容器
echo ----------------------------------------
ssh -o StrictHostKeyChecking=no root@172.16.43.189 "cd ~/evaluation && docker compose down"
echo.
pause

echo.
echo [步骤 2/5] 上传后端 JAR 文件
echo ----------------------------------------
scp -o StrictHostKeyChecking=no target/disaster-reduction-evaluation-1.0.0.jar root@172.16.43.189:~/evaluation/target/
echo.
pause

echo.
echo [步骤 3/5] 清理并准备前端目录
echo ----------------------------------------
ssh -o StrictHostKeyChecking=no root@172.16.43.189 "rm -rf ~/evaluation/frontend/dist && mkdir -p ~/evaluation/frontend/dist"
echo.
pause

echo.
echo [步骤 4/5] 上传前端文件
echo ----------------------------------------
scp -o StrictHostKeyChecking=no -r frontend/dist/* root@172.16.43.189:~/evaluation/frontend/dist/
echo.
pause

echo.
echo [步骤 5/5] 重新构建并启动 Docker 容器
echo ----------------------------------------
ssh -o StrictHostKeyChecking=no root@172.16.43.189 "cd ~/evaluation && docker compose build evaluation-app"
echo.
ssh -o StrictHostKeyChecking=no root@172.16.43.189 "cd ~/evaluation && docker compose up -d"
echo.

echo ========================================
echo   部署完成！
echo ========================================
echo.
echo 您可以通过以下命令检查部署状态：
echo ssh root@172.16.43.189 "cd ~/evaluation && docker compose ps"
echo.

pause
