package com.helper.task.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class AuthenticatedUser {

    private UUID userId;
    private String email;
    private String role;

    public boolean isCustomer() {
        return "CUSTOMER".equals(role);
    }

    public boolean isWorker() {
        return "WORKER".equals(role);
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
