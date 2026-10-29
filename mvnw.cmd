@echo off
setlocal
if not defined JAVA_HOME (
    set "JAVA_HOME=C:\Program Files\Java\jdk-26.0.2"
)
call "%~dp0tools\apache-maven-3.9.9\bin\mvn.cmd" %*
