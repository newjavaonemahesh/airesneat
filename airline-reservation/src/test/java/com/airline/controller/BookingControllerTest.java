package com.airline.controller;

import com.airline.dto.BookingDTO;
import com.airline.dto.BookingRequest;
import com.airline.exception.InvalidHoldException;
import com.airline.exception.ResourceNotFoundException;
import com.airline.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
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
                .bookingReference("BK12345678")
                .seatNumber("1A")
                .flightNumber("AA100")
                .passengerName("John Doe")
                .passengerEmail("john@example.com")
                .totalFare(new BigDecimal("150.00"))
                .bookedAt(LocalDateTime.now())
                .cancelled(false)
                .build();

        validRequest = BookingRequest.builder()
                .holdToken("test-token-123")
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phoneNumber("1234567890")
                .build();
    }

    @Test
    void confirmBooking_ShouldReturnCreatedBooking() throws Exception {
        when(bookingService.confirmBooking(any(BookingRequest.class))).thenReturn(testBooking);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingReference").value("BK12345678"))
                .andExpect(jsonPath("$.passengerName").value("John Doe"));
    }

    @Test
    void confirmBooking_WithInvalidHold_ShouldReturn400() throws Exception {
        when(bookingService.confirmBooking(any(BookingRequest.class)))
                .thenThrow(new InvalidHoldException("Invalid hold token"));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void confirmBooking_WithInvalidRequest_ShouldReturn400() throws Exception {
        BookingRequest invalidRequest = BookingRequest.builder()
                .holdToken("")  // Empty token
                .firstName("John")
                .lastName("Doe")
                .email("invalid-email")  // Invalid email
                .build();

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBooking_ShouldReturnBookingDetails() throws Exception {
        when(bookingService.getBookingByReference("BK12345678")).thenReturn(testBooking);

        mockMvc.perform(get("/api/bookings/BK12345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingReference").value("BK12345678"));
    }

    @Test
    void getBooking_WhenNotFound_ShouldReturn404() throws Exception {
        when(bookingService.getBookingByReference("INVALID"))
                .thenThrow(new ResourceNotFoundException("Booking not found"));

        mockMvc.perform(get("/api/bookings/INVALID"))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelBooking_ShouldReturnCancelledBooking() throws Exception {
        BookingDTO cancelledBooking = BookingDTO.builder()
                .id(1L)
                .bookingReference("BK12345678")
                .cancelled(true)
                .build();
        when(bookingService.cancelBooking("BK12345678")).thenReturn(cancelledBooking);

        mockMvc.perform(delete("/api/bookings/BK12345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cancelled").value(true));
    }
}
