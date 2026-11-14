@echo off
setlocal enabledelayedexpansion

echo ========================================================
echo     Starting Apex Digital Banking System (CLI Mode)
echo ========================================================

rem Determine Java Runtime
set JAVA_CMD=java
if exist "C:\Program Files\Java\jdk-27.0.1.0.3\bin\java.exe" (
    set JAVA_CMD="C:\Program Files\Java\jdk-27.0.1.0.3\bin\java.exe"
) else if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set JAVA_CMD="%JAVA_HOME%\bin\java.exe"
    )
)

rem Build if bin directory does not exist
if not exist "bin\com\banking\BankingCLI.class" (
    echo Binaries not found. Triggering automated build first...
    call build.bat
    if %ERRORLEVEL% NEQ 0 (
        echo Build failed. Exiting.
        pause
        exit /b %ERRORLEVEL%
    )
)

rem Classpath
set CP=bin;lib\sqlite-jdbc.jar;lib\mysql-connector-j-8.3.0.jar

%JAVA_CMD% -cp "%CP%" com.banking.BankingCLI
