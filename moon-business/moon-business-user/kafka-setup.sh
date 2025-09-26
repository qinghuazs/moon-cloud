#!/bin/bash

# Moon Cloud Kafka SASL 配置脚本
# 用于快速设置不同环境的Kafka配置

set -e

echo "🚀 Moon Cloud Kafka SASL 配置向导"
echo "=================================="

# 检查.env文件是否存在
if [ ! -f .env ]; then
    echo "📝 .env文件不存在，正在创建..."
    cp .env.template .env
    echo "✅ 已创建.env文件"
fi

echo ""
echo "请选择部署环境："
echo "1) 开发环境 (PLAINTEXT)"
echo "2) 测试环境 (SASL_PLAINTEXT + PLAIN)"
echo "3) 生产环境 (SASL_SSL + SCRAM-SHA-256)"
echo "4) 阿里云Kafka (SASL_SSL + PLAIN)"
echo "5) 腾讯云CKafka (SASL_PLAINTEXT + PLAIN)"
echo "6) 自定义配置"

read -p "请输入选项 (1-6): " choice

case $choice in
    1)
        echo "🔧 配置开发环境..."
        sed -i '' 's/KAFKA_BOOTSTRAP_SERVERS=.*/KAFKA_BOOTSTRAP_SERVERS=localhost:9092/' .env
        sed -i '' 's/KAFKA_SECURITY_PROTOCOL=.*/KAFKA_SECURITY_PROTOCOL=PLAINTEXT/' .env
        sed -i '' 's/KAFKA_SASL_USERNAME=.*/KAFKA_SASL_USERNAME=/' .env
        sed -i '' 's/KAFKA_SASL_PASSWORD=.*/KAFKA_SASL_PASSWORD=/' .env
        echo "✅ 开发环境配置完成"
        ;;
    2)
        echo "🔧 配置测试环境..."
        read -p "请输入Kafka服务器地址 [test-kafka:9092]: " kafka_servers
        kafka_servers=${kafka_servers:-test-kafka:9092}
        read -p "请输入SASL用户名: " sasl_username
        read -s -p "请输入SASL密码: " sasl_password
        echo ""

        sed -i '' "s/KAFKA_BOOTSTRAP_SERVERS=.*/KAFKA_BOOTSTRAP_SERVERS=$kafka_servers/" .env
        sed -i '' 's/KAFKA_SECURITY_PROTOCOL=.*/KAFKA_SECURITY_PROTOCOL=SASL_PLAINTEXT/' .env
        sed -i '' 's/KAFKA_SASL_MECHANISM=.*/KAFKA_SASL_MECHANISM=PLAIN/' .env
        sed -i '' "s/KAFKA_SASL_USERNAME=.*/KAFKA_SASL_USERNAME=$sasl_username/" .env
        sed -i '' "s/KAFKA_SASL_PASSWORD=.*/KAFKA_SASL_PASSWORD=$sasl_password/" .env
        echo "✅ 测试环境配置完成"
        ;;
    3)
        echo "🔧 配置生产环境..."
        read -p "请输入Kafka服务器地址 [prod-kafka:9093]: " kafka_servers
        kafka_servers=${kafka_servers:-prod-kafka:9093}
        read -p "请输入SASL用户名: " sasl_username
        read -s -p "请输入SASL密码: " sasl_password
        echo ""
        read -p "请输入SSL Truststore路径 [/opt/kafka/ssl/kafka.client.truststore.jks]: " truststore_path
        truststore_path=${truststore_path:-/opt/kafka/ssl/kafka.client.truststore.jks}
        read -s -p "请输入Truststore密码: " truststore_password
        echo ""

        sed -i '' "s/KAFKA_BOOTSTRAP_SERVERS=.*/KAFKA_BOOTSTRAP_SERVERS=$kafka_servers/" .env
        sed -i '' 's/KAFKA_SECURITY_PROTOCOL=.*/KAFKA_SECURITY_PROTOCOL=SASL_SSL/' .env
        sed -i '' 's/KAFKA_SASL_MECHANISM=.*/KAFKA_SASL_MECHANISM=SCRAM-SHA-256/' .env
        sed -i '' "s/KAFKA_SASL_USERNAME=.*/KAFKA_SASL_USERNAME=$sasl_username/" .env
        sed -i '' "s/KAFKA_SASL_PASSWORD=.*/KAFKA_SASL_PASSWORD=$sasl_password/" .env
        sed -i '' "s|KAFKA_SSL_TRUSTSTORE_LOCATION=.*|KAFKA_SSL_TRUSTSTORE_LOCATION=$truststore_path|" .env
        sed -i '' "s/KAFKA_SSL_TRUSTSTORE_PASSWORD=.*/KAFKA_SSL_TRUSTSTORE_PASSWORD=$truststore_password/" .env
        echo "✅ 生产环境配置完成"
        ;;
    4)
        echo "🔧 配置阿里云Kafka..."
        read -p "请输入阿里云Kafka实例地址: " kafka_servers
        read -p "请输入实例ID: " instance_id
        read -p "请输入用户名: " username
        read -s -p "请输入密码: " password
        echo ""

        sed -i '' "s/KAFKA_BOOTSTRAP_SERVERS=.*/KAFKA_BOOTSTRAP_SERVERS=$kafka_servers/" .env
        sed -i '' 's/KAFKA_SECURITY_PROTOCOL=.*/KAFKA_SECURITY_PROTOCOL=SASL_SSL/' .env
        sed -i '' 's/KAFKA_SASL_MECHANISM=.*/KAFKA_SASL_MECHANISM=PLAIN/' .env
        sed -i '' "s/KAFKA_SASL_USERNAME=.*/KAFKA_SASL_USERNAME=$instance_id#$username/" .env
        sed -i '' "s/KAFKA_SASL_PASSWORD=.*/KAFKA_SASL_PASSWORD=$password/" .env
        echo "✅ 阿里云Kafka配置完成"
        ;;
    5)
        echo "🔧 配置腾讯云CKafka..."
        read -p "请输入腾讯云CKafka实例地址: " kafka_servers
        read -p "请输入实例ID: " instance_id
        read -p "请输入用户名: " username
        read -s -p "请输入密码: " password
        echo ""

        sed -i '' "s/KAFKA_BOOTSTRAP_SERVERS=.*/KAFKA_BOOTSTRAP_SERVERS=$kafka_servers/" .env
        sed -i '' 's/KAFKA_SECURITY_PROTOCOL=.*/KAFKA_SECURITY_PROTOCOL=SASL_PLAINTEXT/' .env
        sed -i '' 's/KAFKA_SASL_MECHANISM=.*/KAFKA_SASL_MECHANISM=PLAIN/' .env
        sed -i '' "s/KAFKA_SASL_USERNAME=.*/KAFKA_SASL_USERNAME=$instance_id#$username/" .env
        sed -i '' "s/KAFKA_SASL_PASSWORD=.*/KAFKA_SASL_PASSWORD=$password/" .env
        echo "✅ 腾讯云CKafka配置完成"
        ;;
    6)
        echo "🔧 自定义配置..."
        echo "请手动编辑 .env 文件进行配置"
        echo "配置项说明请参考 KAFKA-CONFIGURATION.md"
        ;;
    *)
        echo "❌ 无效选择"
        exit 1
        ;;
esac

echo ""
echo "🎉 Kafka配置完成！"
echo ""
echo "📋 下一步操作："
echo "1. 检查配置: cat .env | grep KAFKA"
echo "2. 测试连接: mvn spring-boot:run"
echo "3. 查看日志: tail -f logs/moon-business-user.log"
echo ""
echo "📖 详细文档: KAFKA-CONFIGURATION.md"
echo "🔐 安全提醒: 请确保 .env 文件不会提交到版本控制系统"

# 设置.env文件权限
chmod 600 .env
echo "🔒 已设置.env文件安全权限 (600)"