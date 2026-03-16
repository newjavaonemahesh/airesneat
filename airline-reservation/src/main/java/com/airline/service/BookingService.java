package com.airline.service;

import com.airline.dto.BookingDTO;
import com.airline.exception.InvalidHoldException;
import com.airline.exception.ResourceNotFoundException;
import com.airline.model.*;
import com.airline.repository.BookingRepository;
import com.airline.repository.PassengerRepository;
import com.airline.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PassengerRepository passengerRepository;
    private final SeatRepository seatRepository;
    private final SeatLockService seatLockService;

    /**
     * Create a booking from a valid hold.
     * Seat state transition: HELD → BOOKED
     * 
     * Rules:
     * - Hold must be active and not expired
     * - Seat must be in HELD status
     * - If seat is HELD by another user, booking fails
     * 
     * @param holdId the hold to convert to booking
     * @param passengerId the passenger making the booking
     * @return booking details
     * @throws ResourceNotFoundException if hold or passenger not found
     * @throws InvalidHoldException if hold is invalid, expired, or belongs to another user
     */
    @Transactional
    public BookingDTO createBooking(Long holdId, Long passengerId) {
        // Validate hold
        SeatHold seatHold = seatLockService.getActiveHold(holdId);
        Seat seat = seatHold.getSeat();

        // Verify seat is still in HELD status
        if (seat.getStatus() != SeatStatus.HELD) {
            throw new InvalidHoldException(
                "Seat is no longer held. Current status: " + seat.getStatus()
            );
        }

        // Get passenger
        Passenger passenger = passengerRepository.findById(passengerId)
                .orElseThrow(() -> new ResourceNotFoundException("Passenger not found with id: " + passengerId));

        // Create booking
        Booking booking = Booking.builder()
                .seat(seat)
                .passenger(passenger)
                .bookingTime(LocalDateTime.now())
                .status(BookingStatus.CONFIRMED)
                .build();

        bookingRepository.save(booking);

        // Update seat status: HELD → BOOKED
        seat.setStatus(SeatStatus.BOOKED);
        seatRepository.save(seat);

        // Deactivate the hold
        seatLockService.deactivateHold(seatHold);

        log.info("Booking created: seat {} for passenger {}", seat.getSeatNumber(), passenger.getName());

        return toBookingDTO(booking);
    }

    /**
     * Get booking by ID
     */
    @Transactional(readOnly = true)
    public BookingDTO getBookingById(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        return toBookingDTO(booking);
    }

    /**
     * Cancel a booking.
     * Seat state transition: BOOKED → AVAILABLE
     */
    @Transactional
    public BookingDTO cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new InvalidHoldException("Booking is already cancelled");
        }

        // Update booking status
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Release seat: BOOKED → AVAILABLE
        Seat seat = booking.getSeat();
        seat.setStatus(SeatStatus.AVAILABLE);
        seatRepository.save(seat);

        log.info("Booking {} cancelled, seat {} released", bookingId, seat.getSeatNumber());

        return toBookingDTO(booking);
    }

    private BookingDTO toBookingDTO(Booking booking) {
        Seat seat = booking.getSeat();
        Passenger passenger = booking.getPassenger();
        
        return BookingDTO.builder()
                .id(booking.getId())
                .passengerName(passenger.getName())
                .passengerEmail(passenger.getEmail())
                .seatNumber(seat.getSeatNumber())
                .flightNumber(seat.getFlight().getFlightNumber())
                .bookingTime(booking.getBookingTime())
                .status(booking.getStatus())
                .build();
    }
}
