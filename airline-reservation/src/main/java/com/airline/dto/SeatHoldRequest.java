package com.airline.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatHoldRequest {
    
    @NotNull(message = "Seat ID is required")
    private Long seatId;
    
    @NotBlank(message = "User ID is required")
    private String userId;
}
