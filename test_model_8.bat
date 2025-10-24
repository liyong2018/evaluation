@echo off
chcp 65001 >nul
echo ========================================
echo 模型8（社区-乡镇）评估测试脚本
echo ========================================
echo.

echo 步骤1: 检查二级指标定权配置...
echo.
mysql -h192.168.15.203 -P30314 -uroot -p123456 evaluate_db -e "SELECT sa.algorithm_order AS '顺序', sa.algorithm_name AS '算法名称', LEFT(sa.ql_expression, 80) AS '表达式' FROM step_algorithm sa JOIN model_step ms ON sa.step_id = ms.id WHERE ms.model_id = 8 AND ms.step_code = 'SECONDARY_WEIGHTING' ORDER BY sa.algorithm_order;" 2>nul

echo.
echo 步骤2: 执行社区-乡镇评估...
echo.
curl -X GET "http://localhost:8082/api/evaluation/execute-model?modelId=8&weightConfigId=2"

echo.
echo.
echo ========================================
echo 测试完成！
echo ========================================
echo.
echo 检查要点:
echo 1. 配置中不应该包含@WEIGHT标记
echo 2. 评估应该执行成功，返回200状态码
echo 3. 如果失败，查看错误信息
echo.

pause
