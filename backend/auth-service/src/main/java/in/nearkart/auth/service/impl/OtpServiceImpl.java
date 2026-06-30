package in.nearkart.auth.service.impl;

import in.nearkart.auth.entity.OtpPurpose;
import in.nearkart.auth.entity.OtpRecord;
import in.nearkart.auth.exception.InvalidOtpException;
import in.nearkart.auth.exception.OtpExpiredException;
import in.nearkart.auth.repository.OtpRepository;
import in.nearkart.auth.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final OtpRepository otpRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${nearkart.otp.expiry-minutes:5}")
    private int otpExpiryMinutes;

    @Override
    @Transactional
    public void sendOtp(String phone, OtpPurpose purpose) {
        otpRepository.invalidateAllOtps(phone, purpose);

        String otp = String.format("%06d", secureRandom.nextInt(999999));

        OtpRecord record = OtpRecord.builder()
                .phone(phone)
                .otp(otp)
                .purpose(purpose)
                .expiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes))
                .build();

        otpRepository.save(record);

        // TODO: Integrate Twilio SMS here
        log.info("[DEV MODE] OTP for phone={} purpose={}: {}", phone, purpose, otp);
    }

    @Override
    @Transactional
    public boolean verifyOtp(String phone, String otp, OtpPurpose purpose) {
        OtpRecord record = otpRepository
                .findTopByPhoneAndPurposeAndIsUsedFalseOrderByCreatedAtDesc(phone, purpose)
                .orElseThrow(() -> new InvalidOtpException("No active OTP found"));

        if (record.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new OtpExpiredException("OTP has expired. Please request a new one.");
        }

        if (!record.getOtp().equals(otp)) {
            record.setAttempts(record.getAttempts() + 1);
            if (record.getAttempts() >= 3) {
                record.setIsUsed(true);
                otpRepository.save(record);
                throw new InvalidOtpException("Too many failed attempts. OTP invalidated.");
            }
            otpRepository.save(record);
            throw new InvalidOtpException("Invalid OTP. Attempts remaining: " + (3 - record.getAttempts()));
        }

        record.setIsUsed(true);
        otpRepository.save(record);
        return true;
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpiredOtps() {
        otpRepository.deleteExpiredOtps(LocalDateTime.now());
        log.info("Expired OTPs cleaned up");
    }
}
