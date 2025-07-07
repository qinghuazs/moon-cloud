package com.moon.cloud.business.gps.orm;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 租户更新拦截器
 * 自动为更新语句添加租户ID和版本号控制
 *
 * @author moon-cloud
 * @since 2024-01-01
 */
@Slf4j
@Component
@Intercepts({
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class TenantUpdateInterceptor implements Interceptor {

    private static final String TENANT_ID_COLUMN = "tenant_id";
    private static final String VERSION_COLUMN = "version";
    private static final String UPDATE_TIME_COLUMN = "update_time";
    
    // 匹配UPDATE语句的正则表达式
    private static final Pattern UPDATE_PATTERN = Pattern.compile(
        "UPDATE\\s+(\\w+)\\s+SET\\s+(.+?)\\s+WHERE\\s+(.+)", 
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    
    // 匹配SET子句的正则表达式
    private static final Pattern SET_PATTERN = Pattern.compile(
        "SET\\s+(.+?)\\s+WHERE", 
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs()[1];
        
        // 只处理更新语句
        if (mappedStatement.getSqlCommandType() != SqlCommandType.UPDATE) {
            return invocation.proceed();
        }
        
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        String originalSql = boundSql.getSql();
        
        // 检查是否需要添加租户和版本控制
        if (!needTenantAndVersionControl(originalSql)) {
            return invocation.proceed();
        }
        
        try {
            String newSql = addTenantAndVersionControl(originalSql, parameter);
            
            if (!newSql.equals(originalSql)) {
                // 创建新的MappedStatement
                MappedStatement newMappedStatement = copyMappedStatement(mappedStatement, newSql, boundSql);
                invocation.getArgs()[0] = newMappedStatement;
                
                log.debug("租户更新拦截器处理SQL: {} -> {}", originalSql.replaceAll("\\s+", " ").trim(), 
                         newSql.replaceAll("\\s+", " ").trim());
            }
        } catch (Exception e) {
            log.error("租户更新拦截器处理失败，使用原始SQL执行", e);
        }
        
        return invocation.proceed();
    }
    
    /**
     * 检查是否需要租户和版本控制
     */
    private boolean needTenantAndVersionControl(String sql) {
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
     * 添加租户和版本控制
     */
    private String addTenantAndVersionControl(String sql, Object parameter) {
        Matcher matcher = UPDATE_PATTERN.matcher(sql);
        if (!matcher.find()) {
            return sql;
        }
        
        String tableName = matcher.group(1);
        String setClause = matcher.group(2);
        String whereClause = matcher.group(3);
        
        // 构建新的SET子句
        StringBuilder newSetClause = new StringBuilder(setClause.trim());
        
        // 添加更新时间
        if (!setClause.toLowerCase().contains(UPDATE_TIME_COLUMN.toLowerCase())) {
            if (!newSetClause.toString().trim().endsWith(",")) {
                newSetClause.append(", ");
            }
            newSetClause.append(UPDATE_TIME_COLUMN).append(" = NOW()");
        }
        
        // 添加版本号递增
        if (!setClause.toLowerCase().contains(VERSION_COLUMN.toLowerCase())) {
            if (!newSetClause.toString().trim().endsWith(",")) {
                newSetClause.append(", ");
            }
            newSetClause.append(VERSION_COLUMN).append(" = ").append(VERSION_COLUMN).append(" + 1");
        }
        
        // 构建新的WHERE子句
        StringBuilder newWhereClause = new StringBuilder();
        
        // 添加租户ID条件
        if (!whereClause.toLowerCase().contains(TENANT_ID_COLUMN.toLowerCase())) {
            newWhereClause.append(TENANT_ID_COLUMN).append(" = '").append(getCurrentTenantId()).append("'");
        }
        
        // 添加版本号条件（乐观锁）
        Integer currentVersion = extractVersionFromParameter(parameter);
        if (currentVersion != null && !whereClause.toLowerCase().contains(VERSION_COLUMN.toLowerCase())) {
            if (newWhereClause.length() > 0) {
                newWhereClause.append(" AND ");
            }
            newWhereClause.append(VERSION_COLUMN).append(" = ").append(currentVersion);
        }
        
        // 组合原有WHERE条件
        if (newWhereClause.length() > 0) {
            newWhereClause.append(" AND (").append(whereClause).append(")");
        } else {
            newWhereClause.append(whereClause);
        }
        
        // 构建最终SQL
        return "UPDATE " + tableName + " SET " + newSetClause + " WHERE " + newWhereClause;
    }
    
    /**
     * 从参数中提取版本号
     */
    private Integer extractVersionFromParameter(Object parameter) {
        if (parameter == null) {
            return null;
        }
        
        try {
            // 处理实体对象
            if (parameter.getClass().getPackage() != null && 
                parameter.getClass().getPackage().getName().contains("entity")) {
                return getFieldValue(parameter, "version", Integer.class);
            }
            
            // 处理Map参数
            if (parameter instanceof java.util.Map) {
                java.util.Map<?, ?> paramMap = (java.util.Map<?, ?>) parameter;
                
                // 尝试从Map中获取version
                Object versionObj = paramMap.get("version");
                if (versionObj instanceof Integer) {
                    return (Integer) versionObj;
                }
                
                // 尝试从Map中获取实体对象
                for (Object value : paramMap.values()) {
                    if (value != null && value.getClass().getPackage() != null &&
                        value.getClass().getPackage().getName().contains("entity")) {
                        Integer version = getFieldValue(value, "version", Integer.class);
                        if (version != null) {
                            return version;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("提取版本号失败", e);
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
}