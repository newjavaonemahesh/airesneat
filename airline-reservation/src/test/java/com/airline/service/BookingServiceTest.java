package com.airline.service;

import com.airline.dto.BookingDTO;
import com.airline.exception.InvalidHoldException;
import com.airline.exception.ResourceNotFoundException;
import com.airline.model.*;
import com.airline.repository.BookingRepository;
import com.airline.repository.PassengerRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService Tests")
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PassengerRepository passengerRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private SeatLockService seatLockService;

    @InjectMocks
    private BookingService bookingService;

    private Seat testSeat;
    private Passenger testPassenger;
    private Booking testBooking;
    private SeatHold testHold;
    private Flight testFlight;

    @BeforeEach
    void setUp() {
        testFlight = Flight.builder()
                .id(1L)
                .flightNumber("AA100")
                .build();

        testSeat = Seat.builder()
                .id(1L)
                .seatNumber("1A")
                .rowNumber(1)
                .fareClass(FareClass.BUSINESS)
                .status(SeatStatus.HELD)
                .flight(testFlight)
                .build();

        testPassenger = Passenger.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();

        testBooking = Booking.builder()
                .id(1L)
                .seat(testSeat)
                .passenger(testPassenger)
                .bookingTime(LocalDateTime.now())
                .status(BookingStatus.CONFIRMED)
                .build();

        testHold = SeatHold.builder()
                .id(1L)
                .seat(testSeat)
                .userId("user123")
                .holdTime(LocalDateTime.now())
                .expirationTime(LocalDateTime.now().plusMinutes(10))
                .active(true)
                .build();
    }

    @Nested
    @DisplayName("createBooking tests")
    class CreateBookingTests {

        @Test
        @DisplayName("createBooking success - should create booking and update seat status")
        void createBooking_WhenValidHold_ShouldCreateBookingSuccessfully() {
            // Arrange
            when(seatLockService.getActiveHold(1L)).thenReturn(testHold);
            when(passengerRepository.findById(1L)).thenReturn(Optional.of(testPassenger));
            when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
            when(seatRepository.save(any(Seat.class))).thenReturn(testSeat);

            // Act
            BookingDTO result = bookingService.createBooking(1L, 1L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getPassengerName()).isEqualTo("John Doe");
            assertThat(result.getSeatNumber()).isEqualTo("1A");
            assertThat(result.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
            assertThat(testSeat.getStatus()).isEqualTo(SeatStatus.BOOKED);
            
            verify(seatLockService).deactivateHold(testHold);
            verify(bookingRepository).save(any(Booking.class));
        }

        @Test
        @DisplayName("booking fails if hold expired - should throw InvalidHoldException")
        void createBooking_WhenHoldExpired_ShouldThrowException() {
            // Arrange
            when(seatLockService.getActiveHold(1L))
                    .thenThrow(new InvalidHoldException("Hold has expired. Please hold the seat again."));

            // Act & Assert
            assertThatThrownBy(() -> bookingService.createBooking(1L, 1L))
                    .isInstanceOf(InvalidHoldException.class)
                    .hasMessageContaining("expired");

            verify(bookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("booking fails if hold not active")
        void createBooking_WhenHoldNotActive_ShouldThrowException() {
            // Arrange
            when(seatLockService.getActiveHold(1L))
                    .thenThrow(new InvalidHoldException("Hold is no longer active"));

            // Act & Assert
            assertThatThrownBy(() -> bookingService.createBooking(1L, 1L))
                    .isInstanceOf(InvalidHoldException.class)
                    .hasMessageContaining("no longer active");
        }

        @Test
        @DisplayName("booking fails if seat no longer held")
        void createBooking_WhenSeatNotHeld_ShouldThrowException() {
            // Arrange
            testSeat.setStatus(SeatStatus.AVAILABLE);
            when(seatLockService.getActiveHold(1L)).thenReturn(testHold);

            // Act & Assert
            assertThatThrownBy(() -> bookingService.createBooking(1L, 1L))
                    .isInstanceOf(InvalidHoldException.class)
                    .hasMessageContaining("no longer held");
        }

        @Test
        @DisplayName("booking fails if passenger not found")
        void createBooking_WhenPassengerNotFound_ShouldThrowException() {
            // Arrange
            when(seatLockService.getActiveHold(1L)).thenReturn(testHold);
            when(passengerRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bookingService.createBooking(1L, 999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Passenger not found");
        }
    }

    @Nested
    @DisplayName("cancelBooking tests")
    class CancelBookingTests {

        @Test
        @DisplayName("cancelBooking success - should cancel and release seat")
        void cancelBooking_WhenExists_ShouldCancelSuccessfully() {
            // Arrange
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
            when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
            when(seatRepository.save(any(Seat.class))).thenReturn(testSeat);

            // Act
            BookingDTO result = bookingService.cancelBooking(1L);

            // Assert
            assertThat(testBooking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
            assertThat(testSeat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
            verify(bookingRepository).save(testBooking);
            verify(seatRepository).save(testSeat);
        }

        @Test
        @DisplayName("cancelBooking fails if booking not found")
        void cancelBooking_WhenNotFound_ShouldThrowException() {
            // Arrange
            when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bookingService.cancelBooking(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Booking not found");
        }

        @Test
        @DisplayName("cancelBooking fails if already cancelled")
        void cancelBooking_WhenAlreadyCancelled_ShouldThrowException() {
            // Arrange
            testBooking.setStatus(BookingStatus.CANCELLED);
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

            // Act & Assert
            assertThatThrownBy(() -> bookingService.cancelBooking(1L))
                    .isInstanceOf(InvalidHoldException.class)
                    .hasMessageContaining("already cancelled");
        }
    }

    @Nested
    @DisplayName("getBookingById tests")
    class GetBookingByIdTests {

        @Test
        @DisplayName("getBookingById success")
        void getBookingById_WhenExists_ShouldReturnBooking() {
            // Arrange
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

            // Act
            BookingDTO result = bookingService.getBookingById(1L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getPassengerName()).isEqualTo("John Doe");
            assertThat(result.getFlightNumber()).isEqualTo("AA100");
        }

        @Test
        @DisplayName("getBookingById fails if not found")
        void getBookingById_WhenNotFound_ShouldThrowException() {
            // Arrange
            when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bookingService.getBookingById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
