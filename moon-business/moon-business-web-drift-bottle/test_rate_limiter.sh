#!/bin/bash

# 测试限流器脚本
echo "开始测试限流器功能..."
echo "配置：每10秒允许1个请求"
echo "测试方法：快速发送5个请求"
echo ""

# 服务器地址
BASE_URL="http://localhost:8083/drift-bottle"

# 测试投放漂流瓶的限流器
echo "=== 测试投放漂流瓶限流器 ==="
for i in {1..5}; do
    echo "第${i}次请求 - $(date '+%H:%M:%S')"
    
    # 发送POST请求投放漂流瓶
    response=$(curl -s -w "HTTP_STATUS:%{http_code}" \
        -X POST \
        -H "Content-Type: application/json" \
        -d '{
            "content": "测试限流器的漂流瓶内容 '${i}'",
            "senderUsername": "testuser'${i}'"
        }' \
        "${BASE_URL}/throw")
    
    # 提取HTTP状态码和响应体
    http_status=$(echo "$response" | grep -o "HTTP_STATUS:[0-9]*" | cut -d: -f2)
    response_body=$(echo "$response" | sed 's/HTTP_STATUS:[0-9]*$//')
    
    echo "状态码: $http_status"
    echo "响应: $response_body"
    echo "---"
    
    # 间隔1秒
    sleep 1
done

echo ""
echo "=== 测试捡漂流瓶限流器 ==="
for i in {1..5}; do
    echo "第${i}次请求 - $(date '+%H:%M:%S')"
    
    # 发送POST请求捡漂流瓶
    response=$(curl -s -w "HTTP_STATUS:%{http_code}" \
        -X POST \
        -d "username=testuser${i}" \
        "${BASE_URL}/pickup")
    
    # 提取HTTP状态码和响应体
    http_status=$(echo "$response" | grep -o "HTTP_STATUS:[0-9]*" | cut -d: -f2)
    response_body=$(echo "$response" | sed 's/HTTP_STATUS:[0-9]*$//')
    
    echo "状态码: $http_status"
    echo "响应: $response_body"
    echo "---"
    
    # 间隔1秒
    sleep 1
done

echo ""
echo "测试完成！"
echo "如果限流器生效，应该看到："
echo "1. 第1个请求成功（状态码200）"
echo "2. 后续请求被限流（可能返回429状态码或fallback响应）"
echo "3. 等待10秒后的请求应该能够成功"