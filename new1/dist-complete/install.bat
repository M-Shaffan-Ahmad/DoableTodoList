@echo off
REM Doable Todo List - Installation Script
REM Copies everything to AppData with proper classpath setup

setlocal enabledelayedexpansion
cd /d "%~dp0"

title Doable Todo List - Installation

echo.
echo ======================================
echo   Doable Todo List - Installation
echo ======================================
echo.

REM Check Java
echo Checking for Java...
java -version >nul 2>&1
if errorlevel 1 (
    echo.
    echo ERROR: Java is not installed!
    echo.
    echo Please install Java from: https://adoptium.net/
    echo After installation, run this setup again.
    echo.
    pause
    exit /b 1
)

echo - Java found successfully!
echo.

REM Setup installation directory
set INSTALL_DIR=%APPDATA%\Doable
echo Installing to: %INSTALL_DIR%
echo.

if not exist "%INSTALL_DIR%" mkdir "%INSTALL_DIR%"
if not exist "%INSTALL_DIR%\lib" mkdir "%INSTALL_DIR%\lib"

REM Copy files
echo Copying application files...
copy /Y "doable-todo-1.0-SNAPSHOT.jar" "%INSTALL_DIR%\doable-todo.jar" >nul 2>&1
xcopy lib\*.jar "%INSTALL_DIR%\lib\" /Y /Q >nul 2>&1
copy /Y "README.md" "%INSTALL_DIR%\README.md" >nul 2>&1
copy /Y "app_icon.png" "%INSTALL_DIR%\app_icon.png" >nul 2>&1

REM Create Start Menu folder
set STARTMENU=%APPDATA%\Microsoft\Windows\Start Menu\Programs\Doable
if not exist "%STARTMENU%" mkdir "%STARTMENU%"

REM Copy the standalone run script
echo Creating launcher...
copy /Y "run-doable.bat" "%STARTMENU%\Run Doable.bat" >nul 2>&1

echo.
echo ======================================
echo   Installation Complete!
echo ======================================
echo.
echo Application installed to: %INSTALL_DIR%
echo Launcher created in Start Menu
echo.

REM Ask to run
set /p runNow="Run Doable now? (Y/N): "
if /i "!runNow!"=="Y" (
    start "" "%STARTMENU%\Run Doable.bat"
)

echo.
pause
