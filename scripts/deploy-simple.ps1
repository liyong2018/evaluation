# Simple deployment script for 172.16.41.139
# Run this in PowerShell with: .\scripts\deploy-simple.ps1

$SERVER = "172.16.41.139"
$USER = "htht"
$REMOTE_DIR = "~/evaluation"
$BASE_DIR = "d:\Evaluation\evaluation"

Write-Host "========================================" -ForegroundColor Green
Write-Host "  Deploying to $SERVER" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# Create SSH options for connection multiplexing
$SSH_CONTROL = "C:\Users\admin\.ssh\control-172.16.41.139"

# Function to run SSH command
function Invoke-SSH {
    param([string]$Command)
    ssh -o ControlMaster=auto -o ControlPath="$SSH_CONTROL" -o ControlPersist=10m -o StrictHostKeyChecking=no "$USER@$SERVER" $Command
}

# Function to run SCP
function Invoke-SCP {
    param([string]$Source, [string]$Dest)
    scp -o ControlMaster=auto -o ControlPath="$SSH_CONTROL" -o ControlPersist=10m -o StrictHostKeyChecking=no -r $Source "${USER}@${SERVER}:${Dest}"
}

Write-Host "Please enter your SSH password when prompted." -ForegroundColor Yellow
Write-Host "You will only need to enter it once (connection will be reused)." -ForegroundColor Yellow
Write-Host ""

# Test connection first
Write-Host "Testing SSH connection..." -ForegroundColor Cyan
Invoke-SSH "echo 'Connected successfully!'"
if ($LASTEXITCODE -ne 0) {
    Write-Host "SSH connection failed!" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Step 1: Stop containers
Write-Host "Step 1: Stopping Docker containers..." -ForegroundColor Green
Invoke-SSH "cd $REMOTE_DIR && docker compose down 2>/dev/null || true"
Write-Host "Done." -ForegroundColor Gray
Write-Host ""

# Step 2: Create directories
Write-Host "Step 2: Creating directories..." -ForegroundColor Green
Invoke-SSH "mkdir -p $REMOTE_DIR/target"
Invoke-SSH "mkdir -p $REMOTE_DIR/frontend/dist"
Write-Host "Done." -ForegroundColor Gray
Write-Host ""

# Step 3: Upload JAR
Write-Host "Step 3: Uploading JAR file (~100MB)..." -ForegroundColor Green
Invoke-SCP "$BASE_DIR\target\disaster-reduction-evaluation-1.0.0.jar" "$REMOTE_DIR/target/"
Write-Host "Done." -ForegroundColor Gray
Write-Host ""

# Step 4: Upload frontend files
Write-Host "Step 4: Uploading frontend files..." -ForegroundColor Green
Invoke-SSH "rm -rf $REMOTE_DIR/frontend/dist/*"
Invoke-SCP "$BASE_DIR\frontend\dist\*" "$REMOTE_DIR/frontend/dist/"
Invoke-SCP "$BASE_DIR\frontend\Dockerfile" "$REMOTE_DIR/frontend/"
Invoke-SCP "$BASE_DIR\frontend\nginx.conf" "$REMOTE_DIR/frontend/"
Write-Host "Done." -ForegroundColor Gray
Write-Host ""

# Step 5: Upload docker files
Write-Host "Step 5: Uploading docker configuration..." -ForegroundColor Green
Invoke-SCP "$BASE_DIR\docker-compose.prod.yml" "$REMOTE_DIR/"
Invoke-SCP "$BASE_DIR\Dockerfile.prod" "$REMOTE_DIR/"
Write-Host "Done." -ForegroundColor Gray
Write-Host ""

# Step 6: Build and start containers
Write-Host "Step 6: Building and starting containers..." -ForegroundColor Green
Write-Host "This may take several minutes..." -ForegroundColor Yellow
Invoke-SSH "cd $REMOTE_DIR && docker compose -f docker-compose.prod.yml up -d --build"
Write-Host "Done." -ForegroundColor Gray
Write-Host ""

# Step 7: Check status
Write-Host "Step 7: Checking deployment status..." -ForegroundColor Green
Write-Host ""
Invoke-SSH "cd $REMOTE_DIR && docker compose ps"
Write-Host ""

Write-Host "========================================" -ForegroundColor Green
Write-Host "  Deployment completed!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Frontend URL: http://${SERVER}:8080" -ForegroundColor Cyan
Write-Host "Backend URL:  http://${SERVER}:8081" -ForegroundColor Cyan
Write-Host ""

# Close control connection
ssh -o ControlPath="$SSH_CONTROL" -O exit "$USER@$SERVER" 2>$null
