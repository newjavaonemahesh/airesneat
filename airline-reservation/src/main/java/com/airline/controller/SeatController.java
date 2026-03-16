package com.airline.controller;

import com.airline.dto.SeatHoldDTO;
import com.airline.service.SeatLockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
@Tag(name = "Seats", description = "Seat hold/lock operations")
public class SeatController {

    private final SeatLockService seatLockService;

    @PostMapping("/{seatId}/hold")
    @Operation(summary = "Hold a seat", description = "Places a temporary hold on a seat (10 minutes)")
    public ResponseEntity<SeatHoldDTO> holdSeat(
            @PathVariable Long seatId,
            @RequestParam String userId) {
        return ResponseEntity.ok(seatLockService.holdSeat(seatId, userId));
    }

    @DeleteMapping("/hold/{holdId}")
    @Operation(summary = "Release seat hold", description = "Releases a held seat")
    public ResponseEntity<Void> releaseHold(@PathVariable Long holdId) {
        seatLockService.releaseHold(holdId);
        return ResponseEntity.noContent().build();
    }
}
