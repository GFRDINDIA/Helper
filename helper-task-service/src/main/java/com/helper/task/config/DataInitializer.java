package com.helper.task.config;

import com.helper.task.entity.Task;
import com.helper.task.enums.*;
import com.helper.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final TaskRepository taskRepository;

    // Sample UUIDs matching auth service test users
    private static final UUID CUSTOMER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID WORKER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Override
    public void run(String... args) {
        if (taskRepository.count() > 0) return;

        // Sample tasks across different domains - located in Mumbai, India
        List<Task> sampleTasks = List.of(
                Task.builder()
                        .customerId(CUSTOMER_ID).title("Fix leaking kitchen tap")
                        .description("The kitchen tap has been leaking for 2 days. Need a plumber to fix it urgently.")
                        .domain(TaskDomain.PLUMBING).pricingModel(PricingModel.BIDDING)
                        .status(TaskStatus.OPEN).budget(new BigDecimal("500.00"))
                        .latitude(19.0760).longitude(72.8777).address("Andheri West, Mumbai 400053")
                        .build(),
                Task.builder()
                        .customerId(CUSTOMER_ID).title("Home deep cleaning - 2BHK")
                        .description("Need full deep cleaning of 2BHK apartment including kitchen and bathrooms.")
                        .domain(TaskDomain.HOUSEHOLD).pricingModel(PricingModel.FIXED)
                        .status(TaskStatus.OPEN).budget(new BigDecimal("2000.00"))
                        .latitude(19.1136).longitude(72.8697).address("Goregaon East, Mumbai 400063")
                        .build(),
                Task.builder()
                        .customerId(CUSTOMER_ID).title("Electrical wiring for new AC unit")
                        .description("Need electrician to install wiring and MCB for new 1.5 ton split AC.")
                        .domain(TaskDomain.ELECTRICIAN).pricingModel(PricingModel.BIDDING)
                        .status(TaskStatus.OPEN).budget(new BigDecimal("1500.00"))
                        .latitude(19.0178).longitude(72.8478).address("Dadar West, Mumbai 400028")
                        .build(),
                Task.builder()
                        .customerId(CUSTOMER_ID).title("Maths tutor for Class 10 CBSE")
                        .description("Looking for experienced maths tutor for my child preparing for board exams. 3 days/week.")
                        .domain(TaskDomain.EDUCATION).pricingModel(PricingModel.BIDDING)
                        .status(TaskStatus.OPEN).budget(new BigDecimal("5000.00"))
                        .latitude(19.0596).longitude(72.8295).address("Bandra West, Mumbai 400050")
                        .build(),
                Task.builder()
                        .customerId(CUSTOMER_ID).title("Deliver birthday cake to Powai")
                        .description("Pick up cake from Theobroma Bandra and deliver to Powai by 5 PM today.")
                        .domain(TaskDomain.DELIVERY).pricingModel(PricingModel.FIXED)
                        .status(TaskStatus.OPEN).budget(new BigDecimal("200.00"))
                        .latitude(19.0544).longitude(72.8406).address("Bandra, Mumbai")
                        .build()
        );

        taskRepository.saveAll(sampleTasks);
        log.info("============================================");
        log.info("  {} sample tasks created for development", sampleTasks.size());
        log.info("  Domains: Plumbing, Household, Electrician, Education, Delivery");
        log.info("  Location: Mumbai, India");
        log.info("============================================");
    }
}
