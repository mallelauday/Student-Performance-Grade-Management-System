@echo off
setlocal
echo ============================================================================
echo Starting Student Performance and Grade Management System...
echo ============================================================================

set "JAVA_CMD=java"
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
    )
) else (
    if exist "C:\Program Files\Java\jdk-26.0.2\bin\java.exe" (
        set "JAVA_CMD=C:\Program Files\Java\jdk-26.0.2\bin\java.exe"
    )
)

if exist "target\student-performance-system-1.0.0.jar" (
    echo Launching application from target\student-performance-system-1.0.0.jar...
    "%JAVA_CMD%" -jar "target\student-performance-system-1.0.0.jar"
) else (
    echo Target JAR not found. Building and launching with Maven Wrapper...
    call "%~dp0mvnw.cmd" compile exec:java -D"exec.mainClass=com.university.grademanagement.Main"
)

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Application exited with an error. Check details above.
    pause
)
