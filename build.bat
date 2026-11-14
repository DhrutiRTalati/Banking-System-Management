@echo off
setlocal enabledelayedexpansion

echo ========================================================
echo        Building Apex Digital Banking System
echo ========================================================

rem 1. Determine Java compiler
set JAVAC_CMD=javac
if exist "C:\Program Files\Java\jdk-27.0.1.0.3\bin\javac.exe" (
    set JAVAC_CMD="C:\Program Files\Java\jdk-27.0.1.0.3\bin\javac.exe"
) else if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\javac.exe" (
        set JAVAC_CMD="%JAVA_HOME%\bin\javac.exe"
    )
)

rem 2. Ensure directories exist
if not exist "bin" mkdir bin
if not exist "data" mkdir data
if not exist "logs" mkdir logs
if not exist "exports" mkdir exports

rem 3. Build source file list
dir /s /b src\*.java > sources.txt

rem 4. Set Classpath
set CP=lib\sqlite-jdbc.jar;lib\mysql-connector-j-8.3.0.jar

rem 5. Compile
echo Compiling source files using %JAVAC_CMD%...
%JAVAC_CMD% -encoding UTF-8 -cp "%CP%" -d bin @sources.txt

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Compilation completed successfully into bin\ directory!
) else (
    echo [ERROR] Compilation failed with error code %ERRORLEVEL%.
    exit /b %ERRORLEVEL%
)

echo ========================================================
