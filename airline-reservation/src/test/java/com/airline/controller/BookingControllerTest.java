package com.airline.controller;

import com.airline.dto.BookingDTO;
import com.airline.exception.InvalidHoldException;
import com.airline.exception.ResourceNotFoundException;
import com.airline.model.BookingStatus;
import com.airline.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

    @MockBean
    private BookingService bookingService;

    private BookingDTO testBooking;

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
    }

    @Test
    void createBooking_ShouldReturnCreatedBooking() throws Exception {
        when(bookingService.createBooking(1L, 1L)).thenReturn(testBooking);

        mockMvc.perform(post("/api/bookings")
                        .param("holdId", "1")
                        .param("passengerId", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.passengerName").value("John Doe"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void createBooking_WhenHoldInvalid_ShouldReturn400() throws Exception {
        when(bookingService.createBooking(1L, 1L))
                .thenThrow(new InvalidHoldException("Hold is not active"));

        mockMvc.perform(post("/api/bookings")
                        .param("holdId", "1")
                        .param("passengerId", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_WhenPassengerNotFound_ShouldReturn404() throws Exception {
        when(bookingService.createBooking(1L, 999L))
                .thenThrow(new ResourceNotFoundException("Passenger not found"));

        mockMvc.perform(post("/api/bookings")
                        .param("holdId", "1")
                        .param("passengerId", "999"))
                .andExpect(status().isNotFound());
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
