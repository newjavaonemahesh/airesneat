package com.airline.service;

import com.airline.dto.FlightDTO;
import com.airline.dto.SeatDTO;
import com.airline.exception.ResourceNotFoundException;
import com.airline.model.*;
import com.airline.repository.FlightRepository;
import com.airline.repository.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlightServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private SeatRepository seatRepository;

    @InjectMocks
    private FlightService flightService;

    private Flight testFlight;
    private Seat testSeat;

    @BeforeEach
    void setUp() {
        testFlight = Flight.builder()
                .id(1L)
                .flightNumber("AA100")
                .departureAirport("JFK")
                .arrivalAirport("LAX")
                .departureTime(LocalDateTime.of(2024, 1, 15, 10, 0))
                .arrivalTime(LocalDateTime.of(2024, 1, 15, 16, 0))
                .build();

        testSeat = Seat.builder()
                .id(1L)
                .seatNumber("1A")
                .rowNumber(1)
                .fareClass(FareClass.BUSINESS)
                .status(SeatStatus.AVAILABLE)
                .flight(testFlight)
                .build();

        testFlight.getSeats().add(testSeat);
    }

    @Test
    void getFlights_ShouldReturnAllFlights() {
        when(flightRepository.findAll()).thenReturn(Arrays.asList(testFlight));
        when(seatRepository.countByFlightIdAndStatus(anyLong(), eq(SeatStatus.AVAILABLE))).thenReturn(1);

        List<FlightDTO> result = flightService.getFlights();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFlightNumber()).isEqualTo("AA100");
    }

    @Test
    void getSeatsForFlight_ShouldReturnAllSeats() {
        when(flightRepository.findById(1L)).thenReturn(Optional.of(testFlight));
        when(seatRepository.findByFlightId(1L)).thenReturn(Arrays.asList(testSeat));

        List<SeatDTO> result = flightService.getSeatsForFlight(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSeatNumber()).isEqualTo("1A");
        assertThat(result.get(0).getRowNumber()).isEqualTo(1);
    }

    @Test
    void getSeatsForFlight_WhenFlightNotExists_ShouldThrowException() {
        when(flightRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> flightService.getSeatsForFlight(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Flight not found");
    }
}
