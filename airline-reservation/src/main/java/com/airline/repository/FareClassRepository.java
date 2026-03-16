package com.airline.repository;

import com.airline.model.FareClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FareClassRepository extends JpaRepository<FareClass, Long> {
    Optional<FareClass> findByName(String name);
}
