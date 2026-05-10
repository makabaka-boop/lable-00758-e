#!/bin/bash

set -e

echo "======================================"
echo "  短视频系统自动化测试执行脚本"
echo "======================================"
echo ""

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "清理之前的测试容器和卷..."
docker compose -f docker-compose.test.yml down -v 2>/dev/null || true

echo ""
echo "======================================"
echo "  1. 运行后端测试"
echo "======================================"
echo ""

docker compose -f docker-compose.test.yml up --build --abort-on-container-exit backend-test
BACKEND_EXIT_CODE=$?

if [ $BACKEND_EXIT_CODE -eq 0 ]; then
    echo "✅ 后端测试通过"
else
    echo "❌ 后端测试失败 (退出码: $BACKEND_EXIT_CODE)"
fi

echo ""
echo "======================================"
echo "  2. 运行前端测试"
echo "======================================"
echo ""

docker compose -f docker-compose.test.yml up --build --abort-on-container-exit frontend-test
FRONTEND_EXIT_CODE=$?

if [ $FRONTEND_EXIT_CODE -eq 0 ]; then
    echo "✅ 前端测试通过"
else
    echo "❌ 前端测试失败 (退出码: $FRONTEND_EXIT_CODE)"
fi

echo ""
echo "======================================"
echo "  测试结果汇总"
echo "======================================"
echo ""

if [ $BACKEND_EXIT_CODE -eq 0 ] && [ $FRONTEND_EXIT_CODE -eq 0 ]; then
    echo "🎉 所有测试通过！"
    EXIT_CODE=0
else
    echo "⚠️  部分测试失败"
    echo "   后端: $( [ $BACKEND_EXIT_CODE -eq 0 ] && echo '✅ 通过' || echo '❌ 失败' )"
    echo "   前端: $( [ $FRONTEND_EXIT_CODE -eq 0 ] && echo '✅ 通过' || echo '❌ 失败' )"
    EXIT_CODE=1
fi

echo ""
echo "清理测试容器..."
docker compose -f docker-compose.test.yml down -v

echo ""
echo "测试完成！"
exit $EXIT_CODE
