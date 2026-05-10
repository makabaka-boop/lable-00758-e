#!/bin/bash

set -e

echo "======================================"
echo "  后端单元测试执行脚本"
echo "======================================"
echo ""

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "清理之前的测试容器..."
docker compose -f docker-compose.test.yml down -v 2>/dev/null || true

echo ""
echo "运行后端测试..."
echo ""

docker compose -f docker-compose.test.yml up --build --abort-on-container-exit backend-test
EXIT_CODE=$?

echo ""
echo "======================================"
echo "  后端测试结果"
echo "======================================"
echo ""

if [ $EXIT_CODE -eq 0 ]; then
    echo "✅ 后端测试通过！"
else
    echo "❌ 后端测试失败 (退出码: $EXIT_CODE)"
fi

echo ""
echo "清理测试容器..."
docker compose -f docker-compose.test.yml down -v

echo ""
echo "测试完成！"
exit $EXIT_CODE
