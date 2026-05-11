@echo off
setlocal enabledelayedexpansion

echo =========================================
echo   短视频项目自动化测试
echo =========================================
echo.

echo [1/3] 检查 Docker 环境...
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Docker 未安装，请先安装 Docker
    exit /b 1
)

docker compose version >nul 2>&1
if %errorlevel% neq 0 (
    docker-compose --version >nul 2>&1
    if %errorlevel% neq 0 (
        echo ❌ Docker Compose 未安装，请先安装 Docker Compose
        exit /b 1
    )
    set COMPOSE_CMD=docker-compose
) else (
    set COMPOSE_CMD=docker compose
)

echo ✅ Docker 环境正常
echo.

echo [2/3] 运行后端测试...
echo -----------------------------------------
%COMPOSE_CMD% run --rm backend-test
set BACKEND_EXIT_CODE=%errorlevel%

echo.
echo [3/3] 运行前端测试...
echo -----------------------------------------
%COMPOSE_CMD% run --rm frontend-test
set FRONTEND_EXIT_CODE=%errorlevel%

echo.
echo =========================================
echo   测试结果汇总
echo =========================================

if %BACKEND_EXIT_CODE% equ 0 (
    echo ✅ 后端测试: 通过
) else (
    echo ❌ 后端测试: 失败
)

if %FRONTEND_EXIT_CODE% equ 0 (
    echo ✅ 前端测试: 通过
) else (
    echo ❌ 前端测试: 失败
)

echo =========================================

if %BACKEND_EXIT_CODE% equ 0 if %FRONTEND_EXIT_CODE% equ 0 (
    echo.
    echo 🎉 所有测试通过！
    exit /b 0
) else (
    echo.
    echo ⚠️  部分测试失败，请检查上面的日志
    exit /b 1
)
