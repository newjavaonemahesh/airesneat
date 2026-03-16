package com.airline.config;

import com.airline.model.*;
import com.airline.repository.FareClassRepository;
import com.airline.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(FareClassRepository fareClassRepository,
                                       FlightRepository flightRepository) {
        return args -> {
            // Create Fare Classes
            FareClass economy = fareClassRepository.save(
                    FareClass.builder()
                            .name("ECONOMY")
                            .basePrice(new BigDecimal("150.00"))
                            .description("Standard economy class seat")
                            .build()
            );

            FareClass premiumEconomy = fareClassRepository.save(
                    FareClass.builder()
                            .name("PREMIUM_ECONOMY")
                            .basePrice(new BigDecimal("300.00"))
                            .description("Premium economy with extra legroom")
                            .build()
            );

            FareClass business = fareClassRepository.save(
                    FareClass.builder()
                            .name("BUSINESS")
                            .basePrice(new BigDecimal("600.00"))
                            .description("Business class with premium amenities")
                            .build()
            );

            FareClass firstClass = fareClassRepository.save(
                    FareClass.builder()
                            .name("FIRST")
                            .basePrice(new BigDecimal("1200.00"))
                            .description("First class luxury experience")
                            .build()
            );

            // Create Flights with Seats
            LocalDateTime now = LocalDateTime.now();

            Flight flight1 = createFlightWithSeats("AA100", "JFK", "LAX",
                    now.plusDays(1), now.plusDays(1).plusHours(6),
                    economy, premiumEconomy, business, firstClass);
            flightRepository.save(flight1);

            Flight flight2 = createFlightWithSeats("UA200", "LAX", "SFO",
                    now.plusDays(2), now.plusDays(2).plusHours(1).plusMinutes(30),
                    economy, premiumEconomy, business, firstClass);
            flightRepository.save(flight2);

            Flight flight3 = createFlightWithSeats("DL300", "ORD", "MIA",
                    now.plusDays(3), now.plusDays(3).plusHours(3),
                    economy, premiumEconomy, business, firstClass);
            flightRepository.save(flight3);
        };
    }

    private Flight createFlightWithSeats(String flightNumber, String origin, String destination,
                                          LocalDateTime departure, LocalDateTime arrival,
                                          FareClass economy, FareClass premiumEconomy,
                                          FareClass business, FareClass firstClass) {
        Flight flight = Flight.builder()
                .flightNumber(flightNumber)
                .origin(origin)
                .destination(destination)
                .departureTime(departure)
                .arrivalTime(arrival)
                .build();

        // First Class seats (Row 1)
        List<String> firstClassSeats = Arrays.asList("1A", "1B", "1C", "1D");
        for (String seatNum : firstClassSeats) {
            flight.addSeat(createSeat(seatNum, firstClass));
        }

        // Business Class seats (Rows 2-3)
        List<String> businessSeats = Arrays.asList("2A", "2B", "2C", "2D", "3A", "3B", "3C", "3D");
        for (String seatNum : businessSeats) {
            flight.addSeat(createSeat(seatNum, business));
        }

        // Premium Economy seats (Rows 4-5)
        List<String> premiumSeats = Arrays.asList("4A", "4B", "4C", "4D", "4E", "4F",
                "5A", "5B", "5C", "5D", "5E", "5F");
        for (String seatNum : premiumSeats) {
            flight.addSeat(createSeat(seatNum, premiumEconomy));
        }

        // Economy seats (Rows 6-10)
        for (int row = 6; row <= 10; row++) {
            for (char col : new char[]{'A', 'B', 'C', 'D', 'E', 'F'}) {
                flight.addSeat(createSeat(row + "" + col, economy));
            }
        }

        return flight;
    }

    private Seat createSeat(String seatNumber, FareClass fareClass) {
        return Seat.builder()
                .seatNumber(seatNumber)
                .status(SeatStatus.AVAILABLE)
                .fareClass(fareClass)
                .build();
    }
}
