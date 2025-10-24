# 测试模型8的API

$uri = "http://localhost:8082/api/evaluation/execute-model?modelId=8&weightConfigId=2"

Write-Host "正在调用API: $uri" -ForegroundColor Yellow
Write-Host ""

try {
    $response = Invoke-RestMethod -Uri $uri -Method Post -ContentType "application/json"
    
    Write-Host "API调用成功！" -ForegroundColor Green
    Write-Host ""
    Write-Host "响应内容:" -ForegroundColor Cyan
    $response | ConvertTo-Json -Depth 10
    
} catch {
    Write-Host "API调用失败！" -ForegroundColor Red
    Write-Host ""
    Write-Host "错误信息:" -ForegroundColor Red
    Write-Host $_.Exception.Message
    
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host ""
        Write-Host "响应内容:" -ForegroundColor Yellow
        Write-Host $responseBody
    }
}
