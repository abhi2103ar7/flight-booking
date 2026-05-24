package com.example.flightbooking.controller;

import com.example.flightbooking.dto.FlightResponse;
import com.example.flightbooking.service.FlightService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/flights")
@Validated
public class FlightController {

    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    /**
     * GET /api/flights
     * Returns all available flights.
     */
    @GetMapping
    public ResponseEntity<List<FlightResponse>> getAllFlights() {
        return ResponseEntity.ok(flightService.getAllFlights());
    }

    /**
     * GET /api/flights/{flightNumber}
     * Returns a specific flight by its flight number.
     */
    @GetMapping("/{flightNumber}")
    public ResponseEntity<FlightResponse> getFlightByNumber(@PathVariable String flightNumber) {
        return ResponseEntity.ok(flightService.getFlightByNumber(flightNumber));
    }
}
