package com.airline.repository;

import com.airline.model.Seat;
import com.airline.model.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByFlightId(Long flightId);

    List<Seat> findByFlightIdAndStatus(Long flightId, SeatStatus status);

    @Query("SELECT COUNT(s) FROM Seat s WHERE s.flight.id = :flightId AND s.status = :status")
    int countByFlightIdAndStatus(@Param("flightId") Long flightId, @Param("status") SeatStatus status);
}
