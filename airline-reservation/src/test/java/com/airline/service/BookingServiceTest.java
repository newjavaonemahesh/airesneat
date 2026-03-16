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
    private BookingRequest validRequest;

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
                .fareClass(FareClass.FIRST)
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
                .expirationTime(LocalDateTime.now().plusMinutes(10))
                .active(true)
                .build();

        validRequest = BookingRequest.builder()
                .userId("user123")
                .passengerName("John Doe")
                .email("john@example.com")
                .build();
    }

    @Test
    void confirmBooking_WhenValidHold_ShouldCreateBooking() {
        when(seatHoldService.validateAndGetHold(1L, "user123")).thenReturn(testHold);
        when(passengerRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(passengerRepository.save(any(Passenger.class))).thenReturn(testPassenger);
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(seatRepository.save(any(Seat.class))).thenReturn(testSeat);

        BookingDTO result = bookingService.confirmBooking(1L, validRequest);

        assertThat(result.getPassengerName()).isEqualTo("John Doe");
        assertThat(result.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        verify(seatHoldService).deactivateHold(testHold);
    }

    @Test
    void confirmBooking_WhenExistingPassenger_ShouldUseExistingPassenger() {
        when(seatHoldService.validateAndGetHold(1L, "user123")).thenReturn(testHold);
        when(passengerRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testPassenger));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(seatRepository.save(any(Seat.class))).thenReturn(testSeat);

        bookingService.confirmBooking(1L, validRequest);

        verify(passengerRepository, never()).save(any(Passenger.class));
    }

    @Test
    void confirmBooking_WhenInvalidHold_ShouldThrowException() {
        when(seatHoldService.validateAndGetHold(1L, "user123"))
                .thenThrow(new InvalidHoldException("No active hold found"));

        assertThatThrownBy(() -> bookingService.confirmBooking(1L, validRequest))
                .isInstanceOf(InvalidHoldException.class);
    }

    @Test
    void cancelBooking_WhenExists_ShouldCancelAndReleaseSeat() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(seatRepository.save(any(Seat.class))).thenReturn(testSeat);

        BookingDTO result = bookingService.cancelBooking(1L);

        assertThat(testBooking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(testSeat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
    }

    @Test
    void cancelBooking_WhenNotFound_ShouldThrowException() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.cancelBooking(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void cancelBooking_WhenAlreadyCancelled_ShouldThrowException() {
        testBooking.setStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        assertThatThrownBy(() -> bookingService.cancelBooking(1L))
                .isInstanceOf(InvalidHoldException.class)
                .hasMessageContaining("already cancelled");
    }

    @Test
    void getBookingById_WhenExists_ShouldReturnBooking() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        BookingDTO result = bookingService.getBookingById(1L);

        assertThat(result.getPassengerName()).isEqualTo("John Doe");
    }

    @Test
    void getBookingById_WhenNotFound_ShouldThrowException() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
