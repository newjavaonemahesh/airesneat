package com.airline.controller;

import com.airline.dto.FlightDTO;
import com.airline.dto.FlightSearchRequest;
import com.airline.dto.SeatDTO;
import com.airline.service.FlightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
@Tag(name = "Flights", description = "Flight search and seat viewing operations")
public class FlightController {

    private final FlightService flightService;

    @GetMapping
    @Operation(summary = "Get all flights", description = "Returns a list of all available flights")
    public ResponseEntity<List<FlightDTO>> getAllFlights() {
        return ResponseEntity.ok(flightService.getAllFlights());
    }

    @GetMapping("/search")
    @Operation(summary = "Search flights", description = "Search flights by origin, destination, and departure time range")
    public ResponseEntity<List<FlightDTO>> searchFlights(
            @Parameter(description = "Origin airport code") @RequestParam(required = false) String origin,
            @Parameter(description = "Destination airport code") @RequestParam(required = false) String destination,
            @Parameter(description = "Departure time from") @RequestParam(required = false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime departureFrom,
            @Parameter(description = "Departure time to") @RequestParam(required = false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime departureTo) {
        
        FlightSearchRequest request = FlightSearchRequest.builder()
                .origin(origin)
                .destination(destination)
                .departureFrom(departureFrom)
                .departureTo(departureTo)
                .build();
        
        return ResponseEntity.ok(flightService.searchFlights(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get flight by ID", description = "Returns details of a specific flight")
    public ResponseEntity<FlightDTO> getFlightById(@PathVariable Long id) {
        return ResponseEntity.ok(flightService.getFlightById(id));
    }

    @GetMapping("/{flightId}/seats")
    @Operation(summary = "Get all seats for a flight", description = "Returns all seats for a specific flight with their status")
    public ResponseEntity<List<SeatDTO>> getSeatsForFlight(@PathVariable Long flightId) {
        return ResponseEntity.ok(flightService.getSeatsForFlight(flightId));
    }

    @GetMapping("/{flightId}/seats/available")
    @Operation(summary = "Get available seats for a flight", description = "Returns only available seats for a specific flight")
    public ResponseEntity<List<SeatDTO>> getAvailableSeatsForFlight(@PathVariable Long flightId) {
        return ResponseEntity.ok(flightService.getAvailableSeatsForFlight(flightId));
    }
}
