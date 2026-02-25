package com.helper.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HelperUserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(HelperUserServiceApplication.class, args);
    }
}
