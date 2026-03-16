package com.airline.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatHoldRequest {
    
    @NotBlank(message = "User ID is required")
    private String userId;
}
