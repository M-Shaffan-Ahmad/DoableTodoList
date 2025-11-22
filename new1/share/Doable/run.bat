@echo off
REM Doable Todo List - Easy Run Script
REM This script requires Java 11 or higher to be installed

echo Checking Java installation...
java -version >nul 2>&1
if errorlevel 1 (
    echo.
    echo ERROR: Java is not installed or not in PATH
    echo.
    echo Please install Java from: https://adoptium.net/
    echo.
    echo After installing Java, restart this script.
    pause
    exit /b 1
)

echo Starting Doable Todo List...
cd /d "%~dp0"
java -jar Doable.jar

pause
