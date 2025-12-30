@echo off
echo Setting Java 17 environment...
set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "PATH=C:\Program Files\Java\jdk-17\bin;%PATH%"
echo Java version:
java -version
echo.
echo Maven version:
mvn -version
echo.
echo Running Maven build...
mvn clean compile -e
if %ERRORLEVEL% EQU 0 (
    echo Build successful!
) else (
    echo Build failed with error code %ERRORLEVEL%
)
pause