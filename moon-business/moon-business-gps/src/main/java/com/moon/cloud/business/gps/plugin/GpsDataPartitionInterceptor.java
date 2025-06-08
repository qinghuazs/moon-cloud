package com.moon.cloud.business.gps.plugin;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GPS数据分表拦截器
 * 根据gps_time字段自动将数据路由到对应日期的分表
 *
 * @author moon-cloud
 * @since 2024-01-01
 */
@Slf4j
@Component
@Intercepts({
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class GpsDataPartitionInterceptor implements Interceptor {

    private static final String GPS_DATA_TABLE = "gps_data";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Pattern GPS_DATA_PATTERN = Pattern.compile("\\b" + GPS_DATA_TABLE + "\\b", Pattern.CASE_INSENSITIVE);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs()[1];
        
        // 获取原始SQL
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        String originalSql = boundSql.getSql();
        
        // 检查是否需要进行分表处理
        if (!needPartition(originalSql)) {
            return invocation.proceed();
        }
        
        try {
            // 获取GPS时间
            LocalDateTime gpsTime = extractGpsTime(parameter, mappedStatement.getSqlCommandType());
            if (gpsTime == null) {
                log.warn("无法获取GPS时间，使用原始SQL执行: {}", originalSql);
                return invocation.proceed();
            }
            
            // 生成分表名
            String partitionTableName = generatePartitionTableName(gpsTime);
            
            // 替换SQL中的表名
            String newSql = replaceTableName(originalSql, partitionTableName);
            
            // 创建新的MappedStatement
            MappedStatement newMappedStatement = copyMappedStatement(mappedStatement, newSql, boundSql);
            invocation.getArgs()[0] = newMappedStatement;
            
            log.debug("GPS数据分表路由: {} -> {}, GPS时间: {}", GPS_DATA_TABLE, partitionTableName, gpsTime);
            
        } catch (Exception e) {
            log.error("GPS数据分表处理失败，使用原始SQL执行", e);
        }
        
        return invocation.proceed();
    }
    
    /**
     * 检查是否需要进行分表处理
     */
    private boolean needPartition(String sql) {
        return GPS_DATA_PATTERN.matcher(sql).find();
    }
    
    /**
     * 从参数中提取GPS时间
     */
    private LocalDateTime extractGpsTime(Object parameter, SqlCommandType sqlCommandType) {
        if (parameter == null) {
            return null;
        }
        
        try {
            // 处理实体对象
            if (parameter.getClass().getSimpleName().contains("GpsData")) {
                return getFieldValue(parameter, "gpsTime", LocalDateTime.class);
            }
            
            // 处理Map参数
            if (parameter instanceof java.util.Map) {
                java.util.Map<?, ?> paramMap = (java.util.Map<?, ?>) parameter;
                
                // 尝试从Map中获取gpsTime
                Object gpsTimeObj = paramMap.get("gpsTime");
                if (gpsTimeObj instanceof LocalDateTime) {
                    return (LocalDateTime) gpsTimeObj;
                }
                
                // 尝试从Map中获取实体对象
                for (Object value : paramMap.values()) {
                    if (value != null && value.getClass().getSimpleName().contains("GpsData")) {
                        LocalDateTime gpsTime = getFieldValue(value, "gpsTime", LocalDateTime.class);
                        if (gpsTime != null) {
                            return gpsTime;
                        }
                    }
                }
            }
            
            // 对于查询操作，如果没有找到gpsTime，使用当前时间
            if (sqlCommandType == SqlCommandType.SELECT) {
                return LocalDateTime.now();
            }
            
        } catch (Exception e) {
            log.warn("提取GPS时间失败", e);
        }
        
        return null;
    }
    
    /**
     * 通过反射获取字段值
     */
    @SuppressWarnings("unchecked")
    private <T> T getFieldValue(Object obj, String fieldName, Class<T> fieldType) {
        try {
            Field field = findField(obj.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                Object value = field.get(obj);
                if (fieldType.isInstance(value)) {
                    return (T) value;
                }
            }
        } catch (Exception e) {
            log.debug("获取字段值失败: {}.{}", obj.getClass().getSimpleName(), fieldName);
        }
        return null;
    }
    
    /**
     * 查找字段（包括父类）
     */
    private Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null && clazz != Object.class) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }
    
    /**
     * 生成分表名
     */
    private String generatePartitionTableName(LocalDateTime gpsTime) {
        String dateSuffix = gpsTime.format(DATE_FORMATTER);
        return GPS_DATA_TABLE + "_" + dateSuffix;
    }
    
    /**
     * 替换SQL中的表名
     */
    private String replaceTableName(String sql, String newTableName) {
        return GPS_DATA_PATTERN.matcher(sql).replaceAll(newTableName);
    }
    
    /**
     * 复制MappedStatement并替换SQL
     */
    private MappedStatement copyMappedStatement(MappedStatement ms, String newSql, BoundSql boundSql) {
        MappedStatement.Builder builder = new MappedStatement.Builder(
            ms.getConfiguration(),
            ms.getId(),
            new StaticSqlSource(ms.getConfiguration(), newSql, boundSql.getParameterMappings()),
            ms.getSqlCommandType()
        );
        
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.timeout(ms.getTimeout());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        if (ms.getKeyProperties() != null && ms.getKeyProperties().length > 0) {
            builder.keyProperty(String.join(",", ms.getKeyProperties()));
        }
        builder.databaseId(ms.getDatabaseId());
        builder.lang(ms.getLang());
        builder.resultOrdered(ms.isResultOrdered());
        //builder.resultSets(Arrays.toms.getResultSets());
        builder.resultMaps(ms.getResultMaps());
        builder.resultSetType(ms.getResultSetType());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());
        builder.cache(ms.getCache());
        
        return builder.build();
    }
    
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }
    
    @Override
    public void setProperties(Properties properties) {
        // 可以通过配置文件设置插件属性
    }
    
    /**
     * 静态SQL源
     */
    private static class StaticSqlSource implements org.apache.ibatis.mapping.SqlSource {
        private final String sql;
        private final java.util.List<org.apache.ibatis.mapping.ParameterMapping> parameterMappings;
        private final org.apache.ibatis.session.Configuration configuration;
        
        public StaticSqlSource(org.apache.ibatis.session.Configuration configuration, 
                              String sql, 
                              java.util.List<org.apache.ibatis.mapping.ParameterMapping> parameterMappings) {
            this.sql = sql;
            this.parameterMappings = parameterMappings;
            this.configuration = configuration;
        }
        
        @Override
        public BoundSql getBoundSql(Object parameterObject) {
            return new BoundSql(configuration, sql, parameterMappings, parameterObject);
        }
    }
}