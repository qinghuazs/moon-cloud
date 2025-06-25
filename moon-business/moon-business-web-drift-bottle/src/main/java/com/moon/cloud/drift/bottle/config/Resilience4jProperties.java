package com.moon.cloud.drift.bottle.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * Resilience4j 配置属性类
 * 支持动态配置熔断器、限流器、重试等参数
 */
@Component
@ConfigurationProperties(prefix = "resilience4j")
public class Resilience4jProperties {

    private CircuitBreakerConfig circuitbreaker = new CircuitBreakerConfig();
    private RateLimiterConfig ratelimiter = new RateLimiterConfig();
    private RetryConfig retry = new RetryConfig();

    public CircuitBreakerConfig getCircuitbreaker() {
        return circuitbreaker;
    }

    public void setCircuitbreaker(CircuitBreakerConfig circuitbreaker) {
        this.circuitbreaker = circuitbreaker;
    }

    public RateLimiterConfig getRatelimiter() {
        return ratelimiter;
    }

    public void setRatelimiter(RateLimiterConfig ratelimiter) {
        this.ratelimiter = ratelimiter;
    }

    public RetryConfig getRetry() {
        return retry;
    }

    public void setRetry(RetryConfig retry) {
        this.retry = retry;
    }

    /**
     * 熔断器配置
     */
    public static class CircuitBreakerConfig {
        private InstanceConfig instances = new InstanceConfig();

        public InstanceConfig getInstances() {
            return instances;
        }

        public void setInstances(InstanceConfig instances) {
            this.instances = instances;
        }

        public static class InstanceConfig {
            private DriftBottleConfig driftBottle = new DriftBottleConfig();

            public DriftBottleConfig getDriftBottle() {
                return driftBottle;
            }

            public void setDriftBottle(DriftBottleConfig driftBottle) {
                this.driftBottle = driftBottle;
            }

            public static class DriftBottleConfig {
                private int slidingWindowSize = 10;
                private String slidingWindowType = "COUNT_BASED";
                private int minimumNumberOfCalls = 5;
                private float failureRateThreshold = 50.0f;
                private long slowCallDurationThreshold = 2000;
                private float slowCallRateThreshold = 50.0f;
                private long waitDurationInOpenState = 30;
                private int permittedNumberOfCallsInHalfOpenState = 3;
                private boolean automaticTransitionFromOpenToHalfOpenEnabled = true;
                private List<String> recordExceptions = List.of("java.lang.Exception");
                private List<String> ignoreExceptions = List.of("java.lang.IllegalArgumentException");

                // Getters and Setters
                public int getSlidingWindowSize() {
                    return slidingWindowSize;
                }

                public void setSlidingWindowSize(int slidingWindowSize) {
                    this.slidingWindowSize = slidingWindowSize;
                }

                public String getSlidingWindowType() {
                    return slidingWindowType;
                }

                public void setSlidingWindowType(String slidingWindowType) {
                    this.slidingWindowType = slidingWindowType;
                }

                public int getMinimumNumberOfCalls() {
                    return minimumNumberOfCalls;
                }

                public void setMinimumNumberOfCalls(int minimumNumberOfCalls) {
                    this.minimumNumberOfCalls = minimumNumberOfCalls;
                }

                public float getFailureRateThreshold() {
                    return failureRateThreshold;
                }

                public void setFailureRateThreshold(float failureRateThreshold) {
                    this.failureRateThreshold = failureRateThreshold;
                }

                public long getSlowCallDurationThreshold() {
                    return slowCallDurationThreshold;
                }

                public void setSlowCallDurationThreshold(long slowCallDurationThreshold) {
                    this.slowCallDurationThreshold = slowCallDurationThreshold;
                }

                public float getSlowCallRateThreshold() {
                    return slowCallRateThreshold;
                }

                public void setSlowCallRateThreshold(float slowCallRateThreshold) {
                    this.slowCallRateThreshold = slowCallRateThreshold;
                }

                public long getWaitDurationInOpenState() {
                    return waitDurationInOpenState;
                }

                public void setWaitDurationInOpenState(long waitDurationInOpenState) {
                    this.waitDurationInOpenState = waitDurationInOpenState;
                }

                public int getPermittedNumberOfCallsInHalfOpenState() {
                    return permittedNumberOfCallsInHalfOpenState;
                }

                public void setPermittedNumberOfCallsInHalfOpenState(int permittedNumberOfCallsInHalfOpenState) {
                    this.permittedNumberOfCallsInHalfOpenState = permittedNumberOfCallsInHalfOpenState;
                }

                public boolean isAutomaticTransitionFromOpenToHalfOpenEnabled() {
                    return automaticTransitionFromOpenToHalfOpenEnabled;
                }

                public void setAutomaticTransitionFromOpenToHalfOpenEnabled(boolean automaticTransitionFromOpenToHalfOpenEnabled) {
                    this.automaticTransitionFromOpenToHalfOpenEnabled = automaticTransitionFromOpenToHalfOpenEnabled;
                }

                public List<String> getRecordExceptions() {
                    return recordExceptions;
                }

                public void setRecordExceptions(List<String> recordExceptions) {
                    this.recordExceptions = recordExceptions;
                }

                public List<String> getIgnoreExceptions() {
                    return ignoreExceptions;
                }

                public void setIgnoreExceptions(List<String> ignoreExceptions) {
                    this.ignoreExceptions = ignoreExceptions;
                }
            }
        }
    }

    /**
     * 限流器配置
     */
    public static class RateLimiterConfig {
        private InstanceConfig instances = new InstanceConfig();

        public InstanceConfig getInstances() {
            return instances;
        }

        public void setInstances(InstanceConfig instances) {
            this.instances = instances;
        }

        public static class InstanceConfig {
            private DriftBottleConfig driftBottle = new DriftBottleConfig();

            public DriftBottleConfig getDriftBottle() {
                return driftBottle;
            }

            public void setDriftBottle(DriftBottleConfig driftBottle) {
                this.driftBottle = driftBottle;
            }

            public static class DriftBottleConfig {
                private int limitRefreshPeriod = 1;
                private int limitForPeriod = 10;
                private long timeoutDuration = 500;

                public int getLimitRefreshPeriod() {
                    return limitRefreshPeriod;
                }

                public void setLimitRefreshPeriod(int limitRefreshPeriod) {
                    this.limitRefreshPeriod = limitRefreshPeriod;
                }

                public int getLimitForPeriod() {
                    return limitForPeriod;
                }

                public void setLimitForPeriod(int limitForPeriod) {
                    this.limitForPeriod = limitForPeriod;
                }

                public long getTimeoutDuration() {
                    return timeoutDuration;
                }

                public void setTimeoutDuration(long timeoutDuration) {
                    this.timeoutDuration = timeoutDuration;
                }
            }
        }
    }

    /**
     * 重试配置
     */
    public static class RetryConfig {
        private InstanceConfig instances = new InstanceConfig();

        public InstanceConfig getInstances() {
            return instances;
        }

        public void setInstances(InstanceConfig instances) {
            this.instances = instances;
        }

        public static class InstanceConfig {
            private DriftBottleConfig driftBottle = new DriftBottleConfig();

            public DriftBottleConfig getDriftBottle() {
                return driftBottle;
            }

            public void setDriftBottle(DriftBottleConfig driftBottle) {
                this.driftBottle = driftBottle;
            }

            public static class DriftBottleConfig {
                private int maxAttempts = 3;
                private long waitDuration = 1000;

                public int getMaxAttempts() {
                    return maxAttempts;
                }

                public void setMaxAttempts(int maxAttempts) {
                    this.maxAttempts = maxAttempts;
                }

                public long getWaitDuration() {
                    return waitDuration;
                }

                public void setWaitDuration(long waitDuration) {
                    this.waitDuration = waitDuration;
                }
            }
        }
    }
}