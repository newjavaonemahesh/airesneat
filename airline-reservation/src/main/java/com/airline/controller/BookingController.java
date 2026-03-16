package com.airline.controller;

import com.airline.dto.BookingDTO;
import com.airline.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Booking operations")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "Create booking", description = "Creates a booking from a valid hold")
    public ResponseEntity<BookingDTO> createBooking(
            @RequestParam Long holdId,
            @RequestParam Long passengerId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.createBooking(holdId, passengerId));
    }

    @GetMapping("/{bookingId}")
    @Operation(summary = "Get booking", description = "Returns booking details")
    public ResponseEntity<BookingDTO> getBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.getBookingById(bookingId));
    }

    @DeleteMapping("/{bookingId}")
    @Operation(summary = "Cancel booking", description = "Cancels a booking and releases the seat")
    public ResponseEntity<BookingDTO> cancelBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.cancelBooking(bookingId));
    }
}
