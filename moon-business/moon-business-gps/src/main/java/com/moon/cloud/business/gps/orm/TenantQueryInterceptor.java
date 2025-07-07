package com.moon.cloud.business.gps.orm;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.regex.Pattern;

/**
 * 租户查询拦截器
 * 自动为查询语句添加租户ID和软删除标识
 *
 * @author moon-cloud
 * @since 2024-01-01
 */
@Slf4j
@Component
@Intercepts({
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class TenantQueryInterceptor implements Interceptor {

    private static final String TENANT_ID_COLUMN = "tenant_id";
    private static final String DELETE_FLAG_COLUMN = "dr";
    private static final String CURRENT_TENANT_ID = "getCurrentTenantId()";
    
    // 匹配WHERE子句的正则表达式
    private static final Pattern WHERE_PATTERN = Pattern.compile("\\bWHERE\\b", Pattern.CASE_INSENSITIVE);
    // 匹配FROM子句的正则表达式
    private static final Pattern FROM_PATTERN = Pattern.compile("\\bFROM\\s+(\\w+)", Pattern.CASE_INSENSITIVE);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs()[1];
        
        // 只处理查询语句
        if (mappedStatement.getSqlCommandType() != SqlCommandType.SELECT) {
            return invocation.proceed();
        }
        
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        String originalSql = boundSql.getSql();
        
        // 检查是否需要添加租户条件
        if (!needTenantFilter(originalSql)) {
            return invocation.proceed();
        }
        
        try {
            String newSql = addTenantAndDeleteConditions(originalSql);
            
            if (!newSql.equals(originalSql)) {
                // 创建新的MappedStatement
                MappedStatement newMappedStatement = copyMappedStatement(mappedStatement, newSql, boundSql);
                invocation.getArgs()[0] = newMappedStatement;
                
                log.debug("租户查询拦截器处理SQL: {} -> {}", originalSql.replaceAll("\\s+", " ").trim(), 
                         newSql.replaceAll("\\s+", " ").trim());
            }
        } catch (Exception e) {
            log.error("租户查询拦截器处理失败，使用原始SQL执行", e);
        }
        
        return invocation.proceed();
    }
    
    /**
     * 检查是否需要租户过滤
     */
    private boolean needTenantFilter(String sql) {
        // 如果SQL中已经包含租户ID条件，则不需要添加
        if (sql.toLowerCase().contains(TENANT_ID_COLUMN.toLowerCase())) {
            return false;
        }
        
        // 检查是否是系统表或特殊表，这些表不需要租户过滤
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
     * 添加租户和删除条件
     */
    private String addTenantAndDeleteConditions(String sql) {
        String result = sql;
        
        // 添加租户条件和软删除条件
        String tenantCondition = TENANT_ID_COLUMN + " = '" + getCurrentTenantId() + "'";
        String deleteCondition = DELETE_FLAG_COLUMN + " = 0";
        String combinedCondition = tenantCondition + " AND " + deleteCondition;
        
        if (WHERE_PATTERN.matcher(result).find()) {
            // 如果已有WHERE子句，在条件前添加
            result = WHERE_PATTERN.matcher(result).replaceFirst("WHERE " + combinedCondition + " AND ");
        } else {
            // 如果没有WHERE子句，添加WHERE子句
            // 查找ORDER BY, GROUP BY, HAVING, LIMIT等子句的位置
            String[] keywords = {"ORDER\\s+BY", "GROUP\\s+BY", "HAVING", "LIMIT", "OFFSET"};
            int insertPos = result.length();
            
            for (String keyword : keywords) {
                Pattern pattern = Pattern.compile("\\b" + keyword + "\\b", Pattern.CASE_INSENSITIVE);
                java.util.regex.Matcher matcher = pattern.matcher(result);
                if (matcher.find()) {
                    insertPos = Math.min(insertPos, matcher.start());
                }
            }
            
            result = result.substring(0, insertPos).trim() + " WHERE " + combinedCondition + " " + 
                    result.substring(insertPos);
        }
        
        return result;
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