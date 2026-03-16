package com.airline.controller;

import com.airline.dto.BookingDTO;
import com.airline.dto.BookingRequest;
import com.airline.exception.InvalidHoldException;
import com.airline.exception.ResourceNotFoundException;
import com.airline.model.BookingStatus;
import com.airline.service.BookingService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingDTO testBooking;
    private BookingRequest validRequest;

    @BeforeEach
    void setUp() {
        testBooking = BookingDTO.builder()
                .id(1L)
                .seatNumber("1A")
                .flightNumber("AA100")
                .passengerName("John Doe")
                .passengerEmail("john@example.com")
                .bookingTime(LocalDateTime.now())
                .status(BookingStatus.CONFIRMED)
                .build();

        validRequest = BookingRequest.builder()
                .userId("user123")
                .passengerName("John Doe")
                .email("john@example.com")
                .build();
    }

    @Test
    void confirmBooking_ShouldReturnCreatedBooking() throws Exception {
        when(bookingService.confirmBooking(eq(1L), any(BookingRequest.class))).thenReturn(testBooking);

        mockMvc.perform(post("/api/bookings/seats/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.passengerName").value("John Doe"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void confirmBooking_WithInvalidHold_ShouldReturn400() throws Exception {
        when(bookingService.confirmBooking(eq(1L), any(BookingRequest.class)))
                .thenThrow(new InvalidHoldException("No active hold found"));

        mockMvc.perform(post("/api/bookings/seats/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void confirmBooking_WithInvalidRequest_ShouldReturn400() throws Exception {
        BookingRequest invalidRequest = BookingRequest.builder()
                .userId("")  // Empty userId
                .passengerName("John")
                .email("invalid-email")  // Invalid email
                .build();

        mockMvc.perform(post("/api/bookings/seats/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBooking_ShouldReturnBookingDetails() throws Exception {
        when(bookingService.getBookingById(1L)).thenReturn(testBooking);

        mockMvc.perform(get("/api/bookings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.passengerName").value("John Doe"));
    }

    @Test
    void getBooking_WhenNotFound_ShouldReturn404() throws Exception {
        when(bookingService.getBookingById(999L))
                .thenThrow(new ResourceNotFoundException("Booking not found"));

        mockMvc.perform(get("/api/bookings/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelBooking_ShouldReturnCancelledBooking() throws Exception {
        BookingDTO cancelledBooking = BookingDTO.builder()
                .id(1L)
                .status(BookingStatus.CANCELLED)
                .build();
        when(bookingService.cancelBooking(1L)).thenReturn(cancelledBooking);

        mockMvc.perform(delete("/api/bookings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
