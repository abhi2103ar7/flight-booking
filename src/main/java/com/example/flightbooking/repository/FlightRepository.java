package com.example.flightbooking.repository;

import com.example.flightbooking.model.Flight;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class FlightRepository {

    private final ConcurrentHashMap<String, Flight> flights = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        LocalDateTime baseTime = LocalDateTime.of(
                LocalDate.now().plusDays(1), LocalTime.of(6, 0));

        save(Flight.builder()
                .flightNumber("FL001")
                .origin("New York (JFK)")
                .destination("Los Angeles (LAX)")
                .departureTime(baseTime)
                .arrivalTime(baseTime.plusHours(5).plusMinutes(30))
                .totalSeats(150)
                .build());

        save(Flight.builder()
                .flightNumber("FL002")
                .origin("Chicago (ORD)")
                .destination("Miami (MIA)")
                .departureTime(baseTime.plusHours(2))
                .arrivalTime(baseTime.plusHours(2).plusHours(3).plusMinutes(45))
                .totalSeats(180)
                .build());

        save(Flight.builder()
                .flightNumber("FL003")
                .origin("Seattle (SEA)")
                .destination("Boston (BOS)")
                .departureTime(baseTime.plusHours(4))
                .arrivalTime(baseTime.plusHours(4).plusHours(5).plusMinutes(15))
                .totalSeats(120)
                .build());

        save(Flight.builder()
                .flightNumber("FL004")
                .origin("Dallas (DFW)")
                .destination("San Francisco (SFO)")
                .departureTime(baseTime.plusHours(6))
                .arrivalTime(baseTime.plusHours(6).plusHours(4).plusMinutes(10))
                .totalSeats(200)
                .build());

        save(Flight.builder()
                .flightNumber("FL005")
                .origin("Atlanta (ATL)")
                .destination("Denver (DEN)")
                .departureTime(baseTime.plusHours(8))
                .arrivalTime(baseTime.plusHours(8).plusHours(3).plusMinutes(20))
                .totalSeats(160)
                .build());
    }

    public Flight save(Flight flight) {
        flights.put(flight.getFlightNumber(), flight);
        return flight;
    }

    public Optional<Flight> findByFlightNumber(String flightNumber) {
        return Optional.ofNullable(flights.get(flightNumber));
    }

    public List<Flight> findAll() {
        return new ArrayList<>(flights.values());
    }
}
