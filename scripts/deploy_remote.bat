@echo off
setlocal enabledelayedexpansion

REM Configuration
set SERVER=172.16.43.189
set USER=htht
set PASSWORD=1qaz@1234
set REMOTE_DIR=/home/htht/evaluation

echo ========================================
echo Deploying Evaluation System to %SERVER%
echo ========================================

REM Create directories on remote server
echo Creating directories...
powershell -Command "cmdkey /generic:TERMSRV/%SERVER% /user:%USER% /pass:%PASSWORD%"
powershell -Command "$secpass = '%PASSWORD%' | ConvertTo-SecureString -AsPlainText -Force; $cred = New-Object System.Management.Automation.PSCredential ('%USER%', $secpass); $s = New-SSHSession -ComputerName %SERVER% -Credential $cred -AcceptKey; Invoke-SSHCommand -SessionId $s.SessionId -Command 'mkdir -p %REMOTE_DIR%/frontend && mkdir -p %REMOTE_DIR%/nginx/conf.d && mkdir -p %REMOTE_DIR%/logs && mkdir -p %REMOTE_DIR%/uploads'; Remove-SSHSession -SessionId $s.SessionId" 2>nul

echo Copying files...
copy /Y docker-compose.yml %TEMP%\docker-compose.yml
copy /Y Dockerfile %TEMP%\Dockerfile

echo Deployment files prepared
echo ========================================
echo Please deploy manually or use proper SSH tool
endlocal
