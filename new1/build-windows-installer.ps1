# Build script for creating Windows installer for Doable Todo List
# This script uses jpackage (bundled with JDK 16+)

Write-Host ""
Write-Host "======================================"
Write-Host "  Doable Todo List - Building Windows Installer"
Write-Host "======================================"
Write-Host ""

# Step 1: Check if jpackage is available
Write-Host "[1/3] Checking for jpackage..."
$jpackage = Get-Command jpackage -ErrorAction SilentlyContinue

if (-not $jpackage) {
    Write-Host "ERROR: jpackage not found!" -ForegroundColor Red
    Write-Host ""
    Write-Host "jpackage requires JDK 16 or later with jpackage support."
    Write-Host ""
    Write-Host "Download options:"
    Write-Host "  1. Oracle JDK: https://www.oracle.com/java/technologies/downloads/"
    Write-Host "  2. Eclipse Temurin: https://adoptium.net/"
    Write-Host "  3. Microsoft Build of OpenJDK: https://www.microsoft.com/openjdk"
    Write-Host ""
    Write-Host "Make sure the JDK bin folder is in your PATH."
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "✓ jpackage found at: $($jpackage.Source)" -ForegroundColor Green
Write-Host ""

# Step 2: Build JAR
Write-Host "[2/3] Building JAR file..."
mvn clean package -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Build failed!" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "✓ JAR file created" -ForegroundColor Green
Write-Host ""

# Step 3: Create Windows installer
Write-Host "[3/3] Creating Windows installer..."
Write-Host ""

# Create dist directory if it doesn't exist
if (-not (Test-Path "target\dist")) {
    New-Item -ItemType Directory -Path "target\dist" -Force | Out-Null
}

# Run jpackage
jpackage `
    --input target `
    --dest target\dist `
    --name Doable `
    --main-jar doable-todo-1.0-SNAPSHOT-shaded.jar `
    --main-class com.doable.MainApp `
    --type exe `
    --win-console false `
    --win-menu true `
    --win-shortcut true `
    --app-version 1.0 `
    --vendor "Doable" `
    --description "A simple and efficient todo list application" `
    --icon src/main/resources/app_icon.png

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "======================================"
    Write-Host "  ✓ Success! Installer created."
    Write-Host "======================================"
    Write-Host ""
    Write-Host "The installer file is located in:"
    Write-Host "  target\dist\Doable-1.0.exe"
    Write-Host ""
    Write-Host "Share this .exe file with your friends!"
    Write-Host ""
    
    # Try to open the folder
    Start-Process explorer.exe -ArgumentList "target\dist"
} else {
    Write-Host ""
    Write-Host "ERROR: Installer creation failed!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Troubleshooting:"
    Write-Host "  - Ensure JDK 16+ is installed with jpackage support"
    Write-Host "  - Check that all source files compiled successfully"
    Write-Host "  - Try running: jpackage --help"
    Write-Host ""
}

Read-Host "Press Enter to exit"
