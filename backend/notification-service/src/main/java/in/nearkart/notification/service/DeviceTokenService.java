package in.nearkart.notification.service;

import in.nearkart.notification.dto.DeviceTokenRequest;
import in.nearkart.notification.entity.DeviceToken;
import in.nearkart.notification.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceTokenService {

    private final DeviceTokenRepository repository;

    @Transactional
    public DeviceToken registerToken(DeviceTokenRequest request) {
        return repository.findByFcmToken(request.getFcmToken())
                .map(existing -> {
                    existing.setUserId(request.getUserId());
                    existing.setDeviceType(request.getDeviceType());
                    existing.setActive(true);
                    return repository.save(existing);
                })
                .orElseGet(() -> repository.save(DeviceToken.builder()
                        .userId(request.getUserId())
                        .fcmToken(request.getFcmToken())
                        .deviceType(request.getDeviceType())
                        .active(true)
                        .build()));
    }

    @Transactional
    public void deregisterToken(String fcmToken) {
        repository.findByFcmToken(fcmToken).ifPresent(dt -> {
            dt.setActive(false);
            repository.save(dt);
        });
    }

    public List<DeviceToken> getTokensByUser(String userId) {
        return repository.findByUserIdAndActiveTrue(userId);
    }
}
