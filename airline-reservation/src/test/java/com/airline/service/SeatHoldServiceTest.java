package com.airline.service;

import com.airline.dto.SeatHoldDTO;
import com.airline.dto.SeatHoldRequest;
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
class SeatHoldServiceTest {

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private SeatHoldRepository seatHoldRepository;

    @InjectMocks
    private SeatHoldService seatHoldService;

    private Seat testSeat;
    private SeatHold testHold;
    private Flight testFlight;
    private SeatHoldRequest holdRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(seatHoldService, "holdDurationMinutes", 10);

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

        holdRequest = SeatHoldRequest.builder()
                .userId("user123")
                .build();
    }

    @Test
    void holdSeat_WhenAvailable_ShouldCreateHold() {
        when(seatRepository.findById(1L)).thenReturn(Optional.of(testSeat));
        when(seatHoldRepository.save(any(SeatHold.class))).thenReturn(testHold);
        when(seatRepository.save(any(Seat.class))).thenReturn(testSeat);

        SeatHoldDTO result = seatHoldService.holdSeat(1L, holdRequest);

        assertThat(result.getSeatNumber()).isEqualTo("1A");
        assertThat(result.getUserId()).isEqualTo("user123");
        verify(seatRepository).save(testSeat);
    }

    @Test
    void holdSeat_WhenSeatNotFound_ShouldThrowException() {
        when(seatRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seatHoldService.holdSeat(999L, holdRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Seat not found");
    }

    @Test
    void holdSeat_WhenSeatNotAvailable_ShouldThrowException() {
        testSeat.setStatus(SeatStatus.HELD);
        when(seatRepository.findById(1L)).thenReturn(Optional.of(testSeat));

        assertThatThrownBy(() -> seatHoldService.holdSeat(1L, holdRequest))
                .isInstanceOf(SeatNotAvailableException.class);
    }

    @Test
    void releaseHold_WhenValidHold_ShouldReleaseHold() {
        testSeat.setStatus(SeatStatus.HELD);
        when(seatHoldRepository.findBySeatIdAndUserIdAndActiveTrue(1L, "user123"))
                .thenReturn(Optional.of(testHold));
        when(seatRepository.save(any(Seat.class))).thenReturn(testSeat);
        when(seatHoldRepository.save(any(SeatHold.class))).thenReturn(testHold);

        seatHoldService.releaseHold(1L, "user123");

        assertThat(testSeat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
        verify(seatRepository).save(testSeat);
    }

    @Test
    void releaseHold_WhenInvalidHold_ShouldThrowException() {
        when(seatHoldRepository.findBySeatIdAndUserIdAndActiveTrue(1L, "invalidUser"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> seatHoldService.releaseHold(1L, "invalidUser"))
                .isInstanceOf(InvalidHoldException.class);
    }

    @Test
    void validateAndGetHold_WhenValid_ShouldReturnHold() {
        when(seatHoldRepository.findBySeatIdAndUserIdAndActiveTrue(1L, "user123"))
                .thenReturn(Optional.of(testHold));

        SeatHold result = seatHoldService.validateAndGetHold(1L, "user123");

        assertThat(result.getUserId()).isEqualTo("user123");
    }

    @Test
    void validateAndGetHold_WhenExpired_ShouldThrowException() {
        testHold.setExpirationTime(LocalDateTime.now().minusMinutes(5));
        testSeat.setStatus(SeatStatus.HELD);
        when(seatHoldRepository.findBySeatIdAndUserIdAndActiveTrue(1L, "user123"))
                .thenReturn(Optional.of(testHold));
        when(seatRepository.save(any(Seat.class))).thenReturn(testSeat);
        when(seatHoldRepository.save(any(SeatHold.class))).thenReturn(testHold);

        assertThatThrownBy(() -> seatHoldService.validateAndGetHold(1L, "user123"))
                .isInstanceOf(InvalidHoldException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void releaseExpiredHolds_ShouldReleaseAllExpiredHolds() {
        testSeat.setStatus(SeatStatus.HELD);
        when(seatHoldRepository.findExpiredHolds(any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(testHold));
        when(seatRepository.save(any(Seat.class))).thenReturn(testSeat);
        when(seatHoldRepository.save(any(SeatHold.class))).thenReturn(testHold);

        seatHoldService.releaseExpiredHolds();

        verify(seatRepository).save(testSeat);
        assertThat(testSeat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
    }

    @Test
    void deactivateHold_ShouldSetActiveToFalse() {
        when(seatHoldRepository.save(any(SeatHold.class))).thenReturn(testHold);

        seatHoldService.deactivateHold(testHold);

        assertThat(testHold.isActive()).isFalse();
        verify(seatHoldRepository).save(testHold);
    }
}
