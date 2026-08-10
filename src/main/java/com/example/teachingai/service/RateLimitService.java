package com.example.teachingai.service;

import com.example.teachingai.exception.RateLimitException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<String, Deque<Long>> slidingWindows = new ConcurrentHashMap<>();
    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> rateLimitScript;
    private final boolean redisEnabled;

    public RateLimitService(
            @Value("${app.redis.enabled:false}") boolean redisEnabled,
            StringRedisTemplate redisTemplate,
            DefaultRedisScript<Long> rateLimitScript
    ) {
        this.redisEnabled = redisEnabled;
        this.redisTemplate = redisTemplate;
        this.rateLimitScript = rateLimitScript;
    }

    public void check(String key, int limit, int windowSeconds) {
        String rateKey = "rate:" + key;
        if (redisEnabled) {
            try {
                Long count = redisTemplate.execute(
                        rateLimitScript,
                        java.util.List.of(rateKey),
                        String.valueOf(limit),
                        String.valueOf(windowSeconds),
                        String.valueOf(System.currentTimeMillis())
                );
                if (count != null && count == -1L) {
                    throw new RateLimitException("请求过于频繁，请稍后再试");
                }
                return;
            } catch (RateLimitException exception) {
                throw exception;
            } catch (Exception ignored) {
                // Fall back to local sliding window when Redis is unavailable.
            }
        }

        long now = System.currentTimeMillis();
        long windowMillis = windowSeconds * 1000L;
        Deque<Long> timestamps = slidingWindows.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > windowMillis) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= limit) {
                throw new RateLimitException("请求过于频繁，请稍后再试");
            }
            timestamps.addLast(now);
        }
    }
}
