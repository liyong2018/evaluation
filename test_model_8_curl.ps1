$uri = "http://localhost:8082/api/evaluation/execute-model?modelId=8&weightConfigId=2"

# 区域代码列表（前3个社区用于测试）
$regionCodes = @("511425001001", "511425001002", "511425001003")
$body = $regionCodes | ConvertTo-Json

Write-Host "Testing Model 8..." -ForegroundColor Cyan
Write-Host "URI: $uri" -ForegroundColor Gray
Write-Host "Region Codes: $($regionCodes -join ', ')" -ForegroundColor Gray
Write-Host ""

try {
    $response = Invoke-RestMethod -Uri $uri -Method Post -Body $body -ContentType "application/json" -ErrorAction Stop
    
    Write-Host "Success!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Response:" -ForegroundColor Yellow
    $response | ConvertTo-Json -Depth 5
    
    Write-Host ""
    Write-Host "Checking database results..." -ForegroundColor Cyan
    
} catch {
    Write-Host "Failed!" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}
