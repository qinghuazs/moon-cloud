package com.moon.cloud.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 应用配置属性类
 * 统一管理应用程序的配置属性
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfigProperties {

    /**
     * JWT配置
     */
    private Jwt jwt = new Jwt();

    /**
     * Google OAuth配置
     */
    private Google google = new Google();

    /**
     * 安全配置
     */
    private Security security = new Security();

    public static class Jwt {
        private String secret = System.getenv("JWT_SECRET") != null ?
            System.getenv("JWT_SECRET") : "moonCloudUserSecretKey2024";
        private Long expiration = System.getenv("JWT_EXPIRATION") != null ?
            Long.parseLong(System.getenv("JWT_EXPIRATION")) : 86400000L; // 24小时
        private Long refreshExpiration = System.getenv("JWT_REFRESH_EXPIRATION") != null ?
            Long.parseLong(System.getenv("JWT_REFRESH_EXPIRATION")) : 604800000L; // 7天
        private String header = "Authorization";
        private String prefix = "Bearer";

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public Long getExpiration() {
            return expiration;
        }

        public void setExpiration(Long expiration) {
            this.expiration = expiration;
        }

        public Long getRefreshExpiration() {
            return refreshExpiration;
        }

        public void setRefreshExpiration(Long refreshExpiration) {
            this.refreshExpiration = refreshExpiration;
        }

        public String getHeader() {
            return header;
        }

        public void setHeader(String header) {
            this.header = header;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }
    }

    public static class Google {
        private OAuth oauth = new OAuth();

        public static class OAuth {
            private String clientId = System.getenv("GOOGLE_CLIENT_ID") != null ?
                System.getenv("GOOGLE_CLIENT_ID") : "your-google-client-id";
            private String clientSecret = System.getenv("GOOGLE_CLIENT_SECRET");

            public String getClientId() {
                return clientId;
            }

            public void setClientId(String clientId) {
                this.clientId = clientId;
            }

            public String getClientSecret() {
                return clientSecret;
            }

            public void setClientSecret(String clientSecret) {
                this.clientSecret = clientSecret;
            }
        }

        public OAuth getOauth() {
            return oauth;
        }

        public void setOauth(OAuth oauth) {
            this.oauth = oauth;
        }
    }

    public static class Security {
        private boolean enableCsrf = false;
        private boolean enableFrameOptions = false;
        private int maxLoginAttempts = 5;
        private long lockoutDuration = 900000L; // 15分钟
        private boolean enableRateLimiting = true;
        private int rateLimitPerMinute = 60;

        public boolean isEnableCsrf() {
            return enableCsrf;
        }

        public void setEnableCsrf(boolean enableCsrf) {
            this.enableCsrf = enableCsrf;
        }

        public boolean isEnableFrameOptions() {
            return enableFrameOptions;
        }

        public void setEnableFrameOptions(boolean enableFrameOptions) {
            this.enableFrameOptions = enableFrameOptions;
        }

        public int getMaxLoginAttempts() {
            return maxLoginAttempts;
        }

        public void setMaxLoginAttempts(int maxLoginAttempts) {
            this.maxLoginAttempts = maxLoginAttempts;
        }

        public long getLockoutDuration() {
            return lockoutDuration;
        }

        public void setLockoutDuration(long lockoutDuration) {
            this.lockoutDuration = lockoutDuration;
        }

        public boolean isEnableRateLimiting() {
            return enableRateLimiting;
        }

        public void setEnableRateLimiting(boolean enableRateLimiting) {
            this.enableRateLimiting = enableRateLimiting;
        }

        public int getRateLimitPerMinute() {
            return rateLimitPerMinute;
        }

        public void setRateLimitPerMinute(int rateLimitPerMinute) {
            this.rateLimitPerMinute = rateLimitPerMinute;
        }
    }

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    public Google getGoogle() {
        return google;
    }

    public void setGoogle(Google google) {
        this.google = google;
    }

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }
}