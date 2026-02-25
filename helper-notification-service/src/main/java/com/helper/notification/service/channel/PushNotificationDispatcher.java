package com.helper.notification.service.channel;

import com.helper.notification.entity.DeviceToken;
import com.helper.notification.entity.Notification;
import com.helper.notification.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Sends push notifications via Firebase Cloud Messaging.
 * In dev mode (firebase.enabled=false), logs the push instead.
 * In production, uses the Firebase Admin SDK.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PushNotificationDispatcher {

    private final DeviceTokenRepository deviceTokenRepo;

    @Value("${app.firebase.enabled:false}")
    private boolean firebaseEnabled;

    @Async("notificationExecutor")
    public boolean send(Notification notification) {
        UUID userId = notification.getUserId();
        List<DeviceToken> tokens = deviceTokenRepo.findByUserIdAndIsActiveTrue(userId);

        if (tokens.isEmpty()) {
            log.debug("No active device tokens for user {}. Push skipped.", userId);
            return false;
        }

        for (DeviceToken dt : tokens) {
            try {
                if (firebaseEnabled) {
                    sendViaFirebase(dt.getToken(), notification);
                } else {
                    log.info("[PUSH-DEV] To: {} ({}) | Title: {} | Body: {}",
                            userId, dt.getPlatform(), notification.getTitle(), notification.getBody());
                }
            } catch (Exception e) {
                log.error("Push failed for token {} user {}: {}", dt.getTokenId(), userId, e.getMessage());
                // Mark stale token inactive if Firebase returns invalid token error
                if (e.getMessage() != null && e.getMessage().contains("INVALID_ARGUMENT")) {
                    dt.setIsActive(false);
                    deviceTokenRepo.save(dt);
                    log.warn("Deactivated stale FCM token: {}", dt.getTokenId());
                }
            }
        }
        return true;
    }

    private void sendViaFirebase(String fcmToken, Notification notification) {
        // Phase 2: Firebase Admin SDK integration
        // com.google.firebase.messaging.Message message = com.google.firebase.messaging.Message.builder()
        //         .setToken(fcmToken)
        //         .setNotification(com.google.firebase.messaging.Notification.builder()
        //                 .setTitle(notification.getTitle())
        //                 .setBody(notification.getBody())
        //                 .build())
        //         .putData("event", notification.getEvent().name())
        //         .putData("notificationId", notification.getNotificationId().toString())
        //         .build();
        // String response = com.google.firebase.messaging.FirebaseMessaging.getInstance().send(message);
        // log.info("FCM sent. Response: {}", response);

        log.info("[PUSH-FCM] Token: {}... | Title: {}", fcmToken.substring(0, Math.min(20, fcmToken.length())), notification.getTitle());
    }
}
