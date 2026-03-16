package com.airline.repository;

import com.airline.model.Booking;
import com.airline.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findBySeatIdAndStatus(Long seatId, BookingStatus status);

    List<Booking> findByPassengerId(Long passengerId);

    List<Booking> findByPassengerIdAndStatus(Long passengerId, BookingStatus status);
}
