@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

if not exist "build\classes" mkdir "build\classes"

echo [1/3] Compiling Java sources...
set SRC=
for /r "src\main\java" %%f in (*.java) do set "SRC=!SRC! "%%f""
javac -encoding UTF-8 -source 1.8 -target 1.8 -cp "lib\*" -d "build\classes" !SRC!
if errorlevel 1 exit /b 1

echo [2/3] Copying resources and frontend files...
xcopy /e /y /i "src\main\resources\*" "build\classes\" >nul
if errorlevel 1 exit /b 1

echo [3/3] Building fat jar...
pushd "build\classes"
jar xf "..\..\lib\sqlite-jdbc-3.36.0.3.jar"
popd
jar cfe "build\calculator-backend.jar" com.course.calculator.Main -C "build\classes" .
if errorlevel 1 exit /b 1

echo Build succeeded: build\calculator-backend.jar
