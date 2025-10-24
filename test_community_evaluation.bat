@echo off
chcp 65001 >nul
echo ========================================
echo 社区评估TOPSIS修复验证脚本
echo ========================================
echo.

echo 步骤1: 检查数据库配置是否正确...
echo.
mysql -h192.168.15.203 -P30314 -uroot -p123456 evaluate_db -e "SELECT sa.algorithm_order AS '顺序', sa.algorithm_name AS '算法名称', LEFT(sa.ql_expression, 50) AS '表达式' FROM step_algorithm sa JOIN model_step ms ON sa.step_id = ms.id WHERE ms.model_id = 4 AND ms.step_code = 'TOPSIS_DISTANCE' ORDER BY sa.algorithm_order;" 2>nul

echo.
echo 步骤2: 执行社区评估...
echo.
curl -X GET "http://localhost:8081/api/evaluation/execute-model?modelId=4&weightConfigId=2"

echo.
echo.
echo 步骤3: 查看评估结果（前5条）...
echo.
mysql -h192.168.15.203 -P30314 -uroot -p123456 evaluate_db -e "SELECT region_code AS '区域代码', ROUND(MANAGEMENT_SCORE, 6) AS '灾害管理能力', ROUND(SUPPORT_SCORE, 6) AS '灾害备灾能力', ROUND(CAPABILITY_SCORE, 6) AS '自救转移能力' FROM (SELECT * FROM survey_data WHERE model_id = 4 ORDER BY id DESC LIMIT 5) t ORDER BY region_code;" 2>nul

echo.
echo ========================================
echo 验证完成！
echo ========================================
echo.
echo 检查要点:
echo 1. TOPSIS距离计算步骤应该有6个算法
echo 2. 算法表达式应该使用@TOPSIS_POSITIVE和@TOPSIS_NEGATIVE
echo 3. 评估结果中的三个能力得分应该不为0
echo.

pause
