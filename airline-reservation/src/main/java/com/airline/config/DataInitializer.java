package com.airline.config;

import com.airline.model.*;
import com.airline.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(FlightRepository flightRepository) {
        return args -> {
            LocalDateTime now = LocalDateTime.now();

            Flight flight1 = createFlightWithSeats("AA100", "JFK", "LAX",
                    now.plusDays(1), now.plusDays(1).plusHours(6));
            flightRepository.save(flight1);

            Flight flight2 = createFlightWithSeats("UA200", "LAX", "SFO",
                    now.plusDays(2), now.plusDays(2).plusHours(1).plusMinutes(30));
            flightRepository.save(flight2);

            Flight flight3 = createFlightWithSeats("DL300", "ORD", "MIA",
                    now.plusDays(3), now.plusDays(3).plusHours(3));
            flightRepository.save(flight3);
        };
    }

    private Flight createFlightWithSeats(String flightNumber, String departureAirport, String arrivalAirport,
                                          LocalDateTime departure, LocalDateTime arrival) {
        Flight flight = Flight.builder()
                .flightNumber(flightNumber)
                .departureAirport(departureAirport)
                .arrivalAirport(arrivalAirport)
                .departureTime(departure)
                .arrivalTime(arrival)
                .build();

        // First Class seats (Row 1)
        List<String> firstClassCols = Arrays.asList("A", "B", "C", "D");
        for (String col : firstClassCols) {
            flight.addSeat(createSeat("1" + col, 1, FareClass.FIRST));
        }

        // Business Class seats (Rows 2-3)
        for (int row = 2; row <= 3; row++) {
            for (String col : Arrays.asList("A", "B", "C", "D")) {
                flight.addSeat(createSeat(row + col, row, FareClass.BUSINESS));
            }
        }

        // Premium Economy seats (Rows 4-5)
        for (int row = 4; row <= 5; row++) {
            for (String col : Arrays.asList("A", "B", "C", "D", "E", "F")) {
                flight.addSeat(createSeat(row + col, row, FareClass.PREMIUM_ECONOMY));
            }
        }

        // Economy seats (Rows 6-10)
        for (int row = 6; row <= 10; row++) {
            for (String col : Arrays.asList("A", "B", "C", "D", "E", "F")) {
                flight.addSeat(createSeat(row + col, row, FareClass.ECONOMY));
            }
        }

        return flight;
    }

    private Seat createSeat(String seatNumber, int rowNumber, FareClass fareClass) {
        return Seat.builder()
                .seatNumber(seatNumber)
                .rowNumber(rowNumber)
                .fareClass(fareClass)
                .status(SeatStatus.AVAILABLE)
                .build();
    }
}
