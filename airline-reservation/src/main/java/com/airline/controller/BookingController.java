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

    @PostMapping
    @Operation(summary = "Confirm booking", description = "Confirms a booking using a valid hold token")
    public ResponseEntity<BookingDTO> confirmBooking(@Valid @RequestBody BookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.confirmBooking(request));
    }

    @GetMapping("/{bookingReference}")
    @Operation(summary = "Get booking details", description = "Returns booking details by booking reference")
    public ResponseEntity<BookingDTO> getBooking(@PathVariable String bookingReference) {
        return ResponseEntity.ok(bookingService.getBookingByReference(bookingReference));
    }

    @DeleteMapping("/{bookingReference}")
    @Operation(summary = "Cancel booking", description = "Cancels a booking and releases the seat")
    public ResponseEntity<BookingDTO> cancelBooking(@PathVariable String bookingReference) {
        return ResponseEntity.ok(bookingService.cancelBooking(bookingReference));
    }
}
