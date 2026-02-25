package com.helper.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HelperAuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HelperAuthServiceApplication.class, args);
    }
}
