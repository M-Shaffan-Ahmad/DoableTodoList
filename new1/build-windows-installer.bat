@echo off
REM Batch file to launch PowerShell script for building Windows installer
REM This is because PowerShell scripts have better output handling

echo Starting Windows Installer Builder...
powershell.exe -ExecutionPolicy Bypass -File "%~dp0build-windows-installer.ps1"
