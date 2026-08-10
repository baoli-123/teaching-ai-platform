package com.example.teachingai.aspect;

import com.example.teachingai.entity.AuditLog;
import com.example.teachingai.mapper.AuditLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogMapper auditLogMapper;

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint joinPoint, com.example.teachingai.annotation.AuditLog auditLog) throws Throwable {
        long start = System.currentTimeMillis();
        boolean success = true;
        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            success = false;
            throw throwable;
        } finally {
            saveAuditLog(auditLog.value(), joinPoint.getSignature().toShortString(), start, success);
        }
    }

    private void saveAuditLog(String action, String methodSignature, long start, boolean success) {
        AuditLog entity = new AuditLog();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        entity.setUsername(authentication == null ? "anonymous" : authentication.getName());
        entity.setAction(action);
        entity.setMethod(methodSignature);
        entity.setDurationMs(System.currentTimeMillis() - start);
        entity.setSuccess(success);

        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            entity.setIp(getClientIp(request));
            entity.setPath(request.getRequestURI());
            entity.setMethod(request.getMethod());
        }
        try {
            auditLogMapper.insert(entity);
        } catch (Exception ignored) {
            // Audit failure must not break the main business flow.
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
