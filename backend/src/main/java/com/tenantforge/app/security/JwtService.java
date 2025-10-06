package com.tenantforge.app.security;

import com.tenantforge.app.domain.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtProperties properties;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
    }

    public TokenPair generateTokens(AppUser user) {
        String accessToken = buildToken(user, properties.accessTokenTtl(), "access");
        String refreshToken = buildToken(user, properties.refreshTokenTtl(), "refresh");
        long accessExpiresIn = properties.accessTokenTtl().getSeconds();
        long refreshExpiresIn = properties.refreshTokenTtl().getSeconds();
        return new TokenPair(accessToken, refreshToken, accessExpiresIn, refreshExpiresIn);
    }

    private String buildToken(AppUser user, java.time.Duration ttl, String tokenType) {
        Instant now = Instant.now();
        Instant expiry = now.plus(ttl);
        return Jwts.builder()
                .issuer(properties.issuer())
                .subject(user.getId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claims(Map.of(
                        "tenant_id", user.getTenant().getId().toString(),
                        "role", user.getRole().name(),
                        "email", user.getEmail(),
                        "token_type", tokenType))
                .signWith(Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8)))
                .requireIssuer(properties.issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID extractUserId(Claims claims) {
        return UUID.fromString(claims.getSubject());
    }

    public String tokenType(Claims claims) {
        return claims.get("token_type", String.class);
    }
}
