package com.helper.notification.dto.request;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdatePreferencesRequest {
    private Boolean pushEnabled;
    private Boolean smsEnabled;
    private Boolean emailEnabled;
    private Boolean inAppEnabled;
    private Boolean quietHoursEnabled;
    private Integer quietStartHour;
    private Integer quietEndHour;
    private Boolean promotionalEnabled;
}
