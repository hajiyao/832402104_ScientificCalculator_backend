@echo off
setlocal
cd /d "%~dp0"

if not exist "build\calculator-backend.jar" call build.bat
if errorlevel 1 (
    echo Build failed.
    pause
    exit /b 1
)

java -jar "build\calculator-backend.jar"
