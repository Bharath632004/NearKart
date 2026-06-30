package in.nearkart.auth.service;

import in.nearkart.auth.entity.OtpPurpose;

public interface OtpService {
    void sendOtp(String phone, OtpPurpose purpose);
    boolean verifyOtp(String phone, String otp, OtpPurpose purpose);
}
