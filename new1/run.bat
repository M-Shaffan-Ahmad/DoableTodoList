@echo off
REM Doable Todo List - Smart Launcher
REM Automatically finds the JAR and runs it with all dependencies

setlocal enabledelayedexpansion

REM Detect current directory
set SCRIPT_DIR=%~dp0
cd /d "!SCRIPT_DIR!"

REM Check if JAR exists in current directory
if exist "Doable.jar" (
    set JAR_FILE=!SCRIPT_DIR!Doable.jar
) else if exist "doable-todo-1.0-SNAPSHOT.jar" (
    set JAR_FILE=!SCRIPT_DIR!doable-todo-1.0-SNAPSHOT.jar
) else (
    echo Error: Doable.jar not found!
    pause
    exit /b 1
)

REM Try javaw first (no console window), fallback to java if not available
where javaw >nul 2>&1
if errorlevel 1 (
    java -jar "!JAR_FILE!"
) else (
    javaw -jar "!JAR_FILE!"
    exit /b 0
)
