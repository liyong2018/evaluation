@echo off
echo Updating Model 8 Step 1 Formulas...

mysql -h 192.168.15.203 -P 30314 -u root -p123456 evaluate_db -e "UPDATE step_algorithm SET output_param = 'HAS_EMERGENCY_PLAN', ql_expression = 'has_emergency_plan == \"是\" ? 1.0 : 0.0' WHERE id = 1737;"

mysql -h 192.168.15.203 -P 30314 -u root -p123456 evaluate_db -e "UPDATE step_algorithm SET output_param = 'HAS_VULNERABLE_GROUPS_LIST', ql_expression = 'has_vulnerable_groups_list == \"是\" ? 1.0 : 0.0' WHERE id = 1738;"

mysql -h 192.168.15.203 -P 30314 -u root -p123456 evaluate_db -e "UPDATE step_algorithm SET output_param = 'HAS_DISASTER_POINTS_LIST', ql_expression = 'has_disaster_points_list == \"是\" ? 1.0 : 0.0' WHERE id = 1739;"

mysql -h 192.168.15.203 -P 30314 -u root -p123456 evaluate_db -e "UPDATE step_algorithm SET output_param = 'HAS_DISASTER_MAP', ql_expression = 'has_disaster_map == \"是\" ? 1.0 : 0.0' WHERE id = 1740;"

mysql -h 192.168.15.203 -P 30314 -u root -p123456 evaluate_db -e "UPDATE step_algorithm SET output_param = 'RESIDENT_POPULATION', ql_expression = 'resident_population != null ? resident_population : 0' WHERE id = 1741;"

mysql -h 192.168.15.203 -P 30314 -u root -p123456 evaluate_db -e "UPDATE step_algorithm SET output_param = 'LAST_YEAR_FUNDING_AMOUNT', ql_expression = 'last_year_funding_amount != null ? last_year_funding_amount : 0.0' WHERE id = 1742;"

mysql -h 192.168.15.203 -P 30314 -u root -p123456 evaluate_db -e "UPDATE step_algorithm SET output_param = 'MATERIALS_EQUIPMENT_VALUE', ql_expression = 'materials_equipment_value != null ? materials_equipment_value : 0.0' WHERE id = 1743;"

mysql -h 192.168.15.203 -P 30314 -u root -p123456 evaluate_db -e "UPDATE step_algorithm SET output_param = 'MEDICAL_SERVICE_COUNT', ql_expression = 'medical_service_count != null ? medical_service_count : 0' WHERE id = 1744;"

mysql -h 192.168.15.203 -P 30314 -u root -p123456 evaluate_db -e "UPDATE step_algorithm SET output_param = 'MILITIA_RESERVE_COUNT', ql_expression = 'militia_reserve_count != null ? militia_reserve_count : 0' WHERE id = 1745;"

mysql -h 192.168.15.203 -P 30314 -u root -p123456 evaluate_db -e "UPDATE step_algorithm SET output_param = 'REGISTERED_VOLUNTEER_COUNT', ql_expression = 'registered_volunteer_count != null ? registered_volunteer_count : 0' WHERE id = 1839;"

mysql -h 192.168.15.203 -P 30314 -u root -p123456 evaluate_db -e "UPDATE step_algorithm SET output_param = 'LAST_YEAR_TRAINING_PARTICIPANTS', ql_expression = 'last_year_training_participants != null ? last_year_training_participants : 0' WHERE id = 1840;"

mysql -h 192.168.15.203 -P 30314 -u root -p123456 evaluate_db -e "UPDATE step_algorithm SET output_param = 'LAST_YEAR_DRILL_PARTICIPANTS', ql_expression = 'last_year_drill_participants != null ? last_year_drill_participants : 0' WHERE id = 1841;"

mysql -h 192.168.15.203 -P 30314 -u root -p123456 evaluate_db -e "UPDATE step_algorithm SET output_param = 'EMERGENCY_SHELTER_CAPACITY', ql_expression = 'emergency_shelter_capacity != null ? emergency_shelter_capacity : 0' WHERE id = 1842;"

echo.
echo Verifying updates...
mysql -h 192.168.15.203 -P 30314 -u root -p123456 evaluate_db -e "SELECT algorithm_order, algorithm_name, output_param FROM step_algorithm WHERE step_id = 50 ORDER BY algorithm_order;"

echo.
echo Done!
pause
