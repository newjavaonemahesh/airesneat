package com.airline.service;

import com.airline.dto.BookingDTO;
import com.airline.dto.BookingRequest;
import com.airline.exception.InvalidHoldException;
import com.airline.exception.ResourceNotFoundException;
import com.airline.model.*;
import com.airline.repository.BookingRepository;
import com.airline.repository.PassengerRepository;
import com.airline.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PassengerRepository passengerRepository;
    private final SeatRepository seatRepository;
    private final SeatHoldService seatHoldService;

    @Transactional
    public BookingDTO confirmBooking(BookingRequest request) {
        // Validate hold
        SeatHold seatHold = seatHoldService.validateAndGetHold(request.getHoldToken());
        Seat seat = seatHold.getSeat();

        // Create or get passenger
        Passenger passenger = passengerRepository.findByEmail(request.getEmail())
                .orElseGet(() -> passengerRepository.save(
                        Passenger.builder()
                                .firstName(request.getFirstName())
                                .lastName(request.getLastName())
                                .email(request.getEmail())
                                .phoneNumber(request.getPhoneNumber())
                                .build()
                ));

        // Create booking
        String bookingReference = generateBookingReference();
        Booking booking = Booking.builder()
                .bookingReference(bookingReference)
                .seat(seat)
                .passenger(passenger)
                .totalFare(seat.getFareClass().getBasePrice())
                .bookedAt(LocalDateTime.now())
                .cancelled(false)
                .build();

        bookingRepository.save(booking);

        // Update seat status to BOOKED
        seat.setStatus(SeatStatus.BOOKED);
        seatRepository.save(seat);

        // Deactivate the hold
        seatHoldService.deactivateHold(seatHold);

        return toBookingDTO(booking);
    }

    @Transactional
    public BookingDTO cancelBooking(String bookingReference) {
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with reference: " + bookingReference));

        if (booking.isCancelled()) {
            throw new InvalidHoldException("Booking is already cancelled");
        }

        // Update booking
        booking.setCancelled(true);
        booking.setCancelledAt(LocalDateTime.now());
        bookingRepository.save(booking);

        // Release seat
        Seat seat = booking.getSeat();
        seat.setStatus(SeatStatus.AVAILABLE);
        seatRepository.save(seat);

        return toBookingDTO(booking);
    }

    @Transactional(readOnly = true)
    public BookingDTO getBookingByReference(String bookingReference) {
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with reference: " + bookingReference));
        return toBookingDTO(booking);
    }

    private String generateBookingReference() {
        return "BK" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private BookingDTO toBookingDTO(Booking booking) {
        Seat seat = booking.getSeat();
        Passenger passenger = booking.getPassenger();
        
        return BookingDTO.builder()
                .id(booking.getId())
                .bookingReference(booking.getBookingReference())
                .seatNumber(seat.getSeatNumber())
                .flightNumber(seat.getFlight().getFlightNumber())
                .passengerName(passenger.getFirstName() + " " + passenger.getLastName())
                .passengerEmail(passenger.getEmail())
                .totalFare(booking.getTotalFare())
                .bookedAt(booking.getBookedAt())
                .cancelled(booking.isCancelled())
                .build();
    }
}
