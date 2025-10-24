@echo off
chcp 65001 >nul
echo ========================================
echo 社区评估TOPSIS配置修复脚本
echo ========================================
echo.

echo 正在执行修复...
echo.

mysql -h192.168.15.203 -P30314 -uroot -p123456 evaluate_db < fix_community_topsis_config.sql

echo.
echo ========================================
echo 修复完成！
echo ========================================
echo.
echo 下一步操作:
echo 1. 重新执行社区评估
echo 2. 检查TOPSIS计算结果是否正确
echo.

pause
