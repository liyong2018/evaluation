# Deployment Script for Remote Server
# This script requires manual password entry for each step

$SERVER = "172.16.43.189"
$USER = "root"
$REMOTE_DIR = "~/evaluation"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Deploy to Remote Server" -ForegroundColor Cyan
Write-Host "   Server: $SERVER" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Define deployment steps
$steps = @(
    @{Name="Stop Docker containers";       Command="cd $REMOTE_DIR && docker compose down"},
    @{Name="Upload JAR file";               Command="scp target/disaster-reduction-evaluation-1.0.0.jar $USER@$SERVER`:$REMOTE_DIR/target/"; IsSCP=$true; LocalPath="target/disaster-reduction-evaluation-1.0.0.jar"},
    @{Name="Clear remote frontend dist";    Command="ssh $USER@$SERVER `"rm -rf $REMOTE_DIR/frontend/dist && mkdir -p $REMOTE_DIR/frontend/dist`""},
    @{Name="Upload frontend files";         Command="scp -r frontend/dist/* $USER@$SERVER`:$REMOTE_DIR/frontend/dist/"; IsSCP=$true; LocalPath="frontend/dist/*"},
    @{Name="Rebuild Docker image";          Command="ssh $USER@$SERVER `"cd $REMOTE_DIR && docker compose build evaluation-app`""},
    @{Name="Start Docker containers";       Command="ssh $USER@$SERVER `"cd $REMOTE_DIR && docker compose up -d`""}
)

# Execute each step
for ($i = 0; $i -lt $steps.Count; $i++) {
    $step = $steps[$i]
    Write-Host "Step $($i+1): $($step.Name)" -ForegroundColor Yellow
    Write-Host "Executing: $($step.Command)" -ForegroundColor Gray

    try {
        Invoke-Expression $step.Command
        Write-Host "✓ Step $($i+1) completed" -ForegroundColor Green
    } catch {
        Write-Host "✗ Step $($i+1) failed: $_" -ForegroundColor Red
    }

    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Deployment completed!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Verify deployment
Write-Host "Verifying deployment..." -ForegroundColor Yellow
$checkCommand = "ssh $USER@$SERVER `"cd $REMOTE_DIR && docker compose ps`""
Invoke-Expression $checkCommand
