package com.airline.controller;

import com.airline.dto.FlightDTO;
import com.airline.dto.SeatDTO;
import com.airline.service.FlightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/flights")
@RequiredArgsConstructor
@Tag(name = "Flights", description = "Flight and seat retrieval operations")
public class FlightController {

    private final FlightService flightService;

    @GetMapping
    @Operation(summary = "Get all flights", description = "Returns a list of all available flights")
    public ResponseEntity<List<FlightDTO>> getFlights() {
        return ResponseEntity.ok(flightService.getFlights());
    }

    @GetMapping("/{flightId}/seats")
    @Operation(summary = "Get seats for a flight", description = "Returns all seats for a specific flight")
    public ResponseEntity<List<SeatDTO>> getSeatsForFlight(@PathVariable Long flightId) {
        return ResponseEntity.ok(flightService.getSeatsForFlight(flightId));
    }
}
