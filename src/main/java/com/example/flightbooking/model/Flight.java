package com.example.flightbooking.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Flight {

    private String flightNumber;
    private String origin;
    private String destination;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private int totalSeats;

    @Builder.Default
    private AtomicInteger bookedSeats = new AtomicInteger(0);

    /**
     * Returns the number of seats still available on this flight.
     */
    public int getAvailableSeats() {
        return totalSeats - bookedSeats.get();
    }
}
