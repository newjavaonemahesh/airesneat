package com.airline.controller;

import com.airline.dto.SeatHoldDTO;
import com.airline.dto.SeatHoldRequest;
import com.airline.exception.InvalidHoldException;
import com.airline.exception.ResourceNotFoundException;
import com.airline.exception.SeatNotAvailableException;
import com.airline.service.SeatLockService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
    private SeatLockService seatLockService;

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
                .seatId(1L)
                .userId("user123")
                .build();
    }

    @Test
    void holdSeat_ShouldReturnCreatedHold() throws Exception {
        when(seatLockService.holdSeat(1L, "user123")).thenReturn(testHold);

        mockMvc.perform(post("/seats/hold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(holdRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.holdId").value(1))
                .andExpect(jsonPath("$.seatNumber").value("1A"))
                .andExpect(jsonPath("$.userId").value("user123"));
    }

    @Test
    void holdSeat_WhenSeatNotFound_ShouldReturn404() throws Exception {
        holdRequest.setSeatId(999L);
        when(seatLockService.holdSeat(999L, "user123"))
                .thenThrow(new ResourceNotFoundException("Seat not found"));

        mockMvc.perform(post("/seats/hold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(holdRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void holdSeat_WhenSeatNotAvailable_ShouldReturn409() throws Exception {
        when(seatLockService.holdSeat(anyLong(), anyString()))
                .thenThrow(new SeatNotAvailableException("Seat is not available"));

        mockMvc.perform(post("/seats/hold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(holdRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void holdSeat_WhenInvalidRequest_ShouldReturn400() throws Exception {
        SeatHoldRequest invalidRequest = SeatHoldRequest.builder()
                .seatId(null)  // Missing seat ID
                .userId("")    // Empty user ID
                .build();

        mockMvc.perform(post("/seats/hold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void releaseHold_ShouldReturnNoContent() throws Exception {
        doNothing().when(seatLockService).releaseHold(1L);

        mockMvc.perform(delete("/seats/hold/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void releaseHold_WhenHoldNotFound_ShouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Hold not found"))
                .when(seatLockService).releaseHold(999L);

        mockMvc.perform(delete("/seats/hold/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void releaseHold_WhenHoldNotActive_ShouldReturn400() throws Exception {
        doThrow(new InvalidHoldException("Hold is not active"))
                .when(seatLockService).releaseHold(1L);

        mockMvc.perform(delete("/seats/hold/1"))
                .andExpect(status().isBadRequest());
    }
}
