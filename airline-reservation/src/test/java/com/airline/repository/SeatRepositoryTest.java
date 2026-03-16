package com.airline.repository;

import com.airline.model.FareClass;
import com.airline.model.Flight;
import com.airline.model.Seat;
import com.airline.model.SeatStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SeatRepositoryTest {

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private FareClassRepository fareClassRepository;

    private Flight testFlight;
    private FareClass testFareClass;

    @BeforeEach
    void setUp() {
        testFareClass = fareClassRepository.save(FareClass.builder()
                .name("ECONOMY")
                .basePrice(new BigDecimal("150.00"))
                .build());

        testFlight = Flight.builder()
                .flightNumber("TEST100")
                .origin("JFK")
                .destination("LAX")
                .departureTime(LocalDateTime.now().plusDays(1))
                .arrivalTime(LocalDateTime.now().plusDays(1).plusHours(5))
                .build();

        Seat seat1 = Seat.builder()
                .seatNumber("1A")
                .status(SeatStatus.AVAILABLE)
                .fareClass(testFareClass)
                .build();

        Seat seat2 = Seat.builder()
                .seatNumber("1B")
                .status(SeatStatus.HELD)
                .fareClass(testFareClass)
                .build();

        testFlight.addSeat(seat1);
        testFlight.addSeat(seat2);
        testFlight = flightRepository.save(testFlight);
    }

    @Test
    void findByFlightId_ShouldReturnAllSeats() {
        List<Seat> seats = seatRepository.findByFlightId(testFlight.getId());
        assertThat(seats).hasSize(2);
    }

    @Test
    void findByFlightIdAndStatus_ShouldReturnFilteredSeats() {
        List<Seat> availableSeats = seatRepository.findByFlightIdAndStatus(
                testFlight.getId(), SeatStatus.AVAILABLE);
        
        assertThat(availableSeats).hasSize(1);
        assertThat(availableSeats.get(0).getSeatNumber()).isEqualTo("1A");
    }

    @Test
    void countByFlightIdAndStatus_ShouldReturnCorrectCount() {
        int count = seatRepository.countByFlightIdAndStatus(
                testFlight.getId(), SeatStatus.AVAILABLE);
        
        assertThat(count).isEqualTo(1);
    }
}
