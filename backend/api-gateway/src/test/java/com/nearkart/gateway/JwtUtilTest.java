package com.nearkart.gateway;

import com.nearkart.gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setup() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "NearKartSuperSecretKey2024MustBe256BitsLongForHS256Algorithm!");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
    }

    @Test
    void testInvalidToken() {
        assertFalse(jwtUtil.isValid("invalid.token.here"));
    }

    @Test
    void testNullToken() {
        assertFalse(jwtUtil.isValid(null));
    }

    @Test
    void testEmptyToken() {
        assertFalse(jwtUtil.isValid(""));
    }
}
