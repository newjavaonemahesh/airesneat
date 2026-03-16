package com.airline.controller;

import com.airline.dto.FlightDTO;
import com.airline.dto.FlightSearchRequest;
import com.airline.dto.SeatDTO;
import com.airline.exception.ResourceNotFoundException;
import com.airline.model.SeatStatus;
import com.airline.service.FlightService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FlightController.class)
class FlightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FlightService flightService;

    private FlightDTO testFlight;
    private SeatDTO testSeat;

    @BeforeEach
    void setUp() {
        testFlight = FlightDTO.builder()
                .id(1L)
                .flightNumber("AA100")
                .origin("JFK")
                .destination("LAX")
                .departureTime(LocalDateTime.of(2024, 1, 15, 10, 0))
                .arrivalTime(LocalDateTime.of(2024, 1, 15, 16, 0))
                .totalSeats(50)
                .availableSeats(45)
                .build();

        testSeat = SeatDTO.builder()
                .id(1L)
                .seatNumber("1A")
                .status(SeatStatus.AVAILABLE)
                .fareClassName("ECONOMY")
                .price(new BigDecimal("150.00"))
                .build();
    }

    @Test
    void getAllFlights_ShouldReturnFlightList() throws Exception {
        when(flightService.getAllFlights()).thenReturn(Arrays.asList(testFlight));

        mockMvc.perform(get("/api/flights"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].flightNumber").value("AA100"))
                .andExpect(jsonPath("$[0].origin").value("JFK"));
    }

    @Test
    void searchFlights_ShouldReturnMatchingFlights() throws Exception {
        when(flightService.searchFlights(any(FlightSearchRequest.class)))
                .thenReturn(Arrays.asList(testFlight));

        mockMvc.perform(get("/api/flights/search")
                        .param("origin", "JFK")
                        .param("destination", "LAX"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].flightNumber").value("AA100"));
    }

    @Test
    void getFlightById_ShouldReturnFlight() throws Exception {
        when(flightService.getFlightById(1L)).thenReturn(testFlight);

        mockMvc.perform(get("/api/flights/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flightNumber").value("AA100"));
    }

    @Test
    void getFlightById_WhenNotFound_ShouldReturn404() throws Exception {
        when(flightService.getFlightById(999L))
                .thenThrow(new ResourceNotFoundException("Flight not found"));

        mockMvc.perform(get("/api/flights/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getSeatsForFlight_ShouldReturnSeats() throws Exception {
        when(flightService.getSeatsForFlight(1L)).thenReturn(Arrays.asList(testSeat));

        mockMvc.perform(get("/api/flights/1/seats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].seatNumber").value("1A"));
    }

    @Test
    void getAvailableSeatsForFlight_ShouldReturnOnlyAvailableSeats() throws Exception {
        when(flightService.getAvailableSeatsForFlight(1L)).thenReturn(Arrays.asList(testSeat));

        mockMvc.perform(get("/api/flights/1/seats/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"));
    }
}
