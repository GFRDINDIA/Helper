package com.helper.notification.service;

import com.helper.notification.dto.request.RegisterDeviceRequest;
import com.helper.notification.entity.DeviceToken;
import com.helper.notification.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceTokenService {

    private final DeviceTokenRepository tokenRepo;

    @Transactional
    public DeviceToken registerDevice(UUID userId, RegisterDeviceRequest request) {
        // Check if token already exists — update user association
        var existing = tokenRepo.findByToken(request.getToken());
        if (existing.isPresent()) {
            DeviceToken dt = existing.get();
            dt.setUserId(userId);
            dt.setIsActive(true);
            dt.setPlatform(request.getPlatform());
            dt.setDeviceName(request.getDeviceName());
            log.info("Device token re-registered for user {}: {}", userId, dt.getTokenId());
            return tokenRepo.save(dt);
        }

        DeviceToken token = DeviceToken.builder()
                .userId(userId)
                .token(request.getToken())
                .platform(request.getPlatform())
                .deviceName(request.getDeviceName())
                .isActive(true)
                .build();

        token = tokenRepo.save(token);
        log.info("New device token registered: {} for user {} platform {}", token.getTokenId(), userId, request.getPlatform());
        return token;
    }

    @Transactional
    public void deactivateToken(String token) {
        tokenRepo.findByToken(token).ifPresent(dt -> {
            dt.setIsActive(false);
            tokenRepo.save(dt);
            log.info("Device token deactivated: {}", dt.getTokenId());
        });
    }

    public List<DeviceToken> getUserDevices(UUID userId) {
        return tokenRepo.findByUserIdAndIsActiveTrue(userId);
    }
}
