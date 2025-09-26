package com.moon.cloud.appstore.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * 数据库配置类
 * 使用环境变量配置数据库连接
 *
 * @author Moon Cloud
 * @since 2024-09-26
 */
@Slf4j
@Configuration
@EnableTransactionManagement
public class DatabaseConfig {

    private final Dotenv dotenv;

    public DatabaseConfig() {
        this.dotenv = Dotenv.configure()
                .directory(".")
                .ignoreIfMissing()
                .load();
    }

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();

        // 数据库连接配置
        String jdbcUrl = String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true",
                getEnvValue("DB_HOST", "localhost"),
                getEnvValue("DB_PORT", "3306"),
                getEnvValue("DB_NAME", "moon_appstore")
        );

        config.setJdbcUrl(jdbcUrl);
        config.setUsername(getEnvValue("DB_USERNAME", "root"));
        config.setPassword(getEnvValue("DB_PASSWORD", "root123456"));
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // HikariCP 连接池配置
        config.setMaximumPoolSize(Integer.parseInt(getEnvValue("DB_MAX_POOL_SIZE", "10")));
        config.setMinimumIdle(Integer.parseInt(getEnvValue("DB_MIN_IDLE", "5")));
        config.setConnectionTimeout(Long.parseLong(getEnvValue("DB_CONNECTION_TIMEOUT", "30000")));
        config.setIdleTimeout(Long.parseLong(getEnvValue("DB_IDLE_TIMEOUT", "600000")));
        config.setMaxLifetime(Long.parseLong(getEnvValue("DB_MAX_LIFETIME", "1800000")));

        // 性能优化配置
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        log.info("数据库连接配置完成: {}", jdbcUrl);
        log.info("连接池配置: 最大连接数={}, 最小空闲连接={}",
                config.getMaximumPoolSize(), config.getMinimumIdle());

        return new HikariDataSource(config);
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    private String getEnvValue(String key, String defaultValue) {
        // 优先从系统环境变量获取
        String value = System.getenv(key);
        if (value != null && !value.isEmpty()) {
            return value;
        }

        // 从.env文件获取
        value = dotenv.get(key);
        if (value != null && !value.isEmpty()) {
            return value;
        }

        // 返回默认值
        return defaultValue;
    }
}