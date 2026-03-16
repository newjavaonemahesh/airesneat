package com.airline.controller;

import com.airline.dto.SeatHoldDTO;
import com.airline.dto.SeatHoldRequest;
import com.airline.exception.InvalidHoldException;
import com.airline.exception.ResourceNotFoundException;
import com.airline.exception.SeatNotAvailableException;
import com.airline.service.SeatHoldService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SeatController.class)
class SeatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SeatHoldService seatHoldService;

    private SeatHoldDTO testHold;
    private SeatHoldRequest holdRequest;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        testHold = SeatHoldDTO.builder()
                .holdId(1L)
                .seatId(1L)
                .seatNumber("1A")
                .userId("user123")
                .holdTime(now)
                .expirationTime(now.plusMinutes(10))
                .message("Seat held successfully")
                .build();

        holdRequest = SeatHoldRequest.builder()
                .userId("user123")
                .build();
    }

    @Test
    void holdSeat_ShouldReturnHoldDetails() throws Exception {
        when(seatHoldService.holdSeat(eq(1L), any(SeatHoldRequest.class))).thenReturn(testHold);

        mockMvc.perform(post("/api/seats/1/hold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(holdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.seatNumber").value("1A"));
    }

    @Test
    void holdSeat_WhenSeatNotFound_ShouldReturn404() throws Exception {
        when(seatHoldService.holdSeat(eq(999L), any(SeatHoldRequest.class)))
                .thenThrow(new ResourceNotFoundException("Seat not found"));

        mockMvc.perform(post("/api/seats/999/hold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(holdRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void holdSeat_WhenSeatNotAvailable_ShouldReturn409() throws Exception {
        when(seatHoldService.holdSeat(eq(1L), any(SeatHoldRequest.class)))
                .thenThrow(new SeatNotAvailableException("Seat is not available"));

        mockMvc.perform(post("/api/seats/1/hold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(holdRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void releaseHold_ShouldReturnNoContent() throws Exception {
        doNothing().when(seatHoldService).releaseHold(1L, "user123");

        mockMvc.perform(delete("/api/seats/1/hold")
                        .param("userId", "user123"))
                .andExpect(status().isNoContent());
    }

    @Test
    void releaseHold_WhenInvalidHold_ShouldReturn400() throws Exception {
        doThrow(new InvalidHoldException("No active hold found"))
                .when(seatHoldService).releaseHold(1L, "invalidUser");

        mockMvc.perform(delete("/api/seats/1/hold")
                        .param("userId", "invalidUser"))
                .andExpect(status().isBadRequest());
    }
}
