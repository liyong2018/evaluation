# 调试步骤1的输出

$baseUrl = "http://localhost:8080"
$body = @{
    modelId = 8
    regionCodes = @("511425108001")
    weightConfigId = 1
} | ConvertTo-Json

Write-Host "执行模型8..." -ForegroundColor Green

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/model/execute" -Method Post -Body $body -ContentType "application/json"
    
    if ($response.success) {
        Write-Host "`n=== 1. tableData第一行的所有字段 ===" -ForegroundColor Yellow
        $firstRow = $response.data.tableData[0]
        $allFields = @($firstRow.PSObject.Properties.Name)
        Write-Host "总字段数: $($allFields.Count)"
        $allFields | ForEach-Object { Write-Host "  - $_" }
        
        Write-Host "`n=== 2. columns配置 ===" -ForegroundColor Yellow
        Write-Host "总列数: $($response.data.columns.Count)"
        $response.data.columns | ForEach-Object {
            Write-Host "  - label: $($_.label), prop: $($_.prop), stepOrder: $($_.stepOrder)"
        }
        
        Write-Host "`n=== 3. stepResults ===" -ForegroundColor Yellow
        $response.data.stepResults.PSObject.Properties | ForEach-Object {
            Write-Host "步骤: $($_.Name)"
            $stepData = $_.Value
            if ($stepData.outputToAlgorithmName) {
                Write-Host "  outputToAlgorithmName数量: $($stepData.outputToAlgorithmName.Count)"
                $stepData.outputToAlgorithmName.PSObject.Properties | ForEach-Object {
                    Write-Host "    $($_.Name) -> $($_.Value)"
                }
            }
        }
        
    } else {
        Write-Host "执行失败: $($response.message)" -ForegroundColor Red
    }
} catch {
    Write-Host "错误: $_" -ForegroundColor Red
}
