# PowerShell Deployment Script
$ErrorActionPreference = "Stop"

$SERVER = "172.16.43.189"
$USER = "htht"
$PASSWORD = "1qaz@1234"
$REMOTE_DIR = "/home/htht/evaluation"
$LOCAL_DIR = "D:\Evaluation\evaluation"

Write-Host "========================================" -ForegroundColor Green
Write-Host "Deploying Evaluation System" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

# Install SSH.NET module if needed
if (-not (Get-Module -ListAvailable -Name SSH)) {
    try {
        Install-Package SSH.NET -Scope CurrentUser -Force -SkipDependencies
    } catch {
        Write-Host "Installing SSH.NET via NuGet..." -ForegroundColor Yellow
        # Try alternate installation method
    }
}

Write-Host "Using plink/pscp method..."
Write-Host "Note: First connection may require password acceptance"

# Create a temporary expect script for automated password input
$expectScript = @"
#!/usr/bin/expect -f
set timeout 60
set password "1qaz@1234"
spawn ssh htht@172.16.43.189 [lindex \$argv 0]
expect {
    "password:" {
        send "\$password\r"
        exp_continue
    }
    "yes/no" {
        send "yes\r"
        exp_continue
    }
}
"@

# For Windows, use native SCP with manual password entry first time
Write-Host "========================================" -ForegroundColor Yellow
Write-Host "Please run these commands manually:" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow
Write-Host "1. Accept SSH host key first:"
Write-Host "   ssh htht@172.16.43.189 'echo OK'"
Write-Host ""
Write-Host "2. Or use PowerShell with SSH credentials:"
Write-Host ""

$scriptContent = @"
#`# Save as deploy.ps1 and run:
`$password = ConvertTo-SecureString '1qaz@1234' -AsPlainText -Force
`$credential = New-Object System.Management.Automation.PSCredential ('htht', `$password)

`# Copy files using scp (will ask for password once)
scp docker-compose.yml htht@172.16.43.189:$REMOTE_DIR/
scp Dockerfile htht@172.16.43.189:$REMOTE_DIR/
scp -r nginx/conf.d htht@172.16.43.189:$REMOTE_DIR/nginx/
scp -r frontend/dist htht@172.16.43.189:$REMOTE_DIR/frontend/

`# Then SSH to start services
ssh htht@172.16.43.189 "cd $REMOTE_DIR && docker compose down; docker compose up -d"
"@

Write-Host $scriptContent
Write-Host ""
Write-Host "Or run this PowerShell script for automated deployment:"
Write-Host ""

# Create automated script
$autoScript = @'
# Automated Deployment Script
$SERVER = "172.16.43.189"
$USER = "htht"
$PASSWORD = "1qaz@1234"
$REMOTE_DIR = "/home/htht/evaluation"

# Use sshpass if available (Git Bash/WSL)
$command = @"
sshpass -p "1qaz@1234" ssh -o StrictHostKeyChecking=no htht@172.16.43.189 "mkdir -p $REMOTE_DIR/frontend/dist && mkdir -p $REMOTE_DIR/nginx/conf.d && echo 'Directories created'"
"@
Write-Host "Run via Git Bash or WSL:"
Write-Host $command
'@

Write-Host $autoScript
