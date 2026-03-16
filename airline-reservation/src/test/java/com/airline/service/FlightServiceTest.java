package com.airline.service;

import com.airline.dto.FlightDTO;
import com.airline.dto.FlightSearchRequest;
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

import java.math.BigDecimal;
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
    private FareClass testFareClass;

    @BeforeEach
    void setUp() {
        testFareClass = FareClass.builder()
                .id(1L)
                .name("ECONOMY")
                .basePrice(new BigDecimal("150.00"))
                .build();

        testFlight = Flight.builder()
                .id(1L)
                .flightNumber("AA100")
                .origin("JFK")
                .destination("LAX")
                .departureTime(LocalDateTime.of(2024, 1, 15, 10, 0))
                .arrivalTime(LocalDateTime.of(2024, 1, 15, 16, 0))
                .build();

        testSeat = Seat.builder()
                .id(1L)
                .seatNumber("1A")
                .status(SeatStatus.AVAILABLE)
                .flight(testFlight)
                .fareClass(testFareClass)
                .build();

        testFlight.getSeats().add(testSeat);
    }

    @Test
    void getAllFlights_ShouldReturnAllFlights() {
        when(flightRepository.findAll()).thenReturn(Arrays.asList(testFlight));
        when(seatRepository.countByFlightIdAndStatus(anyLong(), eq(SeatStatus.AVAILABLE))).thenReturn(1);

        List<FlightDTO> result = flightService.getAllFlights();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFlightNumber()).isEqualTo("AA100");
    }

    @Test
    void searchFlights_ShouldReturnMatchingFlights() {
        FlightSearchRequest request = FlightSearchRequest.builder()
                .origin("JFK")
                .destination("LAX")
                .build();

        when(flightRepository.searchFlights(anyString(), anyString(), any(), any()))
                .thenReturn(Arrays.asList(testFlight));
        when(seatRepository.countByFlightIdAndStatus(anyLong(), eq(SeatStatus.AVAILABLE))).thenReturn(1);

        List<FlightDTO> result = flightService.searchFlights(request);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrigin()).isEqualTo("JFK");
    }

    @Test
    void getFlightById_WhenExists_ShouldReturnFlight() {
        when(flightRepository.findById(1L)).thenReturn(Optional.of(testFlight));
        when(seatRepository.countByFlightIdAndStatus(anyLong(), eq(SeatStatus.AVAILABLE))).thenReturn(1);

        FlightDTO result = flightService.getFlightById(1L);

        assertThat(result.getFlightNumber()).isEqualTo("AA100");
    }

    @Test
    void getFlightById_WhenNotExists_ShouldThrowException() {
        when(flightRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> flightService.getFlightById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Flight not found");
    }

    @Test
    void getSeatsForFlight_ShouldReturnAllSeats() {
        when(flightRepository.existsById(1L)).thenReturn(true);
        when(seatRepository.findByFlightId(1L)).thenReturn(Arrays.asList(testSeat));

        List<SeatDTO> result = flightService.getSeatsForFlight(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSeatNumber()).isEqualTo("1A");
    }

    @Test
    void getSeatsForFlight_WhenFlightNotExists_ShouldThrowException() {
        when(flightRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> flightService.getSeatsForFlight(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAvailableSeatsForFlight_ShouldReturnOnlyAvailableSeats() {
        when(flightRepository.existsById(1L)).thenReturn(true);
        when(seatRepository.findByFlightIdAndStatus(1L, SeatStatus.AVAILABLE))
                .thenReturn(Arrays.asList(testSeat));

        List<SeatDTO> result = flightService.getAvailableSeatsForFlight(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(SeatStatus.AVAILABLE);
    }
}
