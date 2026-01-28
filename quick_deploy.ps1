<#
.SYNOPSIS
    快速部署脚本 (Quick Deploy Script)
    功能：在本地构建前后端代码，通过 SCP 上传至服务器，并直接更新运行中的 Docker 容器。
    前提：本机已配置 Maven、Node.js，且已配置服务器 SSH 免密登录。
#>

$ErrorActionPreference = "Stop"

# ================= 配置区域 =================
$RemoteUser = "htht"
$RemoteHost = "172.16.43.189"
$BackendContainer = "evaluation-backend"
$FrontendContainer = "evaluation-frontend"

# 本地路径
$BackendJarPath = "target/disaster-reduction-evaluation-1.0.0.jar"
$FrontendDistPath = "frontend/dist"

# ================= 1. 构建后端 =================
Write-Host "`n=== 1. 开始构建后端 (Backend) ===" -ForegroundColor Cyan
# 使用 cmd /c 确保 mvn 命令在 PowerShell 中正确执行
cmd /c "mvn clean package -DskipTests"
if ($LASTEXITCODE -ne 0) { 
    Write-Error "后端构建失败！"
    exit 1 
}

# ================= 2. 构建前端 =================
Write-Host "`n=== 2. 开始构建前端 (Frontend) ===" -ForegroundColor Cyan
Push-Location frontend
try {
    if (-not (Test-Path "node_modules")) {
        Write-Host "检测到 node_modules 不存在，正在安装依赖..." -ForegroundColor Yellow
        cmd /c "npm ci"
    }
    cmd /c "npm run build"
    if ($LASTEXITCODE -ne 0) { 
        throw "前端构建失败！" 
    }
}
catch {
    Write-Error $_
    exit 1
}
finally {
    Pop-Location
}

# ================= 3. 上传文件 =================
Write-Host "`n=== 3. 上传文件至服务器 ($RemoteHost) ===" -ForegroundColor Cyan

# 上传 Backend JAR
if (Test-Path $BackendJarPath) {
    Write-Host "正在上传后端 JAR 包..."
    scp $BackendJarPath "${RemoteUser}@${RemoteHost}:~/app.jar"
    if ($LASTEXITCODE -ne 0) { Write-Error "上传后端 JAR 失败"; exit 1 }
} else {
    Write-Error "找不到后端构建产物: $BackendJarPath"
    exit 1
}

# 上传 Frontend Dist
if (Test-Path $FrontendDistPath) {
    Write-Host "正在上传前端静态资源..."
    scp -r $FrontendDistPath "${RemoteUser}@${RemoteHost}:~/frontend_dist"
    if ($LASTEXITCODE -ne 0) { Write-Error "上传前端资源失败"; exit 1 }
} else {
    Write-Error "找不到前端构建产物: $FrontendDistPath"
    exit 1
}

# ================= 4. 远程部署 =================
Write-Host "`n=== 4. 执行远程部署 ===" -ForegroundColor Cyan

$RemoteCommands = @(
    # --- 后端部署 ---
    "echo '>>> [Backend] Updating JAR...'"
    "docker cp ~/app.jar ${BackendContainer}:/app/app.jar"
    "echo '>>> [Backend] Restarting Container...'"
    "docker restart ${BackendContainer}"
    
    # --- 前端部署 ---
    "echo '>>> [Frontend] Cleaning old files...'"
    # 删除容器内原有的 html 目录
    "docker exec ${FrontendContainer} rm -rf /usr/share/nginx/html"
    "echo '>>> [Frontend] Copying new files...'"
    # 将上传的 dist 目录复制为容器内的 html 目录
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
    Write-Host "`nSUCCESS: 服务已更新并重启。" -ForegroundColor Green
} else {
    Write-Error "`nFAILED: 远程部署过程中发生错误。"
}
