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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeatHoldService {

    private final SeatRepository seatRepository;
    private final SeatHoldRepository seatHoldRepository;

    @Value("${airline.seat-hold.duration-minutes:10}")
    private int holdDurationMinutes;

    @Transactional
    public SeatHoldDTO holdSeat(Long seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found with id: " + seatId));

        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            throw new SeatNotAvailableException("Seat " + seat.getSeatNumber() + " is not available. Current status: " + seat.getStatus());
        }

        // Create hold
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(holdDurationMinutes);
        String holdToken = UUID.randomUUID().toString();

        SeatHold seatHold = SeatHold.builder()
                .seat(seat)
                .holdToken(holdToken)
                .createdAt(now)
                .expiresAt(expiresAt)
                .active(true)
                .build();

        seatHoldRepository.save(seatHold);

        // Update seat status
        seat.setStatus(SeatStatus.HELD);
        seatRepository.save(seat);

        return SeatHoldDTO.builder()
                .holdId(seatHold.getId())
                .seatId(seat.getId())
                .seatNumber(seat.getSeatNumber())
                .holdToken(holdToken)
                .expiresAt(expiresAt.toString())
                .message("Seat held successfully. Complete booking within " + holdDurationMinutes + " minutes.")
                .build();
    }

    @Transactional
    public void releaseHold(String holdToken) {
        SeatHold seatHold = seatHoldRepository.findByHoldTokenAndActiveTrue(holdToken)
                .orElseThrow(() -> new InvalidHoldException("Invalid or expired hold token"));

        releaseHoldInternal(seatHold);
    }

    @Transactional
    public SeatHold validateAndGetHold(String holdToken) {
        SeatHold seatHold = seatHoldRepository.findByHoldTokenAndActiveTrue(holdToken)
                .orElseThrow(() -> new InvalidHoldException("Invalid or expired hold token"));

        if (seatHold.getExpiresAt().isBefore(LocalDateTime.now())) {
            releaseHoldInternal(seatHold);
            throw new InvalidHoldException("Hold has expired. Please hold the seat again.");
        }

        return seatHold;
    }

    @Transactional
    public void deactivateHold(SeatHold seatHold) {
        seatHold.setActive(false);
        seatHoldRepository.save(seatHold);
    }

    @Scheduled(fixedRate = 60000) // Run every minute
    @Transactional
    public void releaseExpiredHolds() {
        LocalDateTime now = LocalDateTime.now();
        List<SeatHold> expiredHolds = seatHoldRepository.findExpiredHolds(now);
        
        for (SeatHold hold : expiredHolds) {
            releaseHoldInternal(hold);
        }
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
