package com.nearkart.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final StringRedisTemplate redisTemplate;
    private static final String OTP_PREFIX = "otp:";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Value("${app.otp.expiry-minutes}")
    private int otpExpiryMinutes;

    @Value("${app.otp.length}")
    private int otpLength;

    public String generateAndStoreOtp(String identifier) {
        String otp = generateOtp();
        String key = OTP_PREFIX + identifier;
        redisTemplate.opsForValue().set(key, otp, Duration.ofMinutes(otpExpiryMinutes));
        log.info("OTP generated for identifier: {}", identifier);
        return otp;
    }

    public boolean verifyOtp(String identifier, String otp) {
        String key = OTP_PREFIX + identifier;
        String storedOtp = redisTemplate.opsForValue().get(key);
        if (storedOtp != null && storedOtp.equals(otp)) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }

    private String generateOtp() {
        int bound = (int) Math.pow(10, otpLength);
        int otp = RANDOM.nextInt(bound);
        return String.format("%0" + otpLength + "d", otp);
    }
}
