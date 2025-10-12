[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$baseUrl = "http://localhost:8081"

# Test 1: Get model
Write-Host "Test 1: Get evaluation models" -ForegroundColor Cyan
$models = Invoke-RestMethod -Uri "$baseUrl/api/model-management/models" -Method Get
Write-Host "Success: $($models.success)"
if ($models.success -and $models.data.Count -gt 0) {
    $modelId = $models.data[0].id
    Write-Host "Model ID: $modelId, Name: $($models.data[0].modelName)"
}

# Test 2: Get regions
Write-Host "`nTest 2: Get region tree" -ForegroundColor Cyan
$regions = Invoke-RestMethod -Uri "$baseUrl/api/region/tree" -Method Get
if ($regions.success -and $regions.data.Count -gt 0) {
    $province = $regions.data[0]
    Write-Host "Province: $($province.name), Code: $($province.code)"
    if ($province.children -and $province.children.Count -gt 0) {
        $city = $province.children[0]
        if ($city.children -and $city.children.Count -gt 0) {
            $county = $city.children[0]
            if ($county.children -and $county.children.Count -gt 0) {
                $township = $county.children[0]
                $regionCode = $township.code
                Write-Host "Using township: $($township.name), Code: $regionCode"
            }
        }
    }
}

# Test 3: Get weight config
Write-Host "`nTest 3: Get weight configs" -ForegroundColor Cyan
$weights = Invoke-RestMethod -Uri "$baseUrl/api/weight-config" -Method Get
if ($weights.success -and $weights.data.Count -gt 0) {
    $weightConfigId = $weights.data[0].id
    Write-Host "Weight Config ID: $weightConfigId, Name: $($weights.data[0].configName)"
}

# Test 4: Execute model
Write-Host "`nTest 4: Execute model" -ForegroundColor Cyan
$regionList = @($regionCode)
$requestBody = ConvertTo-Json -InputObject $regionList -Depth 2

Write-Host "Request: modelId=$modelId, regionCode=$regionCode, weightConfigId=$weightConfigId"

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/evaluation/execute-model?modelId=$modelId&weightConfigId=$weightConfigId" -Method Post -Body $requestBody -ContentType "application/json; charset=utf-8"
    Write-Host "Execution Success: $($response.success)"
    
    if ($response.success) {
        # Test 5: Generate result table
        Write-Host "`nTest 5: Generate result table" -ForegroundColor Cyan
        $tableRequestBody = $response.data | ConvertTo-Json -Depth 10
        
        $tableResponse = Invoke-RestMethod -Uri "$baseUrl/api/evaluation/generate-table" -Method Post -Body $tableRequestBody -ContentType "application/json; charset=utf-8"
        
        if ($tableResponse.success -and $tableResponse.data.Count -gt 0) {
            Write-Host "Table rows: $($tableResponse.data.Count)"
            $firstRow = $tableResponse.data[0]
            
            Write-Host "`nColumns:" -ForegroundColor Yellow
            $firstRow.PSObject.Properties | ForEach-Object {
                Write-Host "  $($_.Name) = $($_.Value)"
            }
            
            # Check specific issues
            Write-Host "`nChecking fixes:" -ForegroundColor Green
            
            # Issue 1: Check regionName exists and is Chinese
            if ($firstRow.PSObject.Properties['regionName']) {
                $regionName = $firstRow.regionName
                Write-Host "[1] Region Name: $regionName (Code: $($firstRow.regionCode))"
            } else {
                Write-Host "[1] ERROR: regionName column not found!" -ForegroundColor Red
            }
            
            # Issue 2: Check for Chinese column names
            $chineseColumns = @()
            $firstRow.PSObject.Properties | ForEach-Object {
                if ($_.Name -match '[\u4e00-\u9fa5]') {
                    $chineseColumns += $_.Name
                }
            }
            Write-Host "[2] Chinese columns ($($chineseColumns.Count)): $($chineseColumns -join ', ')"
            
            # Issue 3: Check risk assessment value
            $riskCol = $firstRow.PSObject.Properties | Where-Object { $_.Name -match '风险' } | Select-Object -First 1
            if ($riskCol) {
                Write-Host "[3] Risk Assessment Column: $($riskCol.Name) = $($riskCol.Value)"
            } else {
                Write-Host "[3] WARNING: No risk assessment column found" -ForegroundColor Yellow
            }
        }
    }
} catch {
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host $_.Exception.StackTrace
}
