package com.airline.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightSearchRequest {
    private String departureAirport;
    private String arrivalAirport;
    private LocalDateTime departureFrom;
    private LocalDateTime departureTo;
}
