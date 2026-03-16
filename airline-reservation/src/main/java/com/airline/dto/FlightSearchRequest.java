package com.airline.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightSearchRequest {
    private String origin;
    private String destination;
    private LocalDateTime departureFrom;
    private LocalDateTime departureTo;
}
