package com.airline.service;

import com.airline.dto.FlightDTO;
import com.airline.dto.SeatDTO;
import com.airline.exception.ResourceNotFoundException;
import com.airline.model.*;
import com.airline.repository.FlightRepository;
import com.airline.repository.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FlightService Tests")
class FlightServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private SeatRepository seatRepository;

    @InjectMocks
    private FlightService flightService;

    private Flight testFlight;
    private Seat testSeat1;
    private Seat testSeat2;

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

        testSeat1 = Seat.builder()
                .id(1L)
                .seatNumber("1A")
                .rowNumber(1)
                .fareClass(FareClass.BUSINESS)
                .status(SeatStatus.AVAILABLE)
                .flight(testFlight)
                .build();

        testSeat2 = Seat.builder()
                .id(2L)
                .seatNumber("3A")
                .rowNumber(3)
                .fareClass(FareClass.ECONOMY)
                .status(SeatStatus.AVAILABLE)
                .flight(testFlight)
                .build();

        testFlight.getSeats().add(testSeat1);
        testFlight.getSeats().add(testSeat2);
    }

    @Nested
    @DisplayName("getFlights tests")
    class GetFlightsTests {

        @Test
        @DisplayName("retrieve flights - should return all flights")
        void getFlights_ShouldReturnAllFlights() {
            // Arrange
            Flight flight2 = Flight.builder()
                    .id(2L)
                    .flightNumber("UA200")
                    .departureAirport("LAX")
                    .arrivalAirport("ORD")
                    .departureTime(LocalDateTime.now().plusDays(1))
                    .arrivalTime(LocalDateTime.now().plusDays(1).plusHours(4))
                    .build();

            when(flightRepository.findAll()).thenReturn(Arrays.asList(testFlight, flight2));
            when(seatRepository.countByFlightIdAndStatus(anyLong(), eq(SeatStatus.AVAILABLE)))
                    .thenReturn(2);

            // Act
            List<FlightDTO> result = flightService.getFlights();

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getFlightNumber()).isEqualTo("AA100");
            assertThat(result.get(0).getDepartureAirport()).isEqualTo("JFK");
            assertThat(result.get(0).getArrivalAirport()).isEqualTo("LAX");
            assertThat(result.get(1).getFlightNumber()).isEqualTo("UA200");
        }

        @Test
        @DisplayName("retrieve flights - should return empty list when no flights")
        void getFlights_WhenNoFlights_ShouldReturnEmptyList() {
            // Arrange
            when(flightRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<FlightDTO> result = flightService.getFlights();

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("retrieve flights - should include seat counts")
        void getFlights_ShouldIncludeSeatCounts() {
            // Arrange
            when(flightRepository.findAll()).thenReturn(Arrays.asList(testFlight));
            when(seatRepository.countByFlightIdAndStatus(1L, SeatStatus.AVAILABLE)).thenReturn(25);

            // Act
            List<FlightDTO> result = flightService.getFlights();

            // Assert
            assertThat(result.get(0).getTotalSeats()).isEqualTo(2);
            assertThat(result.get(0).getAvailableSeats()).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("getSeatsForFlight tests")
    class GetSeatsForFlightTests {

        @Test
        @DisplayName("retrieve seats - should return all seats for flight")
        void getSeatsForFlight_ShouldReturnAllSeats() {
            // Arrange
            when(flightRepository.findById(1L)).thenReturn(Optional.of(testFlight));
            when(seatRepository.findByFlightId(1L)).thenReturn(Arrays.asList(testSeat1, testSeat2));

            // Act
            List<SeatDTO> result = flightService.getSeatsForFlight(1L);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getSeatNumber()).isEqualTo("1A");
            assertThat(result.get(0).getRowNumber()).isEqualTo(1);
            assertThat(result.get(0).getFareClass()).isEqualTo(FareClass.BUSINESS);
            assertThat(result.get(0).getStatus()).isEqualTo(SeatStatus.AVAILABLE);
            assertThat(result.get(1).getSeatNumber()).isEqualTo("3A");
            assertThat(result.get(1).getFareClass()).isEqualTo(FareClass.ECONOMY);
        }

        @Test
        @DisplayName("retrieve seats - should throw exception when flight not found")
        void getSeatsForFlight_WhenFlightNotFound_ShouldThrowException() {
            // Arrange
            when(flightRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> flightService.getSeatsForFlight(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Flight not found");
        }

        @Test
        @DisplayName("retrieve seats - should return empty list when no seats")
        void getSeatsForFlight_WhenNoSeats_ShouldReturnEmptyList() {
            // Arrange
            Flight emptyFlight = Flight.builder().id(2L).flightNumber("UA200").build();
            when(flightRepository.findById(2L)).thenReturn(Optional.of(emptyFlight));
            when(seatRepository.findByFlightId(2L)).thenReturn(Collections.emptyList());

            // Act
            List<SeatDTO> result = flightService.getSeatsForFlight(2L);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("retrieve seats - should include all seat details")
        void getSeatsForFlight_ShouldIncludeAllSeatDetails() {
            // Arrange
            testSeat1.setStatus(SeatStatus.HELD);
            when(flightRepository.findById(1L)).thenReturn(Optional.of(testFlight));
            when(seatRepository.findByFlightId(1L)).thenReturn(Arrays.asList(testSeat1));

            // Act
            List<SeatDTO> result = flightService.getSeatsForFlight(1L);

            // Assert
            SeatDTO seat = result.get(0);
            assertThat(seat.getId()).isEqualTo(1L);
            assertThat(seat.getSeatNumber()).isEqualTo("1A");
            assertThat(seat.getRowNumber()).isEqualTo(1);
            assertThat(seat.getFareClass()).isEqualTo(FareClass.BUSINESS);
            assertThat(seat.getStatus()).isEqualTo(SeatStatus.HELD);
        }
    }
}
