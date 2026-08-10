package com.example.teachingai.aspect;

import com.example.teachingai.annotation.RateLimit;
import com.example.teachingai.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RateLimitService rateLimitService;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = rateLimit.key() + ":" + joinPoint.getSignature().toShortString();
        rateLimitService.check(key, rateLimit.limit(), rateLimit.windowSeconds());
        return joinPoint.proceed();
    }
}
