package com.helper.rating;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HelperRatingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(HelperRatingServiceApplication.class, args);
    }
}
