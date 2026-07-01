package com.nearkart.user.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    public UUID extractUserId(String token) {
        return UUID.fromString(getClaims(token).getSubject());
    }

    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser().verifyWith(getKey()).build()
                .parseSignedClaims(token).getPayload();
    }

    private SecretKey getKey() {
        // Use Base64 decoding to safely support secrets of any length >= 256 bits.
        // Raw .getBytes() risks WeakKeyException if the secret string is too short.
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }
}
