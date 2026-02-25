package com.helper.user.config;

import com.helper.user.entity.*;
import com.helper.user.enums.*;
import com.helper.user.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final WorkerProfileRepository workerRepo;
    private final WorkerSkillRepository skillRepo;
    private final CustomerProfileRepository customerRepo;

    private static final UUID WORKER_1 = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID WORKER_2 = UUID.fromString("00000000-0000-0000-0000-000000000011");
    private static final UUID CUSTOMER_1 = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Override
    public void run(String... args) {
        if (workerRepo.count() > 0) return;

        // Worker 1: Verified plumber + electrician in Mumbai
        WorkerProfile w1 = workerRepo.save(WorkerProfile.builder()
                .workerId(WORKER_1).bio("Experienced plumber and electrician with 8 years in Mumbai.")
                .latitude(19.0760).longitude(72.8777).baseAddress("Andheri West, Mumbai")
                .averageRating(4.5).totalRatings(28).totalTasksCompleted(35)
                .verificationStatus(VerificationStatus.VERIFIED).isAvailable(true).build());

        skillRepo.save(WorkerSkill.builder().workerProfile(w1).domain(TaskDomain.PLUMBING)
                .priceModel(PricingModel.BOTH).fixedRate(new BigDecimal("500.00"))
                .latitude(19.0760).longitude(72.8777).serviceRadiusKm(15).build());
        skillRepo.save(WorkerSkill.builder().workerProfile(w1).domain(TaskDomain.ELECTRICIAN)
                .priceModel(PricingModel.FIXED).fixedRate(new BigDecimal("800.00"))
                .latitude(19.0760).longitude(72.8777).serviceRadiusKm(10).build());

        w1.getAvailabilitySlots().add(AvailabilitySlot.builder().workerProfile(w1)
                .dayOfWeek(DayOfWeek.MONDAY).startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(18, 0)).build());
        w1.getAvailabilitySlots().add(AvailabilitySlot.builder().workerProfile(w1)
                .dayOfWeek(DayOfWeek.TUESDAY).startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(18, 0)).build());
        w1.getAvailabilitySlots().add(AvailabilitySlot.builder().workerProfile(w1)
                .dayOfWeek(DayOfWeek.WEDNESDAY).startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(18, 0)).build());
        workerRepo.save(w1);

        // Worker 2: Delivery worker pending verification
        WorkerProfile w2 = workerRepo.save(WorkerProfile.builder()
                .workerId(WORKER_2).bio("Fast and reliable delivery services across South Mumbai.")
                .latitude(18.9388).longitude(72.8354).baseAddress("Colaba, Mumbai")
                .verificationStatus(VerificationStatus.PENDING).isAvailable(true).build());

        skillRepo.save(WorkerSkill.builder().workerProfile(w2).domain(TaskDomain.DELIVERY)
                .priceModel(PricingModel.BIDDING)
                .latitude(18.9388).longitude(72.8354).serviceRadiusKm(20).build());

        // Customer profile
        CustomerProfile c1 = customerRepo.save(CustomerProfile.builder()
                .customerId(CUSTOMER_1).averageRating(4.8).totalRatings(12).totalTasksPosted(15).build());

        c1.getAddresses().add(CustomerAddress.builder().customerProfile(c1)
                .label("Home").addressLine1("123 Marine Drive").city("Mumbai")
                .state("Maharashtra").pinCode("400020").latitude(18.9440).longitude(72.8234).isDefault(true).build());
        c1.getAddresses().add(CustomerAddress.builder().customerProfile(c1)
                .label("Office").addressLine1("456 BKC Road").city("Mumbai")
                .state("Maharashtra").pinCode("400051").latitude(19.0654).longitude(72.8687).isDefault(false).build());
        customerRepo.save(c1);

        log.info("============================================");
        log.info("  Sample profiles created:");
        log.info("  - 2 Worker profiles (1 verified, 1 pending)");
        log.info("  - 1 Customer profile with 2 addresses");
        log.info("  - Skills: Plumbing, Electrician, Delivery");
        log.info("============================================");
    }
}
