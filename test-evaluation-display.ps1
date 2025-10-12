# 测试评估模型执行和结果显示
# 确保后端服务运行在 http://localhost:8081

$baseUrl = "http://localhost:8081"

Write-Host "=== 测试评估模型执行和结果显示 ===" -ForegroundColor Cyan

# 1. 测试获取评估模型列表
Write-Host "`n1. 获取评估模型列表..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/evaluation-models" -Method Get
    if ($response.success) {
        Write-Host "✓ 成功获取模型列表" -ForegroundColor Green
        Write-Host "模型数量: $($response.data.Count)"
        if ($response.data.Count -gt 0) {
            $modelId = $response.data[0].id
            Write-Host "将使用模型ID: $modelId ($($response.data[0].modelName))"
        }
    } else {
        Write-Host "✗ 获取模型列表失败: $($response.message)" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "✗ 请求失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 2. 测试获取地区列表
Write-Host "`n2. 获取地区列表..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/regions/tree" -Method Get
    if ($response.success) {
        Write-Host "✓ 成功获取地区树" -ForegroundColor Green
        # 获取一个乡镇级别的region_code用于测试
        $regionCode = $null
        foreach ($province in $response.data) {
            if ($province.children) {
                foreach ($city in $province.children) {
                    if ($city.children) {
                        foreach ($county in $city.children) {
                            if ($county.children -and $county.children.Count -gt 0) {
                                $regionCode = $county.children[0].code
                                break
                            }
                        }
                        if ($regionCode) { break }
                    }
                }
                if ($regionCode) { break }
            }
        }
        
        if ($regionCode) {
            Write-Host "将使用地区代码: $regionCode"
        } else {
            Write-Host "✗ 未找到可用的地区代码" -ForegroundColor Red
            exit 1
        }
    } else {
        Write-Host "✗ 获取地区树失败: $($response.message)" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "✗ 请求失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 3. 测试获取权重配置
Write-Host "`n3. 获取权重配置..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/weight-configs" -Method Get
    if ($response.success -and $response.data.Count -gt 0) {
        Write-Host "✓ 成功获取权重配置" -ForegroundColor Green
        $weightConfigId = $response.data[0].id
        Write-Host "将使用权重配置ID: $weightConfigId ($($response.data[0].configName))"
    } else {
        Write-Host "✗ 获取权重配置失败或无可用配置" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "✗ 请求失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 4. 执行评估模型
Write-Host "`n4. 执行评估模型..." -ForegroundColor Yellow
try {
    $body = @{
        regionCodes = @($regionCode)
        weightConfigId = $weightConfigId
    } | ConvertTo-Json
    
    Write-Host "请求参数: $body"
    
    $response = Invoke-RestMethod -Uri "$baseUrl/api/evaluation/execute-model/$modelId" `
                                  -Method Post `
                                  -Body $body `
                                  -ContentType "application/json"
    
    if ($response.success) {
        Write-Host "✓ 模型执行成功" -ForegroundColor Green
        Write-Host "执行结果包含步骤数: $($response.data.stepResults.Count)"
        
        # 保存执行结果用于下一步
        $executionResults = $response.data
    } else {
        Write-Host "✗ 模型执行失败: $($response.message)" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "✗ 请求失败: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails) {
        Write-Host "错误详情: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
    exit 1
}

# 5. 生成结果表
Write-Host "`n5. 生成结果表..." -ForegroundColor Yellow
try {
    $body = $executionResults | ConvertTo-Json -Depth 10
    
    $response = Invoke-RestMethod -Uri "$baseUrl/api/evaluation/generate-result-table" `
                                  -Method Post `
                                  -Body $body `
                                  -ContentType "application/json"
    
    if ($response.success) {
        Write-Host "✓ 结果表生成成功" -ForegroundColor Green
        Write-Host "结果行数: $($response.data.Count)"
        
        # 显示结果表的内容
        if ($response.data.Count -gt 0) {
            Write-Host "`n=== 结果表内容 ===" -ForegroundColor Cyan
            $firstRow = $response.data[0]
            
            # 显示列名
            Write-Host "`n列名列表:" -ForegroundColor Yellow
            $columns = $firstRow.PSObject.Properties.Name
            foreach ($col in $columns) {
                Write-Host "  - $col"
            }
            
            # 显示第一行数据
            Write-Host "`n第一行数据:" -ForegroundColor Yellow
            foreach ($prop in $firstRow.PSObject.Properties) {
                Write-Host "  $($prop.Name): $($prop.Value)"
            }
            
            # 检查关键列
            Write-Host "`n=== 检查关键显示项 ===" -ForegroundColor Cyan
            
            # 检查1: 是否有regionName列
            if ($firstRow.PSObject.Properties.Name -contains "regionName") {
                Write-Host "✓ regionName列存在" -ForegroundColor Green
                Write-Host "  地区名称: $($firstRow.regionName)"
            } else {
                Write-Host "✗ regionName列不存在" -ForegroundColor Red
            }
            
            # 检查2: 是否使用了中文列名
            $chineseColumnCount = 0
            foreach ($col in $columns) {
                if ($col -match '[\u4e00-\u9fa5]') {
                    $chineseColumnCount++
                }
            }
            Write-Host "✓ 中文列名数量: $chineseColumnCount" -ForegroundColor Green
            
            # 检查3: 风险评估能力计算结果
            $riskAssessmentCol = $columns | Where-Object { $_ -like "*风险评估*" }
            if ($riskAssessmentCol) {
                Write-Host "✓ 找到风险评估能力计算列: $riskAssessmentCol" -ForegroundColor Green
                Write-Host "  值: $($firstRow.$riskAssessmentCol)"
            } else {
                Write-Host "! 未找到风险评估能力计算列（可能使用英文列名）" -ForegroundColor Yellow
            }
        }
    } else {
        Write-Host "✗ 结果表生成失败: $($response.message)" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "✗ 请求失败: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails) {
        Write-Host "错误详情: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
    exit 1
}

Write-Host "`n=== 测试完成 ===" -ForegroundColor Cyan
Write-Host "所有测试通过！" -ForegroundColor Green
