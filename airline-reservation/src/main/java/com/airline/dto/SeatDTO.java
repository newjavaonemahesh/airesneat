package com.airline.dto;

import com.airline.model.SeatStatus;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatDTO {
    private Long id;
    private String seatNumber;
    private SeatStatus status;
    private String fareClassName;
    private BigDecimal price;
}
