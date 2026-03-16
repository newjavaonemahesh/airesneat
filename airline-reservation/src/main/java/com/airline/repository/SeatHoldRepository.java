package com.airline.repository;

import com.airline.model.SeatHold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatHoldRepository extends JpaRepository<SeatHold, Long> {

    Optional<SeatHold> findByHoldTokenAndActiveTrue(String holdToken);

    Optional<SeatHold> findBySeatIdAndActiveTrue(Long seatId);

    @Query("SELECT sh FROM SeatHold sh WHERE sh.active = true AND sh.expiresAt < :now")
    List<SeatHold> findExpiredHolds(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE SeatHold sh SET sh.active = false WHERE sh.active = true AND sh.expiresAt < :now")
    int deactivateExpiredHolds(@Param("now") LocalDateTime now);
}
