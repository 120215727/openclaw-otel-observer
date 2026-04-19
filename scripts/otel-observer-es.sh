#!/bin/bash

# Elasticsearch 容器管理脚本
# 用于启动/停止/重启 otel-observer-es 容器
# 数据持久化到 data/data-es，日志到 data/logs

CONTAINER_NAME="otel-observer-es"
IMAGE_NAME="elasticsearch:8.11.0"
HOST_PORT=9200
CONTAINER_PORT=9200
DATA_DIR="/Users/li/codes/idea20260318/otel-observer/data/data-es"
LOGS_DIR="/Users/li/codes/idea20260318/otel-observer/data/logs-es"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 确保目录存在
mkdir -p "$DATA_DIR"
mkdir -p "$LOGS_DIR"

# 设置目录权限（ES 需要写入权限）
chmod 777 "$DATA_DIR"
chmod 777 "$LOGS_DIR"

# 检查容器是否存在
container_exists() {
    docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"
}

# 检查容器是否运行
container_running() {
    docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"
}

# 启动容器
start_container() {
    if container_running; then
        echo -e "${YELLOW}容器 ${CONTAINER_NAME} 已在运行中${NC}"
        return 0
    fi

    if container_exists; then
        echo -e "${BLUE}启动已存在的容器 ${CONTAINER_NAME}...${NC}"
        docker start "$CONTAINER_NAME"
    else
        echo -e "${BLUE}创建并启动新容器 ${CONTAINER_NAME}...${NC}"
        docker run -d \
            --name "$CONTAINER_NAME" \
            -p "${HOST_PORT}:${CONTAINER_PORT}" \
            -e "discovery.type=single-node" \
            -e "xpack.security.enabled=false" \
            -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
            -v "$DATA_DIR:/usr/share/elasticsearch/data" \
            -v "$LOGS_DIR:/usr/share/elasticsearch/logs" \
            "$IMAGE_NAME"
    fi

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ 容器 ${CONTAINER_NAME} 启动成功！${NC}"
        echo ""
        echo -e "${BLUE}访问地址: http://localhost:${HOST_PORT}${NC}"
        echo -e "${BLUE}数据目录: ${DATA_DIR}${NC}"
        echo -e "${BLUE}日志目录: ${LOGS_DIR}${NC}"
        echo ""
        echo -e "${YELLOW}等待 Elasticsearch 启动...${NC}"
        sleep 5
        echo -e "${YELLOW}检查健康状态...${NC}"
        for i in {1..30}; do
            if curl -s "http://localhost:${HOST_PORT}/_cluster/health?pretty" > /dev/null 2>&1; then
                echo -e "${GREEN}✅ Elasticsearch 已就绪！${NC}"
                curl -s "http://localhost:${HOST_PORT}/_cluster/health?pretty"
                return 0
            fi
            sleep 2
        done
        echo -e "${RED}⚠️  超时，但容器已启动，请稍后检查${NC}"
    else
        echo -e "${RED}❌ 容器启动失败${NC}"
        return 1
    fi
}

# 停止容器
stop_container() {
    if ! container_exists; then
        echo -e "${YELLOW}容器 ${CONTAINER_NAME} 不存在${NC}"
        return 0
    fi

    if ! container_running; then
        echo -e "${YELLOW}容器 ${CONTAINER_NAME} 已停止${NC}"
        return 0
    fi

    echo -e "${BLUE}停止容器 ${CONTAINER_NAME}...${NC}"
    docker stop "$CONTAINER_NAME"
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ 容器已停止${NC}"
    else
        echo -e "${RED}❌ 停止容器失败${NC}"
        return 1
    fi
}

# 重启容器
restart_container() {
    echo -e "${BLUE}重启容器 ${CONTAINER_NAME}...${NC}"
    stop_container
    sleep 2
    start_container
}

# 查看状态
status_container() {
    if ! container_exists; then
        echo -e "${RED}容器 ${CONTAINER_NAME} 不存在${NC}"
        return 1
    fi

    echo -e "${BLUE}容器状态:${NC}"
    docker ps -a --filter "name=^/${CONTAINER_NAME}$" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

    if container_running; then
        echo ""
        echo -e "${BLUE}Elasticsearch 健康状态:${NC}"
        curl -s "http://localhost:${HOST_PORT}/_cluster/health?pretty" 2>/dev/null || echo -e "${RED}无法连接到 Elasticsearch${NC}"
    fi
}

# 查看日志
logs_container() {
    if ! container_exists; then
        echo -e "${RED}容器 ${CONTAINER_NAME} 不存在${NC}"
        return 1
    fi

    echo -e "${BLUE}容器日志 (按 Ctrl+C 退出):${NC}"
    docker logs -f "$CONTAINER_NAME"
}

# 删除容器（保留数据）
remove_container() {
    if ! container_exists; then
        echo -e "${YELLOW}容器 ${CONTAINER_NAME} 不存在${NC}"
        return 0
    fi

    echo -e "${RED}警告: 此操作将删除容器（数据会保留在 ${DATA_DIR}）${NC}"
    read -p "输入 'yes' 确认删除: " CONFIRM
    if [ "$CONFIRM" != "yes" ]; then
        echo -e "${YELLOW}操作已取消${NC}"
        return 0
    fi

    stop_container
    sleep 2
    echo -e "${BLUE}删除容器 ${CONTAINER_NAME}...${NC}"
    docker rm -f "$CONTAINER_NAME"
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ 容器已删除（数据仍保留在 ${DATA_DIR}）${NC}"
    else
        echo -e "${RED}❌ 删除容器失败${NC}"
        return 1
    fi
}

# 删除容器和数据
rm_container() {
    local SKIP_CONFIRM=${1:-false}

    if container_exists; then
        if [ "$SKIP_CONFIRM" != "true" ]; then
            echo -e "${RED}警告: 此操作将删除容器和所有数据！${NC}"
            read -p "输入 'yes' 确认删除容器和数据: " CONFIRM
            if [ "$CONFIRM" != "yes" ]; then
                echo -e "${YELLOW}操作已取消${NC}"
                return 0
            fi
        fi

        echo -e "${BLUE}强制删除容器 ${CONTAINER_NAME}...${NC}"
        docker rm -f "$CONTAINER_NAME" 2>/dev/null
        echo -e "${GREEN}✅ 容器已删除${NC}"
    else
        echo -e "${YELLOW}容器 ${CONTAINER_NAME} 不存在${NC}"
    fi

    if [ -d "$DATA_DIR" ]; then
        echo -e "${BLUE}删除数据目录: ${DATA_DIR}...${NC}"
        rm -rf "$DATA_DIR"
        echo -e "${GREEN}✅ 数据目录已删除${NC}"
    fi

    if [ -d "$LOGS_DIR" ]; then
        echo -e "${BLUE}删除日志目录: ${LOGS_DIR}...${NC}"
        rm -rf "$LOGS_DIR"
        echo -e "${GREEN}✅ 日志目录已删除${NC}"
    fi

    echo -e "${GREEN}✅ 清理完成！${NC}"
}

# 重新创建（删除容器和数据，然后重新启动）
recreate_container() {
    echo -e "${RED}警告: 此操作将删除容器和所有数据，然后重新创建！${NC}"

    # 先删除（跳过二次确认）
    rm_container true
    sleep 2

    # 重新创建目录
    mkdir -p "$DATA_DIR"
    mkdir -p "$LOGS_DIR"
    chmod 777 "$DATA_DIR"
    chmod 777 "$LOGS_DIR"

    # 启动
    echo ""
    echo -e "${BLUE}重新创建并启动容器...${NC}"
    start_container
}

# 显示帮助
show_help() {
    echo -e "${BLUE}Elasticsearch 容器管理脚本${NC}"
    echo ""
    echo "用法: $0 [命令]"
    echo ""
    echo "命令:"
    echo "  start     - 启动容器"
    echo "  stop      - 停止容器"
    echo "  restart   - 重启容器"
    echo "  status    - 查看状态"
    echo "  logs      - 查看日志"
    echo "  remove    - 删除容器（保留数据）"
    echo "  rm        - 删除容器和数据"
    echo "  recreate  - 删除容器和数据，重新创建并启动"
    echo "  help      - 显示帮助"
    echo ""
    echo "示例:"
    echo "  $0 start     # 启动 ES"
    echo "  $0 stop      # 停止 ES"
    echo "  $0 rm        # 删除容器和数据"
    echo "  $0 recreate  # 重新创建（清空数据后重启）"
    echo "  $0 status    # 查看状态"
}

# 主逻辑
case "$1" in
    start)
        start_container
        ;;
    stop)
        stop_container
        ;;
    restart)
        restart_container
        ;;
    status)
        status_container
        ;;
    logs)
        logs_container
        ;;
    remove)
        remove_container
        ;;
    rm)
        rm_container
        ;;
    recreate)
        recreate_container
        ;;
    help)
        show_help
        ;;
    *)
        if [ -z "$1" ]; then
            show_help
        else
            echo -e "${RED}未知命令: $1${NC}"
            echo ""
            show_help
            exit 1
        fi
        ;;
esac
