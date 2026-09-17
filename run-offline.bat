@echo off
rem =====================================================================
rem  eGovFrame 3.8 sample - OFFLINE run on embedded Jetty (Windows)
rem  http://localhost:8080/   (Ctrl+C to stop)
rem  For Oracle:  run-offline.bat -Dspring.profiles.active=oracle
rem =====================================================================
setlocal
set "HERE=%~dp0"
set "MVN=%HERE%tools\apache-maven-3.9.16\bin\mvn.cmd"

if "%JAVA_HOME%"=="" (
  echo [ERROR] JAVA_HOME is not set. Point it at a JDK 1.8, e.g.:
  echo     set "JAVA_HOME=C:\Program Files\Zulu\zulu-8"
  exit /b 1
)
echo JAVA_HOME=%JAVA_HOME%
echo Starting Jetty on http://localhost:8080/  (Ctrl+C to stop)
call "%MVN%" -o -Dmaven.repo.local="%HERE%m2-repo" -f "%HERE%egovframe-sample\pom.xml" jetty:run %*
endlocal
