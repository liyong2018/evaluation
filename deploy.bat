@echo off
setlocal enabledelayedexpansion

set PASSWORD=Htht@12#$
set SERVER=172.16.43.189
set USER=root
set REMOTE_DIR=~/evaluation

echo Stopping Docker containers on remote server...
ssh -o StrictHostKeyChecking=no %USER%@%SERVER% "cd %REMOTE_DIR% && docker compose down" 2>&1

echo.
echo Copying JAR file...
scp -o StrictHostKeyChecking=no target/disaster-reduction-evaluation-1.0.0.jar %USER%@%SERVER%:%REMOTE_DIR%/target/ 2>&1

echo.
echo Copying frontend files...
scp -o StrictHostKeyChecking=no -r frontend/dist/* %USER%@%SERVER%:%REMOTE_DIR%/frontend/dist/ 2>&1

echo.
echo Rebuilding and starting Docker containers...
ssh -o StrictHostKeyChecking=no %USER%@%SERVER% "cd %REMOTE_DIR% && docker compose build evaluation-app && docker compose up -d" 2>&1

echo.
echo Deployment completed!
pause
