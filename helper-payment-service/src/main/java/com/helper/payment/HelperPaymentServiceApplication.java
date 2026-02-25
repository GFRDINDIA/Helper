package com.helper.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HelperPaymentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(HelperPaymentServiceApplication.class, args);
    }
}
