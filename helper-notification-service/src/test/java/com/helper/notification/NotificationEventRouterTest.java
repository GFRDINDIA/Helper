package com.helper.notification;

import com.helper.notification.enums.NotificationChannel;
import com.helper.notification.enums.NotificationEvent;
import com.helper.notification.service.NotificationEventRouter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that the NotificationEventRouter correctly maps PRD Section 3.8 events.
 */
class NotificationEventRouterTest {

    @Test
    @DisplayName("PRD: New task in area → Push + In-app")
    void testNewTaskInArea() {
        Set<NotificationChannel> ch = NotificationEventRouter.getChannels(NotificationEvent.NEW_TASK_IN_AREA);
        assertEquals(2, ch.size());
        assertTrue(ch.contains(NotificationChannel.PUSH));
        assertTrue(ch.contains(NotificationChannel.IN_APP));
        assertFalse(ch.contains(NotificationChannel.SMS));
        assertFalse(ch.contains(NotificationChannel.EMAIL));
    }

    @Test
    @DisplayName("PRD: New bid received → Push + In-app")
    void testNewBidReceived() {
        Set<NotificationChannel> ch = NotificationEventRouter.getChannels(NotificationEvent.NEW_BID_RECEIVED);
        assertTrue(ch.contains(NotificationChannel.PUSH));
        assertTrue(ch.contains(NotificationChannel.IN_APP));
        assertEquals(2, ch.size());
    }

    @Test
    @DisplayName("PRD: Bid accepted → Push + In-app + SMS")
    void testBidAccepted() {
        Set<NotificationChannel> ch = NotificationEventRouter.getChannels(NotificationEvent.BID_ACCEPTED);
        assertTrue(ch.contains(NotificationChannel.PUSH));
        assertTrue(ch.contains(NotificationChannel.IN_APP));
        assertTrue(ch.contains(NotificationChannel.SMS));
        assertEquals(3, ch.size());
    }

    @Test
    @DisplayName("PRD: Bid rejected → Push + In-app + SMS")
    void testBidRejected() {
        Set<NotificationChannel> ch = NotificationEventRouter.getChannels(NotificationEvent.BID_REJECTED);
        assertEquals(3, ch.size());
        assertTrue(ch.contains(NotificationChannel.SMS));
    }

    @Test
    @DisplayName("PRD: Task status change → Push + In-app")
    void testTaskStatusChange() {
        Set<NotificationChannel> ch = NotificationEventRouter.getChannels(NotificationEvent.TASK_STATUS_CHANGE);
        assertEquals(2, ch.size());
        assertTrue(ch.contains(NotificationChannel.PUSH));
        assertTrue(ch.contains(NotificationChannel.IN_APP));
    }

    @Test
    @DisplayName("PRD: Payment received → Push + In-app + SMS")
    void testPaymentReceived() {
        Set<NotificationChannel> ch = NotificationEventRouter.getChannels(NotificationEvent.PAYMENT_RECEIVED);
        assertEquals(3, ch.size());
        assertTrue(ch.contains(NotificationChannel.SMS));
    }

    @Test
    @DisplayName("PRD: Rating received → In-app only")
    void testRatingReceived() {
        Set<NotificationChannel> ch = NotificationEventRouter.getChannels(NotificationEvent.RATING_RECEIVED);
        assertEquals(1, ch.size());
        assertTrue(ch.contains(NotificationChannel.IN_APP));
    }

    @Test
    @DisplayName("PRD: KYC approved → Push + In-app + SMS + Email (all 4)")
    void testKycApproved() {
        Set<NotificationChannel> ch = NotificationEventRouter.getChannels(NotificationEvent.KYC_APPROVED);
        assertEquals(4, ch.size());
        assertTrue(ch.contains(NotificationChannel.PUSH));
        assertTrue(ch.contains(NotificationChannel.IN_APP));
        assertTrue(ch.contains(NotificationChannel.SMS));
        assertTrue(ch.contains(NotificationChannel.EMAIL));
    }

    @Test
    @DisplayName("PRD: KYC rejected → Push + In-app + SMS + Email (all 4)")
    void testKycRejected() {
        Set<NotificationChannel> ch = NotificationEventRouter.getChannels(NotificationEvent.KYC_REJECTED);
        assertEquals(4, ch.size());
    }

    @Test
    @DisplayName("PRD: Promotional → Push + Email")
    void testPromotional() {
        Set<NotificationChannel> ch = NotificationEventRouter.getChannels(NotificationEvent.PROMOTIONAL);
        assertEquals(2, ch.size());
        assertTrue(ch.contains(NotificationChannel.PUSH));
        assertTrue(ch.contains(NotificationChannel.EMAIL));
        assertFalse(ch.contains(NotificationChannel.SMS));
    }

    @ParameterizedTest
    @EnumSource(NotificationEvent.class)
    @DisplayName("Every event has at least one channel")
    void testAllEventsHaveChannels(NotificationEvent event) {
        Set<NotificationChannel> ch = NotificationEventRouter.getChannels(event);
        assertNotNull(ch);
        assertFalse(ch.isEmpty(), "Event " + event + " has no channels!");
    }

    @Test
    @DisplayName("requiresChannel helper works correctly")
    void testRequiresChannel() {
        assertTrue(NotificationEventRouter.requiresChannel(NotificationEvent.PAYMENT_RECEIVED, NotificationChannel.SMS));
        assertFalse(NotificationEventRouter.requiresChannel(NotificationEvent.RATING_RECEIVED, NotificationChannel.SMS));
        assertTrue(NotificationEventRouter.requiresChannel(NotificationEvent.RATING_RECEIVED, NotificationChannel.IN_APP));
    }
}
