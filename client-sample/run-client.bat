@echo off
rem =====================================================================
rem  HMAC 클라이언트 샘플 컴파일 + 실행 (Windows)
rem  사용법: run-client.bat [baseUrl] [apiKey] [secret]
rem    예)  run-client.bat http://localhost:8080 EXTSYS001 change-me-external-system-secret-0001
rem =====================================================================
setlocal
set "HERE=%~dp0"

if "%JAVA_HOME%"=="" (
  echo [ERROR] JAVA_HOME 를 JDK 1.8 로 지정하세요.  예: set "JAVA_HOME=C:\Program Files\Zulu\zulu-8"
  exit /b 1
)

if not exist "%HERE%build" mkdir "%HERE%build"
"%JAVA_HOME%\bin\javac.exe" -encoding UTF-8 -d "%HERE%build" "%HERE%src\egovframework\example\client\EgovApiClient.java"
if errorlevel 1 ( echo 컴파일 실패 & exit /b 1 )
"%JAVA_HOME%\bin\java.exe" -cp "%HERE%build" egovframework.example.client.EgovApiClient %*
endlocal
