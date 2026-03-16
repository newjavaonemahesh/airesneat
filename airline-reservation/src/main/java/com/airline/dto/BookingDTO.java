package com.airline.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDTO {
    private Long id;
    private String bookingReference;
    private String seatNumber;
    private String flightNumber;
    private String passengerName;
    private String passengerEmail;
    private BigDecimal totalFare;
    private LocalDateTime bookedAt;
    private boolean cancelled;
}
