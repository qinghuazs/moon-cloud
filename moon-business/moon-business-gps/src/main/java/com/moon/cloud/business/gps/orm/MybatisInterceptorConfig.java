package com.moon.cloud.business.gps.orm;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * MyBatis拦截器配置类
 * 负责注册所有的自定义拦截器
 *
 * @author moon-cloud
 * @since 2024-01-01
 */
@Slf4j
@Configuration
@DependsOn("sqlSessionFactory")
public class MybatisInterceptorConfig {

    @Autowired
    private List<SqlSessionFactory> sqlSessionFactoryList;
    
    @Autowired
    private TenantQueryInterceptor tenantQueryInterceptor;
    
    @Autowired
    private TenantUpdateInterceptor tenantUpdateInterceptor;
    
    @Autowired
    private TenantInsertInterceptor tenantInsertInterceptor;
    
    @Autowired
    private SqlLogInterceptor sqlLogInterceptor;

    @PostConstruct
    public void addInterceptors() {
        log.info("开始注册MyBatis拦截器...");
        
        for (SqlSessionFactory sqlSessionFactory : sqlSessionFactoryList) {
            org.apache.ibatis.session.Configuration configuration = sqlSessionFactory.getConfiguration();
            
            // 注册拦截器（注意顺序很重要）
            // 1. 首先注册SQL日志拦截器，确保能记录所有SQL
            if (!containsInterceptor(configuration, SqlLogInterceptor.class)) {
                configuration.addInterceptor(sqlLogInterceptor);
                log.info("已注册SQL日志拦截器: {}", SqlLogInterceptor.class.getSimpleName());
            }
            
            // 2. 注册租户查询拦截器
            if (!containsInterceptor(configuration, TenantQueryInterceptor.class)) {
                configuration.addInterceptor(tenantQueryInterceptor);
                log.info("已注册租户查询拦截器: {}", TenantQueryInterceptor.class.getSimpleName());
            }
            
            // 3. 注册租户更新拦截器
            if (!containsInterceptor(configuration, TenantUpdateInterceptor.class)) {
                configuration.addInterceptor(tenantUpdateInterceptor);
                log.info("已注册租户更新拦截器: {}", TenantUpdateInterceptor.class.getSimpleName());
            }
            
            // 4. 注册租户插入拦截器
            if (!containsInterceptor(configuration, TenantInsertInterceptor.class)) {
                configuration.addInterceptor(tenantInsertInterceptor);
                log.info("已注册租户插入拦截器: {}", TenantInsertInterceptor.class.getSimpleName());
            }
        }
        
        log.info("MyBatis拦截器注册完成，共注册了{}个SqlSessionFactory", sqlSessionFactoryList.size());
    }
    
    /**
     * 检查是否已经包含指定类型的拦截器
     */
    private boolean containsInterceptor(org.apache.ibatis.session.Configuration configuration, 
                                       Class<?> interceptorClass) {
        return configuration.getInterceptors().stream()
                .anyMatch(interceptor -> interceptor.getClass().equals(interceptorClass));
    }
}