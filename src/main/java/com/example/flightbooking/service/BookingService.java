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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
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
     * Retrieves all bookings.
     */
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll()
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    /**
     * Per-flight ReentrantLock map for thread-safe seat reservation.
     *
     * Why ReentrantLock instead of synchronized(flight)?
     * -------------------------------------------------
     * Using synchronized(flight) is fragile because it depends on object identity.
     * If the Flight object in the ConcurrentHashMap is ever replaced (e.g., via a
     * save/update that creates a new instance), two threads could hold references
     * to different Flight objects and their synchronized blocks would NOT mutually
     * exclude each other — leading to silent overbooking.
     *
     * A ReentrantLock keyed by flightNumber is decoupled from the Flight object's
     * identity. The lock is stable as long as the flight number string is the same,
     * regardless of how many times the Flight object is replaced in the repository.
     *
     * computeIfAbsent guarantees that only one lock is created per flight number,
     * even under concurrent access.
     */
    private final ConcurrentHashMap<String, ReentrantLock> flightLocks = new ConcurrentHashMap<>();

    /**
     * Returns (or lazily creates) the ReentrantLock for the given flight number.
     * computeIfAbsent is atomic in ConcurrentHashMap, so only one lock instance
     * will ever be created per flight number even under concurrent calls.
     */
    private ReentrantLock getLockForFlight(String flightNumber) {
        return flightLocks.computeIfAbsent(flightNumber, k -> new ReentrantLock(true));
    }

    /**
     * Creates a new booking for the specified flight.
     *
     * Thread-safety approach (in lieu of @Transactional, since there is no database):
     * A per-flight ReentrantLock ensures that the check-and-reserve operation is
     * atomic. This prevents race conditions where two concurrent requests could
     * both pass the availability check and cause overbooking.
     *
     * The lock is acquired before reading available seats and released only after
     * the seats have been reserved. This guarantees:
     * 1. Only one thread can check + update seats for a given flight at a time.
     * 2. The lock is always released via try/finally, even if an exception is thrown.
     * 3. The fair ordering (ReentrantLock(true)) prevents thread starvation.
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

        // Acquire the per-flight lock to atomically check-and-reserve seats.
        // This acts as a pessimistic lock for this specific flight, preventing
        // concurrent bookings from overbooking the same flight.
        ReentrantLock lock = getLockForFlight(request.getFlightNumber());
        lock.lock();
        try {
            int available = flight.getAvailableSeats();
            if (available < request.getSeatsBooked()) {
                throw new InsufficientSeatsException(
                        "Not enough seats available on flight " + request.getFlightNumber()
                                + ". Requested: " + request.getSeatsBooked()
                                + ", Available: " + available);
            }

            // Reserve the seats atomically
            flight.getBookedSeats().addAndGet(request.getSeatsBooked());
        } finally {
            lock.unlock();
        }

        // Build and persist the booking (outside the lock since
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
     * The per-flight ReentrantLock ensures that releasing seats is coordinated
     * with any concurrent booking attempts on the same flight. The lock also
     * covers the status check to prevent double-cancellation under concurrency.
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

        Flight flight = flightRepository.findByFlightNumber(booking.getFlightNumber())
                .orElseThrow(() -> new FlightNotFoundException(
                        "Flight not found with number: " + booking.getFlightNumber()));

        // Acquire the per-flight lock to safely check status and release seats.
        // This ensures consistency when concurrent bookings and cancellations
        // happen on the same flight, and prevents double-cancellation races.
        ReentrantLock lock = getLockForFlight(booking.getFlightNumber());
        lock.lock();
        try {
            if (booking.getStatus() == BookingStatus.CANCELLED) {
                throw new IllegalStateException(
                        "Booking " + bookingId + " is already cancelled");
            }

            // Release the reserved seats
            flight.getBookedSeats().addAndGet(-booking.getSeatsBooked());

            // Update booking status within the lock to prevent double-cancellation
            booking.setStatus(BookingStatus.CANCELLED);
        } finally {
            lock.unlock();
        }

        bookingRepository.save(booking);

        return mapToBookingResponse(booking);
    }

    /**
     * Maps a Booking entity to a BookingResponse DTO.
     * Includes a defensive null check to prevent NullPointerException.
     */
    private BookingResponse mapToBookingResponse(Booking booking) {
        if (booking == null) {
            return null;
        }

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
