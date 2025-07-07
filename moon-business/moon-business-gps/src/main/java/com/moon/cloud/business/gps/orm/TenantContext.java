package com.moon.cloud.business.gps.orm;

import lombok.extern.slf4j.Slf4j;

/**
 * 租户上下文工具类
 * 用于管理当前线程的租户信息
 *
 * @author moon-cloud
 * @since 2024-01-01
 */
@Slf4j
public class TenantContext {

    private static final ThreadLocal<String> TENANT_ID_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_ID_HOLDER = new ThreadLocal<>();
    
    private static final String DEFAULT_TENANT_ID = "default_tenant";

    /**
     * 设置当前租户ID
     *
     * @param tenantId 租户ID
     */
    public static void setTenantId(String tenantId) {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            log.warn("设置的租户ID为空，将使用默认租户ID: {}", DEFAULT_TENANT_ID);
            TENANT_ID_HOLDER.set(DEFAULT_TENANT_ID);
        } else {
            TENANT_ID_HOLDER.set(tenantId.trim());
            log.debug("设置当前线程租户ID: {}", tenantId);
        }
    }

    /**
     * 获取当前租户ID
     *
     * @return 租户ID
     */
    public static String getTenantId() {
        String tenantId = TENANT_ID_HOLDER.get();
        if (tenantId == null || tenantId.trim().isEmpty()) {
            log.debug("当前线程未设置租户ID，使用默认租户ID: {}", DEFAULT_TENANT_ID);
            return DEFAULT_TENANT_ID;
        }
        return tenantId;
    }

    /**
     * 设置当前用户ID
     *
     * @param userId 用户ID
     */
    public static void setUserId(String userId) {
        if (userId != null && !userId.trim().isEmpty()) {
            USER_ID_HOLDER.set(userId.trim());
            log.debug("设置当前线程用户ID: {}", userId);
        } else {
            USER_ID_HOLDER.remove();
        }
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID
     */
    public static String getUserId() {
        return USER_ID_HOLDER.get();
    }

    /**
     * 清除当前线程的租户信息
     */
    public static void clear() {
        String tenantId = TENANT_ID_HOLDER.get();
        String userId = USER_ID_HOLDER.get();
        
        TENANT_ID_HOLDER.remove();
        USER_ID_HOLDER.remove();
        
        log.debug("清除当前线程租户信息 - 租户ID: {}, 用户ID: {}", tenantId, userId);
    }

    /**
     * 获取当前线程的租户信息摘要
     *
     * @return 租户信息摘要
     */
    public static String getContextInfo() {
        return String.format("TenantContext[tenantId=%s, userId=%s, thread=%s]", 
                           getTenantId(), getUserId(), Thread.currentThread().getName());
    }

    /**
     * 检查是否已设置租户ID
     *
     * @return true如果已设置租户ID
     */
    public static boolean hasTenantId() {
        String tenantId = TENANT_ID_HOLDER.get();
        return tenantId != null && !tenantId.trim().isEmpty();
    }

    /**
     * 检查是否已设置用户ID
     *
     * @return true如果已设置用户ID
     */
    public static boolean hasUserId() {
        String userId = USER_ID_HOLDER.get();
        return userId != null && !userId.trim().isEmpty();
    }

    /**
     * 在指定租户上下文中执行操作
     *
     * @param tenantId 租户ID
     * @param runnable 要执行的操作
     */
    public static void runWithTenant(String tenantId, Runnable runnable) {
        String originalTenantId = TENANT_ID_HOLDER.get();
        try {
            setTenantId(tenantId);
            runnable.run();
        } finally {
            if (originalTenantId != null) {
                TENANT_ID_HOLDER.set(originalTenantId);
            } else {
                TENANT_ID_HOLDER.remove();
            }
        }
    }

    /**
     * 在指定租户和用户上下文中执行操作
     *
     * @param tenantId 租户ID
     * @param userId   用户ID
     * @param runnable 要执行的操作
     */
    public static void runWithTenantAndUser(String tenantId, String userId, Runnable runnable) {
        String originalTenantId = TENANT_ID_HOLDER.get();
        String originalUserId = USER_ID_HOLDER.get();
        try {
            setTenantId(tenantId);
            setUserId(userId);
            runnable.run();
        } finally {
            if (originalTenantId != null) {
                TENANT_ID_HOLDER.set(originalTenantId);
            } else {
                TENANT_ID_HOLDER.remove();
            }
            
            if (originalUserId != null) {
                USER_ID_HOLDER.set(originalUserId);
            } else {
                USER_ID_HOLDER.remove();
            }
        }
    }

    /**
     * 复制当前线程的租户上下文到新线程
     *
     * @return 租户上下文快照
     */
    public static TenantContextSnapshot snapshot() {
        return new TenantContextSnapshot(getTenantId(), getUserId());
    }

    /**
     * 租户上下文快照
     */
    public static class TenantContextSnapshot {
        private final String tenantId;
        private final String userId;

        private TenantContextSnapshot(String tenantId, String userId) {
            this.tenantId = tenantId;
            this.userId = userId;
        }

        /**
         * 恢复租户上下文
         */
        public void restore() {
            if (tenantId != null) {
                setTenantId(tenantId);
            }
            if (userId != null) {
                setUserId(userId);
            }
        }

        /**
         * 在当前快照上下文中执行操作
         *
         * @param runnable 要执行的操作
         */
        public void runWith(Runnable runnable) {
            String originalTenantId = TENANT_ID_HOLDER.get();
            String originalUserId = USER_ID_HOLDER.get();
            try {
                restore();
                runnable.run();
            } finally {
                if (originalTenantId != null) {
                    TENANT_ID_HOLDER.set(originalTenantId);
                } else {
                    TENANT_ID_HOLDER.remove();
                }
                
                if (originalUserId != null) {
                    USER_ID_HOLDER.set(originalUserId);
                } else {
                    USER_ID_HOLDER.remove();
                }
            }
        }

        public String getTenantId() {
            return tenantId;
        }

        public String getUserId() {
            return userId;
        }

        @Override
        public String toString() {
            return String.format("TenantContextSnapshot[tenantId=%s, userId=%s]", tenantId, userId);
        }
    }
}