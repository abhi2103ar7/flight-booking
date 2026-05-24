package com.example.flightbooking.controller;

import com.example.flightbooking.dto.BookingRequest;
import com.example.flightbooking.dto.BookingResponse;
import com.example.flightbooking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@Validated
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * GET /api/bookings
     * Retrieves all bookings.
     */
    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    /**
     * POST /api/bookings
     * Creates a new booking for a flight.
     * Returns 201 Created with the booking confirmation.
     */
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        BookingResponse response = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * DELETE /api/bookings/{bookingId}
     * Cancels an existing booking and releases the reserved seats.
     * Returns 200 OK with the updated booking (status = CANCELLED).
     */
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable String bookingId) {
        BookingResponse response = bookingService.cancelBooking(bookingId);
        return ResponseEntity.ok(response);
    }
}
