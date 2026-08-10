package com.example.teachingai.aspect;

import com.example.teachingai.annotation.PreventDuplicateSubmit;
import com.example.teachingai.exception.RateLimitException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
@Order(2)
public class PreventDuplicateSubmitAspect {

    private final Map<String, Long> localLocks = new ConcurrentHashMap<>();
    private final StringRedisTemplate redisTemplate;
    private final boolean redisEnabled;

    public PreventDuplicateSubmitAspect(
            StringRedisTemplate redisTemplate,
            @Value("${app.redis.enabled:false}") boolean redisEnabled
    ) {
        this.redisTemplate = redisTemplate;
        this.redisEnabled = redisEnabled;
    }

    @Around("@annotation(preventDuplicateSubmit)")
    public Object around(ProceedingJoinPoint joinPoint, PreventDuplicateSubmit preventDuplicateSubmit) throws Throwable {
        String key = "duplicate:" + preventDuplicateSubmit.key() + ":" + joinPoint.getSignature().toShortString();
        boolean locked = tryLock(key, preventDuplicateSubmit.expireSeconds());
        if (!locked) {
            throw new RateLimitException("请勿重复提交");
        }
        try {
            return joinPoint.proceed();
        } finally {
            release(key);
        }
    }

    private boolean tryLock(String key, int expireSeconds) {
        if (redisEnabled) {
            try {
                Boolean redisLock = redisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofSeconds(expireSeconds));
                if (Boolean.TRUE.equals(redisLock)) {
                    return true;
                }
            } catch (Exception ignored) {
                // Fall back to local lock when Redis is unavailable.
            }
        }
        long now = System.currentTimeMillis();
        Long previous = localLocks.putIfAbsent(key, now + expireSeconds * 1000L);
        if (previous == null) {
            return true;
        }
        if (now > previous) {
            if (localLocks.replace(key, previous, now + expireSeconds * 1000L)) {
                return true;
            }
        }
        return false;
    }

    private void release(String key) {
        localLocks.remove(key);
        if (redisEnabled) {
            redisTemplate.delete(key);
        }
    }
}
