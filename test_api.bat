@echo off
echo Testing Model 8 API...
echo.

powershell -Command "try { $r = Invoke-RestMethod -Uri 'http://localhost:8082/api/evaluation/execute-model?modelId=8&weightConfigId=2' -Method Post -ContentType 'application/json'; Write-Host 'Success!' -ForegroundColor Green; $r | ConvertTo-Json } catch { Write-Host 'Error:' $_.Exception.Message -ForegroundColor Red }"

pause
