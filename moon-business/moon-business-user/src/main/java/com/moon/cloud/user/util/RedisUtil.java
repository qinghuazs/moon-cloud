package com.moon.cloud.user.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.Set;
import java.util.Collection;

/**
 * Redis工具类
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // JWT令牌黑名单前缀
    private static final String JWT_BLACKLIST_PREFIX = "jwt:blacklist:";
    
    // 用户信息缓存前缀
    private static final String USER_INFO_PREFIX = "user:info:";
    
    // 用户权限缓存前缀
    private static final String USER_PERMISSIONS_PREFIX = "user:permissions:";
    
    // 用户角色缓存前缀
    private static final String USER_ROLES_PREFIX = "user:roles:";
    
    // 登录失败次数前缀
    private static final String LOGIN_FAIL_COUNT_PREFIX = "login:fail:";
    
    // IP锁定前缀
    private static final String IP_LOCK_PREFIX = "ip:lock:";

    /**
     * 设置缓存
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置缓存并指定过期时间
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 获取缓存
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除缓存
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 批量删除缓存
     */
    public Long delete(Collection<String> keys) {
        return redisTemplate.delete(keys);
    }

    /**
     * 检查key是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置过期时间
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 获取过期时间
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }

    /**
     * 递增
     */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * 递增指定值
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递减
     */
    public Long decrement(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }

    /**
     * 获取匹配的keys
     */
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    // ==================== JWT令牌黑名单相关方法 ====================

    /**
     * 将JWT令牌加入黑名单
     */
    public void addToBlacklist(String jti, long expireTime) {
        String key = JWT_BLACKLIST_PREFIX + jti;
        set(key, "blacklisted", expireTime, TimeUnit.SECONDS);
    }

    /**
     * 检查JWT令牌是否在黑名单中
     */
    public Boolean isTokenBlacklisted(String jti) {
        String key = JWT_BLACKLIST_PREFIX + jti;
        return hasKey(key);
    }

    /**
     * 从黑名单中移除JWT令牌
     */
    public Boolean removeFromBlacklist(String jti) {
        String key = JWT_BLACKLIST_PREFIX + jti;
        return delete(key);
    }

    /**
     * 清理过期的黑名单令牌
     */
    public void cleanExpiredBlacklistTokens() {
        Set<String> keys = keys(JWT_BLACKLIST_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                Long expire = getExpire(key);
                if (expire != null && expire <= 0) {
                    delete(key);
                }
            }
        }
    }

    // ==================== 用户信息缓存相关方法 ====================

    /**
     * 缓存用户信息
     */
    public void cacheUserInfo(Long userId, Object userInfo, long timeout, TimeUnit unit) {
        String key = USER_INFO_PREFIX + userId;
        set(key, userInfo, timeout, unit);
    }

    /**
     * 获取缓存的用户信息
     */
    public Object getCachedUserInfo(Long userId) {
        String key = USER_INFO_PREFIX + userId;
        return get(key);
    }

    /**
     * 删除用户信息缓存
     */
    public Boolean deleteCachedUserInfo(Long userId) {
        String key = USER_INFO_PREFIX + userId;
        return delete(key);
    }

    // ==================== 用户权限缓存相关方法 ====================

    /**
     * 缓存用户权限
     */
    public void cacheUserPermissions(Long userId, Object permissions, long timeout, TimeUnit unit) {
        String key = USER_PERMISSIONS_PREFIX + userId;
        set(key, permissions, timeout, unit);
    }

    /**
     * 获取缓存的用户权限
     */
    public Object getCachedUserPermissions(Long userId) {
        String key = USER_PERMISSIONS_PREFIX + userId;
        return get(key);
    }

    /**
     * 删除用户权限缓存
     */
    public Boolean deleteCachedUserPermissions(Long userId) {
        String key = USER_PERMISSIONS_PREFIX + userId;
        return delete(key);
    }

    // ==================== 用户角色缓存相关方法 ====================

    /**
     * 缓存用户角色
     */
    public void cacheUserRoles(Long userId, Object roles, long timeout, TimeUnit unit) {
        String key = USER_ROLES_PREFIX + userId;
        set(key, roles, timeout, unit);
    }

    /**
     * 获取缓存的用户角色
     */
    public Object getCachedUserRoles(Long userId) {
        String key = USER_ROLES_PREFIX + userId;
        return get(key);
    }

    /**
     * 删除用户角色缓存
     */
    public Boolean deleteCachedUserRoles(Long userId) {
        String key = USER_ROLES_PREFIX + userId;
        return delete(key);
    }

    /**
     * 清理用户所有缓存
     */
    public void clearUserCache(Long userId) {
        deleteCachedUserInfo(userId);
        deleteCachedUserPermissions(userId);
        deleteCachedUserRoles(userId);
    }

    // ==================== 登录安全相关方法 ====================

    /**
     * 记录登录失败次数
     */
    public Long recordLoginFailure(String identifier) {
        String key = LOGIN_FAIL_COUNT_PREFIX + identifier;
        Long count = increment(key);
        // 设置过期时间为1小时
        expire(key, 1, TimeUnit.HOURS);
        return count;
    }

    /**
     * 获取登录失败次数
     */
    public Long getLoginFailureCount(String identifier) {
        String key = LOGIN_FAIL_COUNT_PREFIX + identifier;
        Object count = get(key);
        return count != null ? Long.valueOf(count.toString()) : 0L;
    }

    /**
     * 清除登录失败次数
     */
    public Boolean clearLoginFailureCount(String identifier) {
        String key = LOGIN_FAIL_COUNT_PREFIX + identifier;
        return delete(key);
    }

    /**
     * 锁定IP地址
     */
    public void lockIp(String ip, long timeout, TimeUnit unit) {
        String key = IP_LOCK_PREFIX + ip;
        set(key, "locked", timeout, unit);
    }

    /**
     * 检查IP是否被锁定
     */
    public Boolean isIpLocked(String ip) {
        String key = IP_LOCK_PREFIX + ip;
        return hasKey(key);
    }

    /**
     * 解锁IP地址
     */
    public Boolean unlockIp(String ip) {
        String key = IP_LOCK_PREFIX + ip;
        return delete(key);
    }
}