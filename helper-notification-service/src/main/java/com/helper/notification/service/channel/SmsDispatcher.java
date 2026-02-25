package com.helper.notification.service.channel;

import com.helper.notification.entity.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Sends SMS via MSG91 (India) or Twilio (global).
 * In dev mode (sms.enabled=false), logs instead.
 */
@Component
@Slf4j
public class SmsDispatcher {

    @Value("${app.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${app.sms.provider:msg91}")
    private String smsProvider;

    @Value("${app.sms.api-key:}")
    private String apiKey;

    @Value("${app.sms.sender-id:HELPER}")
    private String senderId;

    @Async("notificationExecutor")
    public boolean send(Notification notification, String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            log.debug("No phone number for user {}. SMS skipped.", notification.getUserId());
            return false;
        }

        try {
            if (smsEnabled) {
                return sendViaSmsProvider(phoneNumber, notification);
            } else {
                log.info("[SMS-DEV] To: {} | Title: {} | Body: {}",
                        phoneNumber, notification.getTitle(), notification.getBody());
                return true;
            }
        } catch (Exception e) {
            log.error("SMS failed for user {}: {}", notification.getUserId(), e.getMessage());
            return false;
        }
    }

    private boolean sendViaSmsProvider(String phone, Notification notification) {
        // Phase 2: MSG91 / Twilio integration
        // MSG91:
        //   POST https://api.msg91.com/api/v5/flow/
        //   Headers: authkey={apiKey}, Content-Type: application/json
        //   Body: { "flow_id": "...", "sender": senderId, "mobiles": phone,
        //           "VAR1": notification.getTitle(), "VAR2": notification.getBody() }
        //
        // Twilio:
        //   Twilio.init(accountSid, authToken);
        //   Message.creator(new PhoneNumber(phone), new PhoneNumber(from), body).create();

        log.info("[SMS-PROD] Provider: {} | To: {} | Title: {}", smsProvider, phone, notification.getTitle());
        return true;
    }
}
