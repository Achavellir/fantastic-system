package com.samap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AccessManagerApplication {

    public static void main(String[] args) {
        System.out.println("Starting SAMAP - Secure Access Management & Audit Platform...");
        SpringApplication.run(AccessManagerApplication.class, args);
    }
}
