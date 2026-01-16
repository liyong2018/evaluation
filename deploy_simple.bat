@echo off
setlocal enabledelayedexpansion

echo ========================================
echo Deploy to Remote Server
echo Server: 172.16.43.189
echo ========================================
echo.

echo Please enter the SSH password when prompted.
echo.

set SERVER=172.16.43.189
set USER=root
set REMOTE_DIR=~/evaluation

echo Step 1: Stopping Docker containers...
ssh -o StrictHostKeyChecking=no %USER%@%SERVER% "cd %REMOTE_DIR% && docker compose down"

echo.
echo Step 2: Uploading JAR file...
scp -o StrictHostKeyChecking=no target/disaster-reduction-evaluation-1.0.0.jar %USER%@%SERVER%:%REMOTE_DIR%/target/

echo.
echo Step 3: Uploading frontend files...
echo Clearing remote dist directory...
ssh -o StrictHostKeyChecking=no %USER%@%SERVER% "rm -rf %REMOTE_DIR%/frontend/dist && mkdir -p %REMOTE_DIR%/frontend/dist"

echo Uploading files...
scp -o StrictHostKeyChecking=no -r frontend/dist/* %USER%@%SERVER%:%REMOTE_DIR%/frontend/dist/

echo.
echo Step 4: Rebuilding Docker image...
ssh -o StrictHostKeyChecking=no %USER%@%SERVER% "cd %REMOTE_DIR% && docker compose build evaluation-app"

echo.
echo Step 5: Starting Docker containers...
ssh -o StrictHostKeyChecking=no %USER%@%SERVER% "cd %REMOTE_DIR% && docker compose up -d"

echo.
echo ========================================
echo Deployment completed!
echo ========================================
echo.

pause
