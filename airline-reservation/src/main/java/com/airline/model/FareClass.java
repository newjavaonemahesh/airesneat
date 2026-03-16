package com.airline.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "fare_classes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FareClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private BigDecimal basePrice;

    private String description;
}
