@echo off
rem =====================================================================
rem  eGovFrame 3.8 sample - OFFLINE build (Windows)
rem  Uses the bundled Maven + bundled m2-repo. No internet required.
rem  Requires only a JDK 1.8 (set JAVA_HOME).
rem =====================================================================
setlocal
set "HERE=%~dp0"
set "MVN=%HERE%tools\apache-maven-3.9.16\bin\mvn.cmd"

if "%JAVA_HOME%"=="" (
  echo [ERROR] JAVA_HOME is not set. Point it at a JDK 1.8, e.g.:
  echo     set "JAVA_HOME=C:\Program Files\Zulu\zulu-8"
  exit /b 1
)
if not exist "%JAVA_HOME%\bin\java.exe" (
  echo [ERROR] java.exe not found under JAVA_HOME=%JAVA_HOME%
  exit /b 1
)
echo JAVA_HOME=%JAVA_HOME%
"%JAVA_HOME%\bin\java.exe" -version

call "%MVN%" -o -Dmaven.repo.local="%HERE%m2-repo" -f "%HERE%egovframe-sample\pom.xml" clean package %*
if errorlevel 1 (
  echo.
  echo BUILD FAILED
  exit /b 1
)
echo.
echo DONE -^> %HERE%egovframe-sample\target\egovframe-sample.war
endlocal
