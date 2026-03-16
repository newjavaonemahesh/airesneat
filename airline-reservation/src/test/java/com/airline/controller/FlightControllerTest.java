package com.airline.controller;

import com.airline.dto.FlightDTO;
import com.airline.dto.SeatDTO;
import com.airline.exception.ResourceNotFoundException;
import com.airline.model.FareClass;
import com.airline.model.SeatStatus;
import com.airline.service.FlightService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;

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
                .departureAirport("JFK")
                .arrivalAirport("LAX")
                .departureTime(LocalDateTime.of(2024, 1, 15, 10, 0))
                .arrivalTime(LocalDateTime.of(2024, 1, 15, 16, 0))
                .totalSeats(50)
                .availableSeats(45)
                .build();

        testSeat = SeatDTO.builder()
                .id(1L)
                .seatNumber("1A")
                .rowNumber(1)
                .fareClass(FareClass.FIRST)
                .status(SeatStatus.AVAILABLE)
                .build();
    }

    @Test
    void getFlights_ShouldReturnFlightList() throws Exception {
        when(flightService.getFlights()).thenReturn(Arrays.asList(testFlight));

        mockMvc.perform(get("/flights"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].flightNumber").value("AA100"))
                .andExpect(jsonPath("$[0].departureAirport").value("JFK"));
    }

    @Test
    void getSeatsForFlight_ShouldReturnSeats() throws Exception {
        when(flightService.getSeatsForFlight(1L)).thenReturn(Arrays.asList(testSeat));

        mockMvc.perform(get("/flights/1/seats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].seatNumber").value("1A"))
                .andExpect(jsonPath("$[0].rowNumber").value(1))
                .andExpect(jsonPath("$[0].fareClass").value("FIRST"))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"));
    }

    @Test
    void getSeatsForFlight_WhenFlightNotFound_ShouldReturn404() throws Exception {
        when(flightService.getSeatsForFlight(999L))
                .thenThrow(new ResourceNotFoundException("Flight not found"));

        mockMvc.perform(get("/flights/999/seats"))
                .andExpect(status().isNotFound());
    }
}
