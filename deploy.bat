@echo off
setlocal enabledelayedexpansion

echo ========================================
echo   Deploying to Remote Server
echo   Server: 172.16.43.189
echo ========================================
echo.

set SERVER=172.16.43.189
set USER=htht
set REMOTE_DIR=~/evaluation

if not exist target\disaster-reduction-evaluation-1.0.0.jar (
  echo ERROR: target\disaster-reduction-evaluation-1.0.0.jar not found. Please build backend first.
  exit /b 1
)

if not exist frontend\package.json (
  echo ERROR: frontend\package.json not found.
  exit /b 1
)

if not exist frontend\dist (
  echo ERROR: frontend\dist not found. Please build frontend first.
  exit /b 1
)

echo Step 1: Stopping Docker containers...
ssh -o StrictHostKeyChecking=no %USER%@%SERVER% "cd %REMOTE_DIR% && docker compose down"

echo.
echo Step 2: Uploading JAR file...
ssh -o StrictHostKeyChecking=no %USER%@%SERVER% "mkdir -p %REMOTE_DIR%/target"
scp -o StrictHostKeyChecking=no target/disaster-reduction-evaluation-1.0.0.jar %USER%@%SERVER%:%REMOTE_DIR%/target/

echo.
echo Step 3: Clearing and preparing frontend directory...
ssh -o StrictHostKeyChecking=no %USER%@%SERVER% "rm -rf %REMOTE_DIR%/frontend && mkdir -p %REMOTE_DIR%/frontend/dist"

echo.
echo Step 4: Uploading frontend files...
scp -o StrictHostKeyChecking=no -r frontend/dist/* %USER%@%SERVER%:%REMOTE_DIR%/frontend/dist/
scp -o StrictHostKeyChecking=no frontend/Dockerfile %USER%@%SERVER%:%REMOTE_DIR%/frontend/
scp -o StrictHostKeyChecking=no frontend/nginx.conf %USER%@%SERVER%:%REMOTE_DIR%/frontend/
echo Verifying remote frontend Dockerfile...
ssh -o StrictHostKeyChecking=no %USER%@%SERVER% "sed -n '1,5p' %REMOTE_DIR%/frontend/Dockerfile"

echo.
echo Step 5: Rebuilding Docker image...
ssh -o StrictHostKeyChecking=no %USER%@%SERVER% "cd %REMOTE_DIR% && docker compose build --no-cache frontend"
ssh -o StrictHostKeyChecking=no %USER%@%SERVER% "cd %REMOTE_DIR% && docker compose build backend"

echo.
echo Step 6: Starting Docker containers...
ssh -o StrictHostKeyChecking=no %USER%@%SERVER% "cd %REMOTE_DIR% && docker compose up -d"

echo.
echo Step 7: Checking deployment status...
ssh -o StrictHostKeyChecking=no %USER%@%SERVER% "cd %REMOTE_DIR% && docker compose ps"

echo.
echo ========================================
echo   Deployment completed
echo ========================================
echo.

pause
