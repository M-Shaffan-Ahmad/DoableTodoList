@echo off
REM Doable Todo List - Installation Script
REM Simple, reliable installer that works with standard Java

setlocal enabledelayedexpansion
cd /d "%~dp0"

title Doable Todo List - Setup

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

echo - Java found!
echo.

REM Setup installation directory
set INSTALL_DIR=%APPDATA%\Doable
echo Installing to: !INSTALL_DIR!

if not exist "!INSTALL_DIR!" mkdir "!INSTALL_DIR!"

REM Copy files
echo Copying files...
copy /Y "Doable.jar" "!INSTALL_DIR!\Doable.jar" >nul 2>&1
copy /Y "README.md" "!INSTALL_DIR!\README.md" >nul 2>&1
copy /Y "app_icon.png" "!INSTALL_DIR!\app_icon.png" >nul 2>&1

REM Create Start Menu folder
set STARTMENU=%APPDATA%\Microsoft\Windows\Start Menu\Programs\Doable
if not exist "!STARTMENU!" mkdir "!STARTMENU!"

REM Create launcher script
(
echo @echo off
echo title Doable Todo List
echo cd /d "!INSTALL_DIR!"
echo javaw -jar "Doable.jar"
echo if errorlevel 1 java -jar "Doable.jar"
) > "!STARTMENU!\Run Doable.bat"

REM Create shortcut VBS script for desktop (if wanted)
(
echo Set objWS = CreateObject("WScript.Shell"^)
echo strDesktop = objWS.SpecialFolders("Desktop"^)
echo Set objLink = objWS.CreateShortCut(strDesktop ^& "\Doable.lnk"^)
echo objLink.TargetPath = "javaw.exe"
echo objLink.Arguments = "-jar !INSTALL_DIR!\Doable.jar"
echo objLink.WorkingDirectory = "!INSTALL_DIR!"
echo objLink.IconLocation = "!INSTALL_DIR!\app_icon.png"
echo objLink.Save
) > "!STARTMENU!\Create Desktop Shortcut.vbs"

echo.
echo ======================================
echo   Installation Complete!
echo ======================================
echo.
echo Application installed to:
echo   !INSTALL_DIR!
echo.
echo Start Menu entry created. You can now run Doable from:
echo   - Windows Start Menu ^> Doable ^> Run Doable
echo   - Or execute: javaw -jar !INSTALL_DIR!\Doable.jar
echo.

REM Ask to run
set /p runNow="Run Doable now? (Y/N): "
if /i "!runNow!"=="Y" (
    javaw -jar "!INSTALL_DIR!\Doable.jar"
    exit /b 0
)

echo.
pause
