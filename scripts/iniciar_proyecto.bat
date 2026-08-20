@echo off
title FinanceAI - Iniciador del Proyecto
color 0B

echo =======================================================
echo     Iniciando Proyecto FinanceAI - Team 77
echo =======================================================
echo.

echo Cargando variables desde el archivo maestro .env...
for /f "usebackq tokens=1,* delims==" %%a in ("%~dp0.env") do (
    :: Ignoramos las lineas que empiezan con #
    echo %%a | findstr /b /c:"#" >nul || (
        set "%%a=%%~b"
    )
)

echo [1/2] Levantando el Backend (Spring Boot)...
start "Backend FinanceAI (Puerto 8080)" cmd /k "cd /d %~dp0Backend\financeia-backend && .\mvnw spring-boot:run"

:: Esperamos 5 segundos para darle ventaja al backend
timeout /t 5 /nobreak >nul

echo [2/2] Levantando el Frontend (Astro)...
start "Frontend FinanceAI (Puerto 4321)" cmd /k "cd /d %~dp0Frontend && pnpm dev"

echo.
echo =======================================================
echo Todo en marcha! 
echo.
echo Frontend: http://localhost:4321
echo Backend API: http://localhost:8080/api/v1
echo.
echo Revisa las dos nuevas consolas que se acaban de abrir.
echo Para apagar los servidores, simplemente cierra sus ventanas.
echo =======================================================
echo.
pause
