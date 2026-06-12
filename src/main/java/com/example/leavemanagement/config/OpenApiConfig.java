package com.example.leavemanagement.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger configuration. Authentication has been removed from this
 * build, so the API is open and Swagger UI can call every endpoint directly.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI leaveManagementOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Annual Leave Management API (Camunda 7)")
                        .version("1.0.0")
                        .description("Workflow-driven leave request management built on the embedded Camunda 7 engine.")
                        .contact(new Contact().name("Platform Team").email("platform@example.com"))
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
