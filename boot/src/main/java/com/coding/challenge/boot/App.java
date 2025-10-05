package com.coding.challenge.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hello world!
 *
 */

@SpringBootApplication(scanBasePackages = { 
        "com.coding.challenge.boot",
        "com.coding.challenge.api",
        "com.coding.challenge.application",
        "com.coding.challenge.infrastructure",
        })
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
