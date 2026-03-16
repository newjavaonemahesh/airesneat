package com.airline.service;

import com.airline.dto.FlightDTO;
import com.airline.dto.SeatDTO;
import com.airline.exception.ResourceNotFoundException;
import com.airline.model.Flight;
import com.airline.model.Seat;
import com.airline.model.SeatStatus;
import com.airline.repository.FlightRepository;
import com.airline.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlightService {

    private final FlightRepository flightRepository;
    private final SeatRepository seatRepository;

    /**
     * Get all available flights
     */
    public List<FlightDTO> getFlights() {
        return flightRepository.findAll().stream()
                .map(this::toFlightDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all seats for a specific flight
     */
    public List<SeatDTO> getSeatsForFlight(Long flightId) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found with id: " + flightId));
        
        return seatRepository.findByFlightId(flightId).stream()
                .map(this::toSeatDTO)
                .collect(Collectors.toList());
    }

    private FlightDTO toFlightDTO(Flight flight) {
        int totalSeats = flight.getSeats().size();
        int availableSeats = seatRepository.countByFlightIdAndStatus(flight.getId(), SeatStatus.AVAILABLE);
        
        return FlightDTO.builder()
                .id(flight.getId())
                .flightNumber(flight.getFlightNumber())
                .departureAirport(flight.getDepartureAirport())
                .arrivalAirport(flight.getArrivalAirport())
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .totalSeats(totalSeats)
                .availableSeats(availableSeats)
                .build();
    }

    private SeatDTO toSeatDTO(Seat seat) {
        return SeatDTO.builder()
                .id(seat.getId())
                .seatNumber(seat.getSeatNumber())
                .rowNumber(seat.getRowNumber())
                .fareClass(seat.getFareClass())
                .status(seat.getStatus())
                .build();
    }
}
