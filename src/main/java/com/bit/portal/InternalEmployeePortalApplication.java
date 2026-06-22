package com.bit.portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class InternalEmployeePortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(InternalEmployeePortalApplication.class, args);
    }
}
