@echo off
setlocal
set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "PATH=C:\Program Files\Java\jdk-17\bin;%PATH%"

cd /d "d:\Evaluation\evaluation"

echo Running ExtractGreenVariables with Maven dependencies...

REM Build classpath from Maven local repository
set "CP=target/test-classes;target/classes"

REM Add POI jars
for %%f in ("C:\Users\%USERNAME%\.m2\repository\org\apache\poi\poi\5.2.5\*.jar") do call :addjars "%%f"
for %%f in ("C:\Users\%USERNAME%\.m2\repository\org\apache\poi\poi-ooxml\5.2.5\*.jar") do call :addjars "%%f"
for %%f in ("C:\Users\%USERNAME%\.m2\repository\org\apache\poi\poi-ooxml-lite\5.2.5\*.jar") do call :addjars "%%f"
for %%f in ("C:\Users\%USERNAME%\.m2\repository\org\apache\poi\poi-ooxml-full\5.2.5\*.jar") do call :addjars "%%f"

REM Add commons jars
for %%f in ("C:\Users\%USERNAME%\.m2\repository\commons-codec\commons-codec\1.15\*.jar") do call :addjars "%%f"
for %%f in ("C:\Users\%USERNAME%\.m2\repository\commons-io\commons-io\2.11.0\*.jar") do call :addjars "%%f"
for %%f in ("C:\Users\%USERNAME%\.m2\repository\org\apache\commons\commons-compress\1.21\*.jar") do call :addjars "%%f"

REM Add other dependencies
for %%f in ("C:\Users\%USERNAME%\.m2\repository\com\github\virtuald\curvesapi\1.07\*.jar") do call :addjars "%%f"
for %%f in ("C:\Users\%USERNAME%\.m2\repository\org\apache\xmlbeans\xmlbeans\5.1.1\*.jar") do call :addjars "%%f"

java -cp "%CP%" com.evaluate.util.ExtractGreenVariables

goto :eof

:addjars
set "CP=%CP%;%~1"
goto :eof
