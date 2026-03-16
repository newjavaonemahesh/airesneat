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
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Booking operations")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "Create booking", description = "Creates a booking from a valid hold")
    public ResponseEntity<BookingDTO> createBooking(@Valid @RequestBody BookingRequest request) {
        BookingDTO booking = bookingService.createBooking(request.getHoldId(), request.getPassengerId());
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    @DeleteMapping("/{bookingId}")
    @Operation(summary = "Cancel booking", description = "Cancels a booking and releases the seat")
    public ResponseEntity<BookingDTO> cancelBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.cancelBooking(bookingId));
    }
}
