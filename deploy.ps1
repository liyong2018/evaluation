# PowerShell deployment script using SSH.NET
# First, install SSH.NET module: Install-Module -Name Posh-SSH

$ErrorActionPreference = "Stop"

$PASSWORD = "Htht@12#$"
$SERVER = "172.16.43.189"
$USERNAME = "root"
$REMOTE_DIR = "~/evaluation"
$LOCAL_DIR = "D:\Evaluation\evaluation"

Write-Host "Attempting to connect to $SERVER..." -ForegroundColor Cyan

try {
    # Import Posh-SSH module
    Import-Module Posh-SSH -ErrorAction Stop

    # Create secure password
    $secPassword = ConvertTo-SecureString $PASSWORD -AsPlainText -Force
    $credential = New-Object System.Management.Automation.PSCredential ($USERNAME, $secPassword)

    # Create SSH session
    $session = New-SSHSession -ComputerName $SERVER -Credential $credential -AcceptKey

    Write-Host "Connected successfully!" -ForegroundColor Green

    # Stop Docker containers
    Write-Host "`nStopping Docker containers..." -ForegroundColor Yellow
    Invoke-SSHCommand -SessionId $session.SessionId -Command "cd $REMOTE_DIR && docker compose down"

    # Upload JAR file
    Write-Host "`nUploading JAR file..." -ForegroundColor Yellow
    $jarPath = "$LOCAL_DIR\target\disaster-reduction-evaluation-1.0.0.jar"
    $remoteJar = "$REMOTE_DIR/target/disaster-reduction-evaluation-1.0.0.jar"
    Set-SCPFile -SessionId $session.SessionId -LocalFile $jarPath -RemotePath $remoteJar

    # Upload frontend files
    Write-Host "`nUploading frontend files..." -ForegroundColor Yellow
    $localDist = "$LOCAL_DIR\frontend\dist"
    $remoteDist = "$REMOTE_DIR/frontend/dist"

    # Create remote dist directory
    Invoke-SSHCommand -SessionId $session.SessionId -Command "mkdir -p $remoteDist"

    # Upload all files from dist
    Get-ChildItem -Path $localDist -Recurse | ForEach-Object {
        $relativePath = $_.FullName.Substring($localDist.Length + 1).Replace('\', '/')
        $remotePath = "$remoteDist/$relativePath"

        # Create remote directory if needed
        $remoteDir = [System.IO.Path]::GetDirectoryName($remotePath)
        Invoke-SSHCommand -SessionId $session.SessionId -Command "mkdir -p $remoteDir"

        if (-not $_.PSIsContainer) {
            Set-SCPFile -SessionId $session.SessionId -LocalFile $_.FullName -RemotePath $remotePath
        }
    }

    # Rebuild and start Docker containers
    Write-Host "`nRebuilding Docker image..." -ForegroundColor Yellow
    Invoke-SSHCommand -SessionId $session.SessionId -Command "cd $REMOTE_DIR && docker compose build evaluation-app"

    Write-Host "`nStarting Docker containers..." -ForegroundColor Yellow
    Invoke-SSHCommand -SessionId $session.SessionId -Command "cd $REMOTE_DIR && docker compose up -d"

    # Remove session
    Remove-SSHSession -SessionId $session.SessionId

    Write-Host "`nDeployment completed successfully!" -ForegroundColor Green

} catch {
    Write-Host "Error: $_" -ForegroundColor Red
    Write-Host "`nPlease install Posh-SSH module first:" -ForegroundColor Yellow
    Write-Host "Install-Module -Name Posh-SSH -Force" -ForegroundColor Cyan
    exit 1
}
