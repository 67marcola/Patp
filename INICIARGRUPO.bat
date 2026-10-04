@echo off
title Sistema de Gerenciamento - Grupo

echo ==========================================
echo      SISTEMA DE GERENCIAMENTO
echo ==========================================
echo.

echo Verificando Node.js...

where node >nul 2>nul

if %errorlevel% neq 0 (
    echo.
    echo Node.js nao encontrado.
    echo Instalando Node.js LTS...
    echo.

    winget install OpenJS.NodeJS.LTS --accept-source-agreements --accept-package-agreements

    echo.
    echo Node.js instalado.
    echo.
)

echo Verificando dependencias do Frontend...

if not exist "%~dp0frontend\node_modules" (
    echo.
    echo Dependencias nao encontradas.
    echo Instalando dependencias do React...
    echo.

    cd /d "%~dp0frontend"
    npm.cmd install

    if %errorlevel% neq 0 (
        echo.
        echo ERRO ao instalar as dependencias.
        echo.
        pause
        exit /b
    )
)

echo.
echo Iniciando Backend...
start "BACKEND - Spring Boot" cmd /k "cd /d %~dp0sistema && mvnw.cmd spring-boot:run"

echo.
echo Aguardando o Backend...
timeout /t 8 /nobreak >nul

echo.
echo Iniciando Frontend...
start "FRONTEND - React" cmd /k "cd /d %~dp0frontend && npm.cmd run dev"

echo.
echo ==========================================
echo       SISTEMA INICIADO COM SUCESSO
echo ==========================================
echo.
echo Frontend: http://localhost:5173
echo Backend:  http://localhost:8081
echo.

pause