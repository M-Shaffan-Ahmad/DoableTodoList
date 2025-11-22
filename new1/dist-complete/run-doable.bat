@echo off
setlocal enabledelayedexpansion

REM Get the directory where this script is located
set SCRIPT_DIR=%~dp0

REM Set classpath: main JAR + all libraries
set CLASSPATH=!SCRIPT_DIR!doable-todo-1.0-SNAPSHOT.jar
for %%f in (!SCRIPT_DIR!lib\*.jar) do (
    set CLASSPATH=!CLASSPATH!;%%f
)

REM Run the application
java -cp "!CLASSPATH!" com.doable.MainApp %*
