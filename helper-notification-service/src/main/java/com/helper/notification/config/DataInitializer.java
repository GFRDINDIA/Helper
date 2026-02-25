package com.helper.notification.config;

import com.helper.notification.entity.DeviceToken;
import com.helper.notification.entity.Notification;
import com.helper.notification.entity.UserNotificationPreference;
import com.helper.notification.enums.NotificationEvent;
import com.helper.notification.enums.NotificationPriority;
import com.helper.notification.enums.NotificationStatus;
import com.helper.notification.repository.DeviceTokenRepository;
import com.helper.notification.repository.NotificationRepository;
import com.helper.notification.repository.UserNotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final NotificationRepository notifRepo;
    private final DeviceTokenRepository deviceRepo;
    private final UserNotificationPreferenceRepository prefRepo;

    private static final UUID CUSTOMER_1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID WORKER_1 = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID WORKER_2 = UUID.fromString("00000000-0000-0000-0000-000000000011");
    private static final UUID TASK_1 = UUID.fromString("00000000-0000-0000-0000-000000000100");

    @Override
    public void run(String... args) {
        if (notifRepo.count() > 0) return;

        // Sample notifications
        notifRepo.save(Notification.builder().userId(WORKER_1).event(NotificationEvent.BID_ACCEPTED)
                .title("Bid Accepted!").body("Your bid on \"Fix kitchen sink\" has been accepted.")
                .dataJson("{\"taskId\":\"" + TASK_1 + "\"}").priority(NotificationPriority.HIGH)
                .status(NotificationStatus.SENT).pushSent(true).smsSent(true).build());

        notifRepo.save(Notification.builder().userId(WORKER_1).event(NotificationEvent.PAYMENT_RECEIVED)
                .title("Payment Received: ₹500").body("You received ₹500 for plumbing task.")
                .dataJson("{\"taskId\":\"" + TASK_1 + "\",\"amount\":\"500\"}")
                .priority(NotificationPriority.HIGH).status(NotificationStatus.SENT)
                .pushSent(true).smsSent(true).build());

        notifRepo.save(Notification.builder().userId(WORKER_1).event(NotificationEvent.RATING_RECEIVED)
                .title("New 5-Star Rating").body("You received a 5-star rating!")
                .priority(NotificationPriority.NORMAL).status(NotificationStatus.SENT).build());

        notifRepo.save(Notification.builder().userId(CUSTOMER_1).event(NotificationEvent.NEW_BID_RECEIVED)
                .title("New Bid: ₹500 from Rajesh").body("Rajesh bid ₹500 on your plumbing task.")
                .dataJson("{\"taskId\":\"" + TASK_1 + "\",\"bidAmount\":\"500\"}")
                .priority(NotificationPriority.NORMAL).status(NotificationStatus.SENT).pushSent(true).build());

        notifRepo.save(Notification.builder().userId(CUSTOMER_1).event(NotificationEvent.TASK_STATUS_CHANGE)
                .title("Task Update").body("Your task \"Fix kitchen sink\" is now IN_PROGRESS.")
                .priority(NotificationPriority.NORMAL).status(NotificationStatus.SENT).build());

        // Unread notification
        notifRepo.save(Notification.builder().userId(WORKER_2).event(NotificationEvent.KYC_APPROVED)
                .title("KYC Approved!").body("Congratulations! Your verification is complete.")
                .priority(NotificationPriority.HIGH).status(NotificationStatus.SENT)
                .pushSent(true).smsSent(true).emailSent(true).build());

        // Failed notification (for retry testing)
        notifRepo.save(Notification.builder().userId(WORKER_2).event(NotificationEvent.NEW_TASK_IN_AREA)
                .title("New Task Nearby").body("Delivery task posted 2km away.")
                .priority(NotificationPriority.NORMAL).status(NotificationStatus.FAILED)
                .retryCount(1).errorMessage("FCM token expired").build());

        // Device tokens
        deviceRepo.save(DeviceToken.builder().userId(WORKER_1)
                .token("FCM_TOKEN_WORKER1_ANDROID_" + UUID.randomUUID().toString().substring(0, 8))
                .platform("ANDROID").deviceName("Samsung Galaxy S24").isActive(true).build());
        deviceRepo.save(DeviceToken.builder().userId(CUSTOMER_1)
                .token("FCM_TOKEN_CUSTOMER1_IOS_" + UUID.randomUUID().toString().substring(0, 8))
                .platform("IOS").deviceName("iPhone 15 Pro").isActive(true).build());
        deviceRepo.save(DeviceToken.builder().userId(WORKER_2)
                .token("FCM_TOKEN_WORKER2_ANDROID_" + UUID.randomUUID().toString().substring(0, 8))
                .platform("ANDROID").deviceName("OnePlus 12").isActive(true).build());

        // Preferences
        prefRepo.save(UserNotificationPreference.builder().userId(WORKER_1)
                .pushEnabled(true).smsEnabled(true).emailEnabled(true).inAppEnabled(true)
                .quietHoursEnabled(true).quietStartHour(22).quietEndHour(7)
                .promotionalEnabled(true).build());
        prefRepo.save(UserNotificationPreference.builder().userId(CUSTOMER_1)
                .pushEnabled(true).smsEnabled(false).emailEnabled(true).inAppEnabled(true)
                .quietHoursEnabled(false).promotionalEnabled(false).build());

        log.info("============================================");
        log.info("  Notification Service - Sample data:");
        log.info("  - 7 Notifications (5 sent, 1 unread, 1 failed)");
        log.info("  - 3 Device tokens (2 Android, 1 iOS)");
        log.info("  - 2 User preferences");
        log.info("  - Worker 1: quiet hours 10PM-7AM");
        log.info("  - Customer 1: SMS off, promotionals off");
        log.info("============================================");
    }
}
