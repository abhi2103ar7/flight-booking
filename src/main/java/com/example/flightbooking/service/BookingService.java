package com.example.flightbooking.service;

import com.example.flightbooking.dto.BookingRequest;
import com.example.flightbooking.dto.BookingResponse;
import com.example.flightbooking.exception.BookingNotFoundException;
import com.example.flightbooking.exception.FlightNotFoundException;
import com.example.flightbooking.exception.InsufficientSeatsException;
import com.example.flightbooking.model.Booking;
import com.example.flightbooking.model.Booking.BookingStatus;
import com.example.flightbooking.model.Flight;
import com.example.flightbooking.repository.BookingRepository;
import com.example.flightbooking.repository.FlightRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;

    public BookingService(BookingRepository bookingRepository, FlightRepository flightRepository) {
        this.bookingRepository = bookingRepository;
        this.flightRepository = flightRepository;
    }

    /**
     * Creates a new booking for the specified flight.
     *
     * Thread-safety approach (in lieu of @Transactional, since there is no database):
     * We synchronize on the Flight object itself to ensure that the check-and-reserve
     * operation is atomic. This prevents race conditions where two concurrent requests
     * could both pass the availability check and cause overbooking.
     *
     * The synchronized block ensures:
     * 1. Only one thread can check + update seats for a given flight at a time.
     * 2. AtomicInteger.addAndGet() is used for the actual update, providing an
     *    additional layer of thread-safety for the bookedSeats counter.
     *
     * @param request the booking request containing flight number, passenger info, and seat count
     * @return BookingResponse with booking confirmation details
     * @throws FlightNotFoundException    if the requested flight does not exist
     * @throws InsufficientSeatsException if not enough seats are available
     */
    public BookingResponse createBooking(BookingRequest request) {
        Flight flight = flightRepository.findByFlightNumber(request.getFlightNumber())
                .orElseThrow(() -> new FlightNotFoundException(
                        "Flight not found with number: " + request.getFlightNumber()));

        // Synchronized on the Flight object to atomically check-and-reserve seats.
        // This acts as a pessimistic lock for this specific flight, preventing
        // concurrent bookings from overbooking the same flight.
        synchronized (flight) {
            int available = flight.getAvailableSeats();
            if (available < request.getSeatsBooked()) {
                throw new InsufficientSeatsException(
                        "Not enough seats available on flight " + request.getFlightNumber()
                                + ". Requested: " + request.getSeatsBooked()
                                + ", Available: " + available);
            }

            // Reserve the seats atomically
            flight.getBookedSeats().addAndGet(request.getSeatsBooked());
        }

        // Build and persist the booking (outside the synchronized block since
        // booking creation is independent and thread-safe via ConcurrentHashMap)
        Booking booking = Booking.builder()
                .bookingId(UUID.randomUUID().toString())
                .flightNumber(request.getFlightNumber())
                .passengerName(request.getPassengerName())
                .passengerEmail(request.getPassengerEmail())
                .seatsBooked(request.getSeatsBooked())
                .bookingTime(LocalDateTime.now())
                .status(BookingStatus.CONFIRMED)
                .build();

        bookingRepository.save(booking);

        return mapToBookingResponse(booking);
    }

    /**
     * Cancels an existing booking and releases the reserved seats back to the flight.
     *
     * Thread-safety approach:
     * We synchronize on the Flight object to ensure that releasing seats is
     * coordinated with any concurrent booking attempts on the same flight.
     *
     * @param bookingId the UUID of the booking to cancel
     * @return BookingResponse with updated CANCELLED status
     * @throws BookingNotFoundException if no booking exists with the given ID
     * @throws IllegalStateException    if the booking is already cancelled
     */
    public BookingResponse cancelBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(
                        "Booking not found with ID: " + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Booking " + bookingId + " is already cancelled");
        }

        Flight flight = flightRepository.findByFlightNumber(booking.getFlightNumber())
                .orElseThrow(() -> new FlightNotFoundException(
                        "Flight not found with number: " + booking.getFlightNumber()));

        // Synchronized on the Flight object to safely release seats.
        // This ensures consistency when concurrent bookings and cancellations
        // happen on the same flight.
        synchronized (flight) {
            flight.getBookedSeats().addAndGet(-booking.getSeatsBooked());
        }

        // Update booking status
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        return mapToBookingResponse(booking);
    }

    /**
     * Maps a Booking entity to a BookingResponse DTO.
     */
    private BookingResponse mapToBookingResponse(Booking booking) {
        return BookingResponse.builder()
                .bookingId(booking.getBookingId())
                .flightNumber(booking.getFlightNumber())
                .passengerName(booking.getPassengerName())
                .passengerEmail(booking.getPassengerEmail())
                .seatsBooked(booking.getSeatsBooked())
                .bookingTime(booking.getBookingTime())
                .status(booking.getStatus())
                .build();
    }
}
