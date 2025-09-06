package com.vibecode.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
public class AuthApplication {
    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "auth");
        SpringApplication.run(AuthApplication.class, args);
    }
}
