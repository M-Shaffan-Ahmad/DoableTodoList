# How to Export Doable as a Windows Installer

## Quick Start (Easiest Method)

1. Open the project folder: `c:\Users\me\Desktop\java\new1`
2. Double-click **`build-windows-installer.bat`**
3. Wait for the build to complete (takes 2-3 minutes on first build)
4. A file explorer window will open showing your installer
5. Share the `.exe` file with your friends!

## Requirements

**To build the installer, you need:**
- **JDK 16 or later** with jpackage support
  - Download: https://www.oracle.com/java/technologies/downloads/
  - Or: https://adoptium.net/ (Eclipse Temurin)
  - Or: https://www.microsoft.com/openjdk (Microsoft Build of OpenJDK)

**For your friends to run the app, they need:**
- Windows 7 or later
- Nothing else! (JRE is bundled with the installer)

## What Gets Created

After running the build script:
- `target/dist/Doable-1.0.exe` - The Windows installer (~150 MB)
- Friends can install and use it like any regular Windows application

## Installing Yourself (For Testing)

Run `target/dist/Doable-1.0.exe` to install the app locally. It will:
- Install to `C:\Program Files\Doable\` (default, user can change)
- Create a Start Menu shortcut
- Create a Desktop shortcut  
- Be uninstallable via Add/Remove Programs

## Sharing with Friends

**Option 1: Share the Installer (Recommended)**
- Send them the `Doable-1.0.exe` file
- They run it like any other Windows installer
- Most straightforward for non-technical users

**Option 2: Share the JAR (Portable)**
- Alternative: Share `target\doable-todo-1.0-SNAPSHOT-shaded.jar`
- Friends can run it with: `java -jar doable-todo-1.0-SNAPSHOT-shaded.jar`
- Requires JRE 11+ on their computer
- Good for technical users

## Troubleshooting

### Error: "jpackage not found"
This means you have JDK 15 or earlier, which doesn't include jpackage.

**Solution:**
1. Download JDK 16+: https://adoptium.net/
2. Install it
3. Make sure it's in your PATH: `java -version` should show 16+
4. Restart your terminal and try again

### Error: "mvn: command not found"
Maven isn't installed or not in your PATH.

**Solution:**
1. Download Maven: https://maven.apache.org/download.cgi
2. Extract it to a folder (e.g., `C:\tools\apache-maven`)
3. Add to PATH:
   - Press `Win+X`, select "System"
   - Click "Advanced system settings"
   - Click "Environment Variables"
   - Add `C:\tools\apache-maven\bin` to PATH
4. Restart your terminal

### Build takes very long
- First build takes longer (needs to download dependencies)
- Subsequent builds are faster
- You can leave it running - it's normal

### Icon doesn't show in installer
- The icon should appear automatically
- If not, it's just cosmetic - the app works fine

## Application Data

Each user gets their own:
- Database: `%APPDATA%\Doable\tasks.db`
- Settings: Windows Registry (preferences, custom ringtone, etc.)
- Data is kept separate between installations

## Advanced Options

### Manual Build (Command Line)

```powershell
cd c:\Users\me\Desktop\java\new1
mvn clean package -DskipTests
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
    --description "A simple and efficient todo list application"
```

### Portable JAR (No Installation)

Just share the JAR file - friends can run directly:
```powershell
java -jar doable-todo-1.0-SNAPSHOT-shaded.jar
```

Requires JRE 11+ on recipient's system.

## File Sizes

- JAR file: ~23 MB
- Windows installer: ~150 MB (includes JRE)
- Installed app: ~500 MB

## Supported Platforms

- ✅ Windows 7 and later
- ✅ Windows 8 / 8.1
- ✅ Windows 10
- ✅ Windows 11

## Next Steps After Building

1. Test the installer on your own computer
2. Share the `.exe` file with friends via email, cloud storage, etc.
3. Friends run the installer and use the app
4. All data stays on their computer (no cloud sync)

