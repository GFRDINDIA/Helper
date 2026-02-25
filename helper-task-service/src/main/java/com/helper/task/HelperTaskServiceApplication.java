package com.helper.task;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HelperTaskServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HelperTaskServiceApplication.class, args);
    }
}
