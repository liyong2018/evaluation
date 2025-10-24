# 正确更新步骤1的13个算法

$host = "192.168.15.203"
$port = "30314"
$user = "root"
$pass = "123456"
$db = "evaluate_db"

Write-Host "Updating Step 1 algorithms..." -ForegroundColor Green

# 获取每个algorithm_order对应的ID
$ids = @{}
for ($i = 1; $i -le 13; $i++) {
    $query = "SELECT id FROM step_algorithm WHERE step_id = 50 AND algorithm_order = $i"
    $result = mysql -h $host -P $port -u $user -p$pass $db -N -e $query 2>&1 | Select-Object -Last 1
    $ids[$i] = $result.Trim()
    Write-Host "Order $i -> ID $($ids[$i])"
}

# 更新每个算法
$updates = @(
    @{order=1; name='是否有社区（行政村）应急预案'; param='HAS_EMERGENCY_PLAN'; expr='has_emergency_plan == \"是\" ? 1.0 : 0.0'},
    @{order=2; name='是否有本辖区弱势人群清单'; param='HAS_VULNERABLE_GROUPS_LIST'; expr='has_vulnerable_groups_list == \"是\" ? 1.0 : 0.0'},
    @{order=3; name='是否有本辖区地质灾害等隐患点清单'; param='HAS_DISASTER_POINTS_LIST'; expr='has_disaster_points_list == \"是\" ? 1.0 : 0.0'},
    @{order=4; name='是否有社区（行政村）灾害类地图'; param='HAS_DISASTER_MAP'; expr='has_disaster_map == \"是\" ? 1.0 : 0.0'},
    @{order=5; name='常住人口数量'; param='RESIDENT_POPULATION'; expr='resident_population != null ? resident_population : 0'},
    @{order=6; name='上一年度防灾减灾救灾资金投入总金额'; param='LAST_YEAR_FUNDING_AMOUNT'; expr='last_year_funding_amount != null ? last_year_funding_amount : 0.0'},
    @{order=7; name='现有储备物资、装备折合金额'; param='MATERIALS_EQUIPMENT_VALUE'; expr='materials_equipment_value != null ? materials_equipment_value : 0.0'},
    @{order=8; name='社区医疗卫生服务站或村卫生室数量'; param='MEDICAL_SERVICE_COUNT'; expr='medical_service_count != null ? medical_service_count : 0'},
    @{order=9; name='民兵预备役人数'; param='MILITIA_RESERVE_COUNT'; expr='militia_reserve_count != null ? militia_reserve_count : 0'},
    @{order=10; name='登记注册志愿者人数'; param='REGISTERED_VOLUNTEER_COUNT'; expr='registered_volunteer_count != null ? registered_volunteer_count : 0'},
    @{order=11; name='上一年度防灾减灾培训活动培训人次'; param='LAST_YEAR_TRAINING_PARTICIPANTS'; expr='last_year_training_participants != null ? last_year_training_participants : 0'},
    @{order=12; name='参与上一年度组织的防灾减灾演练活动的居民'; param='LAST_YEAR_DRILL_PARTICIPANTS'; expr='last_year_drill_participants != null ? last_year_drill_participants : 0'},
    @{order=13; name='本级灾害应急避难场所容量'; param='EMERGENCY_SHELTER_CAPACITY'; expr='emergency_shelter_capacity != null ? emergency_shelter_capacity : 0'}
)

foreach ($update in $updates) {
    $id = $ids[$update.order]
    $query = "UPDATE step_algorithm SET algorithm_name = '$($update.name)', output_param = '$($update.param)', ql_expression = '$($update.expr)' WHERE id = $id"
    Write-Host "Updating order $($update.order) (ID $id)..."
    mysql -h $host -P $port -u $user -p$pass $db -e $query 2>&1 | Out-Null
}

Write-Host "`nVerifying updates..." -ForegroundColor Yellow
mysql -h $host -P $port -u $user -p$pass $db -e "SELECT algorithm_order, algorithm_name, output_param FROM step_algorithm WHERE step_id = 50 ORDER BY algorithm_order"

Write-Host "`nDone!" -ForegroundColor Green
