package com.airline.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequest {
    
    @NotNull(message = "Hold ID is required")
    private Long holdId;
    
    @NotNull(message = "Passenger ID is required")
    private Long passengerId;
}
