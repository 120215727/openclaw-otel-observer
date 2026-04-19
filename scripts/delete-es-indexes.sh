#!/bin/bash

# 删除 Elasticsearch 所有索引脚本
# 使用方法: ./delete-es-indexes.sh [--confirm]

ES_URL="http://localhost:9200"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}正在连接 Elasticsearch: ${ES_URL}${NC}"

# 检查 ES 是否可用
if ! curl -s "${ES_URL}/_cluster/health" > /dev/null; then
    echo -e "${RED}错误: 无法连接到 Elasticsearch${NC}"
    exit 1
fi

# 获取所有索引
INDEXES=$(curl -s "${ES_URL}/_cat/indices?v" | tail -n +2 | awk '{print $3}')

if [ -z "$INDEXES" ]; then
    echo -e "${GREEN}没有索引需要删除${NC}"
    exit 0
fi

echo ""
echo -e "${YELLOW}找到以下索引:${NC}"
echo "$INDEXES" | awk '{print "  - " $0}'
echo ""

# 确认删除
if [ "$1" != "--confirm" ]; then
    echo -e "${RED}警告: 此操作将永久删除以上所有索引!${NC}"
    read -p "输入 'yes' 确认删除: " CONFIRM
    if [ "$CONFIRM" != "yes" ]; then
        echo -e "${YELLOW}操作已取消${NC}"
        exit 0
    fi
fi

# 删除所有索引
echo ""
echo -e "${YELLOW}正在删除索引...${NC}"

for INDEX in $INDEXES; do
    echo -n "  删除 $INDEX ... "
    RESPONSE=$(curl -s -X DELETE "${ES_URL}/${INDEX}")
    if echo "$RESPONSE" | grep -q '"acknowledged":true'; then
        echo -e "${GREEN}成功${NC}"
    else
        echo -e "${RED}失败: ${RESPONSE}${NC}"
    fi
done

echo ""
echo -e "${GREEN}完成!${NC}"
