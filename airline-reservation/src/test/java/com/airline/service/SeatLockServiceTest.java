package com.airline.service;

import com.airline.dto.SeatHoldDTO;
import com.airline.exception.InvalidHoldException;
import com.airline.exception.ResourceNotFoundException;
import com.airline.exception.SeatNotAvailableException;
import com.airline.model.*;
import com.airline.repository.SeatHoldRepository;
import com.airline.repository.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatLockServiceTest {

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private SeatHoldRepository seatHoldRepository;

    @InjectMocks
    private SeatLockService seatLockService;

    private Seat testSeat;
    private SeatHold testHold;
    private Flight testFlight;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(seatLockService, "holdDurationMinutes", 10);

        testFlight = Flight.builder()
                .id(1L)
                .flightNumber("AA100")
                .build();

        testSeat = Seat.builder()
                .id(1L)
                .seatNumber("1A")
                .rowNumber(1)
                .fareClass(FareClass.FIRST)
                .status(SeatStatus.AVAILABLE)
                .flight(testFlight)
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

    @Test
    void holdSeat_WhenAvailable_ShouldCreateHold() {
        when(seatRepository.findById(1L)).thenReturn(Optional.of(testSeat));
        when(seatHoldRepository.save(any(SeatHold.class))).thenReturn(testHold);
        when(seatRepository.save(any(Seat.class))).thenReturn(testSeat);

        SeatHoldDTO result = seatLockService.holdSeat(1L, "user123");

        assertThat(result.getSeatNumber()).isEqualTo("1A");
        assertThat(result.getUserId()).isEqualTo("user123");
        assertThat(testSeat.getStatus()).isEqualTo(SeatStatus.HELD);
    }

    @Test
    void holdSeat_WhenSeatNotFound_ShouldThrowException() {
        when(seatRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seatLockService.holdSeat(999L, "user123"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Seat not found");
    }

    @Test
    void holdSeat_WhenSeatHeld_ShouldThrowException() {
        testSeat.setStatus(SeatStatus.HELD);
        when(seatRepository.findById(1L)).thenReturn(Optional.of(testSeat));

        assertThatThrownBy(() -> seatLockService.holdSeat(1L, "user123"))
                .isInstanceOf(SeatNotAvailableException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void holdSeat_WhenSeatBooked_ShouldThrowException() {
        testSeat.setStatus(SeatStatus.BOOKED);
        when(seatRepository.findById(1L)).thenReturn(Optional.of(testSeat));

        assertThatThrownBy(() -> seatLockService.holdSeat(1L, "user123"))
                .isInstanceOf(SeatNotAvailableException.class);
    }

    @Test
    void releaseHold_WhenValidHold_ShouldRelease() {
        testSeat.setStatus(SeatStatus.HELD);
        when(seatHoldRepository.findById(1L)).thenReturn(Optional.of(testHold));
        when(seatRepository.save(any(Seat.class))).thenReturn(testSeat);
        when(seatHoldRepository.save(any(SeatHold.class))).thenReturn(testHold);

        seatLockService.releaseHold(1L);

        assertThat(testSeat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
        assertThat(testHold.isActive()).isFalse();
    }

    @Test
    void releaseHold_WhenHoldNotFound_ShouldThrowException() {
        when(seatHoldRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seatLockService.releaseHold(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void releaseHold_WhenHoldNotActive_ShouldThrowException() {
        testHold.setActive(false);
        when(seatHoldRepository.findById(1L)).thenReturn(Optional.of(testHold));

        assertThatThrownBy(() -> seatLockService.releaseHold(1L))
                .isInstanceOf(InvalidHoldException.class)
                .hasMessageContaining("no longer active");
    }

    @Test
    void expireHolds_ShouldReleaseExpiredHolds() {
        testSeat.setStatus(SeatStatus.HELD);
        when(seatHoldRepository.findExpiredHolds(any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(testHold));
        when(seatRepository.save(any(Seat.class))).thenReturn(testSeat);
        when(seatHoldRepository.save(any(SeatHold.class))).thenReturn(testHold);

        seatLockService.expireHolds();

        verify(seatRepository).save(testSeat);
        assertThat(testSeat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
    }

    @Test
    void getActiveHold_WhenValid_ShouldReturnHold() {
        when(seatHoldRepository.findById(1L)).thenReturn(Optional.of(testHold));

        SeatHold result = seatLockService.getActiveHold(1L);

        assertThat(result.getUserId()).isEqualTo("user123");
    }

    @Test
    void getActiveHold_WhenExpired_ShouldThrowException() {
        testHold.setExpirationTime(LocalDateTime.now().minusMinutes(5));
        when(seatHoldRepository.findById(1L)).thenReturn(Optional.of(testHold));

        assertThatThrownBy(() -> seatLockService.getActiveHold(1L))
                .isInstanceOf(InvalidHoldException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void deactivateHold_ShouldSetActiveToFalse() {
        when(seatHoldRepository.save(any(SeatHold.class))).thenReturn(testHold);

        seatLockService.deactivateHold(testHold);

        assertThat(testHold.isActive()).isFalse();
    }
}
