package com.helper.auth.dto.response;

import com.helper.auth.enums.Role;
import com.helper.auth.enums.VerificationStatus;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private UserInfo user;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private UUID userId;
        private String fullName;
        private String email;
        private String phone;
        private Role role;
        private VerificationStatus verificationStatus;
        private boolean emailVerified;
        private String profileImageUrl;
    }
}
