package com.airline.service;

import com.airline.dto.BookingDTO;
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
                .expirationTime(LocalDateTime.now().plusMinutes(10))
                .active(true)
                .build();
    }

    @Test
    void createBooking_WhenValidHold_ShouldCreateBooking() {
        when(seatLockService.getActiveHold(1L)).thenReturn(testHold);
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(testPassenger));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(seatRepository.save(any(Seat.class))).thenReturn(testSeat);

        BookingDTO result = bookingService.createBooking(1L, 1L);

        assertThat(result.getPassengerName()).isEqualTo("John Doe");
        assertThat(result.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(testSeat.getStatus()).isEqualTo(SeatStatus.BOOKED);
        verify(seatLockService).deactivateHold(testHold);
    }

    @Test
    void createBooking_WhenSeatNotHeld_ShouldThrowException() {
        testSeat.setStatus(SeatStatus.AVAILABLE);
        when(seatLockService.getActiveHold(1L)).thenReturn(testHold);

        assertThatThrownBy(() -> bookingService.createBooking(1L, 1L))
                .isInstanceOf(InvalidHoldException.class)
                .hasMessageContaining("no longer held");
    }

    @Test
    void createBooking_WhenHoldInvalid_ShouldThrowException() {
        when(seatLockService.getActiveHold(1L))
                .thenThrow(new InvalidHoldException("Hold is not active"));

        assertThatThrownBy(() -> bookingService.createBooking(1L, 1L))
                .isInstanceOf(InvalidHoldException.class);
    }

    @Test
    void createBooking_WhenPassengerNotFound_ShouldThrowException() {
        when(seatLockService.getActiveHold(1L)).thenReturn(testHold);
        when(passengerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Passenger not found");
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
