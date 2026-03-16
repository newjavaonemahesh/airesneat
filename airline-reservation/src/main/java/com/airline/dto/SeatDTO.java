package com.airline.dto;

import com.airline.model.FareClass;
import com.airline.model.SeatStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatDTO {
    private Long id;
    private String seatNumber;
    private Integer rowNumber;
    private FareClass fareClass;
    private SeatStatus status;
}
