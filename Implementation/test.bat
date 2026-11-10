@echo off
setlocal

set "JDK_BIN=C:\Program Files\Java\jdk-27.0.1.0.3\bin"
if not exist "%JDK_BIN%\java.exe" (
    echo [ERROR] JDK 27 java.exe not found at "%JDK_BIN%".
    exit /b 1
)

if not exist "bin\TestRunner.class" (
    echo [*] TestRunner not compiled. Running build.bat first...
    call build.bat
    if %ERRORLEVEL% neq 0 exit /b %ERRORLEVEL%
)

echo ================================================================================
echo   Executing CS213 Automated Plain-Java Test Runner (Team 6 - Java AWT)
echo ================================================================================

"%JDK_BIN%\java.exe" --enable-native-access=ALL-UNNAMED -cp "bin;lib\*" TestRunner

exit /b %ERRORLEVEL%

