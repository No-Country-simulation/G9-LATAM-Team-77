@echo off
title FinanceAI - Iniciador del Proyecto
color 0B

echo =======================================================
echo     Iniciando Proyecto FinanceAI - Team 77
echo =======================================================
echo.

:: 1. Cargar variables desde el archivo maestro .env si existe
if exist "%~dp0..\.env" (
    echo Cargando variables desde el archivo maestro .env...
    for /f "usebackq tokens=1,* delims==" %%a in ("%~dp0..\.env") do (
        echo %%a | findstr /b /c:"#" >nul || (
            set "%%a=%%~b"
        )
    )
)

:: 2. Deteccion automatica y portable de JAVA_HOME para cualquier computadora
if not defined JAVA_HOME (
    for /d %%i in (%USERPROFILE%\.jdks\*17* %USERPROFILE%\.jdks\* "C:\Program Files\Java\jdk-17*" "C:\Program Files\Java\jdk*" "C:\Program Files\Eclipse Adoptium\jdk-17*" "C:\Program Files\Eclipse Adoptium\jdk*" "C:\Program Files\Microsoft\jdk-17*" "C:\Program Files\Microsoft\jdk*" "C:\Program Files\Amazon Corretto\jdk-17*" "C:\Program Files\Amazon Corretto\jdk*") do (
        if not defined JAVA_HOME (
            if exist "%%~i\bin\java.exe" set "JAVA_HOME=%%~i"
        )
    )
)

if defined JAVA_HOME (
    echo Java detectado: %JAVA_HOME%
    set "PATH=%JAVA_HOME%\bin;%PATH%"
) else (
    echo [INFO] Utilizando la configuracion predeterminada de Java en PATH...
)

echo.
echo [1/2] Levantando el Backend (Spring Boot)...
if defined JAVA_HOME (
    start "Backend FinanceAI (Puerto 8080)" cmd /k "cd /d %~dp0..\Backend\financeia-backend && set JAVA_HOME=%JAVA_HOME%&& set PATH=%JAVA_HOME%\bin;%%PATH%%&& .\mvnw.cmd spring-boot:run"
) else (
    start "Backend FinanceAI (Puerto 8080)" cmd /k "cd /d %~dp0..\Backend\financeia-backend && .\mvnw.cmd spring-boot:run"
)

:: Esperamos 5 segundos para darle ventaja al backend
timeout /t 5 /nobreak >nul

echo [2/2] Levantando el Frontend (Astro)...
start "Frontend FinanceAI (Puerto 4321)" cmd /k "cd /d %~dp0..\Frontend && pnpm dev"

echo.
echo =======================================================
echo Todo en marcha! 
echo.
echo Frontend: http://localhost:4321
echo Backend API: http://localhost:8080/api/v1
echo Data Science: (Se ejecuta automaticamente bajo demanda desde el Backend)
echo.
echo Revisa las dos nuevas consolas que se acaban de abrir.
echo Para apagar los servidores, simplemente cierra sus ventanas.
echo =======================================================
echo.
pause
