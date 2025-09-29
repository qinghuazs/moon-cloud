package com.moon.cloud.response.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Moon Response 配置属性
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "moon.response")
public class MoonResponseProperties {

    /**
     * 是否启用统一响应
     */
    private boolean enabled = true;

    /**
     * 是否启用全局异常处理
     */
    private boolean exceptionHandlerEnabled = true;

    /**
     * 是否在响应中包含追踪ID
     */
    private boolean includeTraceId = true;

    /**
     * 是否在响应中包含时间戳
     */
    private boolean includeTimestamp = true;

    /**
     * 是否在错误响应中包含堆栈信息（仅开发环境建议开启）
     */
    private boolean includeStackTrace = false;

    /**
     * 默认成功消息
     */
    private String defaultSuccessMessage = "操作成功";

    /**
     * 默认失败消息
     */
    private String defaultErrorMessage = "操作失败";

    /**
     * 追踪ID请求头名称
     */
    private String traceIdHeader = "X-Trace-Id";

    /**
     * 是否打印异常日志
     */
    private boolean logException = true;
}