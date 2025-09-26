package com.mooncloud.shorturl.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * 数据库配置
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Slf4j
@Configuration
public class DatabaseConfig {

    @Value("${DB_HOST:localhost}")
    private String host;

    @Value("${DB_PORT:3306}")
    private String port;

    @Value("${DB_NAME:shorturl}")
    private String database;

    @Value("${DB_USERNAME:root}")
    private String username;

    @Value("${DB_PASSWORD:password}")
    private String password;

    @Value("${DB_HIKARI_MINIMUM_IDLE:5}")
    private int minimumIdle;

    @Value("${DB_HIKARI_MAXIMUM_POOL_SIZE:20}")
    private int maximumPoolSize;

    @Value("${DB_HIKARI_IDLE_TIMEOUT:30000}")
    private long idleTimeout;

    @Value("${DB_HIKARI_MAX_LIFETIME:900000}")
    private long maxLifetime;

    @Value("${DB_HIKARI_CONNECTION_TIMEOUT:30000}")
    private long connectionTimeout;

    @Bean
    @Primary
    public DataSource dataSource() {
        String url = String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true",
                                 host, port, database);

        log.info("配置数据库连接: url={}, username={}", url, username);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // HikariCP连接池配置
        config.setMinimumIdle(minimumIdle);
        config.setMaximumPoolSize(maximumPoolSize);
        config.setAutoCommit(true);
        config.setIdleTimeout(idleTimeout);
        config.setPoolName("ShortUrlHikariCP");
        config.setMaxLifetime(maxLifetime);
        config.setConnectionTimeout(connectionTimeout);

        // 连接池健康检查
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(3000);

        return new HikariDataSource(config);
    }
}