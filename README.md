# Flight Ticket Booking REST API

## 1. Project Overview
A robust, thread-safe RESTful API for a flight ticket booking system. This application allows users to view available flights, make bookings, and cancel them. It was built with a strong emphasis on concurrency and thread-safety to prevent overbooking scenarios in a high-traffic environment.

## 2. Tech Stack
- **Java 17**
- **Spring Boot 3** (Web, Validation)
- **Maven**
- **In-memory Storage** (`ConcurrentHashMap` for thread-safe data access without a database)
- **Lombok** (Boilerplate reduction)

## 3. How to Run

### Prerequisites
- Java 17 installed
- Maven installed

### Run the Application
1. Clone the repository and navigate to the project root:
   ```bash
   cd flight-booking
   ```
2. Run the application using Maven:
   ```bash
   mvn spring-boot:run
   ```
3. The server will start on port 8080. You can access the API at `http://localhost:8080`

## 4. API Endpoints

| HTTP Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/flights` | Retrieve a list of all available flights |
| `GET` | `/api/flights/{flightNumber}` | Retrieve details for a specific flight |
| `GET` | `/api/bookings` | Retrieve a list of all current bookings |
| `POST` | `/api/bookings` | Create a new flight booking |
| `DELETE` | `/api/bookings/{bookingId}` | Cancel an existing booking |

## 5. Example Requests

### List All Flights
```bash
curl --location 'http://localhost:8080/api/flights'
```

### Get Specific Flight (e.g., FL001)
```bash
curl --location 'http://localhost:8080/api/flights/FL001'
```

### Create a Booking
```bash
curl --location 'http://localhost:8080/api/bookings' \
--header 'Content-Type: application/json' \
--data '{
    "flightNumber": "FL001",
    "passengerName": "John Doe",
    "passengerEmail": "john.doe@example.com",
    "seatsBooked": 2
}'
```

### Cancel a Booking
Replace the UUID with the actual `bookingId` received during creation.
```bash
curl --location --request DELETE 'http://localhost:8080/api/bookings/123e4567-e89b-12d3-a456-426614174000'
```

### Error Case: Booking on a Full Flight (409 Conflict)
If a user attempts to book more seats than available, the API responds with a structured error:
```bash
curl --location 'http://localhost:8080/api/bookings' \
--header 'Content-Type: application/json' \
--data '{
    "flightNumber": "FL001",
    "passengerName": "Jane Doe",
    "passengerEmail": "jane@example.com",
    "seatsBooked": 200
}'
```
**Response (409 Conflict):**
```json
{
    "timestamp": "2026-05-24T10:00:00.000",
    "status": 409,
    "error": "Conflict",
    "message": "Not enough seats available on flight FL001. Requested: 200, Available: 150",
    "path": "/api/bookings"
}
```

## 6. Pre-loaded Sample Flights

The application starts with the following 5 sample flights pre-loaded in memory (all departing on tomorrow's date):

| Flight Number | Origin | Destination | Total Seats |
|---|---|---|---|
| **FL001** | New York (JFK) | Los Angeles (LAX) | 150 |
| **FL002** | Chicago (ORD) | Miami (MIA) | 180 |
| **FL003** | Seattle (SEA) | Boston (BOS) | 120 |
| **FL004** | Dallas (DFW) | San Francisco (SFO) | 200 |
| **FL005** | Atlanta (ATL) | Denver (DEN) | 160 |


## 7. What I Would Improve With More Time

If I had more time to expand this project for production readiness, I would implement:

- **Persistent Storage**: Replace the in-memory maps with PostgreSQL and Spring Data JPA.
- **Comprehensive Testing**: Add extensive unit and integration tests using JUnit 5, Mockito, and Testcontainers.
- **Pagination**: Implement pagination for the flight and booking listing endpoints to handle large datasets.
- **Search Capabilities**: Add endpoints to search flights by route, date, and availability.
- **Structured Logging (MDC)**: Implement centralized, structured JSON logging with Mapped Diagnostic Context (MDC) trace IDs for request tracking.
- **Docker/Containerization Support**: Provide a `Dockerfile` and `docker-compose.yml` for seamless deployment.
- **OpenAPI/Swagger Documentation**: Auto-generate interactive API documentation using `springdoc-openapi`.
- **Idempotency Keys**: Add support for `Idempotency-Key` headers on booking creation to prevent duplicate bookings during network retries.

