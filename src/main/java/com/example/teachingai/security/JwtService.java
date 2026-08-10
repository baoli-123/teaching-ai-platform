package com.example.teachingai.security;

import com.example.teachingai.entity.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JwtService {

    private final SecretKey key;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;
    private final Map<String, String> refreshTokens = new ConcurrentHashMap<>();

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-expiration-ms}") long accessExpirationMs,
            @Value("${app.jwt.refresh-expiration-ms}") long refreshExpirationMs
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String generateAccessToken(AppUser user) {
        return buildToken(user, accessExpirationMs, "access");
    }

    public String generateRefreshToken(AppUser user) {
        String token = buildToken(user, refreshExpirationMs, "refresh");
        refreshTokens.put(user.getUsername(), token);
        return token;
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isRefreshTokenValid(String username, String token) {
        String stored = refreshTokens.get(username);
        return stored != null && stored.equals(token);
    }

    public void revokeRefreshToken(String username) {
        refreshTokens.remove(username);
    }

    private String buildToken(AppUser user, long expirationMs, String tokenType) {
        Date now = new Date();
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("role", user.getRole())
                .claim("tokenType", tokenType)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }
}
