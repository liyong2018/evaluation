# 测试步骤1的13列数据是否完整

$baseUrl = "http://localhost:8080"
$modelId = 8

Write-Host "=== 测试步骤1的13列数据 ===" -ForegroundColor Green

# 执行模型8
Write-Host "`n1. 执行模型8..." -ForegroundColor Yellow
$executeUrl = "$baseUrl/api/model/execute"
$body = @{
    modelId = $modelId
    regionCodes = @("511425108001", "511425108002", "511425108003")
    weightConfigId = 1
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri $executeUrl -Method Post -Body $body -ContentType "application/json"
    
    if ($response.success) {
        Write-Host "✓ 模型执行成功" -ForegroundColor Green
        
        # 检查tableData的第一行
        if ($response.data.tableData -and $response.data.tableData.Count -gt 0) {
            $firstRow = $response.data.tableData[0]
            
            Write-Host "`n2. 检查第一行数据的所有字段:" -ForegroundColor Yellow
            $allFields = $firstRow.PSObject.Properties | Select-Object -ExpandProperty Name
            Write-Host "总字段数: $($allFields.Count)"
            
            # 列出所有字段
            $allFields | ForEach-Object {
                $value = $firstRow.$_
                Write-Host "  - $_`: $value"
            }
            
            # 检查步骤1的13个字段
            Write-Host "`n3. 检查步骤1的13个预期字段:" -ForegroundColor Yellow
            $step1Fields = @(
                'Plan Construction',
                'Hazard Inspection',
                'Risk Assessment',
                'Financial Input',
                'Material Reserve',
                'Medical Support',
                'Self Mutual Aid',
                'Public Evacuation',
                'Relocation Shelter',
                'Resident Population',
                'Funding Amount',
                'Materials Value',
                'Shelter Capacity'
            )
            
            $missingFields = @()
            foreach ($field in $step1Fields) {
                if ($firstRow.$field -ne $null) {
                    Write-Host "  ✓ $field`: $($firstRow.$field)" -ForegroundColor Green
                } else {
                    Write-Host "  ✗ $field`: 缺失" -ForegroundColor Red
                    $missingFields += $field
                }
            }
            
            if ($missingFields.Count -eq 0) {
                Write-Host "`n✓ 所有13个字段都存在！" -ForegroundColor Green
            } else {
                Write-Host "`n✗ 缺失 $($missingFields.Count) 个字段: $($missingFields -join ', ')" -ForegroundColor Red
            }
            
            # 检查columns配置
            Write-Host "`n4. 检查columns配置:" -ForegroundColor Yellow
            if ($response.data.columns) {
                Write-Host "总列数: $($response.data.columns.Count)"
                
                # 按stepOrder分组
                $columnsByStep = $response.data.columns | Group-Object -Property stepOrder
                foreach ($group in $columnsByStep) {
                    $stepOrder = $group.Name
                    $count = $group.Count
                    if ($stepOrder -eq "") {
                        Write-Host "  基础列: $count 列"
                    } else {
                        Write-Host "  步骤 $stepOrder`: $count 列"
                    }
                }
            }
            
        } else {
            Write-Host "✗ 没有tableData" -ForegroundColor Red
        }
        
    } else {
        Write-Host "✗ 模型执行失败: $($response.message)" -ForegroundColor Red
    }
    
} catch {
    Write-Host "✗ 请求失败: $_" -ForegroundColor Red
}

Write-Host "`n=== 测试完成 ===" -ForegroundColor Green
