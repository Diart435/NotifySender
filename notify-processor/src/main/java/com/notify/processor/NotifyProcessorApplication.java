package com.notify.processor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.resilience.annotation.EnableResilientMethods;

@SpringBootApplication
@EnableResilientMethods
public class NotifyProcessorApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotifyProcessorApplication.class, args);
    }
}
