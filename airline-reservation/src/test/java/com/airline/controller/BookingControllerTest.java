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
    private BookingRequest bookingRequest;

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

        bookingRequest = BookingRequest.builder()
                .holdId(1L)
                .passengerId(1L)
                .build();
    }

    @Test
    void createBooking_ShouldReturnCreatedBooking() throws Exception {
        when(bookingService.createBooking(1L, 1L)).thenReturn(testBooking);

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.passengerName").value("John Doe"))
                .andExpect(jsonPath("$.seatNumber").value("1A"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void createBooking_WhenHoldInvalid_ShouldReturn400() throws Exception {
        when(bookingService.createBooking(1L, 1L))
                .thenThrow(new InvalidHoldException("Hold is not active"));

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_WhenPassengerNotFound_ShouldReturn404() throws Exception {
        bookingRequest.setPassengerId(999L);
        when(bookingService.createBooking(1L, 999L))
                .thenThrow(new ResourceNotFoundException("Passenger not found"));

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBooking_WhenInvalidRequest_ShouldReturn400() throws Exception {
        BookingRequest invalidRequest = BookingRequest.builder()
                .holdId(null)       // Missing hold ID
                .passengerId(null)  // Missing passenger ID
                .build();

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancelBooking_ShouldReturnCancelledBooking() throws Exception {
        BookingDTO cancelledBooking = BookingDTO.builder()
                .id(1L)
                .seatNumber("1A")
                .flightNumber("AA100")
                .passengerName("John Doe")
                .status(BookingStatus.CANCELLED)
                .build();
        when(bookingService.cancelBooking(1L)).thenReturn(cancelledBooking);

        mockMvc.perform(delete("/bookings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cancelBooking_WhenNotFound_ShouldReturn404() throws Exception {
        when(bookingService.cancelBooking(999L))
                .thenThrow(new ResourceNotFoundException("Booking not found"));

        mockMvc.perform(delete("/bookings/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelBooking_WhenAlreadyCancelled_ShouldReturn400() throws Exception {
        when(bookingService.cancelBooking(1L))
                .thenThrow(new InvalidHoldException("Booking is already cancelled"));

        mockMvc.perform(delete("/bookings/1"))
                .andExpect(status().isBadRequest());
    }
}
