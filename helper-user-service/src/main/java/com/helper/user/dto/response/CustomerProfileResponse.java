package com.helper.user.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CustomerProfileResponse {

    private UUID customerId;
    private String profileImageUrl;
    private Double averageRating;
    private Integer totalRatings;
    private Integer totalTasksPosted;
    private List<AddressResponse> addresses;
    private List<PaymentMethodResponse> paymentMethods;
    private LocalDateTime createdAt;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AddressResponse {
        private UUID addressId;
        private String label;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String pinCode;
        private Double latitude;
        private Double longitude;
        private Boolean isDefault;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PaymentMethodResponse {
        private UUID methodId;
        private String methodType;
        private String label;
        private String maskedIdentifier;
        private Boolean isDefault;
    }
}
