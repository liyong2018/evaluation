# 测试乡镇聚合功能
# 执行模型8，验证按乡镇分组聚合的功能

$baseUrl = "http://localhost:8080"
$modelId = 8

Write-Host "=== 测试乡镇聚合功能 ===" -ForegroundColor Green
Write-Host "说明：此测试验证模型8是否正确按乡镇分组聚合社区数据`n" -ForegroundColor Cyan

# 首先查询数据库，获取同一个乡镇的多个社区
Write-Host "1. 查询数据库，获取测试数据..." -ForegroundColor Yellow
$query = "SELECT region_code, township_name, community_name FROM community_disaster_reduction_capacity LIMIT 10;"
$communities = mysql -h 192.168.15.203 -P 30314 -u root -p123456 evaluate_db -e $query 2>&1 | Select-Object -Skip 1

Write-Host "找到的社区数据:"
$communities | ForEach-Object { Write-Host "  $_" }

# 执行模型8
Write-Host "`n2. 执行模型8..." -ForegroundColor Yellow
$executeUrl = "$baseUrl/api/model/execute"

# 使用实际的社区代码（这里需要根据实际数据调整）
$body = @{
    modelId = $modelId
    regionCodes = @("511425108001", "511425108002", "511425108003")
    weightConfigId = 1
} | ConvertTo-Json

Write-Host "请求URL: $executeUrl"
Write-Host "请求体: $body"

try {
    $response = Invoke-RestMethod -Uri $executeUrl -Method Post -Body $body -ContentType "application/json"
    
    Write-Host "`n2. 检查返回数据..." -ForegroundColor Yellow
    
    if ($response.success) {
        Write-Host "✓ 模型执行成功" -ForegroundColor Green
        
        # 检查tableData
        if ($response.data.tableData) {
            Write-Host "`n3. 检查tableData字段..." -ForegroundColor Yellow
            $firstRow = $response.data.tableData[0]
            
            Write-Host "第一行数据的字段:"
            $firstRow.PSObject.Properties | ForEach-Object {
                Write-Host "  - $($_.Name): $($_.Value)"
            }
            
            # 检查是否包含township_name
            if ($firstRow.townshipName) {
                Write-Host "`n✓ 包含townshipName字段: $($firstRow.townshipName)" -ForegroundColor Green
            } else {
                Write-Host "`n✗ 缺少townshipName字段" -ForegroundColor Red
            }
            
            # 检查是否包含communityName
            if ($firstRow.communityName) {
                Write-Host "✓ 包含communityName字段: $($firstRow.communityName)" -ForegroundColor Green
            } else {
                Write-Host "✗ 缺少communityName字段" -ForegroundColor Red
            }
            
            # 显示所有行的乡镇信息
            Write-Host "`n4. 检查聚合结果:" -ForegroundColor Yellow
            $rowCount = $response.data.tableData.Count
            Write-Host "  返回行数: $rowCount"
            
            if ($rowCount -eq 1) {
                Write-Host "  ✓ 正确！返回1行数据（乡镇级别）" -ForegroundColor Green
            } else {
                Write-Host "  ✗ 错误！应该返回1行乡镇数据，实际返回 $rowCount 行" -ForegroundColor Red
            }
            
            $response.data.tableData | ForEach-Object {
                Write-Host "`n  乡镇: $($_.townshipName)"
                Write-Host "  地区代码: $($_.regionCode)"
                Write-Host "  预案编制能力: $($_.'Plan Construction')"
                Write-Host "  隐患排查能力: $($_.'Hazard Inspection')"
                Write-Host "  风险评估能力: $($_.'Risk Assessment')"
            }
            
            # 验证字段名
            Write-Host "`n5. 验证字段名:" -ForegroundColor Yellow
            $firstRow = $response.data.tableData[0]
            $expectedFields = @('Plan Construction', 'Hazard Inspection', 'Risk Assessment', 
                               'Financial Input', 'Material Reserve', 'Medical Support',
                               'Self Mutual Aid', 'Public Evacuation', 'Relocation Shelter')
            
            $allFieldsPresent = $true
            foreach ($field in $expectedFields) {
                if ($firstRow.$field -ne $null) {
                    Write-Host "  ✓ $field" -ForegroundColor Green
                } else {
                    Write-Host "  ✗ 缺少字段: $field" -ForegroundColor Red
                    $allFieldsPresent = $false
                }
            }
            
            if ($allFieldsPresent) {
                Write-Host "`n  ✓ 所有字段都存在" -ForegroundColor Green
            } else {
                Write-Host "`n  ✗ 部分字段缺失" -ForegroundColor Red
            }
            
        } else {
            Write-Host "✗ 没有tableData" -ForegroundColor Red
        }
        
    } else {
        Write-Host "✗ 模型执行失败: $($response.message)" -ForegroundColor Red
    }
    
} catch {
    Write-Host "✗ 请求失败: $_" -ForegroundColor Red
    Write-Host "错误详情: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== 测试完成 ===" -ForegroundColor Green
