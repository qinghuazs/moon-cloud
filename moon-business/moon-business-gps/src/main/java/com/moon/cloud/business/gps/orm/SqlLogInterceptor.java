package com.moon.cloud.business.gps.orm;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;

/**
 * SQL日志拦截器
 * 记录执行的具体SQL语句和执行时间
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
public class SqlLogInterceptor implements Interceptor {

    private static final String SLOW_SQL_THRESHOLD_KEY = "slowSqlThreshold";
    private static final long DEFAULT_SLOW_SQL_THRESHOLD = 1000L; // 默认慢SQL阈值1秒
    
    private long slowSqlThreshold = DEFAULT_SLOW_SQL_THRESHOLD;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs()[1];
        
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        Configuration configuration = mappedStatement.getConfiguration();
        
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        
        Object result = null;
        Throwable exception = null;
        
        try {
            // 执行原方法
            result = invocation.proceed();
            return result;
        } catch (Throwable e) {
            exception = e;
            throw e;
        } finally {
            // 记录结束时间
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            
            // 记录SQL执行信息
            logSqlExecution(mappedStatement, boundSql, parameter, configuration, 
                          executionTime, result, exception);
        }
    }
    
    /**
     * 记录SQL执行信息
     */
    private void logSqlExecution(MappedStatement mappedStatement, BoundSql boundSql, 
                                Object parameter, Configuration configuration,
                                long executionTime, Object result, Throwable exception) {
        try {
            String sqlId = mappedStatement.getId();
            String sqlType = mappedStatement.getSqlCommandType().name();
            String originalSql = boundSql.getSql();
            String completeSql = getCompleteSql(boundSql, parameter, configuration);
            
            // 构建日志信息
            StringBuilder logBuilder = new StringBuilder();
            logBuilder.append("\n=== SQL执行日志 ===");
            logBuilder.append("\nSQL ID: ").append(sqlId);
            logBuilder.append("\nSQL类型: ").append(sqlType);
            logBuilder.append("\n执行时间: ").append(executionTime).append("ms");
            logBuilder.append("\n原始SQL: ").append(formatSql(originalSql));
            logBuilder.append("\n完整SQL: ").append(formatSql(completeSql));
            
            // 记录参数信息
            if (parameter != null) {
                logBuilder.append("\n参数信息: ").append(getParameterInfo(parameter));
            }
            
            // 记录结果信息
            if (exception == null) {
                logBuilder.append("\n执行结果: 成功");
                if (result != null) {
                    if (result instanceof List) {
                        logBuilder.append("，返回记录数: ").append(((List<?>) result).size());
                    } else if (result instanceof Number) {
                        logBuilder.append("，影响行数: ").append(result);
                    }
                }
            } else {
                logBuilder.append("\n执行结果: 失败");
                logBuilder.append("\n异常信息: ").append(exception.getMessage());
            }
            
            logBuilder.append("\n===================");
            
            // 根据执行时间和结果选择日志级别
            if (exception != null) {
                log.error(logBuilder.toString(), exception);
            } else if (executionTime >= slowSqlThreshold) {
                log.warn("[慢SQL警告] {}", logBuilder.toString());
            } else {
                log.info(logBuilder.toString());
            }
            
        } catch (Exception e) {
            log.error("记录SQL执行日志失败", e);
        }
    }
    
    /**
     * 获取完整的SQL（替换参数）
     */
    private String getCompleteSql(BoundSql boundSql, Object parameterObject, Configuration configuration) {
        String sql = boundSql.getSql().replaceAll("[\\s]+", " ");
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        
        if (parameterMappings != null && !parameterMappings.isEmpty()) {
            MetaObject metaObject = parameterObject == null ? null : 
                configuration.newMetaObject(parameterObject);
            
            for (ParameterMapping parameterMapping : parameterMappings) {
                if (parameterMapping.getMode() != org.apache.ibatis.mapping.ParameterMode.OUT) {
                    Object value;
                    String propertyName = parameterMapping.getProperty();
                    
                    if (boundSql.hasAdditionalParameter(propertyName)) {
                        value = boundSql.getAdditionalParameter(propertyName);
                    } else if (parameterObject == null) {
                        value = null;
                    } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                        value = parameterObject;
                    } else {
                        value = metaObject == null ? null : metaObject.getValue(propertyName);
                    }
                    
                    sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(value)));
                }
            }
        }
        
        return sql;
    }
    
    /**
     * 获取参数值的字符串表示
     */
    private String getParameterValue(Object obj) {
        if (obj == null) {
            return "null";
        }
        
        if (obj instanceof String) {
            return "'" + obj + "'";
        }
        
        if (obj instanceof Date) {
            DateFormat formatter = DateFormat.getDateTimeInstance(
                DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA);
            return "'" + formatter.format((Date) obj) + "'";
        }
        
        return obj.toString();
    }
    
    /**
     * 格式化SQL
     */
    private String formatSql(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return sql;
        }
        
        // 简单的SQL格式化
        return sql.replaceAll("\\s+", " ")
                 .replaceAll("\\s*,\\s*", ", ")
                 .replaceAll("\\s*=\\s*", " = ")
                 .replaceAll("\\s*>\\s*", " > ")
                 .replaceAll("\\s*<\\s*", " < ")
                 .replaceAll("\\s*>=\\s*", " >= ")
                 .replaceAll("\\s*<=\\s*", " <= ")
                 .replaceAll("\\s*!=\\s*", " != ")
                 .replaceAll("\\s*<>\\s*", " <> ")
                 .trim();
    }
    
    /**
     * 获取参数信息
     */
    private String getParameterInfo(Object parameter) {
        if (parameter == null) {
            return "null";
        }
        
        try {
            // 如果是基本类型或字符串，直接返回
            if (parameter instanceof String || 
                parameter instanceof Number || 
                parameter instanceof Boolean ||
                parameter instanceof Date) {
                return parameter.toString();
            }
            
            // 如果是Map，返回Map的内容
            if (parameter instanceof java.util.Map) {
                java.util.Map<?, ?> map = (java.util.Map<?, ?>) parameter;
                StringBuilder sb = new StringBuilder();
                sb.append("{");
                boolean first = true;
                for (java.util.Map.Entry<?, ?> entry : map.entrySet()) {
                    if (!first) {
                        sb.append(", ");
                    }
                    sb.append(entry.getKey()).append("=").append(entry.getValue());
                    first = false;
                }
                sb.append("}");
                return sb.toString();
            }
            
            // 如果是实体对象，返回类名
            return parameter.getClass().getSimpleName() + "@" + 
                   Integer.toHexString(parameter.hashCode());
                   
        } catch (Exception e) {
            return "参数解析失败: " + e.getMessage();
        }
    }
    
    /**
     * 获取当前线程信息
     */
    private String getThreadInfo() {
        Thread currentThread = Thread.currentThread();
        return currentThread.getName() + "(" + currentThread.getId() + ")";
    }
    
    /**
     * 获取调用栈信息
     */
    private String getStackTrace() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        
        // 查找第一个非框架类的调用
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            if (!className.startsWith("java.") && 
                !className.startsWith("org.apache.ibatis") &&
                !className.startsWith("org.springframework") &&
                !className.startsWith("com.sun.") &&
                !className.contains("$$") &&
                !className.contains("CGLIB")) {
                return element.getClassName() + "." + element.getMethodName() + 
                       "(" + element.getFileName() + ":" + element.getLineNumber() + ")";
            }
        }
        
        return "未知调用位置";
    }
    
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }
    
    @Override
    public void setProperties(Properties properties) {
        String threshold = properties.getProperty(SLOW_SQL_THRESHOLD_KEY);
        if (threshold != null && !threshold.trim().isEmpty()) {
            try {
                this.slowSqlThreshold = Long.parseLong(threshold.trim());
                log.info("设置慢SQL阈值为: {}ms", this.slowSqlThreshold);
            } catch (NumberFormatException e) {
                log.warn("慢SQL阈值配置无效: {}, 使用默认值: {}ms", threshold, DEFAULT_SLOW_SQL_THRESHOLD);
                this.slowSqlThreshold = DEFAULT_SLOW_SQL_THRESHOLD;
            }
        }
    }
}