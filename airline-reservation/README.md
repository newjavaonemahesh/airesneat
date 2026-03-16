# Airline Seat Reservation System

A Spring Boot REST API for airline seat reservations with seat holding, booking, and automatic hold expiration.

## Architecture Overview

### Technology Stack
- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **H2 In-Memory Database**
- **Lombok**
- **SpringDoc OpenAPI (Swagger)**
- **JUnit 5 + Mockito**
- **JaCoCo** (Test Coverage)

### Project Structure
```
com.airline
├── controller/          # REST controllers
│   ├── FlightController
│   ├── SeatController
│   └── BookingController
├── service/             # Business logic
│   ├── FlightService
│   ├── SeatLockService
│   └── BookingService
├── repository/          # Data access layer
│   ├── FlightRepository
│   ├── SeatRepository
│   ├── SeatHoldRepository
│   ├── PassengerRepository
│   └── BookingRepository
├── model/               # JPA entities
│   ├── Flight
│   ├── Seat
│   ├── SeatHold
│   ├── Passenger
│   └── Booking
├── dto/                 # Data transfer objects
├── exception/           # Custom exceptions
└── config/              # Configuration classes
```

### Entity Relationships
```
Flight (1) ──────< (N) Seat
                        │
                        ├──< SeatHold (N)
                        │
                        └──(1) Booking (1)──(1) Passenger
```

### Seat State Machine
```
    ┌─────────────────────────────────────┐
    │                                     │
    ▼                                     │
AVAILABLE ──(hold)──> HELD ──(book)──> BOOKED
    ▲                   │                 │
    │                   │                 │
    └───(expire/release)┘                 │
    │                                     │
    └────────────(cancel)─────────────────┘
```

## API Endpoints

### Flight Controller (`/flights`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/flights` | Get all flights |
| GET | `/flights/{flightId}/seats` | Get all seats for a flight |

### Seat Controller (`/seats`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/seats/hold` | Hold a seat (10-min expiration) |
| DELETE | `/seats/hold/{holdId}` | Release a seat hold |

### Booking Controller (`/bookings`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/bookings` | Create booking from hold |
| DELETE | `/bookings/{bookingId}` | Cancel a booking |

## How to Run the Application

### Prerequisites
- Java 17+
- Maven 3.8+

### Run with Maven
```bash
cd airline-reservation
mvn spring-boot:run
```

### Run as JAR
```bash
mvn clean package -DskipTests
java -jar target/airline-reservation-1.0.0.jar
```

### Access Points
- **API Base URL:** http://localhost:8082
- **Swagger UI:** http://localhost:8082/swagger-ui.html
- **OpenAPI Docs:** http://localhost:8082/api-docs
- **H2 Console:** http://localhost:8082/h2-console
  - JDBC URL: `jdbc:h2:mem:airlinedb`
  - Username: `sa`
  - Password: (empty)

## How to Run Tests

### Run All Tests
```bash
mvn test
```

### Run with Coverage Report
```bash
mvn clean test
# Report available at: target/site/jacoco/index.html
```

### Run Specific Test Class
```bash
mvn test -Dtest=SeatLockServiceTest
mvn test -Dtest=BookingServiceTest
mvn test -Dtest=FlightServiceTest
```

### Current Coverage
- **Line Coverage:** 91%
- **Instruction Coverage:** 79%

## Example curl Commands

### 1. Get All Flights
```bash
curl -X GET http://localhost:8082/flights
```

**Response:**
```json
[
  {
    "id": 1,
    "flightNumber": "AA100",
    "departureAirport": "JFK",
    "arrivalAirport": "LAX",
    "departureTime": "2024-01-15T08:00:00",
    "arrivalTime": "2024-01-15T11:30:00",
    "totalSeats": 30,
    "availableSeats": 30
  }
]
```

### 2. Get Seats for a Flight
```bash
curl -X GET http://localhost:8082/flights/1/seats
```

**Response:**
```json
[
  {
    "id": 1,
    "seatNumber": "1A",
    "rowNumber": 1,
    "fareClass": "BUSINESS",
    "status": "AVAILABLE"
  },
  {
    "id": 2,
    "seatNumber": "1B",
    "rowNumber": 1,
    "fareClass": "BUSINESS",
    "status": "AVAILABLE"
  }
]
```

### 3. Hold a Seat
```bash
curl -X POST http://localhost:8082/seats/hold \
  -H "Content-Type: application/json" \
  -d '{"seatId": 1, "userId": "user123"}'
```

**Response:**
```json
{
  "holdId": 1,
  "seatId": 1,
  "seatNumber": "1A",
  "userId": "user123",
  "holdTime": "2024-01-15T10:00:00",
  "expirationTime": "2024-01-15T10:10:00",
  "message": "Seat held successfully. Complete booking within 10 minutes."
}
```

### 4. Create a Booking
```bash
curl -X POST http://localhost:8082/bookings \
  -H "Content-Type: application/json" \
  -d '{"holdId": 1, "passengerId": 1}'
```

**Response:**
```json
{
  "id": 1,
  "passengerName": "John Doe",
  "passengerEmail": "john@example.com",
  "seatNumber": "1A",
  "flightNumber": "AA100",
  "bookingTime": "2024-01-15T10:05:00",
  "status": "CONFIRMED"
}
```

### 5. Cancel a Booking
```bash
curl -X DELETE http://localhost:8082/bookings/1
```

**Response:**
```json
{
  "id": 1,
  "passengerName": "John Doe",
  "passengerEmail": "john@example.com",
  "seatNumber": "1A",
  "flightNumber": "AA100",
  "bookingTime": "2024-01-15T10:05:00",
  "status": "CANCELLED"
}
```

### 6. Release a Hold
```bash
curl -X DELETE http://localhost:8082/seats/hold/1
```

**Response:** `204 No Content`

### Complete Booking Flow
```bash
# 1. View available flights
curl -s http://localhost:8082/flights | jq

# 2. View seats for flight 1
curl -s http://localhost:8082/flights/1/seats | jq

# 3. Hold seat 1
HOLD_RESPONSE=$(curl -s -X POST http://localhost:8082/seats/hold \
  -H "Content-Type: application/json" \
  -d '{"seatId": 1, "userId": "user123"}')
HOLD_ID=$(echo $HOLD_RESPONSE | jq -r '.holdId')
echo "Hold ID: $HOLD_ID"

# 4. Create booking with hold
curl -s -X POST http://localhost:8082/bookings \
  -H "Content-Type: application/json" \
  -d "{\"holdId\": $HOLD_ID, \"passengerId\": 1}" | jq
```

## Configuration

### application.properties
```properties
# Server
server.port=8082

# H2 Database
spring.datasource.url=jdbc:h2:mem:airlinedb
spring.h2.console.enabled=true

# Seat Hold Duration
airline.seat-hold.duration-minutes=10
```

## Sample Data

The application initializes with:
- **3 Flights:** AA100 (JFK→LAX), UA200 (LAX→ORD), DL300 (ORD→MIA)
- **30 Seats per flight:** 10 Business (rows 1-2), 20 Economy (rows 3-6)
- **2 Passengers:** John Doe, Jane Smith

## Error Handling

| HTTP Status | Exception | Description |
|-------------|-----------|-------------|
| 404 | ResourceNotFoundException | Resource not found |
| 409 | SeatNotAvailableException | Seat already held/booked |
| 400 | InvalidHoldException | Invalid or expired hold |

## License

MIT License
