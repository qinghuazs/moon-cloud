package com.moon.cloud.email.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 邮件配置类
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Configuration
@ConfigurationProperties(prefix = "moon.email")
public class EmailConfig {

    /**
     * SMTP服务器主机
     */
    private String host = "smtp.gmail.com";

    /**
     * SMTP服务器端口
     */
    private Integer port = 587;

    /**
     * 发送方邮箱
     */
    private String username;

    /**
     * 发送方密码/授权码
     */
    private String password;

    /**
     * 发送方名称
     */
    private String fromName = "Moon Cloud";

    /**
     * 是否启用TLS
     */
    private Boolean enableTls = true;

    /**
     * 是否启用SSL
     */
    private Boolean enableSsl = false;

    /**
     * 连接超时时间(毫秒)
     */
    private Integer connectionTimeout = 60000;

    /**
     * 读取超时时间(毫秒)
     */
    private Integer readTimeout = 60000;

    /**
     * 是否启用调试模式
     */
    private Boolean debug = false;

    /**
     * 默认字符编码
     */
    private String encoding = "UTF-8";

    /**
     * 模板存储路径
     */
    private String templatePath = "classpath:/templates/email/";

    /**
     * 附件存储路径
     */
    private String attachmentPath = "/tmp/email/attachments/";

    /**
     * 单次发送最大收件人数量
     */
    private Integer maxRecipients = 50;

    /**
     * 邮件队列最大大小
     */
    private Integer queueMaxSize = 1000;

    /**
     * 异步发送线程池大小
     */
    private Integer asyncPoolSize = 10;

    /**
     * 是否启用邮件队列
     */
    private Boolean enableQueue = true;

    /**
     * 是否启用发送历史记录
     */
    private Boolean enableHistory = true;

    /**
     * 重试次数
     */
    private Integer retryCount = 3;

    /**
     * 重试间隔(毫秒)
     */
    private Long retryInterval = 5000L;

    // Getters and Setters
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public Boolean getEnableTls() {
        return enableTls;
    }

    public void setEnableTls(Boolean enableTls) {
        this.enableTls = enableTls;
    }

    public Boolean getEnableSsl() {
        return enableSsl;
    }

    public void setEnableSsl(Boolean enableSsl) {
        this.enableSsl = enableSsl;
    }

    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public Integer getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
    }

    public Boolean getDebug() {
        return debug;
    }

    public void setDebug(Boolean debug) {
        this.debug = debug;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }

    public String getAttachmentPath() {
        return attachmentPath;
    }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
    }

    public Integer getMaxRecipients() {
        return maxRecipients;
    }

    public void setMaxRecipients(Integer maxRecipients) {
        this.maxRecipients = maxRecipients;
    }

    public Integer getQueueMaxSize() {
        return queueMaxSize;
    }

    public void setQueueMaxSize(Integer queueMaxSize) {
        this.queueMaxSize = queueMaxSize;
    }

    public Integer getAsyncPoolSize() {
        return asyncPoolSize;
    }

    public void setAsyncPoolSize(Integer asyncPoolSize) {
        this.asyncPoolSize = asyncPoolSize;
    }

    public Boolean getEnableQueue() {
        return enableQueue;
    }

    public void setEnableQueue(Boolean enableQueue) {
        this.enableQueue = enableQueue;
    }

    public Boolean getEnableHistory() {
        return enableHistory;
    }

    public void setEnableHistory(Boolean enableHistory) {
        this.enableHistory = enableHistory;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public Long getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(Long retryInterval) {
        this.retryInterval = retryInterval;
    }
}