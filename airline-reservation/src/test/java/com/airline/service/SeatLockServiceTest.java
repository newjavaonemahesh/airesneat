package com.airline.service;

import com.airline.dto.SeatHoldDTO;
import com.airline.exception.InvalidHoldException;
import com.airline.exception.ResourceNotFoundException;
import com.airline.exception.SeatNotAvailableException;
import com.airline.model.*;
import com.airline.repository.SeatHoldRepository;
import com.airline.repository.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SeatLockService Tests")
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
                .fareClass(FareClass.BUSINESS)
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

    @Nested
    @DisplayName("holdSeat tests")
    class HoldSeatTests {

        @Test
        @DisplayName("holdSeat success - should create hold and update seat status")
        void holdSeat_WhenSeatAvailable_ShouldCreateHoldSuccessfully() {
            // Arrange
            when(seatRepository.findById(1L)).thenReturn(Optional.of(testSeat));
            when(seatHoldRepository.save(any(SeatHold.class))).thenReturn(testHold);
            when(seatRepository.save(any(Seat.class))).thenReturn(testSeat);

            // Act
            SeatHoldDTO result = seatLockService.holdSeat(1L, "user123");

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getSeatNumber()).isEqualTo("1A");
            assertThat(result.getUserId()).isEqualTo("user123");
            assertThat(result.getExpirationTime()).isNotNull();
            assertThat(testSeat.getStatus()).isEqualTo(SeatStatus.HELD);
            
            verify(seatHoldRepository).save(any(SeatHold.class));
            verify(seatRepository).save(testSeat);
        }

        @Test
        @DisplayName("cannot hold already held seat - should throw SeatNotAvailableException")
        void holdSeat_WhenSeatAlreadyHeld_ShouldThrowException() {
            // Arrange
            testSeat.setStatus(SeatStatus.HELD);
            when(seatRepository.findById(1L)).thenReturn(Optional.of(testSeat));

            // Act & Assert
            assertThatThrownBy(() -> seatLockService.holdSeat(1L, "user123"))
                    .isInstanceOf(SeatNotAvailableException.class)
                    .hasMessageContaining("not available")
                    .hasMessageContaining("HELD");

            verify(seatHoldRepository, never()).save(any());
        }

        @Test
        @DisplayName("cannot hold already booked seat")
        void holdSeat_WhenSeatBooked_ShouldThrowException() {
            // Arrange
            testSeat.setStatus(SeatStatus.BOOKED);
            when(seatRepository.findById(1L)).thenReturn(Optional.of(testSeat));

            // Act & Assert
            assertThatThrownBy(() -> seatLockService.holdSeat(1L, "user123"))
                    .isInstanceOf(SeatNotAvailableException.class)
                    .hasMessageContaining("BOOKED");
        }

        @Test
        @DisplayName("holdSeat with non-existent seat should throw ResourceNotFoundException")
        void holdSeat_WhenSeatNotFound_ShouldThrowException() {
            // Arrange
            when(seatRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> seatLockService.holdSeat(999L, "user123"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Seat not found");
        }
    }

    @Nested
    @DisplayName("releaseHold tests")
    class ReleaseHoldTests {

        @Test
        @DisplayName("releaseHold success")
        void releaseHold_WhenValidHold_ShouldReleaseSuccessfully() {
            // Arrange
            testSeat.setStatus(SeatStatus.HELD);
            when(seatHoldRepository.findById(1L)).thenReturn(Optional.of(testHold));
            when(seatRepository.save(any(Seat.class))).thenReturn(testSeat);
            when(seatHoldRepository.save(any(SeatHold.class))).thenReturn(testHold);

            // Act
            seatLockService.releaseHold(1L);

            // Assert
            assertThat(testSeat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
            assertThat(testHold.isActive()).isFalse();
            verify(seatRepository).save(testSeat);
            verify(seatHoldRepository).save(testHold);
        }

        @Test
        @DisplayName("releaseHold with inactive hold should throw exception")
        void releaseHold_WhenHoldNotActive_ShouldThrowException() {
            // Arrange
            testHold.setActive(false);
            when(seatHoldRepository.findById(1L)).thenReturn(Optional.of(testHold));

            // Act & Assert
            assertThatThrownBy(() -> seatLockService.releaseHold(1L))
                    .isInstanceOf(InvalidHoldException.class)
                    .hasMessageContaining("no longer active");
        }
    }

    @Nested
    @DisplayName("expireHolds tests")
    class ExpireHoldsTests {

        @Test
        @DisplayName("expireHolds should release all expired holds")
        void expireHolds_ShouldReleaseAllExpiredHolds() {
            // Arrange
            testSeat.setStatus(SeatStatus.HELD);
            when(seatHoldRepository.findExpiredHolds(any(LocalDateTime.class)))
                    .thenReturn(Arrays.asList(testHold));
            when(seatRepository.save(any(Seat.class))).thenReturn(testSeat);
            when(seatHoldRepository.save(any(SeatHold.class))).thenReturn(testHold);

            // Act
            seatLockService.expireHolds();

            // Assert
            assertThat(testSeat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
            assertThat(testHold.isActive()).isFalse();
            verify(seatRepository).save(testSeat);
        }

        @Test
        @DisplayName("expireHolds with no expired holds should do nothing")
        void expireHolds_WhenNoExpiredHolds_ShouldDoNothing() {
            // Arrange
            when(seatHoldRepository.findExpiredHolds(any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());

            // Act
            seatLockService.expireHolds();

            // Assert
            verify(seatRepository, never()).save(any());
            verify(seatHoldRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getActiveHold tests")
    class GetActiveHoldTests {

        @Test
        @DisplayName("getActiveHold with valid hold should return hold")
        void getActiveHold_WhenValid_ShouldReturnHold() {
            // Arrange
            when(seatHoldRepository.findById(1L)).thenReturn(Optional.of(testHold));

            // Act
            SeatHold result = seatLockService.getActiveHold(1L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo("user123");
        }

        @Test
        @DisplayName("getActiveHold with expired hold should throw exception")
        void getActiveHold_WhenExpired_ShouldThrowException() {
            // Arrange
            testHold.setExpirationTime(LocalDateTime.now().minusMinutes(5));
            when(seatHoldRepository.findById(1L)).thenReturn(Optional.of(testHold));

            // Act & Assert
            assertThatThrownBy(() -> seatLockService.getActiveHold(1L))
                    .isInstanceOf(InvalidHoldException.class)
                    .hasMessageContaining("expired");
        }
    }
}
