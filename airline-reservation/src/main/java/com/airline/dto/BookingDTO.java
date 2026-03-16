package com.airline.dto;

import com.airline.model.BookingStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDTO {
    private Long id;
    private String passengerName;
    private String passengerEmail;
    private String seatNumber;
    private String flightNumber;
    private LocalDateTime bookingTime;
    private BookingStatus status;
}
