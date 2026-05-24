package com.example.flightbooking.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Builder.Default
    private String bookingId = UUID.randomUUID().toString();

    private String flightNumber;
    private String passengerName;
    private String passengerEmail;
    private int seatsBooked;

    @Builder.Default
    private LocalDateTime bookingTime = LocalDateTime.now();

    @Builder.Default
    private BookingStatus status = BookingStatus.CONFIRMED;

    public enum BookingStatus {
        CONFIRMED,
        CANCELLED
    }
}
