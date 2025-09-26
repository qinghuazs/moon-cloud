package com.moon.cloud.captcha.storage;

import com.moon.cloud.captcha.core.Captcha;
import com.moon.cloud.captcha.core.CaptchaStorage;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 内存验证码存储实现
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
@Component
public class MemoryCaptchaStorage implements CaptchaStorage {

    private final Map<String, Captcha> storage = new ConcurrentHashMap<>();
    private final Map<String, Integer> failureCount = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public MemoryCaptchaStorage() {
        // 每分钟清理一次过期的验证码
        scheduler.scheduleAtFixedRate(this::cleanExpired, 1, 1, TimeUnit.MINUTES);
    }

    @Override
    public void save(String key, Captcha captcha, long timeout, TimeUnit unit) {
        captcha.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(timeout)));
        storage.put(key, captcha);
    }

    @Override
    public Captcha get(String key) {
        Captcha captcha = storage.get(key);
        if (captcha != null && captcha.isExpired()) {
            storage.remove(key);
            return null;
        }
        return captcha;
    }

    @Override
    public boolean remove(String key) {
        failureCount.remove(key);
        return storage.remove(key) != null;
    }

    @Override
    public boolean exists(String key) {
        Captcha captcha = get(key);
        return captcha != null && !captcha.isExpired();
    }

    @Override
    public void update(String key, Captcha captcha) {
        if (storage.containsKey(key)) {
            storage.put(key, captcha);
        }
    }

    @Override
    public int recordFailure(String key) {
        return failureCount.merge(key, 1, Integer::sum);
    }

    @Override
    public void clear() {
        storage.clear();
        failureCount.clear();
    }

    /**
     * 清理过期的验证码
     */
    private void cleanExpired() {
        LocalDateTime now = LocalDateTime.now();
        storage.entrySet().removeIf(entry -> {
            Captcha captcha = entry.getValue();
            return captcha.getExpireTime() != null && captcha.getExpireTime().isBefore(now);
        });
    }

    /**
     * 关闭清理任务
     */
    public void shutdown() {
        scheduler.shutdown();
    }
}