package com.helper.user.entity;

import com.helper.user.enums.PaymentMethodType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "saved_payment_methods")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SavedPaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "method_id")
    private UUID methodId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerProfile customerProfile;

    @Enumerated(EnumType.STRING)
    @Column(name = "method_type", nullable = false, length = 20)
    private PaymentMethodType methodType;

    @Column(length = 100)
    private String label; // "Personal UPI", "SBI Debit Card"

    @Column(name = "masked_identifier", length = 50)
    private String maskedIdentifier; // "XXXX1234", "user@upi"

    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;
}
