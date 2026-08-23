@echo off
title FinanceAI - Exponer Proyecto con HTTPS Seguro
color 0A

:: Configuracion de Java Home
set JAVA_HOME=C:\Users\ferna\.jdks\ms-17.0.20.1
set PATH=%JAVA_HOME%\bin;%PATH%

echo ======================================================================
echo          FinanceAI - Exposicion Segura a Internet con HTTPS
echo ======================================================================
echo.
echo Cargando variables de entorno desde el archivo .env...
for /f "usebackq tokens=1,* delims==" %%a in ("%~dp0..\.env") do (
    echo %%a | findstr /b /c:"#" >nul || (
        set "%%a=%%~b"
    )
)

echo.
echo [1] Iniciar con Cloudflare Tunnel (Recomendado - 100%% Seguro y HTTPS)
echo [2] Iniciar con Localtunnel (HTTPS rapido)
echo [3] Iniciar solo de forma local (http://localhost:4321)
echo.
set /p OPCION="Selecciona una opcion [1, 2 o 3]: "

if "%OPCION%"=="1" goto CLOUDFLARE_TUNNEL
if "%OPCION%"=="2" goto LOCAL_TUNNEL
if "%OPCION%"=="3" goto SOLO_LOCAL
goto SALIR

:CLOUDFLARE_TUNNEL
echo.
echo Verificando ejecutable de Cloudflare...
if not exist "%~dp0cloudflared.exe" (
    echo Descargando cloudflared.exe oficial...
    curl.exe -L -o "%~dp0cloudflared.exe" "https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-windows-amd64.exe"
)

echo.
echo ======================================================================
echo  1. Levantando Backend (Spring Boot en puerto 8080)...
echo ======================================================================
start "Backend FinanceAI (8080)" cmd /k "cd /d %~dp0..\Backend\financeia-backend && .\mvnw.cmd spring-boot:run"

echo Esperando 6 segundos a que inicie el backend...
timeout /t 6 /nobreak >nul

echo.
echo ======================================================================
echo  2. Levantando Frontend (Astro en puerto 4321)...
echo ======================================================================
start "Frontend FinanceAI (4321)" cmd /k "cd /d %~dp0..\Frontend && pnpm dev"

echo Esperando 4 segundos a que inicie el frontend...
timeout /t 4 /nobreak >nul

echo.
echo ======================================================================
echo  3. Creando enlace HTTPS seguro con Cloudflare...
echo ======================================================================
start "Cloudflare HTTPS - ENLACE PUBLICO" cmd /k "%~dp0cloudflared.exe tunnel --url http://localhost:4321"

goto RESUMEN

:LOCAL_TUNNEL
echo.
echo ======================================================================
echo  1. Levantando Backend en puerto 8080...
echo ======================================================================
start "Backend FinanceAI (8080)" cmd /k "cd /d %~dp0..\Backend\financeia-backend && .\mvnw.cmd spring-boot:run"

timeout /t 6 /nobreak >nul

echo.
echo ======================================================================
echo  2. Levantando Frontend en puerto 4321...
echo ======================================================================
start "Frontend FinanceAI (4321)" cmd /k "cd /d %~dp0..\Frontend && pnpm dev"

timeout /t 4 /nobreak >nul

echo.
echo ======================================================================
echo  3. Creando enlace HTTPS seguro con Localtunnel...
echo ======================================================================
start "Localtunnel HTTPS - ENLACE PUBLICO" cmd /k "npx localtunnel --port 4321"

goto RESUMEN

:SOLO_LOCAL
call "%~dp0iniciar_proyecto.bat"
exit /b

:RESUMEN
echo.
echo ======================================================================
echo              TODO ESTA EN MARCHA Y EXPUESTO CON HTTPS
echo ======================================================================
echo.
echo  Revisa la ventana 'Cloudflare HTTPS - ENLACE PUBLICO':
echo  - Ahi veras la URL https://xxxx.trycloudflare.com
echo  - Copia esa URL y pasasela a tus companeros.
echo.
echo  El Frontend se encargara automaticamente de redirigir todas
echo  las peticiones al Backend de forma transparente y segura.
echo.
echo  Para apagar los servidores, simplemente cierra las ventanas.
echo ======================================================================
echo.
pause
:SALIR
