package in.nearkart.auth.util;

import in.nearkart.auth.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {

    private final SecretKey key;

    @Value("${nearkart.jwt.access-token-expiry-ms:900000}")
    private long accessTokenExpiryMs;

    @Value("${nearkart.jwt.refresh-token-expiry-ms:604800000}")
    private long refreshTokenExpiryMs;

    public JwtUtil(@Value("${nearkart.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateAccessToken(User user) {
        return buildToken(user.getPhone(), user.getRole().name(), accessTokenExpiryMs);
    }

    public String generateRefreshToken(User user) {
        return buildToken(user.getPhone(), user.getRole().name(), refreshTokenExpiryMs);
    }

    private String buildToken(String subject, String role, long expiryMs) {
        Date now = new Date();
        return Jwts.builder()
                .subject(subject)
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiryMs))
                .signWith(key)
                .compact();
    }

    public String extractPhone(String token) {
        return parseClaims(token).getPayload().getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String phone = extractPhone(token);
            return phone.equals(userDetails.getUsername())
                    && !parseClaims(token).getPayload().getExpiration().before(new Date());
        } catch (JwtException e) {
            log.warn("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }

    private Jws<Claims> parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
    }
}
