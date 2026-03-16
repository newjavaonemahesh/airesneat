package com.airline.controller;

import com.airline.dto.SeatHoldDTO;
import com.airline.dto.SeatHoldRequest;
import com.airline.service.SeatLockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/seats")
@RequiredArgsConstructor
@Tag(name = "Seats", description = "Seat hold operations")
public class SeatController {

    private final SeatLockService seatLockService;

    @PostMapping("/hold")
    @Operation(summary = "Hold a seat", description = "Places a temporary hold on a seat (10 minutes)")
    public ResponseEntity<SeatHoldDTO> holdSeat(@Valid @RequestBody SeatHoldRequest request) {
        SeatHoldDTO hold = seatLockService.holdSeat(request.getSeatId(), request.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(hold);
    }

    @DeleteMapping("/hold/{holdId}")
    @Operation(summary = "Release seat hold", description = "Releases a held seat")
    public ResponseEntity<Void> releaseHold(@PathVariable Long holdId) {
        seatLockService.releaseHold(holdId);
        return ResponseEntity.noContent().build();
    }
}
