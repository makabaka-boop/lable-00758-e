#!/bin/bash
set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

MODE="${1:-docker}"
BACKEND_RESULT=0
FRONTEND_RESULT=0

echo ""
echo "============================================"
echo "   短视频项目 - 自动化测试执行"
echo "   模式: $MODE"
echo "============================================"
echo ""

run_backend_local() {
    echo -e "${YELLOW}[后端] 开始执行 Spring Boot 测试...${NC}"
    echo "--------------------------------------------"
    cd backend
    mvn test -B -Dspring.profiles.active=test 2>&1 | tee /tmp/backend-test-output.log
    BACKEND_RESULT=${PIPESTATUS[0]}
    cd ..
    if [ $BACKEND_RESULT -eq 0 ]; then
        echo -e "${GREEN}[后端] ✓ 所有测试通过${NC}"
    else
        echo -e "${RED}[后端] ✗ 测试失败${NC}"
    fi
    echo ""
}

run_frontend_local() {
    echo -e "${YELLOW}[前端] 开始执行 Vue 3 测试...${NC}"
    echo "--------------------------------------------"
    cd frontend
    npm install --registry=https://registry.npmmirror.com 2>&1 | tail -5
    npm run test 2>&1 | tee /tmp/frontend-test-output.log
    FRONTEND_RESULT=${PIPESTATUS[0]}
    cd ..
    if [ $FRONTEND_RESULT -eq 0 ]; then
        echo -e "${GREEN}[前端] ✓ 所有测试通过${NC}"
    else
        echo -e "${RED}[前端] ✗ 测试失败${NC}"
    fi
    echo ""
}

run_backend_docker() {
    echo -e "${YELLOW}[后端] 在 Docker 中执行 Spring Boot 测试...${NC}"
    echo "--------------------------------------------"
    docker compose -f docker-compose.test.yml build backend-test 2>&1 | tail -5
    docker compose -f docker-compose.test.yml run --rm backend-test 2>&1 | tee /tmp/backend-test-output.log
    BACKEND_RESULT=${PIPESTATUS[0]}
    if [ $BACKEND_RESULT -eq 0 ]; then
        echo -e "${GREEN}[后端] ✓ 所有测试通过${NC}"
    else
        echo -e "${RED}[后端] ✗ 测试失败${NC}"
    fi
    docker compose -f docker-compose.test.yml rm -f backend-test 2>/dev/null
    echo ""
}

run_frontend_docker() {
    echo -e "${YELLOW}[前端] 在 Docker 中执行 Vue 3 测试...${NC}"
    echo "--------------------------------------------"
    docker compose -f docker-compose.test.yml build frontend-test 2>&1 | tail -5
    docker compose -f docker-compose.test.yml run --rm frontend-test 2>&1 | tee /tmp/frontend-test-output.log
    FRONTEND_RESULT=${PIPESTATUS[0]}
    if [ $FRONTEND_RESULT -eq 0 ]; then
        echo -e "${GREEN}[前端] ✓ 所有测试通过${NC}"
    else
        echo -e "${RED}[前端] ✗ 测试失败${NC}"
    fi
    docker compose -f docker-compose.test.yml rm -f frontend-test 2>/dev/null
    echo ""
}

print_summary() {
    echo "============================================"
    echo "   测试结果汇总"
    echo "============================================"
    if [ $BACKEND_RESULT -eq 0 ]; then
        echo -e "  后端测试:  ${GREEN}PASS${NC}"
    else
        echo -e "  后端测试:  ${RED}FAIL${NC}"
    fi
    if [ $FRONTEND_RESULT -eq 0 ]; then
        echo -e "  前端测试:  ${GREEN}PASS${NC}"
    else
        echo -e "  前端测试:  ${RED}FAIL${NC}"
    fi
    echo "============================================"

    if [ $BACKEND_RESULT -ne 0 ] || [ $FRONTEND_RESULT -ne 0 ]; then
        echo ""
        echo -e "${RED}部分测试未通过，请检查上方输出${NC}"
        exit 1
    else
        echo ""
        echo -e "${GREEN}所有测试通过！${NC}"
        exit 0
    fi
}

case "$MODE" in
    docker)
        run_backend_docker
        run_frontend_docker
        ;;
    local)
        run_backend_local
        run_frontend_local
        ;;
    backend)
        if [ "${2:-docker}" = "local" ]; then
            run_backend_local
        else
            run_backend_docker
        fi
        ;;
    frontend)
        if [ "${2:-docker}" = "local" ]; then
            run_frontend_local
        else
            run_frontend_docker
        fi
        ;;
    *)
        echo "用法: $0 {docker|local|backend [local]|frontend [local]}"
        echo ""
        echo "  docker    - 使用 Docker 执行所有测试 (默认)"
        echo "  local     - 在本地执行所有测试"
        echo "  backend   - 仅执行后端测试 (可选: local)"
        echo "  frontend  - 仅执行前端测试 (可选: local)"
        exit 1
        ;;
esac

print_summary
