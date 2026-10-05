@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

if not exist "build\calculator-backend.jar" call build.bat
if errorlevel 1 exit /b 1

if not exist "build\test-classes" mkdir "build\test-classes"

echo Compiling tests...
set TSRC=
for /r "src\test\java" %%f in (*.java) do set "TSRC=!TSRC! "%%f""
javac -encoding UTF-8 -source 1.8 -target 1.8 -cp "build\classes;lib\*" -d "build\test-classes" !TSRC!
if errorlevel 1 exit /b 1

java -cp "build\classes;build\test-classes;lib\*" com.course.calculator.core.ExpressionEvaluatorTest
