# Moon Cloud Kafka SASL认证配置指南

## 概述

Moon Business User 服务支持完整的Kafka SASL认证配置，包括PLAIN、SCRAM-SHA-256/512和GSSAPI(Kerberos)认证机制，以及SSL加密传输。

## 配置方式

### 1. 环境变量配置（推荐）

复制 `.env.template` 为 `.env` 文件：
```bash
cp .env.template .env
```

### 2. 系统环境变量

也可以直接设置系统环境变量，优先级高于 `.env` 文件。

## Kafka配置详解

### 基础连接配置

| 环境变量 | 描述 | 默认值 | 示例 |
|---------|------|--------|------|
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka服务器地址 | `localhost:9092` | `broker1:9092,broker2:9092,broker3:9092` |
| `KAFKA_PRODUCER_ACKS` | 确认模式 | `all` | `all`, `1`, `0` |
| `KAFKA_PRODUCER_RETRIES` | 重试次数 | `3` | `3` |
| `KAFKA_PRODUCER_BATCH_SIZE` | 批处理大小 | `16384` | `16384` |
| `KAFKA_PRODUCER_LINGER_MS` | 延迟发送时间 | `1` | `1` |
| `KAFKA_PRODUCER_BUFFER_MEMORY` | 缓冲区内存 | `33554432` | `33554432` |

### 安全协议配置

| 环境变量 | 描述 | 可选值 | 说明 |
|---------|------|--------|------|
| `KAFKA_SECURITY_PROTOCOL` | 安全协议 | `PLAINTEXT`, `SASL_PLAINTEXT`, `SASL_SSL`, `SSL` | 生产环境建议使用`SASL_SSL` |

### SASL认证配置

| 环境变量 | 描述 | 可选值 | 说明 |
|---------|------|--------|------|
| `KAFKA_SASL_MECHANISM` | SASL认证机制 | `PLAIN`, `SCRAM-SHA-256`, `SCRAM-SHA-512`, `GSSAPI` | 建议使用`SCRAM-SHA-256` |
| `KAFKA_SASL_USERNAME` | SASL用户名 | - | 必须配置（除GSSAPI外） |
| `KAFKA_SASL_PASSWORD` | SASL密码 | - | 必须配置（除GSSAPI外） |

### SSL配置（当使用SSL时）

| 环境变量 | 描述 | 说明 |
|---------|------|------|
| `KAFKA_SSL_TRUSTSTORE_LOCATION` | Truststore文件路径 | 验证服务器证书 |
| `KAFKA_SSL_TRUSTSTORE_PASSWORD` | Truststore密码 | - |
| `KAFKA_SSL_KEYSTORE_LOCATION` | Keystore文件路径 | 双向认证时需要 |
| `KAFKA_SSL_KEYSTORE_PASSWORD` | Keystore密码 | - |
| `KAFKA_SSL_KEY_PASSWORD` | Key密码 | - |

## 配置场景示例

### 1. 开发环境（无认证）

```bash
# .env文件配置
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_SECURITY_PROTOCOL=PLAINTEXT
```

### 2. 测试环境（SASL_PLAINTEXT + PLAIN）

```bash
# .env文件配置
KAFKA_BOOTSTRAP_SERVERS=test-kafka:9092
KAFKA_SECURITY_PROTOCOL=SASL_PLAINTEXT
KAFKA_SASL_MECHANISM=PLAIN
KAFKA_SASL_USERNAME=test_user
KAFKA_SASL_PASSWORD=test_password
```

### 3. 生产环境（SASL_SSL + SCRAM-SHA-256）

```bash
# .env文件配置
KAFKA_BOOTSTRAP_SERVERS=prod-kafka-1:9093,prod-kafka-2:9093,prod-kafka-3:9093
KAFKA_SECURITY_PROTOCOL=SASL_SSL
KAFKA_SASL_MECHANISM=SCRAM-SHA-256
KAFKA_SASL_USERNAME=prod_user
KAFKA_SASL_PASSWORD=secure_password_here

# SSL配置
KAFKA_SSL_TRUSTSTORE_LOCATION=/opt/kafka/ssl/kafka.client.truststore.jks
KAFKA_SSL_TRUSTSTORE_PASSWORD=truststore_password
```

### 4. 企业环境（SASL_SSL + GSSAPI/Kerberos）

```bash
# .env文件配置
KAFKA_BOOTSTRAP_SERVERS=enterprise-kafka:9093
KAFKA_SECURITY_PROTOCOL=SASL_SSL
KAFKA_SASL_MECHANISM=GSSAPI

# SSL配置
KAFKA_SSL_TRUSTSTORE_LOCATION=/opt/kafka/ssl/kafka.client.truststore.jks
KAFKA_SSL_TRUSTSTORE_PASSWORD=truststore_password

# Kerberos配置通过系统属性设置
# -Djava.security.krb5.conf=/opt/kafka/krb5.conf
# -Djava.security.auth.login.config=/opt/kafka/kafka_client_jaas.conf
```

### 5. 阿里云Kafka（SASL_SSL + PLAIN）

```bash
# .env文件配置
KAFKA_BOOTSTRAP_SERVERS=alikafka-post-cn-xxx.alicloudmq.com:9093
KAFKA_SECURITY_PROTOCOL=SASL_SSL
KAFKA_SASL_MECHANISM=PLAIN
KAFKA_SASL_USERNAME=your_instance_id#your_username
KAFKA_SASL_PASSWORD=your_password
```

### 6. 腾讯云CKafka（SASL_PLAINTEXT + PLAIN）

```bash
# .env文件配置
KAFKA_BOOTSTRAP_SERVERS=ckafka-xxx.tencentcloudmq.com:9092
KAFKA_SECURITY_PROTOCOL=SASL_PLAINTEXT
KAFKA_SASL_MECHANISM=PLAIN
KAFKA_SASL_USERNAME=your_instance_id#your_username
KAFKA_SASL_PASSWORD=your_password
```

## 启动命令

### 开发环境
```bash
# 确保.env文件存在并配置正确
mvn spring-boot:run
```

### 生产环境
```bash
# 方式1：使用.env文件
java -jar moon-business-user.jar

# 方式2：直接使用环境变量
KAFKA_BOOTSTRAP_SERVERS=prod-kafka:9093 \
KAFKA_SECURITY_PROTOCOL=SASL_SSL \
KAFKA_SASL_USERNAME=prod_user \
KAFKA_SASL_PASSWORD=secure_password \
java -jar moon-business-user.jar
```

### Docker运行
```bash
# Dockerfile
FROM openjdk:21-jre-slim
COPY target/moon-business-user.jar app.jar
COPY .env .env
ENTRYPOINT ["java", "-jar", "/app.jar"]

# docker-compose.yml
version: '3.8'
services:
  moon-user:
    image: moon-business-user
    environment:
      - KAFKA_BOOTSTRAP_SERVERS=kafka:9093
      - KAFKA_SECURITY_PROTOCOL=SASL_SSL
      - KAFKA_SASL_MECHANISM=SCRAM-SHA-256
      - KAFKA_SASL_USERNAME=${KAFKA_USER}
      - KAFKA_SASL_PASSWORD=${KAFKA_PASSWORD}
    volumes:
      - ./ssl:/opt/kafka/ssl:ro
```

## 配置验证

启动应用时会在日志中看到Kafka配置加载信息：

```log
2024-01-01 10:00:00.000 [main] INFO  c.m.c.u.config.KafkaConfig - 启用Kafka安全协议: SASL_SSL
2024-01-01 10:00:00.001 [main] INFO  c.m.c.u.config.KafkaConfig - 配置SASL认证机制: SCRAM-SHA-256
2024-01-01 10:00:00.002 [main] INFO  c.m.c.u.config.KafkaConfig - 配置SCRAM认证: username=prod_user, mechanism=SCRAM-SHA-256
2024-01-01 10:00:00.003 [main] INFO  c.m.c.u.config.KafkaConfig - 配置SSL truststore: /opt/kafka/ssl/kafka.client.truststore.jks
2024-01-01 10:00:00.004 [main] INFO  c.m.c.u.config.KafkaConfig - Kafka Producer配置完成: servers=prod-kafka:9093, security=SASL_SSL
```

## SSL证书配置

### 1. 获取Kafka集群的CA证书

```bash
# 从Kafka服务器获取证书
openssl s_client -connect kafka-broker:9093 -servername kafka-broker \
  -showcerts </dev/null 2>/dev/null | openssl x509 -outform PEM > kafka-ca-cert.pem
```

### 2. 创建客户端Truststore

```bash
# 创建truststore并导入CA证书
keytool -keystore kafka.client.truststore.jks \
        -alias ca-cert \
        -import \
        -file kafka-ca-cert.pem \
        -storepass your_truststore_password \
        -keypass your_truststore_password \
        -noprompt
```

### 3. 双向认证（可选）

```bash
# 生成客户端密钥对
keytool -keystore kafka.client.keystore.jks \
        -alias client \
        -validity 365 \
        -genkey \
        -keyalg RSA \
        -storepass your_keystore_password \
        -keypass your_key_password \
        -dname "CN=client"

# 导出客户端证书请求
keytool -keystore kafka.client.keystore.jks \
        -alias client \
        -certreq \
        -file client-cert-request.csr \
        -storepass your_keystore_password

# 使用CA签名客户端证书（需要CA私钥）
openssl x509 -req -CA ca-cert.pem -CAkey ca-key.pem \
        -in client-cert-request.csr \
        -out client-cert-signed.pem \
        -days 365 -CAcreateserial

# 导入签名证书到keystore
keytool -keystore kafka.client.keystore.jks \
        -alias client \
        -import \
        -file client-cert-signed.pem \
        -storepass your_keystore_password
```

## 安全最佳实践

### 1. 认证机制选择

- **开发环境**: `PLAINTEXT`（仅限本地开发）
- **测试环境**: `SASL_PLAINTEXT` + `PLAIN`
- **生产环境**: `SASL_SSL` + `SCRAM-SHA-256`
- **企业环境**: `SASL_SSL` + `GSSAPI`（Kerberos）

### 2. 密码管理

- 使用强密码（至少16位，包含大小写字母、数字、特殊字符）
- 定期轮换密码
- 使用密钥管理系统（如HashiCorp Vault、AWS Secrets Manager）
- 避免在日志中记录敏感信息

### 3. 网络安全

- 使用SSL/TLS加密传输
- 配置防火墙限制访问
- 使用VPC或私有网络
- 启用网络访问控制列表

### 4. 证书管理

- 定期更新SSL证书
- 使用可信的CA机构证书
- 配置证书链验证
- 监控证书过期时间

## 故障排除

### 常见问题

1. **认证失败**
   ```log
   org.apache.kafka.common.errors.SaslAuthenticationException: Authentication failed
   ```
   - 检查用户名和密码是否正确
   - 验证SASL机制是否匹配
   - 确认Kafka服务器支持该认证机制

2. **SSL连接失败**
   ```log
   javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException
   ```
   - 检查truststore路径和密码
   - 验证服务器证书是否可信
   - 确认SSL协议版本兼容

3. **连接超时**
   ```log
   org.apache.kafka.common.errors.TimeoutException: Failed to update metadata
   ```
   - 检查bootstrap.servers配置
   - 验证网络连通性
   - 确认防火墙规则

4. **权限不足**
   ```log
   org.apache.kafka.common.errors.TopicAuthorizationException: Not authorized to access topics
   ```
   - 检查用户权限设置
   - 验证Topic ACL配置
   - 确认用户组权限

### 调试模式

开启Kafka客户端调试日志：

```yaml
# application.yml
logging:
  level:
    org.apache.kafka: DEBUG
    org.springframework.kafka: DEBUG
```

```bash
# 或使用JVM参数
java -Dorg.apache.kafka.clients.NetworkClient.level=DEBUG \
     -jar moon-business-user.jar
```

## 监控和指标

### 连接指标

应用启动后可通过Spring Boot Actuator查看Kafka连接状态：

```bash
# 健康检查
curl http://localhost:8080/api/user/actuator/health

# Kafka指标
curl http://localhost:8080/api/user/actuator/metrics/kafka.producer
```

### 自定义健康检查

```java
@Component
public class KafkaHealthIndicator implements HealthIndicator {

    @Autowired
    private KafkaTemplate<String, EmailEvent> kafkaTemplate;

    @Override
    public Health health() {
        try {
            // 测试连接
            ListenableFuture<SendResult<String, EmailEvent>> future =
                kafkaTemplate.send("health-check", new EmailEvent());
            future.get(5, TimeUnit.SECONDS);

            return Health.up()
                .withDetail("kafka", "Connected")
                .build();
        } catch (Exception e) {
            return Health.down(e)
                .withDetail("kafka", "Connection failed")
                .build();
        }
    }
}
```

这份配置指南提供了完整的Kafka SASL认证配置方案，支持各种生产环境的安全要求。