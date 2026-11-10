@echo off
setlocal enabledelayedexpansion

echo ================================================================================
echo   CS213 Advanced JAVA Programming - Build Script (Team 6)
echo   Project: Connected Vehicle CTI Security Operations Console (Java AWT)
echo ================================================================================

set "JAVA_HOME=C:\Program Files\Java\jdk-27.0.1.0.3"
set "JAVAC=%JAVA_HOME%\bin\javac.exe"

if not exist "%JAVAC%" (
    echo [ERROR] JDK 27 javac not found at: "%JAVAC%"
    echo Please verify that Java JDK 27 is installed at the specified path.
    exit /b 1
)

set "LIB_DIR=lib"
set "SRC_DIR=src"
set "BIN_DIR=bin"

if not exist "%BIN_DIR%" mkdir "%BIN_DIR%"

echo [*] Gathering Java source files...
powershell -NoProfile -Command "Get-ChildItem -Recurse src -Filter *.java | Resolve-Path -Relative | Set-Content sources.txt"

echo [*] Compiling Java source files using JDK 27...
echo [*] Classpath: "%LIB_DIR%\*"

"%JAVAC%" -encoding UTF-8 -cp "%LIB_DIR%\*" -d "%BIN_DIR%" @sources.txt
set "BUILD_RESULT=%ERRORLEVEL%"

if exist sources.txt del sources.txt

if %BUILD_RESULT% equ 0 (
    echo [SUCCESS] Build completed successfully. Bytecode compiled to %BIN_DIR%\
) else (
    echo [ERROR] Compilation failed with error level %BUILD_RESULT%.
    exit /b %BUILD_RESULT%
)

endlocal
