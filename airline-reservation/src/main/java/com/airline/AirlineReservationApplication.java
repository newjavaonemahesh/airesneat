package com.airline;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AirlineReservationApplication {

    public static void main(String[] args) {
        SpringApplication.run(AirlineReservationApplication.class, args);
    }
}
