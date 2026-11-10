@echo off
setlocal

set "JDK_BIN=C:\Program Files\Java\jdk-27.0.1.0.3\bin"
if not exist "%JDK_BIN%\java.exe" (
    echo [ERROR] JDK 27 java.exe not found at "%JDK_BIN%".
    exit /b 1
)

if not exist "bin" (
    echo [*] Bin directory missing. Running build.bat first...
    call build.bat
    if %ERRORLEVEL% neq 0 exit /b %ERRORLEVEL%
)

echo [*] Launching Connected Vehicle CTI Console (Java AWT - Full Dataset)...
echo [*] Initial credentials: admin / admin123
"%JDK_BIN%\java.exe" --enable-native-access=ALL-UNNAMED -cp "bin;lib\*" Main

exit /b %ERRORLEVEL%

