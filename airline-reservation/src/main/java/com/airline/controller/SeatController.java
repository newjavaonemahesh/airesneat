package com.airline.controller;

import com.airline.dto.SeatHoldDTO;
import com.airline.service.SeatHoldService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
@Tag(name = "Seats", description = "Seat hold operations")
public class SeatController {

    private final SeatHoldService seatHoldService;

    @PostMapping("/{seatId}/hold")
    @Operation(summary = "Hold a seat", description = "Places a temporary hold on a seat for 10 minutes")
    public ResponseEntity<SeatHoldDTO> holdSeat(@PathVariable Long seatId) {
        return ResponseEntity.ok(seatHoldService.holdSeat(seatId));
    }

    @DeleteMapping("/hold/{holdToken}")
    @Operation(summary = "Release seat hold", description = "Releases a held seat, making it available again")
    public ResponseEntity<Void> releaseHold(@PathVariable String holdToken) {
        seatHoldService.releaseHold(holdToken);
        return ResponseEntity.noContent().build();
    }
}
