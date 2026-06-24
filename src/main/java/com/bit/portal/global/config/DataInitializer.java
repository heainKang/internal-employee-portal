package com.bit.portal.global.config;

import com.bit.portal.domain.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final EmployeeService employeeService;

    @Value("${app.admin.initial-email}")
    private String adminEmail;

    @Value("${app.admin.initial-password}")
    private String adminPassword;

    @Value("${app.test.seed-employees:false}")
    private boolean seedTestEmployees;

    @Value("${app.test.seed-password:Test1234!}")
    private String seedPassword;

    @Override
    public void run(ApplicationArguments args) {
        employeeService.createInitialAdmin(adminEmail, adminPassword);
        log.info("DataInitializer: admin account ready [{}]", adminEmail);

        if (seedTestEmployees) {
            employeeService.createTestEmployees(seedPassword);
        }
    }
}
