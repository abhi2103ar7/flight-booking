package com.example.flightbooking.service;

import com.example.flightbooking.dto.FlightResponse;
import com.example.flightbooking.exception.FlightNotFoundException;
import com.example.flightbooking.model.Flight;
import com.example.flightbooking.repository.FlightRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FlightService {

    private final FlightRepository flightRepository;

    public FlightService(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    /**
     * Retrieves all available flights and maps them to FlightResponse DTOs.
     */
    public List<FlightResponse> getAllFlights() {
        return flightRepository.findAll()
                .stream()
                .map(this::mapToFlightResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single flight by its flight number.
     *
     * @throws FlightNotFoundException if no flight exists with the given number
     */
    public FlightResponse getFlightByNumber(String flightNumber) {
        Flight flight = flightRepository.findByFlightNumber(flightNumber)
                .orElseThrow(() -> new FlightNotFoundException(
                        "Flight not found with number: " + flightNumber));
        return mapToFlightResponse(flight);
    }

    /**
     * Maps a Flight entity to a FlightResponse DTO, including the computed availableSeats.
     * Includes a defensive null check to prevent NullPointerException.
     */
    private FlightResponse mapToFlightResponse(Flight flight) {
        if (flight == null) {
            return null;
        }
        
        return FlightResponse.builder()
                .flightNumber(flight.getFlightNumber())
                .origin(flight.getOrigin())
                .destination(flight.getDestination())
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .totalSeats(flight.getTotalSeats())
                .availableSeats(flight.getAvailableSeats())
                .build();
    }
}
