package com.airline.controller;

import com.airline.dto.SeatHoldDTO;
import com.airline.exception.InvalidHoldException;
import com.airline.exception.ResourceNotFoundException;
import com.airline.exception.SeatNotAvailableException;
import com.airline.service.SeatLockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

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
    private SeatLockService seatLockService;

    private SeatHoldDTO testHold;

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
    }

    @Test
    void holdSeat_ShouldReturnHoldDetails() throws Exception {
        when(seatLockService.holdSeat(1L, "user123")).thenReturn(testHold);

        mockMvc.perform(post("/api/seats/1/hold")
                        .param("userId", "user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holdId").value(1))
                .andExpect(jsonPath("$.seatNumber").value("1A"));
    }

    @Test
    void holdSeat_WhenSeatNotFound_ShouldReturn404() throws Exception {
        when(seatLockService.holdSeat(999L, "user123"))
                .thenThrow(new ResourceNotFoundException("Seat not found"));

        mockMvc.perform(post("/api/seats/999/hold")
                        .param("userId", "user123"))
                .andExpect(status().isNotFound());
    }

    @Test
    void holdSeat_WhenSeatNotAvailable_ShouldReturn409() throws Exception {
        when(seatLockService.holdSeat(1L, "user123"))
                .thenThrow(new SeatNotAvailableException("Seat is not available"));

        mockMvc.perform(post("/api/seats/1/hold")
                        .param("userId", "user123"))
                .andExpect(status().isConflict());
    }

    @Test
    void releaseHold_ShouldReturnNoContent() throws Exception {
        doNothing().when(seatLockService).releaseHold(1L);

        mockMvc.perform(delete("/api/seats/hold/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void releaseHold_WhenHoldNotFound_ShouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Hold not found"))
                .when(seatLockService).releaseHold(999L);

        mockMvc.perform(delete("/api/seats/hold/999"))
                .andExpect(status().isNotFound());
    }
}
