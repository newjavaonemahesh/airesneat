package com.airline.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatHoldDTO {
    private Long holdId;
    private Long seatId;
    private String seatNumber;
    private String userId;
    private LocalDateTime holdTime;
    private LocalDateTime expirationTime;
    private String message;
}
