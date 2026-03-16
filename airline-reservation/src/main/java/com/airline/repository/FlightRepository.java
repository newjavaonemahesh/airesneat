package com.airline.repository;

import com.airline.model.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {

    Optional<Flight> findByFlightNumber(String flightNumber);

    @Query("SELECT f FROM Flight f WHERE " +
           "(:departureAirport IS NULL OR f.departureAirport = :departureAirport) AND " +
           "(:arrivalAirport IS NULL OR f.arrivalAirport = :arrivalAirport) AND " +
           "(:departureFrom IS NULL OR f.departureTime >= :departureFrom) AND " +
           "(:departureTo IS NULL OR f.departureTime <= :departureTo)")
    List<Flight> searchFlights(
            @Param("departureAirport") String departureAirport,
            @Param("arrivalAirport") String arrivalAirport,
            @Param("departureFrom") LocalDateTime departureFrom,
            @Param("departureTo") LocalDateTime departureTo);
}
