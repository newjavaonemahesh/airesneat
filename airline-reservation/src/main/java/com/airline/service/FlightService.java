package com.airline.service;

import com.airline.dto.FlightDTO;
import com.airline.dto.FlightSearchRequest;
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

    public List<FlightDTO> searchFlights(FlightSearchRequest request) {
        // Filter flights based on search criteria
        return flightRepository.findAll().stream()
                .filter(f -> request.getDepartureAirport() == null || 
                             f.getDepartureAirport().equals(request.getDepartureAirport()))
                .filter(f -> request.getArrivalAirport() == null || 
                             f.getArrivalAirport().equals(request.getArrivalAirport()))
                .filter(f -> request.getDepartureFrom() == null || 
                             !f.getDepartureTime().isBefore(request.getDepartureFrom()))
                .filter(f -> request.getDepartureTo() == null || 
                             !f.getDepartureTime().isAfter(request.getDepartureTo()))
                .map(this::toFlightDTO)
                .collect(Collectors.toList());
    }

    public List<FlightDTO> getAllFlights() {
        return flightRepository.findAll().stream()
                .map(this::toFlightDTO)
                .collect(Collectors.toList());
    }

    public FlightDTO getFlightById(Long id) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found with id: " + id));
        return toFlightDTO(flight);
    }

    public List<SeatDTO> getSeatsForFlight(Long flightId) {
        if (!flightRepository.existsById(flightId)) {
            throw new ResourceNotFoundException("Flight not found with id: " + flightId);
        }
        
        List<Seat> seats = seatRepository.findByFlightId(flightId);
        return seats.stream()
                .map(this::toSeatDTO)
                .collect(Collectors.toList());
    }

    public List<SeatDTO> getAvailableSeatsForFlight(Long flightId) {
        if (!flightRepository.existsById(flightId)) {
            throw new ResourceNotFoundException("Flight not found with id: " + flightId);
        }
        
        List<Seat> seats = seatRepository.findByFlightIdAndStatus(flightId, SeatStatus.AVAILABLE);
        return seats.stream()
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
