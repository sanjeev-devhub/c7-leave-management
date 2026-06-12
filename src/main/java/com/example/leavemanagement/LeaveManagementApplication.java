package com.example.leavemanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Annual Leave Management application running on the
 * embedded Camunda 7 engine.
 *
 * <p>BPMN deployment is performed explicitly at startup by
 * {@code StartupProcessDeployer} via the {@code RepositoryService}, rather than
 * relying on {@code @EnableProcessApplication} + {@code META-INF/processes.xml}.
 * Explicit deployment is deterministic across the Spring Boot starter and a
 * manual/modular embedded-engine setup, and logs exactly what was deployed.</p>
 */
@SpringBootApplication
public class LeaveManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeaveManagementApplication.class, args);
    }
}