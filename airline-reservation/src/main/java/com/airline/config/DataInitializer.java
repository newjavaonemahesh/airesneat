package com.airline.config;

import com.airline.model.*;
import com.airline.repository.FlightRepository;
import com.airline.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(FlightRepository flightRepository,
                                       PassengerRepository passengerRepository) {
        return args -> {
            log.info("Initializing sample data...");

            // Create sample passengers
            passengerRepository.save(Passenger.builder()
                    .name("John Doe")
                    .email("john@example.com")
                    .build());
            
            passengerRepository.save(Passenger.builder()
                    .name("Jane Smith")
                    .email("jane@example.com")
                    .build());

            LocalDateTime now = LocalDateTime.now();

            // Flight 1: JFK -> LAX
            Flight flight1 = createFlightWithSeats(
                    "AA100", "JFK", "LAX",
                    now.plusDays(1).withHour(8).withMinute(0),
                    now.plusDays(1).withHour(11).withMinute(30)
            );
            flightRepository.save(flight1);

            // Flight 2: LAX -> ORD
            Flight flight2 = createFlightWithSeats(
                    "UA200", "LAX", "ORD",
                    now.plusDays(2).withHour(10).withMinute(0),
                    now.plusDays(2).withHour(16).withMinute(0)
            );
            flightRepository.save(flight2);

            // Flight 3: ORD -> MIA
            Flight flight3 = createFlightWithSeats(
                    "DL300", "ORD", "MIA",
                    now.plusDays(3).withHour(14).withMinute(0),
                    now.plusDays(3).withHour(18).withMinute(30)
            );
            flightRepository.save(flight3);

            log.info("Sample data initialized: 3 flights with 30 seats each");
        };
    }

    /**
     * Creates a flight with 30 seats:
     * - Rows 1-2: Business class (10 seats, 5 per row)
     * - Rows 3-6: Economy class (20 seats, 5 per row)
     */
    private Flight createFlightWithSeats(String flightNumber, 
                                          String departureAirport, 
                                          String arrivalAirport,
                                          LocalDateTime departureTime, 
                                          LocalDateTime arrivalTime) {
        Flight flight = Flight.builder()
                .flightNumber(flightNumber)
                .departureAirport(departureAirport)
                .arrivalAirport(arrivalAirport)
                .departureTime(departureTime)
                .arrivalTime(arrivalTime)
                .build();

        // Business class: Rows 1-2 (10 seats)
        for (int row = 1; row <= 2; row++) {
            for (char col : new char[]{'A', 'B', 'C', 'D', 'E'}) {
                Seat seat = Seat.builder()
                        .seatNumber(row + String.valueOf(col))
                        .rowNumber(row)
                        .fareClass(FareClass.BUSINESS)
                        .status(SeatStatus.AVAILABLE)
                        .build();
                flight.addSeat(seat);
            }
        }

        // Economy class: Rows 3-6 (20 seats)
        for (int row = 3; row <= 6; row++) {
            for (char col : new char[]{'A', 'B', 'C', 'D', 'E'}) {
                Seat seat = Seat.builder()
                        .seatNumber(row + String.valueOf(col))
                        .rowNumber(row)
                        .fareClass(FareClass.ECONOMY)
                        .status(SeatStatus.AVAILABLE)
                        .build();
                flight.addSeat(seat);
            }
        }

        return flight;
    }
}
