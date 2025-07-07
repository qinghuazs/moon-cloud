package com.moon.cloud.business.gps.orm;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 租户插入拦截器
 * 自动为插入语句添加租户ID和主键ID（雪花算法）
 *
 * @author moon-cloud
 * @since 2024-01-01
 */
@Slf4j
@Component
@Intercepts({
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class TenantInsertInterceptor implements Interceptor {

    private static final String TENANT_ID_COLUMN = "tenant_id";
    private static final String ID_COLUMN = "id";
    private static final String CREATE_TIME_COLUMN = "create_time";
    private static final String UPDATE_TIME_COLUMN = "update_time";
    private static final String VERSION_COLUMN = "version";
    private static final String DELETE_FLAG_COLUMN = "dr";
    
    // 雪花算法ID生成器
    private final SnowflakeIdGenerator snowflakeIdGenerator = new SnowflakeIdGenerator(1, 1);
    
    // 匹配INSERT语句的正则表达式
    private static final Pattern INSERT_PATTERN = Pattern.compile(
        "INSERT\\s+INTO\\s+(\\w+)\\s*\\(([^)]+)\\)\\s*VALUES\\s*\\(([^)]+)\\)", 
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs()[1];
        
        // 只处理插入语句
        if (mappedStatement.getSqlCommandType() != SqlCommandType.INSERT) {
            return invocation.proceed();
        }
        
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        String originalSql = boundSql.getSql();
        
        // 检查是否需要添加租户和主键
        if (!needTenantAndIdGeneration(originalSql)) {
            return invocation.proceed();
        }
        
        try {
            String newSql = addTenantAndIdFields(originalSql, parameter);
            
            if (!newSql.equals(originalSql)) {
                // 创建新的MappedStatement
                MappedStatement newMappedStatement = copyMappedStatement(mappedStatement, newSql, boundSql);
                invocation.getArgs()[0] = newMappedStatement;
                
                log.debug("租户插入拦截器处理SQL: {} -> {}", originalSql.replaceAll("\\s+", " ").trim(), 
                         newSql.replaceAll("\\s+", " ").trim());
            }
        } catch (Exception e) {
            log.error("租户插入拦截器处理失败，使用原始SQL执行", e);
        }
        
        return invocation.proceed();
    }
    
    /**
     * 检查是否需要租户和ID生成
     */
    private boolean needTenantAndIdGeneration(String sql) {
        // 检查是否是系统表或特殊表
        String lowerSql = sql.toLowerCase();
        if (lowerSql.contains("information_schema") || 
            lowerSql.contains("performance_schema") ||
            lowerSql.contains("mysql") ||
            lowerSql.contains("sys")) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 添加租户和ID字段
     */
    private String addTenantAndIdFields(String sql, Object parameter) {
        Matcher matcher = INSERT_PATTERN.matcher(sql);
        if (!matcher.find()) {
            return sql;
        }
        
        String tableName = matcher.group(1);
        String columns = matcher.group(2);
        String values = matcher.group(3);
        
        // 解析列名和值
        String[] columnArray = columns.split(",");
        String[] valueArray = values.split(",");
        
        // 清理空格
        for (int i = 0; i < columnArray.length; i++) {
            columnArray[i] = columnArray[i].trim();
        }
        for (int i = 0; i < valueArray.length; i++) {
            valueArray[i] = valueArray[i].trim();
        }
        
        StringBuilder newColumns = new StringBuilder(columns);
        StringBuilder newValues = new StringBuilder(values);
        
        // 检查并添加主键ID
        if (!containsColumn(columnArray, ID_COLUMN)) {
            // 检查参数中是否已设置ID
            String existingId = extractIdFromParameter(parameter);
            if (existingId == null || existingId.trim().isEmpty()) {
                // 生成新的雪花ID
                String newId = String.valueOf(snowflakeIdGenerator.nextId());
                setIdToParameter(parameter, newId);
                
                newColumns.append(", ").append(ID_COLUMN);
                newValues.append(", '").append(newId).append("'");
            }
        }
        
        // 检查并添加租户ID
        if (!containsColumn(columnArray, TENANT_ID_COLUMN)) {
            newColumns.append(", ").append(TENANT_ID_COLUMN);
            newValues.append(", '").append(getCurrentTenantId()).append("'");
        }
        
        // 检查并添加创建时间
        if (!containsColumn(columnArray, CREATE_TIME_COLUMN)) {
            newColumns.append(", ").append(CREATE_TIME_COLUMN);
            newValues.append(", NOW()");
        }
        
        // 检查并添加更新时间
        if (!containsColumn(columnArray, UPDATE_TIME_COLUMN)) {
            newColumns.append(", ").append(UPDATE_TIME_COLUMN);
            newValues.append(", NOW()");
        }
        
        // 检查并添加版本号
        if (!containsColumn(columnArray, VERSION_COLUMN)) {
            newColumns.append(", ").append(VERSION_COLUMN);
            newValues.append(", 0");
        }
        
        // 检查并添加删除标识
        if (!containsColumn(columnArray, DELETE_FLAG_COLUMN)) {
            newColumns.append(", ").append(DELETE_FLAG_COLUMN);
            newValues.append(", 0");
        }
        
        return "INSERT INTO " + tableName + " (" + newColumns + ") VALUES (" + newValues + ")";
    }
    
    /**
     * 检查列是否存在
     */
    private boolean containsColumn(String[] columns, String targetColumn) {
        for (String column : columns) {
            if (column.trim().equalsIgnoreCase(targetColumn)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 从参数中提取ID
     */
    private String extractIdFromParameter(Object parameter) {
        if (parameter == null) {
            return null;
        }
        
        try {
            // 处理实体对象
            if (parameter.getClass().getPackage() != null && 
                parameter.getClass().getPackage().getName().contains("entity")) {
                Object id = getFieldValue(parameter, "id", Object.class);
                return id != null ? id.toString() : null;
            }
            
            // 处理Map参数
            if (parameter instanceof java.util.Map) {
                java.util.Map<?, ?> paramMap = (java.util.Map<?, ?>) parameter;
                
                // 尝试从Map中获取id
                Object idObj = paramMap.get("id");
                if (idObj != null) {
                    return idObj.toString();
                }
                
                // 尝试从Map中获取实体对象
                for (Object value : paramMap.values()) {
                    if (value != null && value.getClass().getPackage() != null &&
                        value.getClass().getPackage().getName().contains("entity")) {
                        Object id = getFieldValue(value, "id", Object.class);
                        if (id != null) {
                            return id.toString();
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("提取ID失败", e);
        }
        
        return null;
    }
    
    /**
     * 设置ID到参数中
     */
    private void setIdToParameter(Object parameter, String id) {
        if (parameter == null) {
            return;
        }
        
        try {
            // 处理实体对象
            if (parameter.getClass().getPackage() != null && 
                parameter.getClass().getPackage().getName().contains("entity")) {
                setFieldValue(parameter, "id", id);
                return;
            }
            
            // 处理Map参数
            if (parameter instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> paramMap = (java.util.Map<String, Object>) parameter;
                paramMap.put("id", id);
                
                // 尝试设置到Map中的实体对象
                for (Object value : paramMap.values()) {
                    if (value != null && value.getClass().getPackage() != null &&
                        value.getClass().getPackage().getName().contains("entity")) {
                        setFieldValue(value, "id", id);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("设置ID失败", e);
        }
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
                if (value != null && (fieldType == Object.class || fieldType.isInstance(value))) {
                    return (T) value;
                }
            }
        } catch (Exception e) {
            log.debug("获取字段值失败: {}.{}", obj.getClass().getSimpleName(), fieldName);
        }
        return null;
    }
    
    /**
     * 通过反射设置字段值
     */
    private void setFieldValue(Object obj, String fieldName, Object value) {
        try {
            Field field = findField(obj.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                
                // 类型转换
                if (field.getType() == String.class && value != null) {
                    field.set(obj, value.toString());
                } else if (field.getType() == Long.class && value != null) {
                    field.set(obj, Long.valueOf(value.toString()));
                } else {
                    field.set(obj, value);
                }
            }
        } catch (Exception e) {
            log.debug("设置字段值失败: {}.{}", obj.getClass().getSimpleName(), fieldName);
        }
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
     * 获取当前租户ID
     */
    private String getCurrentTenantId() {
        return TenantContext.getTenantId();
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
        if (ms.getKeyColumns() != null && ms.getKeyColumns().length > 0) {
            builder.keyColumn(String.join(",", ms.getKeyColumns()));
        }
        builder.databaseId(ms.getDatabaseId());
        builder.lang(ms.getLang());
        if (ms.getResultMaps() != null) {
            builder.resultMaps(ms.getResultMaps());
        }
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
        // 可以通过配置文件设置属性
    }
    
    /**
     * 静态SQL源
     */
    private static class StaticSqlSource implements org.apache.ibatis.mapping.SqlSource {
        private final String sql;
        private final java.util.List<org.apache.ibatis.mapping.ParameterMapping> parameterMappings;
        private final org.apache.ibatis.session.Configuration configuration;
        
        public StaticSqlSource(org.apache.ibatis.session.Configuration configuration, String sql, 
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
    
    /**
     * 雪花算法ID生成器
     */
    private static class SnowflakeIdGenerator {
        private final long workerId;
        private final long datacenterId;
        private long sequence = 0L;
        private long lastTimestamp = -1L;
        
        private static final long WORKER_ID_BITS = 5L;
        private static final long DATACENTER_ID_BITS = 5L;
        private static final long SEQUENCE_BITS = 12L;
        
        private static final long MAX_WORKER_ID = -1L ^ (-1L << WORKER_ID_BITS);
        private static final long MAX_DATACENTER_ID = -1L ^ (-1L << DATACENTER_ID_BITS);
        
        private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
        private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
        private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;
        
        private static final long SEQUENCE_MASK = -1L ^ (-1L << SEQUENCE_BITS);
        private static final long EPOCH = 1609459200000L; // 2021-01-01 00:00:00
        
        public SnowflakeIdGenerator(long workerId, long datacenterId) {
            if (workerId > MAX_WORKER_ID || workerId < 0) {
                throw new IllegalArgumentException("Worker ID can't be greater than " + MAX_WORKER_ID + " or less than 0");
            }
            if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
                throw new IllegalArgumentException("Datacenter ID can't be greater than " + MAX_DATACENTER_ID + " or less than 0");
            }
            this.workerId = workerId;
            this.datacenterId = datacenterId;
        }
        
        public synchronized long nextId() {
            long timestamp = timeGen();
            
            if (timestamp < lastTimestamp) {
                throw new RuntimeException("Clock moved backwards. Refusing to generate id for " + (lastTimestamp - timestamp) + " milliseconds");
            }
            
            if (lastTimestamp == timestamp) {
                sequence = (sequence + 1) & SEQUENCE_MASK;
                if (sequence == 0) {
                    timestamp = tilNextMillis(lastTimestamp);
                }
            } else {
                sequence = 0L;
            }
            
            lastTimestamp = timestamp;
            
            return ((timestamp - EPOCH) << TIMESTAMP_LEFT_SHIFT) |
                   (datacenterId << DATACENTER_ID_SHIFT) |
                   (workerId << WORKER_ID_SHIFT) |
                   sequence;
        }
        
        private long tilNextMillis(long lastTimestamp) {
            long timestamp = timeGen();
            while (timestamp <= lastTimestamp) {
                timestamp = timeGen();
            }
            return timestamp;
        }
        
        private long timeGen() {
            return System.currentTimeMillis();
        }
    }
}