package com.airline.service;

import com.airline.dto.SeatHoldDTO;
import com.airline.exception.InvalidHoldException;
import com.airline.exception.ResourceNotFoundException;
import com.airline.exception.SeatNotAvailableException;
import com.airline.model.Seat;
import com.airline.model.SeatHold;
import com.airline.model.SeatStatus;
import com.airline.repository.SeatHoldRepository;
import com.airline.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatLockService {

    private final SeatRepository seatRepository;
    private final SeatHoldRepository seatHoldRepository;

    @Value("${airline.seat-hold.duration-minutes:10}")
    private int holdDurationMinutes;

    /**
     * Hold a seat for a user.
     * Seat state transition: AVAILABLE → HELD
     * 
     * @param seatId the seat to hold
     * @param userId the user requesting the hold
     * @return hold details including expiration time
     * @throws ResourceNotFoundException if seat not found
     * @throws SeatNotAvailableException if seat is not available (already HELD or BOOKED)
     */
    @Transactional
    public SeatHoldDTO holdSeat(Long seatId, String userId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found with id: " + seatId));

        // Check if seat is available
        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            throw new SeatNotAvailableException(
                "Seat " + seat.getSeatNumber() + " is not available. Current status: " + seat.getStatus()
            );
        }

        // Create hold record
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expirationTime = now.plusMinutes(holdDurationMinutes);

        SeatHold seatHold = SeatHold.builder()
                .seat(seat)
                .userId(userId)
                .holdTime(now)
                .expirationTime(expirationTime)
                .active(true)
                .build();

        seatHoldRepository.save(seatHold);

        // Update seat status: AVAILABLE → HELD
        seat.setStatus(SeatStatus.HELD);
        seatRepository.save(seat);

        log.info("Seat {} held by user {} until {}", seat.getSeatNumber(), userId, expirationTime);

        return SeatHoldDTO.builder()
                .holdId(seatHold.getId())
                .seatId(seat.getId())
                .seatNumber(seat.getSeatNumber())
                .userId(userId)
                .holdTime(now)
                .expirationTime(expirationTime)
                .message("Seat held successfully. Complete booking within " + holdDurationMinutes + " minutes.")
                .build();
    }

    /**
     * Release a hold on a seat.
     * Seat state transition: HELD → AVAILABLE
     * 
     * @param holdId the hold to release
     * @throws ResourceNotFoundException if hold not found
     * @throws InvalidHoldException if hold is not active
     */
    @Transactional
    public void releaseHold(Long holdId) {
        SeatHold seatHold = seatHoldRepository.findById(holdId)
                .orElseThrow(() -> new ResourceNotFoundException("Hold not found with id: " + holdId));

        if (!seatHold.isActive()) {
            throw new InvalidHoldException("Hold is no longer active");
        }

        releaseHoldInternal(seatHold);
        log.info("Hold {} released for seat {}", holdId, seatHold.getSeat().getSeatNumber());
    }

    /**
     * Expire all holds that have passed their expiration time.
     * Seat state transition: HELD → AVAILABLE (for expired holds)
     * Runs automatically every minute.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void expireHolds() {
        LocalDateTime now = LocalDateTime.now();
        List<SeatHold> expiredHolds = seatHoldRepository.findExpiredHolds(now);
        
        for (SeatHold hold : expiredHolds) {
            releaseHoldInternal(hold);
            log.info("Expired hold {} for seat {}", hold.getId(), hold.getSeat().getSeatNumber());
        }
        
        if (!expiredHolds.isEmpty()) {
            log.info("Released {} expired holds", expiredHolds.size());
        }
    }

    /**
     * Validate and get an active hold for booking.
     * Used internally by BookingService.
     */
    @Transactional(readOnly = true)
    public SeatHold getActiveHold(Long holdId) {
        SeatHold seatHold = seatHoldRepository.findById(holdId)
                .orElseThrow(() -> new ResourceNotFoundException("Hold not found with id: " + holdId));

        if (!seatHold.isActive()) {
            throw new InvalidHoldException("Hold is no longer active");
        }

        if (seatHold.getExpirationTime().isBefore(LocalDateTime.now())) {
            throw new InvalidHoldException("Hold has expired. Please hold the seat again.");
        }

        return seatHold;
    }

    /**
     * Deactivate a hold after successful booking.
     * Used internally by BookingService.
     */
    @Transactional
    public void deactivateHold(SeatHold seatHold) {
        seatHold.setActive(false);
        seatHoldRepository.save(seatHold);
    }

    private void releaseHoldInternal(SeatHold seatHold) {
        Seat seat = seatHold.getSeat();
        
        // Only release if seat is still in HELD status
        if (seat.getStatus() == SeatStatus.HELD) {
            seat.setStatus(SeatStatus.AVAILABLE);
            seatRepository.save(seat);
        }
        
        seatHold.setActive(false);
        seatHoldRepository.save(seatHold);
    }
}
