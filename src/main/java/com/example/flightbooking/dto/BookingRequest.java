package com.example.flightbooking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

    @NotBlank(message = "Flight number cannot be blank. Please provide a valid flight identifier (e.g., FL001).")
    @Pattern(regexp = "^FL\\d{3}$", message = "Flight number must start with 'FL' followed by 3 digits.")
    private String flightNumber;

    @NotBlank(message = "Passenger name is mandatory for booking.")
    @Size(min = 2, max = 50, message = "Passenger name must be between 2 and 50 characters.")
    private String passengerName;

    @NotBlank(message = "Passenger email is mandatory for sending booking confirmation.")
    @Email(message = "Please provide a valid email format (e.g., user@example.com).")
    private String passengerEmail;

    @Min(value = 1, message = "You must book at least 1 seat.")
    @Max(value = 10, message = "You cannot book more than 10 seats in a single request.")
    private int seatsBooked;
}
