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

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PassengerRepository passengerRepository;
    private final SeatRepository seatRepository;
    private final SeatHoldService seatHoldService;

    @Transactional
    public BookingDTO confirmBooking(Long seatId, BookingRequest request) {
        // Validate hold
        SeatHold seatHold = seatHoldService.validateAndGetHold(seatId, request.getUserId());
        Seat seat = seatHold.getSeat();

        // Create or get passenger
        Passenger passenger = passengerRepository.findByEmail(request.getEmail())
                .orElseGet(() -> passengerRepository.save(
                        Passenger.builder()
                                .name(request.getPassengerName())
                                .email(request.getEmail())
                                .build()
                ));

        // Create booking
        Booking booking = Booking.builder()
                .seat(seat)
                .passenger(passenger)
                .bookingTime(LocalDateTime.now())
                .status(BookingStatus.CONFIRMED)
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
    public BookingDTO cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new InvalidHoldException("Booking is already cancelled");
        }

        // Update booking status
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Release seat
        Seat seat = booking.getSeat();
        seat.setStatus(SeatStatus.AVAILABLE);
        seatRepository.save(seat);

        return toBookingDTO(booking);
    }

    @Transactional(readOnly = true)
    public BookingDTO getBookingById(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
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
