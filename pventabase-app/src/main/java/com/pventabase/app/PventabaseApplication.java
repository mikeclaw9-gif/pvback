package com.pventabase.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.pventabase")
@EntityScan(basePackages = "com.pventabase")
@EnableJpaRepositories(basePackages = "com.pventabase")
public class PventabaseApplication {
    public static void main(String[] args) {
        SpringApplication.run(PventabaseApplication.class, args);
    }
}
