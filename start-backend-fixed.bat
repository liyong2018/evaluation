@echo off
echo Starting Backend Service with UTF-8 Encoding...
cd /d D:\Evaluation\evaluation
java -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -jar target\disaster-reduction-evaluation-1.0.0.jar
pause