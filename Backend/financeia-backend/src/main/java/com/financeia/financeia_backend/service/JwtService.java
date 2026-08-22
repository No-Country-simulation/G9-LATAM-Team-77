package com.financeia.financeia_backend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private static final long EXPIRATION_TIME = 1000L * 60 * 60; // 1 hora
    private static final long RESET_TOKEN_EXPIRATION = 1000L * 60 * 15; // 15 minutos

    private final SecretKey key;

    public JwtService(Environment environment) {
        String secretKey = environment.getRequiredProperty("jwt.secret");

        this.key = Keys.hmacShaKeyFor(
                secretKey.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(
                        new Date(System.currentTimeMillis() + EXPIRATION_TIME)
                )
                .signWith(key)
                .compact();
    }

    public String generatePasswordResetToken(String email) {
        return Jwts.builder()
                .subject(email)
                .claim("purpose", "RESET_PASSWORD")
                .issuedAt(new Date())
                .expiration(
                        new Date(System.currentTimeMillis() + RESET_TOKEN_EXPIRATION)
                )
                .signWith(key)
                .compact();
    }

    public boolean validatePasswordResetToken(String token, String expectedEmail) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String subject = claims.getSubject();
            String purpose = claims.get("purpose", String.class);
            Date expiration = claims.getExpiration();

            return subject != null
                    && subject.equalsIgnoreCase(expectedEmail)
                    && "RESET_PASSWORD".equals(purpose)
                    && expiration != null
                    && expiration.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);

            return true;
        } catch (Exception exception) {
            return false;
        }
    }
}