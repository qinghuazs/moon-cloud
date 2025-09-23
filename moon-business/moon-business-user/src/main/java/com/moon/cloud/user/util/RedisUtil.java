package com.moon.cloud.user.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

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

    // ==================== Hash操作相关方法 ====================

    /**
     * Hash设置值
     */
    public void hSet(String key, String hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    /**
     * Hash批量设置值
     */
    public void hSetAll(String key, Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    /**
     * Hash获取值
     */
    public Object hGet(String key, String hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    /**
     * Hash获取所有键值对
     */
    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * Hash删除字段
     */
    public Long hDelete(String key, Object... hashKeys) {
        return redisTemplate.opsForHash().delete(key, hashKeys);
    }

    /**
     * Hash判断字段是否存在
     */
    public Boolean hExists(String key, String hashKey) {
        return redisTemplate.opsForHash().hasKey(key, hashKey);
    }

    /**
     * Hash获取所有字段
     */
    public Set<Object> hKeys(String key) {
        return redisTemplate.opsForHash().keys(key);
    }

    /**
     * Hash获取字段数量
     */
    public Long hSize(String key) {
        return redisTemplate.opsForHash().size(key);
    }

    /**
     * Hash递增
     */
    public Long hIncrement(String key, String hashKey, long delta) {
        return redisTemplate.opsForHash().increment(key, hashKey, delta);
    }

    // ==================== List操作相关方法 ====================

    /**
     * List左侧推入
     */
    public Long lLeftPush(String key, Object value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * List右侧推入
     */
    public Long lRightPush(String key, Object value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    /**
     * List左侧弹出
     */
    public Object lLeftPop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    /**
     * List右侧弹出
     */
    public Object lRightPop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    /**
     * List获取范围内的元素
     */
    public List<Object> lRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * List获取长度
     */
    public Long lSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    /**
     * List根据索引获取元素
     */
    public Object lIndex(String key, long index) {
        return redisTemplate.opsForList().index(key, index);
    }

    /**
     * List根据索引设置元素
     */
    public void lSet(String key, long index, Object value) {
        redisTemplate.opsForList().set(key, index, value);
    }

    /**
     * List移除元素
     */
    public Long lRemove(String key, long count, Object value) {
        return redisTemplate.opsForList().remove(key, count, value);
    }

    // ==================== Set操作相关方法 ====================

    /**
     * Set添加元素
     */
    public Long sAdd(String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    /**
     * Set移除元素
     */
    public Long sRemove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    /**
     * Set判断元素是否存在
     */
    public Boolean sIsMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * Set获取所有元素
     */
    public Set<Object> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * Set获取元素数量
     */
    public Long sSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    /**
     * Set随机获取元素
     */
    public Object sRandomMember(String key) {
        return redisTemplate.opsForSet().randomMember(key);
    }

    /**
     * Set随机获取多个元素
     */
    public List<Object> sRandomMembers(String key, long count) {
        return redisTemplate.opsForSet().randomMembers(key, count);
    }

    // ==================== ZSet操作相关方法 ====================

    /**
     * ZSet添加元素
     */
    public Boolean zAdd(String key, Object value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * ZSet移除元素
     */
    public Long zRemove(String key, Object... values) {
        return redisTemplate.opsForZSet().remove(key, values);
    }

    /**
     * ZSet获取元素分数
     */
    public Double zScore(String key, Object value) {
        return redisTemplate.opsForZSet().score(key, value);
    }

    /**
     * ZSet获取元素排名
     */
    public Long zRank(String key, Object value) {
        return redisTemplate.opsForZSet().rank(key, value);
    }

    /**
     * ZSet获取范围内的元素（按分数升序）
     */
    public Set<Object> zRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    /**
     * ZSet获取范围内的元素（按分数降序）
     */
    public Set<Object> zReverseRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    /**
     * ZSet根据分数范围获取元素
     */
    public Set<Object> zRangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    /**
     * ZSet获取元素数量
     */
    public Long zSize(String key) {
        return redisTemplate.opsForZSet().size(key);
    }

    /**
     * ZSet获取分数范围内的元素数量
     */
    public Long zCount(String key, double min, double max) {
        return redisTemplate.opsForZSet().count(key, min, max);
    }

    // ==================== 分布式锁相关方法 ====================

    private static final String LOCK_PREFIX = "lock:";
    private static final String LOCK_SUCCESS = "OK";
    private static final Long LOCK_RELEASE_SUCCESS = 1L;

    /**
     * 尝试获取分布式锁
     * @param lockKey 锁的key
     * @param requestId 请求标识
     * @param expireTime 过期时间（秒）
     * @return 是否获取成功
     */
    public Boolean tryLock(String lockKey, String requestId, long expireTime) {
        String key = LOCK_PREFIX + lockKey;
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, requestId, expireTime, TimeUnit.SECONDS);
        return result != null && result;
    }

    /**
     * 尝试获取分布式锁（使用UUID作为请求标识）
     * @param lockKey 锁的key
     * @param expireTime 过期时间（秒）
     * @return 请求标识（用于释放锁），获取失败返回null
     */
    public String tryLock(String lockKey, long expireTime) {
        String requestId = UUID.randomUUID().toString();
        Boolean success = tryLock(lockKey, requestId, expireTime);
        return success ? requestId : null;
    }

    /**
     * 释放分布式锁
     * @param lockKey 锁的key
     * @param requestId 请求标识
     * @return 是否释放成功
     */
    public Boolean releaseLock(String lockKey, String requestId) {
        String key = LOCK_PREFIX + lockKey;
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object result = redisTemplate.execute(
            (org.springframework.data.redis.core.script.RedisScript<Long>) 
            org.springframework.data.redis.core.script.RedisScript.of(script, Long.class),
            java.util.Collections.singletonList(key),
            requestId
        );
        return LOCK_RELEASE_SUCCESS.equals(result);
    }

    // ==================== 工具方法 ====================

    /**
     * 批量检查key是否存在
     */
    public Map<String, Boolean> batchHasKey(Collection<String> keys) {
        Map<String, Boolean> result = new java.util.HashMap<>();
        for (String key : keys) {
            result.put(key, hasKey(key));
        }
        return result;
    }

    /**
     * 批量获取值
     */
    public List<Object> batchGet(Collection<String> keys) {
        return redisTemplate.opsForValue().multiGet(keys);
    }

    /**
     * 批量设置值
     */
    public void batchSet(Map<String, Object> keyValues) {
        redisTemplate.opsForValue().multiSet(keyValues);
    }

    /**
     * 批量设置值（如果key不存在）
     */
    public Boolean batchSetIfAbsent(Map<String, Object> keyValues) {
        return redisTemplate.opsForValue().multiSetIfAbsent(keyValues);
    }

    /**
     * 获取Redis信息
     */
    public java.util.Properties getRedisInfo() {
        return redisTemplate.getConnectionFactory().getConnection().info();
    }

    /**
     * 清空当前数据库
     */
    public void flushDb() {
        redisTemplate.getConnectionFactory().getConnection().flushDb();
    }

    /**
     * 获取数据库大小
     */
    public Long dbSize() {
        return redisTemplate.getConnectionFactory().getConnection().dbSize();
    }
}