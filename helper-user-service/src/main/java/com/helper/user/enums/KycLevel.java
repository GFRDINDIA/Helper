package com.helper.user.enums;

/**
 * KYC levels per domain (from PRD Section 3.3):
 * BASIC: Delivery, Farming, Logistics, Household
 * PROFESSIONAL: Electrician, Plumbing, Construction
 * PROFESSIONAL_PLUS_LICENSE: Medical, Finance, Education
 */
public enum KycLevel {
    BASIC,
    PROFESSIONAL,
    PROFESSIONAL_PLUS_LICENSE;

    public static KycLevel forDomain(TaskDomain domain) {
        return switch (domain) {
            case DELIVERY, FARMING, LOGISTICS, HOUSEHOLD -> BASIC;
            case ELECTRICIAN, PLUMBING, CONSTRUCTION -> PROFESSIONAL;
            case MEDICAL, FINANCE, EDUCATION -> PROFESSIONAL_PLUS_LICENSE;
        };
    }
}
