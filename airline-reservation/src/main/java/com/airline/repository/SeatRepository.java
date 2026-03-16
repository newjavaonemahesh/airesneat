package com.airline.repository;

import com.airline.model.Seat;
import com.airline.model.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByFlightId(Long flightId);

    List<Seat> findByFlightIdAndStatus(Long flightId, SeatStatus status);

    int countByFlightIdAndStatus(Long flightId, SeatStatus status);
}
