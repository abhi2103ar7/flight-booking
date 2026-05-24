package com.example.flightbooking.dto;

import com.example.flightbooking.model.Booking.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private String bookingId;
    private String flightNumber;
    private String passengerName;
    private String passengerEmail;
    private int seatsBooked;
    private LocalDateTime bookingTime;
    private BookingStatus status;
}
