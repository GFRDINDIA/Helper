package com.helper.notification.service.channel;

import com.helper.notification.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;

/**
 * Sends emails via Spring Mail + Thymeleaf HTML templates.
 * In dev mode (mail.enabled=false), logs instead.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailDispatcher {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from-name:Helper}")
    private String fromName;

    @Value("${app.mail.from-address:noreply@helper.app}")
    private String fromAddress;

    @Async("notificationExecutor")
    public boolean send(Notification notification, String emailAddress) {
        if (emailAddress == null || emailAddress.isBlank()) {
            log.debug("No email for user {}. Email skipped.", notification.getUserId());
            return false;
        }

        try {
            if (mailEnabled) {
                return sendViaSmtp(emailAddress, notification);
            } else {
                log.info("[EMAIL-DEV] To: {} | Subject: {} | Body: {}",
                        emailAddress, notification.getTitle(), notification.getBody());
                return true;
            }
        } catch (Exception e) {
            log.error("Email failed for user {}: {}", notification.getUserId(), e.getMessage());
            return false;
        }
    }

    private boolean sendViaSmtp(String to, Notification notification) {
        try {
            // Build HTML from template
            Context ctx = new Context();
            ctx.setVariable("title", notification.getTitle());
            ctx.setVariable("body", notification.getBody());
            ctx.setVariable("event", notification.getEvent().name());
            ctx.setVariable("year", java.time.Year.now().getValue());
            String htmlBody = templateEngine.process("notification-email", ctx);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress, fromName);
            helper.setTo(to);
            helper.setSubject(notification.getTitle());
            helper.setText(htmlBody, true);
            mailSender.send(message);

            log.info("[EMAIL-SMTP] Sent to: {} | Subject: {}", to, notification.getTitle());
            return true;
        } catch (Exception e) {
            log.error("SMTP send failed to {}: {}", to, e.getMessage());
            return false;
        }
    }
}
