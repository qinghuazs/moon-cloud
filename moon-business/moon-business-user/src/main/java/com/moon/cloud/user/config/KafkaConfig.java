package com.moon.cloud.user.config;

import com.moon.cloud.user.event.EmailEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka配置类
 * 支持SASL认证和SSL连接
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Slf4j
@Configuration
public class KafkaConfig {

    // 基础连接配置
    @Value("${KAFKA_BOOTSTRAP_SERVERS:localhost:9093}")
    private String bootstrapServers;

    @Value("${KAFKA_PRODUCER_ACKS:all}")
    private String acks;

    @Value("${KAFKA_PRODUCER_RETRIES:3}")
    private int retries;

    @Value("${KAFKA_PRODUCER_BATCH_SIZE:16384}")
    private int batchSize;

    @Value("${KAFKA_PRODUCER_LINGER_MS:1}")
    private int lingerMs;

    @Value("${KAFKA_PRODUCER_BUFFER_MEMORY:33554432}")
    private int bufferMemory;

    // 安全认证配置
    @Value("${KAFKA_SECURITY_PROTOCOL:SASL_PLAINTEXT}")
    private String securityProtocol;

    @Value("${KAFKA_SASL_MECHANISM:PLAIN}")
    private String saslMechanism;

    @Value("${KAFKA_SASL_USERNAME:admin}")
    private String saslUsername;

    @Value("${KAFKA_SASL_PASSWORD:admin123}")
    private String saslPassword;

    // SSL配置
    @Value("${KAFKA_SSL_TRUSTSTORE_LOCATION:}")
    private String sslTruststoreLocation;

    @Value("${KAFKA_SSL_TRUSTSTORE_PASSWORD:}")
    private String sslTruststorePassword;

    @Value("${KAFKA_SSL_KEYSTORE_LOCATION:}")
    private String sslKeystoreLocation;

    @Value("${KAFKA_SSL_KEYSTORE_PASSWORD:}")
    private String sslKeystorePassword;

    @Value("${KAFKA_SSL_KEY_PASSWORD:}")
    private String sslKeyPassword;

    @Bean
    public ProducerFactory<String, EmailEvent> emailEventProducerFactory() {
        Map<String, Object> configs = new HashMap<>();

        // 基础配置
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configs.put(ProducerConfig.ACKS_CONFIG, acks);
        configs.put(ProducerConfig.RETRIES_CONFIG, retries);
        configs.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        configs.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
        configs.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);

        // JSON序列化配置
        configs.put(JsonSerializer.TYPE_MAPPINGS, "emailEvent:com.moon.cloud.user.event.EmailEvent");

        // 安全认证配置
        configureKafkaSecurity(configs);

        log.info("Kafka Producer配置完成: servers={}, security={}", bootstrapServers, securityProtocol);

        return new DefaultKafkaProducerFactory<>(configs);
    }

    /**
     * 配置Kafka安全认证
     */
    private void configureKafkaSecurity(Map<String, Object> configs) {
        // 设置安全协议
        if (StringUtils.hasText(securityProtocol) && !"PLAINTEXT".equals(securityProtocol)) {
            configs.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);
            log.info("启用Kafka安全协议: {}", securityProtocol);

            // SASL认证配置
            if (securityProtocol.contains("SASL")) {
                configureSaslAuth(configs);
            }

            // SSL配置
            if (securityProtocol.contains("SSL")) {
                configureSslConfig(configs);
            }
        }
    }

    /**
     * 配置SASL认证
     */
    private void configureSaslAuth(Map<String, Object> configs) {
        if (StringUtils.hasText(saslMechanism)) {
            configs.put(SaslConfigs.SASL_MECHANISM, saslMechanism);
            log.info("配置SASL认证机制: {}", saslMechanism);

            // 根据不同的SASL机制配置认证信息
            switch (saslMechanism.toUpperCase()) {
                case "PLAIN":
                    configurePlainAuth(configs);
                    break;
                case "SCRAM-SHA-256":
                case "SCRAM-SHA-512":
                    configureScramAuth(configs);
                    break;
                case "GSSAPI":
                    configureGssapiAuth(configs);
                    break;
                default:
                    log.warn("未知的SASL机制: {}", saslMechanism);
            }
        }
    }

    /**
     * 配置PLAIN认证
     */
    private void configurePlainAuth(Map<String, Object> configs) {
        if (StringUtils.hasText(saslUsername) && StringUtils.hasText(saslPassword)) {
            String jaasConfig = String.format(
                "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" password=\"%s\";",
                saslUsername, saslPassword
            );
            configs.put(SaslConfigs.SASL_JAAS_CONFIG, jaasConfig);
            log.info("配置PLAIN认证: username={}", saslUsername);
        } else {
            log.warn("PLAIN认证需要配置用户名和密码");
        }
    }

    /**
     * 配置SCRAM认证
     */
    private void configureScramAuth(Map<String, Object> configs) {
        if (StringUtils.hasText(saslUsername) && StringUtils.hasText(saslPassword)) {
            String jaasConfig = String.format(
                "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";",
                saslUsername, saslPassword
            );
            configs.put(SaslConfigs.SASL_JAAS_CONFIG, jaasConfig);
            log.info("配置SCRAM认证: username={}, mechanism={}", saslUsername, saslMechanism);
        } else {
            log.warn("SCRAM认证需要配置用户名和密码");
        }
    }

    /**
     * 配置GSSAPI认证 (Kerberos)
     */
    private void configureGssapiAuth(Map<String, Object> configs) {
        // Kerberos配置通常通过系统属性或krb5.conf文件配置
        // 这里可以添加额外的GSSAPI配置
        log.info("配置GSSAPI认证，请确保Kerberos环境已正确配置");
    }

    /**
     * 配置SSL
     */
    private void configureSslConfig(Map<String, Object> configs) {
        if (StringUtils.hasText(sslTruststoreLocation)) {
            configs.put("ssl.truststore.location", sslTruststoreLocation);
            log.info("配置SSL truststore: {}", sslTruststoreLocation);
        }
        if (StringUtils.hasText(sslTruststorePassword)) {
            configs.put("ssl.truststore.password", sslTruststorePassword);
        }
        if (StringUtils.hasText(sslKeystoreLocation)) {
            configs.put("ssl.keystore.location", sslKeystoreLocation);
            log.info("配置SSL keystore: {}", sslKeystoreLocation);
        }
        if (StringUtils.hasText(sslKeystorePassword)) {
            configs.put("ssl.keystore.password", sslKeystorePassword);
        }
        if (StringUtils.hasText(sslKeyPassword)) {
            configs.put("ssl.key.password", sslKeyPassword);
        }
    }

    @Bean
    public KafkaTemplate<String, EmailEvent> emailEventKafkaTemplate() {
        return new KafkaTemplate<>(emailEventProducerFactory());
    }
}