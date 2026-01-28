package com.example.registration.config;

import com.example.registration.enums.Roles;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
    private final byte[] secretKey;

    public JwtUtil(
            @Value("${jwt.secret}")
            String secret
    ) {
        this.secretKey = secret.getBytes(StandardCharsets.UTF_8);
    }
    public String generateToken(String email, Roles role) {

        // ❌ Do NOT log email or role
        log.debug("Generating JWT token");

        String token = Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 43200000))
                .signWith(Keys.hmacShaKeyFor(secretKey), SignatureAlgorithm.HS256)
                .compact();

        log.debug("JWT token generated successfully");

        return token;
    }

    public String extractEmail(String token) {

        try {
            return getClaims(token).getSubject();
        } catch (Exception ex) {
            log.warn("Failed to extract email from JWT");
            throw ex;
        }
    }

    public String extractRole(String token) {

        try {
            return getClaims(token).get("role", String.class);
        } catch (Exception ex) {
            log.warn("Failed to extract role from JWT");
            throw ex;
        }
    }

    public Date extractIssuedAt(String token) {

        try {
            return getClaims(token).getIssuedAt();
        } catch (Exception ex) {
            log.warn("Failed to extract JWT issuedAt");
            throw ex;
        }
    }

    public Date extractExpiration(String token) {

        try {
            return getClaims(token).getExpiration();
        } catch (Exception ex) {
            log.warn("Failed to extract JWT expiration");
            throw ex;
        }
    }

    private Claims getClaims(String token) {

        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception ex) {
            log.warn("JWT parsing failed");
            throw ex;
        }
    }
}
