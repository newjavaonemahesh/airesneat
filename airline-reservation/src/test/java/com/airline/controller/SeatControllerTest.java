package com.airline.controller;

import com.airline.dto.SeatHoldDTO;
import com.airline.exception.InvalidHoldException;
import com.airline.exception.ResourceNotFoundException;
import com.airline.exception.SeatNotAvailableException;
import com.airline.service.SeatHoldService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SeatController.class)
class SeatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SeatHoldService seatHoldService;

    private SeatHoldDTO testHold;

    @BeforeEach
    void setUp() {
        testHold = SeatHoldDTO.builder()
                .holdId(1L)
                .seatId(1L)
                .seatNumber("1A")
                .holdToken("test-token-123")
                .expiresAt("2024-01-15T10:10:00")
                .message("Seat held successfully")
                .build();
    }

    @Test
    void holdSeat_ShouldReturnHoldDetails() throws Exception {
        when(seatHoldService.holdSeat(1L)).thenReturn(testHold);

        mockMvc.perform(post("/api/seats/1/hold"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holdToken").value("test-token-123"))
                .andExpect(jsonPath("$.seatNumber").value("1A"));
    }

    @Test
    void holdSeat_WhenSeatNotFound_ShouldReturn404() throws Exception {
        when(seatHoldService.holdSeat(999L))
                .thenThrow(new ResourceNotFoundException("Seat not found"));

        mockMvc.perform(post("/api/seats/999/hold"))
                .andExpect(status().isNotFound());
    }

    @Test
    void holdSeat_WhenSeatNotAvailable_ShouldReturn409() throws Exception {
        when(seatHoldService.holdSeat(1L))
                .thenThrow(new SeatNotAvailableException("Seat is not available"));

        mockMvc.perform(post("/api/seats/1/hold"))
                .andExpect(status().isConflict());
    }

    @Test
    void releaseHold_ShouldReturnNoContent() throws Exception {
        doNothing().when(seatHoldService).releaseHold("test-token-123");

        mockMvc.perform(delete("/api/seats/hold/test-token-123"))
                .andExpect(status().isNoContent());
    }

    @Test
    void releaseHold_WhenInvalidToken_ShouldReturn400() throws Exception {
        doThrow(new InvalidHoldException("Invalid hold token"))
                .when(seatHoldService).releaseHold("invalid-token");

        mockMvc.perform(delete("/api/seats/hold/invalid-token"))
                .andExpect(status().isBadRequest());
    }
}
