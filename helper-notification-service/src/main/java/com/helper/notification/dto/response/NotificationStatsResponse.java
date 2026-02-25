package com.helper.notification.dto.response;

import lombok.*;
import java.util.Map;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationStatsResponse {
    private long totalNotifications;
    private long pendingNotifications;
    private long sentNotifications;
    private long failedNotifications;
    private long activeDeviceTokens;
    private Map<String, Long> byStatus;
}
