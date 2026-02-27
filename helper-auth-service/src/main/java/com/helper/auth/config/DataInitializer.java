package com.helper.auth.config;

import com.helper.auth.entity.User;
import com.helper.auth.enums.Role;
import com.helper.auth.enums.VerificationStatus;
import com.helper.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev", "local"})
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Create default admin account if none exists
        if (userRepository.countByRole(Role.ADMIN) == 0) {
            User admin = User.builder()
                    .fullName("Helper Admin")
                    .email("admin@helper.app")
                    .phone("+911234567890")
                    .passwordHash(passwordEncoder.encode("Admin@123"))
                    .role(Role.ADMIN)
                    .emailVerified(true)
                    .verificationStatus(VerificationStatus.VERIFIED)
                    .build();
            userRepository.save(admin);
            log.info("============================================");
            log.info("  Default Admin Account Created (DEV ONLY)");
            log.info("  Email:    admin@helper.app");
            log.info("  Password: Admin@123");
            log.info("  ** CHANGE THIS IN PRODUCTION **");
            log.info("============================================");
        }

        // Create sample customer for testing
        if (userRepository.countByRole(Role.CUSTOMER) == 0) {
            User customer = User.builder()
                    .fullName("Test Customer")
                    .email("customer@test.com")
                    .phone("+911111111111")
                    .passwordHash(passwordEncoder.encode("Test@123"))
                    .role(Role.CUSTOMER)
                    .emailVerified(true)
                    .verificationStatus(VerificationStatus.VERIFIED)
                    .build();
            userRepository.save(customer);
            log.info("  Sample customer created: customer@test.com / Test@123");
        }

        // Create sample worker for testing
        if (userRepository.countByRole(Role.WORKER) == 0) {
            User worker = User.builder()
                    .fullName("Test Worker")
                    .email("worker@test.com")
                    .phone("+912222222222")
                    .passwordHash(passwordEncoder.encode("Test@123"))
                    .role(Role.WORKER)
                    .emailVerified(true)
                    .verificationStatus(VerificationStatus.PENDING)
                    .build();
            userRepository.save(worker);
            log.info("  Sample worker created: worker@test.com / Test@123");
        }
    }
}
