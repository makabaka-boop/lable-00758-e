#!/bin/bash

set -e

echo "========================================="
echo "  短视频项目自动化测试"
echo "========================================="
echo ""

echo "[1/3] 检查 Docker 环境..."
if ! command -v docker &> /dev/null; then
    echo "❌ Docker 未安装，请先安装 Docker"
    exit 1
fi

if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo "❌ Docker Compose 未安装，请先安装 Docker Compose"
    exit 1
fi

echo "✅ Docker 环境正常"
echo ""

echo "[2/3] 运行后端测试..."
echo "-----------------------------------------"
cd "$(dirname "$0")"

if command -v docker-compose &> /dev/null; then
    docker-compose run --rm backend-test
else
    docker compose run --rm backend-test
fi

BACKEND_EXIT_CODE=$?

echo ""
echo "[3/3] 运行前端测试..."
echo "-----------------------------------------"

if command -v docker-compose &> /dev/null; then
    docker-compose run --rm frontend-test
else
    docker compose run --rm frontend-test
fi

FRONTEND_EXIT_CODE=$?

echo ""
echo "========================================="
echo "  测试结果汇总"
echo "========================================="

if [ $BACKEND_EXIT_CODE -eq 0 ]; then
    echo "✅ 后端测试: 通过"
else
    echo "❌ 后端测试: 失败"
fi

if [ $FRONTEND_EXIT_CODE -eq 0 ]; then
    echo "✅ 前端测试: 通过"
else
    echo "❌ 前端测试: 失败"
fi

echo "========================================="

if [ $BACKEND_EXIT_CODE -eq 0 ] && [ $FRONTEND_EXIT_CODE -eq 0 ]; then
    echo ""
    echo "🎉 所有测试通过！"
    exit 0
else
    echo ""
    echo "⚠️  部分测试失败，请检查上面的日志"
    exit 1
fi
