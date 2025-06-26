#!/bin/bash

# 测试最简化限流器应用脚本
echo "开始测试最简化限流器应用..."
echo "端口：8084"
echo "配置：每10秒允许1个请求"
echo ""

# 服务器地址
BASE_URL="http://localhost:8084"

# 等待服务启动
echo "等待服务启动..."
sleep 5

# 首先测试简单接口确认服务可用
echo "=== 测试简单接口（无限流） ==="
for i in {1..3}; do
    echo "第${i}次请求 - $(date '+%H:%M:%S')"
    
    response=$(curl -s -w "HTTP_STATUS:%{http_code}" \
        -X GET \
        "${BASE_URL}/test-simple")
    
    http_status=$(echo "$response" | grep -o "HTTP_STATUS:[0-9]*" | cut -d: -f2)
    response_body=$(echo "$response" | sed 's/HTTP_STATUS:[0-9]*$//')
    
    echo "状态码: $http_status"
    echo "响应: $response_body"
    echo "---"
    
    sleep 1
done

echo ""
echo "=== 测试限流器接口 ==="
for i in {1..5}; do
    echo "第${i}次请求 - $(date '+%H:%M:%S')"
    
    response=$(curl -s -w "HTTP_STATUS:%{http_code}" \
        -X GET \
        "${BASE_URL}/test-rate-limit")
    
    http_status=$(echo "$response" | grep -o "HTTP_STATUS:[0-9]*" | cut -d: -f2)
    response_body=$(echo "$response" | sed 's/HTTP_STATUS:[0-9]*$//')
    
    echo "状态码: $http_status"
    echo "响应: $response_body"
    echo "---"
    
    # 间隔1秒
    sleep 1
done

echo ""
echo "等待10秒后再次测试..."
sleep 10

echo "=== 10秒后的请求 ==="
response=$(curl -s -w "HTTP_STATUS:%{http_code}" \
    -X GET \
    "${BASE_URL}/test-rate-limit")

http_status=$(echo "$response" | grep -o "HTTP_STATUS:[0-9]*" | cut -d: -f2)
response_body=$(echo "$response" | sed 's/HTTP_STATUS:[0-9]*$//')

echo "状态码: $http_status"
echo "响应: $response_body"

echo ""
echo "测试完成！"
echo "如果限流器生效，应该看到："
echo "1. 简单接口所有请求都成功"
echo "2. 限流接口第1个请求成功"
echo "3. 限流接口后续请求被限流（触发fallback）"
echo "4. 等待10秒后的请求应该能够成功"