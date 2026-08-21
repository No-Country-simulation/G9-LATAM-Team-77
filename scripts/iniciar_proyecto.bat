@echo off
title FinanceAI - Iniciador del Proyecto
color 0B

:: Configuracion de Java Home
set JAVA_HOME=C:\Users\ferna\.jdks\ms-17.0.20.1
set PATH=%JAVA_HOME%\bin;%PATH%

echo =======================================================
echo     Iniciando Proyecto FinanceAI - Team 77
echo =======================================================
echo.

echo Cargando variables desde el archivo maestro .env...
for /f "usebackq tokens=1,* delims==" %%a in ("%~dp0..\.env") do (
    :: Ignoramos las lineas que empiezan con #
    echo %%a | findstr /b /c:"#" >nul || (
        set "%%a=%%~b"
    )
)

echo [1/2] Levantando el Backend (Spring Boot)...
start "Backend FinanceAI (Puerto 8080)" cmd /k "cd /d %~dp0..\Backend\financeia-backend && .\mvnw spring-boot:run"

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
