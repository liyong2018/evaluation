@echo off
chcp 65001 >nul
echo ========================================
echo 模型8完整修复验证脚本
echo ========================================
echo.

echo 步骤1: 验证所有@WEIGHT标记是否已修复...
echo.
mysql -h192.168.15.203 -P30314 -uroot -p123456 evaluate_db -e "SELECT COUNT(*) AS '剩余@WEIGHT标记数量' FROM step_algorithm sa JOIN model_step ms ON sa.step_id = ms.id WHERE ms.model_id = 8 AND sa.ql_expression LIKE '%%@WEIGHT%%';" 2>nul

echo.
echo 步骤2: 查看二级指标定权配置...
echo.
mysql -h192.168.15.203 -P30314 -uroot -p123456 evaluate_db -e "SELECT sa.algorithm_order AS '顺序', sa.algorithm_name AS '算法', LEFT(sa.ql_expression, 60) AS '表达式' FROM step_algorithm sa JOIN model_step ms ON sa.step_id = ms.id WHERE ms.model_id = 8 AND ms.step_code = 'SECONDARY_WEIGHTING' ORDER BY sa.algorithm_order;" 2>nul

echo.
echo 步骤3: 查看能力值计算配置...
echo.
mysql -h192.168.15.203 -P30314 -uroot -p123456 evaluate_db -e "SELECT sa.algorithm_order AS '顺序', sa.algorithm_name AS '算法', sa.ql_expression AS '表达式' FROM step_algorithm sa JOIN model_step ms ON sa.step_id = ms.id WHERE ms.model_id = 8 AND ms.step_code = 'CAPABILITY_GRADE' ORDER BY sa.algorithm_order;" 2>nul

echo.
echo 步骤4: 执行社区-乡镇评估...
echo.
echo 正在调用API...
curl -X GET "http://localhost:8082/api/evaluation/execute-model?modelId=8&weightConfigId=2"

echo.
echo.
echo ========================================
echo 验证完成！
echo ========================================
echo.
echo 检查要点:
echo 1. 剩余@WEIGHT标记数量应该为 0
echo 2. 所有表达式中不应该包含@WEIGHT
echo 3. API调用应该返回成功（code: 200）
echo 4. 如果失败，查看具体错误信息
echo.

pause
