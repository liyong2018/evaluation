# ============================================================
# 减灾能力评估系统 - PowerShell部署脚本集合
# Disaster Reduction Evaluation System - PowerShell Deployment Scripts
# ============================================================
#
# 使用方法:
#   .\scripts\deploy.ps1              # 快速部署（本地构建+上传）
#   .\scripts\deploy.ps1 setup-ssh    # 设置SSH密钥认证
#   .\scripts\deploy.ps1 help         # 显示帮助信息
#
# 环境变量:
#   $RemoteUser    远程用户 (默认: htht)
#   $RemoteHost    远程主机 (默认: 172.16.43.189)
#   $BackendContainer 后端容器名 (默认: evaluation-backend)
#   $FrontendContainer 前端容器名 (默认: evaluation-frontend)
#
# 示例:
#   $RemoteUser="admin"; $RemoteHost="192.168.1.100"; .\scripts\deploy.ps1
#
# ============================================================

$ErrorActionPreference = "Stop"

# ============================================================
# 配置区域
# ============================================================
$RemoteUser = $env:RemoteUser ?? "htht"
$RemoteHost = $env:RemoteHost ?? "172.16.43.189"
$BackendContainer = $env:BackendContainer ?? "evaluation-backend"
$FrontendContainer = $env:FrontendContainer ?? "evaluation-frontend"

# 本地路径
$ProjectRoot = $PSScriptRoot | Split-Path -Parent
$BackendJarPath = "$ProjectRoot\target\disaster-reduction-evaluation-1.0.0.jar"
$FrontendDistPath = "$ProjectRoot\frontend\dist"

# ============================================================
# 函数: 显示帮助信息
# ============================================================
function Show-Help {
    Write-Host ""
    Write-Host "=== 减灾能力评估系统 - PowerShell部署脚本 ===" -ForegroundColor Green
    Write-Host ""
    Write-Host "使用方法:" -ForegroundColor Yellow
    Write-Host "  .\scripts\deploy.ps1              快速部署（本地构建+上传）"
    Write-Host "  .\scripts\deploy.ps1 setup-ssh    设置SSH密钥认证"
    Write-Host "  .\scripts\deploy.ps1 help         显示此帮助信息"
    Write-Host ""
    Write-Host "环境变量:" -ForegroundColor Yellow
    Write-Host "  `$env:RemoteUser     远程用户 (默认: htht)"
    Write-Host "  `$env:RemoteHost     远程主机 (默认: 172.16.43.189)"
    Write-Host "  `$env:BackendContainer  后端容器名 (默认: evaluation-backend)"
    Write-Host "  `$env:FrontendContainer  前端容器名 (默认: evaluation-frontend)"
    Write-Host ""
    Write-Host "示例:" -ForegroundColor Yellow
    Write-Host '  $env:RemoteUser="admin"; $env:RemoteHost="192.168.1.100"; .\scripts\deploy.ps1'
    Write-Host ""
}

# ============================================================
# 函数: 设置SSH密钥认证
# ============================================================
function Setup-SSHKey {
    <#
    .SYNOPSIS
        设置SSH密钥认证，实现免密登录
    .DESCRIPTION
        将本地公钥添加到远程服务器的authorized_keys中
    #>

    $password = "Htht@1234"  # 默认密码，可通过环境变量覆盖
    $publicKey = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQCokkVZc2E/PwnrHMQYSyJJs2VM3a08QNeTm49nXqc/koPxUaGwT1uksU9vh4sOwzzCrW6vVuocp6UScYW5T/sYS7B2/LAA3cjvtQ3mo2R0Yy3v3//51zhuxSBynINf6QvKLK99++WrYTzrbS57ulJT29nnyiDgKrAOhh8L2xnSUi0Ivh56tEWSXEFVaknMpBqwq3K6e8q1JqZCy4RNfwOIXW9dwRVwXFWvQC7DnnQtJtG2tmz3T/Cyoq4uS7W5DhnQCiefzAr6Tu8REersK3L6F76vcR/zgmoOw+I6WCVjCZSMX/pM12Fx2ze6315oPH3gmYDQEraKpkDRSWa8+T7iVLQPUO4uczDY/UotfQOVeIW7qCf0N45GZVyQikw+zHcsnv57m64gyr7hu6I0kpEcZn0uKXC0WMN5IFMtB5GDO2JHArSNOtc7BuJjbR91IBilhBvtaka5FJI7LVNuQ3V3Xu5wyUXintuirBBRmj7hfioIPISHqt38R1mgSwMOn5G2dBsYTy99bzaJ3+5HkqCth9PEyfji+pYvbBgkd8TSUtgXkjTT+C8LWFgzZU9JrIZBF115kV8sVkn8O8HDaXxl6J3DyZ1Qag00t6AtCWAJiSujosaTbesqFeUZdqgqOmDJY2D/pIvx+i8NRSc1y7EKqBeBCRPukrCRMKlrAa3QeQ== admin@DESKTOP-KC2OIL3"

    Write-Host "正在设置SSH密钥认证..." -ForegroundColor Cyan

    # 检查expect是否可用
    $expectAvailable = $null -ne (Get-Command expect -ErrorAction SilentlyContinue)

    if ($expectAvailable) {
        # 使用expect脚本自动输入密码
        $expectScript = @"
#!/usr/bin/expect -f
set timeout 30
spawn ssh -o StrictHostKeyChecking=no root@101.126.46.254
expect {
    "password:" {
        send "$password\r"
        expect "#"
        send "mkdir -p ~/.ssh\r"
        expect "#"
        send "chmod 700 ~/.ssh\r"
        expect "#"
        send "echo '$publicKey' >> ~/.ssh/authorized_keys\r"
        expect "#"
        send "chmod 600 ~/.ssh/authorized_keys\r"
        expect "#"
        send "exit\r"
    }
    "#" {
        send "mkdir -p ~/.ssh\r"
        expect "#"
        send "chmod 700 ~/.ssh\r"
        expect "#"
        send "echo '$publicKey' >> ~/.ssh/authorized_keys\r"
        expect "#"
        send "chmod 600 ~/.ssh/authorized_keys\r"
        expect "#"
        send "exit\r"
    }
}
expect eof
"@

        $expectScript | Out-File -FilePath "temp_ssh_setup.exp" -Encoding ASCII

        Write-Host "Running expect script..."
        expect temp_ssh_setup.exp
        Remove-Item temp_ssh_setup.exp

        Write-Host "SSH密钥设置完成！" -ForegroundColor Green
        Write-Host "现在可以使用: ssh root@101.126.46.254" -ForegroundColor Yellow
    } else {
        Write-Host "错误: expect未安装。请手动设置SSH密钥或安装expect。" -ForegroundColor Red
        Write-Host "手动设置步骤:" -ForegroundColor Yellow
        Write-Host "1. 将本地公钥 (~/.ssh/id_rsa.pub) 内容复制"
        Write-Host "2. 登录到远程服务器"
        Write-Host "3. 执行: mkdir -p ~/.ssh && chmod 700 ~/.ssh"
        Write-Host "4. 执行: echo '你的公钥' >> ~/.ssh/authorized_keys"
        Write-Host "5. 执行: chmod 600 ~/.ssh/authorized_keys"
    }
}

# ============================================================
# 函数: 构建后端
# ============================================================
function Build-Backend {
    Write-Host "`n=== 1. 开始构建后端 (Backend) ===" -ForegroundColor Cyan

    Push-Location $ProjectRoot
    try {
        cmd /c "mvn clean package -DskipTests"
        if ($LASTEXITCODE -ne 0) {
            throw "后端构建失败！"
        }
        Write-Host "✓ 后端构建完成" -ForegroundColor Green
    }
    finally {
        Pop-Location
    }
}

# ============================================================
# 函数: 构建前端
# ============================================================
function Build-Frontend {
    Write-Host "`n=== 2. 开始构建前端 (Frontend) ===" -ForegroundColor Cyan

    Push-Location "$ProjectRoot\frontend"
    try {
        if (-not (Test-Path "node_modules")) {
            Write-Host "检测到 node_modules 不存在，正在安装依赖..." -ForegroundColor Yellow
            cmd /c "npm ci"
        }
        cmd /c "npm run build"
        if ($LASTEXITCODE -ne 0) {
            throw "前端构建失败！"
        }
        Write-Host "✓ 前端构建完成" -ForegroundColor Green
    }
    finally {
        Pop-Location
    }
}

# ============================================================
# 函数: 上传文件
# ============================================================
function Upload-Files {
    Write-Host "`n=== 3. 上传文件至服务器 ($RemoteHost) ===" -ForegroundColor Cyan

    # 上传 Backend JAR
    if (Test-Path $BackendJarPath) {
        Write-Host "正在上传后端 JAR 包..."
        scp $BackendJarPath "${RemoteUser}@${RemoteHost}:~/app.jar"
        if ($LASTEXITCODE -ne 0) {
            throw "上传后端 JAR 失败"
        }
        Write-Host "✓ 后端 JAR 上传完成" -ForegroundColor Green
    }
    else {
        throw "找不到后端构建产物: $BackendJarPath"
    }

    # 上传 Frontend Dist
    if (Test-Path $FrontendDistPath) {
        Write-Host "正在上传前端静态资源..."
        scp -r $FrontendDistPath "${RemoteUser}@${RemoteHost}:~/frontend_dist"
        if ($LASTEXITCODE -ne 0) {
            throw "上传前端资源失败"
        }
        Write-Host "✓ 前端资源上传完成" -ForegroundColor Green
    }
    else {
        throw "找不到前端构建产物: $FrontendDistPath"
    }
}

# ============================================================
# 函数: 远程部署
# ============================================================
function Deploy-Remote {
    Write-Host "`n=== 4. 执行远程部署 ===" -ForegroundColor Cyan

    $RemoteCommands = @(
        # --- 后端部署 ---
        "echo '>>> [Backend] Updating JAR...'"
        "docker cp ~/app.jar ${BackendContainer}:/app/app.jar"
        "echo '>>> [Backend] Restarting Container...'"
        "docker restart ${BackendContainer}"

        # --- 前端部署 ---
        "echo '>>> [Frontend] Cleaning old files...'"
        "docker exec ${FrontendContainer} rm -rf /usr/share/nginx/html"
        "echo '>>> [Frontend] Copying new files...'"
        "docker cp ~/frontend_dist ${FrontendContainer}:/usr/share/nginx/html"
        "echo '>>> [Frontend] Restarting Container...'"
        "docker restart ${FrontendContainer}"

        # --- 清理临时文件 ---
        "echo '>>> Cleaning up temporary files...'"
        "rm -rf ~/app.jar ~/frontend_dist"

        "echo '>>> 部署完成！'"
    ) -join " && "

    # 执行远程命令
    ssh "${RemoteUser}@${RemoteHost}" $RemoteCommands

    if ($LASTEXITCODE -eq 0) {
        Write-Host "`n✓ 服务已更新并重启" -ForegroundColor Green
    }
    else {
        throw "远程部署过程中发生错误"
    }
}

# ============================================================
# 函数: 快速部署（完整流程）
# ============================================================
function Invoke-QuickDeploy {
    Write-Host ""
    Write-Host "=== 减灾能力评估系统 - 快速部署 ===" -ForegroundColor Green
    Write-Host "目标服务器: ${RemoteUser}@${RemoteHost}" -ForegroundColor Yellow
    Write-Host ""

    try {
        Build-Backend
        Build-Frontend
        Upload-Files
        Deploy-Remote

        Write-Host ""
        Write-Host "=== 部署成功 ===" -ForegroundColor Green
        Write-Host "前端访问地址: http://${RemoteHost}" -ForegroundColor Yellow
        Write-Host "后端访问地址: http://${RemoteHost}:8081" -ForegroundColor Yellow
    }
    catch {
        Write-Error "`n部署失败: $_"
        exit 1
    }
}

# ============================================================
# 主程序入口
# ============================================================

$command = $args[0]

switch ($command) {
    "setup-ssh" {
        Setup-SSHKey
    }
    "help" {
        Show-Help
    }
    "" {
        Invoke-QuickDeploy
    }
    default {
        Write-Host "未知命令: $command" -ForegroundColor Red
        Write-Host "使用 '.\scripts\deploy.ps1 help' 查看帮助信息" -ForegroundColor Yellow
        exit 1
    }
}
