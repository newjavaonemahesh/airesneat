package com.airline.service;

import com.airline.dto.BookingDTO;
import com.airline.dto.BookingRequest;
import com.airline.exception.InvalidHoldException;
import com.airline.exception.ResourceNotFoundException;
import com.airline.model.*;
import com.airline.repository.BookingRepository;
import com.airline.repository.PassengerRepository;
import com.airline.repository.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PassengerRepository passengerRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private SeatHoldService seatHoldService;

    @InjectMocks
    private BookingService bookingService;

    private Seat testSeat;
    private Passenger testPassenger;
    private Booking testBooking;
    private SeatHold testHold;
    private Flight testFlight;
    private FareClass testFareClass;
    private BookingRequest validRequest;

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
                .build();

        testSeat = Seat.builder()
                .id(1L)
                .seatNumber("1A")
                .status(SeatStatus.HELD)
                .flight(testFlight)
                .fareClass(testFareClass)
                .build();

        testPassenger = Passenger.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .build();

        testBooking = Booking.builder()
                .id(1L)
                .bookingReference("BK12345678")
                .seat(testSeat)
                .passenger(testPassenger)
                .totalFare(new BigDecimal("150.00"))
                .bookedAt(LocalDateTime.now())
                .cancelled(false)
                .build();

        testHold = SeatHold.builder()
                .id(1L)
                .seat(testSeat)
                .holdToken("test-token-123")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .active(true)
                .build();

        validRequest = BookingRequest.builder()
                .holdToken("test-token-123")
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phoneNumber("1234567890")
                .build();
    }

    @Test
    void confirmBooking_WhenValidHold_ShouldCreateBooking() {
        when(seatHoldService.validateAndGetHold("test-token-123")).thenReturn(testHold);
        when(passengerRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(passengerRepository.save(any(Passenger.class))).thenReturn(testPassenger);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(1L);
            return b;
        });
        when(seatRepository.save(any(Seat.class))).thenReturn(testSeat);

        BookingDTO result = bookingService.confirmBooking(validRequest);

        assertThat(result.getBookingReference()).startsWith("BK");
        assertThat(result.getPassengerName()).isEqualTo("John Doe");
        verify(seatHoldService).deactivateHold(testHold);
    }

    @Test
    void confirmBooking_WhenExistingPassenger_ShouldUseExistingPassenger() {
        when(seatHoldService.validateAndGetHold("test-token-123")).thenReturn(testHold);
        when(passengerRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testPassenger));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(seatRepository.save(any(Seat.class))).thenReturn(testSeat);

        bookingService.confirmBooking(validRequest);

        verify(passengerRepository, never()).save(any(Passenger.class));
    }

    @Test
    void confirmBooking_WhenInvalidHold_ShouldThrowException() {
        when(seatHoldService.validateAndGetHold("invalid-token"))
                .thenThrow(new InvalidHoldException("Invalid hold token"));

        validRequest.setHoldToken("invalid-token");

        assertThatThrownBy(() -> bookingService.confirmBooking(validRequest))
                .isInstanceOf(InvalidHoldException.class);
    }

    @Test
    void cancelBooking_WhenExists_ShouldCancelAndReleaseSeat() {
        when(bookingRepository.findByBookingReference("BK12345678"))
                .thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(seatRepository.save(any(Seat.class))).thenReturn(testSeat);

        BookingDTO result = bookingService.cancelBooking("BK12345678");

        assertThat(testBooking.isCancelled()).isTrue();
        assertThat(testSeat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
    }

    @Test
    void cancelBooking_WhenNotFound_ShouldThrowException() {
        when(bookingRepository.findByBookingReference("INVALID"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.cancelBooking("INVALID"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void cancelBooking_WhenAlreadyCancelled_ShouldThrowException() {
        testBooking.setCancelled(true);
        when(bookingRepository.findByBookingReference("BK12345678"))
                .thenReturn(Optional.of(testBooking));

        assertThatThrownBy(() -> bookingService.cancelBooking("BK12345678"))
                .isInstanceOf(InvalidHoldException.class)
                .hasMessageContaining("already cancelled");
    }

    @Test
    void getBookingByReference_WhenExists_ShouldReturnBooking() {
        when(bookingRepository.findByBookingReference("BK12345678"))
                .thenReturn(Optional.of(testBooking));

        BookingDTO result = bookingService.getBookingByReference("BK12345678");

        assertThat(result.getBookingReference()).isEqualTo("BK12345678");
    }

    @Test
    void getBookingByReference_WhenNotFound_ShouldThrowException() {
        when(bookingRepository.findByBookingReference("INVALID"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingByReference("INVALID"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
