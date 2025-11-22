# Sharing Your App with Friends - Quick Guide

## The Easy Way (Windows Installer)

### Step 1: Build the Installer
1. Open `c:\Users\me\Desktop\java\new1`
2. Double-click **`build-windows-installer.bat`**
3. Wait 2-3 minutes for the build to complete
4. File explorer opens showing your installer ✓

### Step 2: Share with Friends
1. Send them the `Doable-1.0.exe` file (about 150 MB)
   - Email, Google Drive, OneDrive, etc.
2. They double-click to install
3. Done! They can start using the app

---

## Requirements

**For you (to build):**
- JDK 16+ (has jpackage)
- Just run the build script - it checks for everything

**For your friends (to use):**
- Windows 7 or later
- That's it! Everything is bundled in the .exe

---

## If You Have Issues

**"jpackage not found"**
→ Download JDK 16+ from https://adoptium.net/

**"mvn command not found"**  
→ Download Maven from https://maven.apache.org/download.cgi

**"Build takes forever"**
→ That's normal first time. Just let it run.

---

## Alternative: Share Just the JAR

If your friends prefer no installation:
1. Send them `target\doable-todo-1.0-SNAPSHOT-shaded.jar`
2. They run: `java -jar doable-todo-1.0-SNAPSHOT-shaded.jar`
3. Requires JRE 11+

---

## Installer Features

✓ Installs to Program Files  
✓ Creates Start Menu shortcut  
✓ Creates Desktop shortcut  
✓ Uninstallable via Control Panel  
✓ Includes JRE (works offline)  
✓ Each user gets separate data  

---

## Need More Details?

See: `WINDOWS_INSTALLER_GUIDE.md` in the project folder
