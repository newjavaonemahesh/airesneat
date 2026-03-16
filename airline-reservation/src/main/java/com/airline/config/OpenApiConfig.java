package com.airline.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI airlineOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Airline Seat Reservation API")
                        .description("REST API for airline seat reservation system. " +
                                "Supports flight search, seat viewing, seat holds with 10-minute expiration, " +
                                "booking confirmation, and booking cancellation.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Airline Support")
                                .email("support@airline.com")));
    }
}
