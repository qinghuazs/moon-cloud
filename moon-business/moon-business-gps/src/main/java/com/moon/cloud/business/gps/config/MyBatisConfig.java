package com.moon.cloud.business.gps.config;

import com.moon.cloud.business.gps.plugin.GpsDataPartitionInterceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * MyBatis配置类
 * 用于注册GPS数据分表拦截器
 *
 * @author moon-cloud
 * @since 2024-01-01
 */
@Configuration
public class MyBatisConfig {

    @Autowired
    private GpsDataPartitionInterceptor gpsDataPartitionInterceptor;

    /**
     * 配置SqlSessionFactory，添加GPS数据分表拦截器
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        
        // 设置Mapper XML文件位置
        sessionFactory.setMapperLocations(
            new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/**/*.xml")
        );
        
        // 添加GPS数据分表拦截器
        sessionFactory.setPlugins(gpsDataPartitionInterceptor);
        
        // 设置类型别名包
        sessionFactory.setTypeAliasesPackage("com.moon.cloud.business.gps.entity");
        
        return sessionFactory.getObject();
    }
}