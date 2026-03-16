package com.airline.controller;

import com.airline.dto.BookingDTO;
import com.airline.dto.BookingRequest;
import com.airline.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Booking confirmation and cancellation operations")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/seats/{seatId}")
    @Operation(summary = "Confirm booking", description = "Confirms a booking for a held seat")
    public ResponseEntity<BookingDTO> confirmBooking(
            @PathVariable Long seatId,
            @Valid @RequestBody BookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.confirmBooking(seatId, request));
    }

    @GetMapping("/{bookingId}")
    @Operation(summary = "Get booking details", description = "Returns booking details by booking ID")
    public ResponseEntity<BookingDTO> getBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.getBookingById(bookingId));
    }

    @DeleteMapping("/{bookingId}")
    @Operation(summary = "Cancel booking", description = "Cancels a booking and releases the seat")
    public ResponseEntity<BookingDTO> cancelBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.cancelBooking(bookingId));
    }
}
