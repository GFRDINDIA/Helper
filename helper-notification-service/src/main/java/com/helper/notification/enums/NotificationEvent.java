package com.helper.notification.enums;

/**
 * Maps directly to PRD Section 3.8 event matrix.
 * Each event defines WHO gets notified and via WHICH channels.
 */
public enum NotificationEvent {
    // Task lifecycle
    NEW_TASK_IN_AREA,       // Workers near task → Push + In-app
    NEW_BID_RECEIVED,       // Customer → Push + In-app
    BID_ACCEPTED,           // Worker → Push + In-app + SMS
    BID_REJECTED,           // Worker → Push + In-app + SMS
    TASK_STATUS_CHANGE,     // Both → Push + In-app
    TASK_CANCELLED,         // Both → Push + In-app + SMS

    // Payment
    PAYMENT_RECEIVED,       // Worker → Push + In-app + SMS
    PAYMENT_REFUNDED,       // Both → Push + In-app

    // Rating
    RATING_RECEIVED,        // Both → In-app

    // KYC
    KYC_SUBMITTED,          // Admin → In-app
    KYC_APPROVED,           // Worker → Push + In-app + SMS + Email
    KYC_REJECTED,           // Worker → Push + In-app + SMS + Email

    // System
    PROMOTIONAL,            // All → Push + Email
    SYSTEM_ALERT,           // Target → Push + In-app + Email
    WELCOME,                // New user → Push + Email
}
