# Deployment Script for 172.16.41.139
# Server: htht@172.16.41.139
# Password: 1qaz@1234

$ErrorActionPreference = "Stop"

# Server configuration
$SERVER = "172.16.41.139"
$USER = "htht"
$PASSWORD = "1qaz@1234"
$REMOTE_DIR = "~/evaluation"

# Create secure password
$SECURE_PASSWORD = ConvertTo-SecureString $PASSWORD -AsPlainText -Force
$CREDENTIAL = New-Object System.Management.Automation.PSCredential ($USER, $SECURE_PASSWORD)

Write-Host "========================================" -ForegroundColor Green
Write-Host "  Deploying to Remote Server" -ForegroundColor Green
Write-Host "  Server: $SERVER" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# Function to execute SSH command
function Invoke-SSHCommand {
    param([string]$Command)

    $sshCommand = "echo `"$COMMAND`" | ssh -o StrictHostKeyChecking=no $USER@$SERVER bash"
    Write-Host "Executing: $Command" -ForegroundColor Cyan

    # Using plink-like approach with password
    $result = cmd /c "echo $COMMAND | ssh -o StrictHostKeyChecking=no $USER@$SERVER bash 2>&1"
    return $result
}

# Check local files
Write-Host "Step 1: Checking local files..." -ForegroundColor Green
if (-not (Test-Path "target\disaster-reduction-evaluation-1.0.0.jar")) {
    Write-Host "ERROR: JAR file not found!" -ForegroundColor Red
    exit 1
}
if (-not (Test-Path "frontend\dist")) {
    Write-Host "ERROR: Frontend dist not found!" -ForegroundColor Red
    exit 1
}
Write-Host "Local files OK" -ForegroundColor Green
Write-Host ""

# Using Windows built-in tools with plink or manual approach
# Since we need password auth, let's use a different approach

Write-Host "Note: Please run the following commands manually or use plink/putty" -ForegroundColor Yellow
Write-Host ""
Write-Host "Alternative: Using batch file with manual password prompt" -ForegroundColor Yellow

# Let's create a simpler batch file approach
$batchContent = @"
@echo off
setlocal enabledelayedexpansion

echo ========================================
echo   Deploying to Remote Server
echo   Server: 172.16.41.139
echo ========================================
echo.

set SERVER=172.16.41.139
set USER=htht
set REMOTE_DIR=~/evaluation

echo Step 1: Stopping Docker containers...
ssh -o StrictHostKeyChecking=no %USER%@%SERVER% "cd %REMOTE_DIR% && docker compose down"

echo.
echo Step 2: Uploading JAR file...
ssh -o StrictHostKeyChecking=no %USER%@%SERVER% "mkdir -p %REMOTE_DIR%/target"
scp -o StrictHostKeyChecking=no target\disaster-reduction-evaluation-1.0.0.jar %USER%@%SERVER%:%REMOTE_DIR%/target/

echo.
echo Step 3: Clearing and preparing frontend directory...
ssh -o StrictHostKeyChecking=no %USER%@%SERVER% "rm -rf %REMOTE_DIR%/frontend && mkdir -p %REMOTE_DIR%/frontend/dist"

echo.
echo Step 4: Uploading frontend files...
scp -o StrictHostKeyChecking=no -r frontend\dist\* %USER%@%SERVER%:%REMOTE_DIR%/frontend/dist/
scp -o StrictHostKeyChecking=no frontend\Dockerfile %USER%@%SERVER%:%REMOTE_DIR%/frontend/
scp -o StrictHostKeyChecking=no frontend\nginx.conf %USER%@%SERVER%:%REMOTE_DIR%/frontend/
scp -o StrictHostKeyChecking=no docker-compose.prod.yml %USER%@%SERVER%:%REMOTE_DIR%/
scp -o StrictHostKeyChecking=no Dockerfile.prod %USER%@%SERVER%:%REMOTE_DIR%/

echo.
echo Step 5: Rebuilding and starting Docker containers...
ssh -o StrictHostKeyChecking=no %USER%@%SERVER% "cd %REMOTE_DIR% && docker compose -f docker-compose.prod.yml up -d --build"

echo.
echo Step 6: Checking deployment status...
ssh -o StrictHostKeyChecking=no %USER%@%SERVER% "cd %REMOTE_DIR% && docker compose ps"

echo.
echo ========================================
echo   Deployment completed
echo ========================================
echo.

pause
"@

$batchPath = "d:\Evaluation\evaluation\deploy-remote.bat"
$batchContent | Out-File -FilePath $batchPath -Encoding ASCII
Write-Host "Created deployment batch file: $batchPath" -ForegroundColor Green
