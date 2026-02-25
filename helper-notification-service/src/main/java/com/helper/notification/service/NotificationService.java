package com.helper.notification.service;

import com.helper.notification.dto.request.SendNotificationRequest;
import com.helper.notification.dto.request.UpdatePreferencesRequest;
import com.helper.notification.dto.response.NotificationResponse;
import com.helper.notification.dto.response.NotificationStatsResponse;
import com.helper.notification.entity.Notification;
import com.helper.notification.entity.UserNotificationPreference;
import com.helper.notification.enums.NotificationChannel;
import com.helper.notification.enums.NotificationEvent;
import com.helper.notification.enums.NotificationStatus;
import com.helper.notification.exception.NotificationExceptions;
import com.helper.notification.repository.DeviceTokenRepository;
import com.helper.notification.repository.NotificationRepository;
import com.helper.notification.repository.UserNotificationPreferenceRepository;
import com.helper.notification.security.AuthenticatedUser;
import com.helper.notification.service.channel.EmailDispatcher;
import com.helper.notification.service.channel.PushNotificationDispatcher;
import com.helper.notification.service.channel.SmsDispatcher;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notifRepo;
    private final UserNotificationPreferenceRepository prefRepo;
    private final DeviceTokenRepository deviceTokenRepo;
    private final PushNotificationDispatcher pushDispatcher;
    private final SmsDispatcher smsDispatcher;
    private final EmailDispatcher emailDispatcher;
    private final ObjectMapper objectMapper;

    @Value("${app.notification.max-retries:3}")
    private int maxRetries;

    @Value("${app.notification.cleanup-days:90}")
    private int cleanupDays;

    // ===== SEND NOTIFICATION (Internal API — called by other services) =====
    @Transactional
    public List<NotificationResponse> sendNotification(SendNotificationRequest request) {
        List<NotificationResponse> results = new ArrayList<>();

        String dataJson = null;
        if (request.getData() != null && !request.getData().isEmpty()) {
            try { dataJson = objectMapper.writeValueAsString(request.getData()); }
            catch (Exception e) { log.warn("Failed to serialize notification data: {}", e.getMessage()); }
        }

        Set<NotificationChannel> requiredChannels = NotificationEventRouter.getChannels(request.getEvent());

        for (UUID userId : request.getUserIds()) {
            Notification notif = Notification.builder()
                    .userId(userId)
                    .event(request.getEvent())
                    .title(request.getTitle())
                    .body(request.getBody())
                    .dataJson(dataJson)
                    .priority(request.getPriority())
                    .status(NotificationStatus.PENDING)
                    .build();
            notif = notifRepo.save(notif);

            // Dispatch to channels based on event type + user preferences
            dispatchToChannels(notif, requiredChannels, userId);

            notif.setStatus(NotificationStatus.SENT);
            notif = notifRepo.save(notif);
            results.add(mapToResponse(notif));
        }

        log.info("Notification sent: event={} recipients={} channels={}",
                request.getEvent(), request.getUserIds().size(), requiredChannels);
        return results;
    }

    // ===== SEND SINGLE NOTIFICATION (convenience) =====
    public NotificationResponse sendToUser(UUID userId, NotificationEvent event,
                                            String title, String body, Map<String, String> data) {
        SendNotificationRequest req = SendNotificationRequest.builder()
                .userIds(List.of(userId)).event(event).title(title).body(body).data(data).build();
        List<NotificationResponse> results = sendNotification(req);
        return results.isEmpty() ? null : results.get(0);
    }

    // ===== GET USER NOTIFICATIONS =====
    public Page<NotificationResponse> getUserNotifications(UUID userId, Pageable pageable) {
        return notifRepo.findByUserIdOrderByCreatedAtDesc(userId, pageable).map(this::mapToResponse);
    }

    public Page<NotificationResponse> getUnreadNotifications(UUID userId, Pageable pageable) {
        return notifRepo.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable).map(this::mapToResponse);
    }

    public long getUnreadCount(UUID userId) {
        return notifRepo.countByUserIdAndIsReadFalse(userId);
    }

    // ===== MARK READ =====
    @Transactional
    public NotificationResponse markAsRead(UUID notificationId, AuthenticatedUser user) {
        Notification n = notifRepo.findById(notificationId)
                .orElseThrow(() -> new NotificationExceptions.NotificationNotFoundException("Notification not found: " + notificationId));
        if (!n.getUserId().equals(user.getUserId())) {
            throw new NotificationExceptions.UnauthorizedNotificationException("Not your notification");
        }
        n.setIsRead(true);
        n.setReadAt(LocalDateTime.now());
        n.setStatus(NotificationStatus.READ);
        return mapToResponse(notifRepo.save(n));
    }

    @Transactional
    public int markAllRead(UUID userId) {
        return notifRepo.markAllReadForUser(userId);
    }

    // ===== DELETE =====
    @Transactional
    public void deleteNotification(UUID notificationId, AuthenticatedUser user) {
        Notification n = notifRepo.findById(notificationId)
                .orElseThrow(() -> new NotificationExceptions.NotificationNotFoundException("Notification not found"));
        if (!n.getUserId().equals(user.getUserId()) && !user.isAdmin()) {
            throw new NotificationExceptions.UnauthorizedNotificationException("Not your notification");
        }
        notifRepo.delete(n);
    }

    // ===== PREFERENCES =====
    public UserNotificationPreference getPreferences(UUID userId) {
        return prefRepo.findById(userId)
                .orElse(UserNotificationPreference.builder().userId(userId).build());
    }

    @Transactional
    public UserNotificationPreference updatePreferences(UUID userId, UpdatePreferencesRequest req) {
        UserNotificationPreference pref = prefRepo.findById(userId)
                .orElse(UserNotificationPreference.builder().userId(userId).build());

        if (req.getPushEnabled() != null) pref.setPushEnabled(req.getPushEnabled());
        if (req.getSmsEnabled() != null) pref.setSmsEnabled(req.getSmsEnabled());
        if (req.getEmailEnabled() != null) pref.setEmailEnabled(req.getEmailEnabled());
        if (req.getInAppEnabled() != null) pref.setInAppEnabled(req.getInAppEnabled());
        if (req.getQuietHoursEnabled() != null) pref.setQuietHoursEnabled(req.getQuietHoursEnabled());
        if (req.getQuietStartHour() != null) pref.setQuietStartHour(req.getQuietStartHour());
        if (req.getQuietEndHour() != null) pref.setQuietEndHour(req.getQuietEndHour());
        if (req.getPromotionalEnabled() != null) pref.setPromotionalEnabled(req.getPromotionalEnabled());

        return prefRepo.save(pref);
    }

    // ===== ADMIN STATS =====
    public NotificationStatsResponse getStats() {
        Map<String, Long> byStatus = new HashMap<>();
        for (NotificationStatus s : NotificationStatus.values()) {
            byStatus.put(s.name(), notifRepo.countByStatus(s));
        }
        return NotificationStatsResponse.builder()
                .totalNotifications(notifRepo.count())
                .pendingNotifications(notifRepo.countByStatus(NotificationStatus.PENDING))
                .sentNotifications(notifRepo.countByStatus(NotificationStatus.SENT))
                .failedNotifications(notifRepo.countByStatus(NotificationStatus.FAILED))
                .activeDeviceTokens(deviceTokenRepo.countByIsActiveTrue())
                .byStatus(byStatus)
                .build();
    }

    // ===== RETRY FAILED (scheduled) =====
    @Scheduled(fixedDelayString = "${app.notification.retry-delay-ms:60000}")
    @Transactional
    public void retryFailed() {
        List<Notification> failed = notifRepo.findByStatusAndRetryCountLessThan(NotificationStatus.FAILED, maxRetries);
        if (failed.isEmpty()) return;

        log.info("Retrying {} failed notifications", failed.size());
        for (Notification n : failed) {
            n.setRetryCount(n.getRetryCount() + 1);
            Set<NotificationChannel> channels = NotificationEventRouter.getChannels(n.getEvent());
            try {
                dispatchToChannels(n, channels, n.getUserId());
                n.setStatus(NotificationStatus.SENT);
            } catch (Exception e) {
                n.setErrorMessage(e.getMessage());
                if (n.getRetryCount() >= maxRetries) {
                    log.warn("Max retries reached for notification {}", n.getNotificationId());
                }
            }
            notifRepo.save(n);
        }
    }

    // ===== CLEANUP OLD (scheduled daily) =====
    @Scheduled(cron = "0 0 3 * * ?") // 3 AM daily
    @Transactional
    public void cleanupOld() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(cleanupDays);
        int deleted = notifRepo.deleteOlderThan(cutoff);
        if (deleted > 0) log.info("Cleaned up {} notifications older than {} days", deleted, cleanupDays);
    }

    // ===== PRIVATE: Channel Dispatch =====
    private void dispatchToChannels(Notification notif, Set<NotificationChannel> channels, UUID userId) {
        UserNotificationPreference pref = getPreferences(userId);

        // Check quiet hours
        if (pref.getQuietHoursEnabled() != null && pref.getQuietHoursEnabled()) {
            int currentHour = LocalDateTime.now().getHour();
            if (isInQuietHours(currentHour, pref.getQuietStartHour(), pref.getQuietEndHour())) {
                // During quiet hours, only send IN_APP (silent)
                log.debug("User {} in quiet hours. Only in-app.", userId);
                return;
            }
        }

        // Skip promotional if user opted out
        if (notif.getEvent() == NotificationEvent.PROMOTIONAL &&
                pref.getPromotionalEnabled() != null && !pref.getPromotionalEnabled()) {
            log.debug("User {} opted out of promotionals. Skipping.", userId);
            return;
        }

        // Dispatch to each channel (respecting preferences)
        if (channels.contains(NotificationChannel.PUSH) &&
                (pref.getPushEnabled() == null || pref.getPushEnabled())) {
            try {
                pushDispatcher.send(notif);
                notif.setPushSent(true);
            } catch (Exception e) {
                log.warn("Push dispatch failed for {}: {}", userId, e.getMessage());
            }
        }

        if (channels.contains(NotificationChannel.SMS) &&
                (pref.getSmsEnabled() == null || pref.getSmsEnabled())) {
            try {
                // In production, phone number would come from User Profile Service
                smsDispatcher.send(notif, null); // TODO: resolve phone from user service
                notif.setSmsSent(true);
            } catch (Exception e) {
                log.warn("SMS dispatch failed for {}: {}", userId, e.getMessage());
            }
        }

        if (channels.contains(NotificationChannel.EMAIL) &&
                (pref.getEmailEnabled() == null || pref.getEmailEnabled())) {
            try {
                // In production, email would come from User Profile Service
                emailDispatcher.send(notif, null); // TODO: resolve email from user service
                notif.setEmailSent(true);
            } catch (Exception e) {
                log.warn("Email dispatch failed for {}: {}", userId, e.getMessage());
            }
        }
    }

    private boolean isInQuietHours(int currentHour, Integer start, Integer end) {
        if (start == null || end == null) return false;
        if (start < end) return currentHour >= start && currentHour < end;
        else return currentHour >= start || currentHour < end; // wraps midnight
    }

    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .notificationId(n.getNotificationId()).userId(n.getUserId())
                .event(n.getEvent().name()).title(n.getTitle()).body(n.getBody())
                .dataJson(n.getDataJson()).priority(n.getPriority().name())
                .status(n.getStatus().name()).isRead(n.getIsRead()).readAt(n.getReadAt())
                .pushSent(n.getPushSent()).smsSent(n.getSmsSent()).emailSent(n.getEmailSent())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
