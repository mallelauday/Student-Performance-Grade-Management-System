@echo off
setlocal
echo ============================================================================
echo Building Student Performance and Grade Management System...
echo ============================================================================

if not defined JAVA_HOME (
    set "JAVA_HOME=C:\Program Files\Java\jdk-26.0.2"
)

call "tools\apache-maven-3.9.9\bin\mvn.cmd" clean package

echo.
if exist "target\student-performance-system-1.0.0.jar" (
    echo ============================================================================
    echo BUILD SUCCESSFUL!
    echo Executable Fat JAR generated at:
    echo   target\student-performance-system-1.0.0.jar
    echo ============================================================================
) else (
    echo Build failed. Please review log output above.
)

pause
