package com.airline.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatHoldDTO {
    private Long holdId;
    private Long seatId;
    private String seatNumber;
    private String holdToken;
    private String expiresAt;
    private String message;
}
