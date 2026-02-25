package com.helper.notification.service;

import com.helper.notification.enums.NotificationChannel;
import com.helper.notification.enums.NotificationEvent;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Maps each PRD notification event to its required delivery channels.
 * This is the single source of truth from PRD Section 3.8.
 *
 * | Event                | Channels                    |
 * |---------------------|-----------------------------|
 * | New task in area     | Push + In-app               |
 * | New bid received     | Push + In-app               |
 * | Bid accepted/rejected| Push + In-app + SMS         |
 * | Task status change   | Push + In-app               |
 * | Payment received     | Push + In-app + SMS         |
 * | Rating received      | In-app                      |
 * | KYC status update    | Push + In-app + SMS + Email |
 * | Promotional/offers   | Push + Email                |
 */
public class NotificationEventRouter {

    private static final Map<NotificationEvent, Set<NotificationChannel>> EVENT_CHANNELS = new EnumMap<>(NotificationEvent.class);

    static {
        // Task lifecycle
        EVENT_CHANNELS.put(NotificationEvent.NEW_TASK_IN_AREA,
                EnumSet.of(NotificationChannel.PUSH, NotificationChannel.IN_APP));
        EVENT_CHANNELS.put(NotificationEvent.NEW_BID_RECEIVED,
                EnumSet.of(NotificationChannel.PUSH, NotificationChannel.IN_APP));
        EVENT_CHANNELS.put(NotificationEvent.BID_ACCEPTED,
                EnumSet.of(NotificationChannel.PUSH, NotificationChannel.IN_APP, NotificationChannel.SMS));
        EVENT_CHANNELS.put(NotificationEvent.BID_REJECTED,
                EnumSet.of(NotificationChannel.PUSH, NotificationChannel.IN_APP, NotificationChannel.SMS));
        EVENT_CHANNELS.put(NotificationEvent.TASK_STATUS_CHANGE,
                EnumSet.of(NotificationChannel.PUSH, NotificationChannel.IN_APP));
        EVENT_CHANNELS.put(NotificationEvent.TASK_CANCELLED,
                EnumSet.of(NotificationChannel.PUSH, NotificationChannel.IN_APP, NotificationChannel.SMS));

        // Payment
        EVENT_CHANNELS.put(NotificationEvent.PAYMENT_RECEIVED,
                EnumSet.of(NotificationChannel.PUSH, NotificationChannel.IN_APP, NotificationChannel.SMS));
        EVENT_CHANNELS.put(NotificationEvent.PAYMENT_REFUNDED,
                EnumSet.of(NotificationChannel.PUSH, NotificationChannel.IN_APP));

        // Rating
        EVENT_CHANNELS.put(NotificationEvent.RATING_RECEIVED,
                EnumSet.of(NotificationChannel.IN_APP));

        // KYC
        EVENT_CHANNELS.put(NotificationEvent.KYC_SUBMITTED,
                EnumSet.of(NotificationChannel.IN_APP));
        EVENT_CHANNELS.put(NotificationEvent.KYC_APPROVED,
                EnumSet.of(NotificationChannel.PUSH, NotificationChannel.IN_APP, NotificationChannel.SMS, NotificationChannel.EMAIL));
        EVENT_CHANNELS.put(NotificationEvent.KYC_REJECTED,
                EnumSet.of(NotificationChannel.PUSH, NotificationChannel.IN_APP, NotificationChannel.SMS, NotificationChannel.EMAIL));

        // System
        EVENT_CHANNELS.put(NotificationEvent.PROMOTIONAL,
                EnumSet.of(NotificationChannel.PUSH, NotificationChannel.EMAIL));
        EVENT_CHANNELS.put(NotificationEvent.SYSTEM_ALERT,
                EnumSet.of(NotificationChannel.PUSH, NotificationChannel.IN_APP, NotificationChannel.EMAIL));
        EVENT_CHANNELS.put(NotificationEvent.WELCOME,
                EnumSet.of(NotificationChannel.PUSH, NotificationChannel.EMAIL));
    }

    /**
     * Get the channels required for a given event.
     */
    public static Set<NotificationChannel> getChannels(NotificationEvent event) {
        return EVENT_CHANNELS.getOrDefault(event, EnumSet.of(NotificationChannel.IN_APP));
    }

    /**
     * Check if a specific channel is required for an event.
     */
    public static boolean requiresChannel(NotificationEvent event, NotificationChannel channel) {
        return getChannels(event).contains(channel);
    }
}
