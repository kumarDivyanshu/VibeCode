package com.vibecode.interview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
public class InterviewApplication {
    public static void main(String[] args) {
//        System.setProperty("spring.profiles.active", "interview");
        SpringApplication.run(InterviewApplication.class, args);
    }
}
