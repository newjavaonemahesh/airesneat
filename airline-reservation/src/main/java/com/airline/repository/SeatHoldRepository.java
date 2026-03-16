package com.airline.repository;

import com.airline.model.SeatHold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatHoldRepository extends JpaRepository<SeatHold, Long> {

    Optional<SeatHold> findBySeatIdAndActiveTrue(Long seatId);

    Optional<SeatHold> findBySeatIdAndUserIdAndActiveTrue(Long seatId, String userId);

    List<SeatHold> findByUserIdAndActiveTrue(String userId);

    @Query("SELECT sh FROM SeatHold sh WHERE sh.active = true AND sh.expirationTime < :now")
    List<SeatHold> findExpiredHolds(@Param("now") LocalDateTime now);
}
