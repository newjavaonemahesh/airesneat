# Airline Seat Reservation System PRD

## Original Problem Statement
Create a Spring Boot project for an Airline Seat Reservation System with:
- Java 17, Spring Boot, Maven
- Spring Web, Spring Data JPA, H2 in-memory database
- Lombok, JUnit + Mockito, JaCoCo test coverage (70% target)
- Package structure: controller, service, repository, model, dto, config, exception
- Entities: Flight, Seat, Passenger, Booking, SeatHold, FareClass
- Seat status enum: AVAILABLE, HELD, BOOKED

## User Choices
- Backend API only (no frontend)
- Swagger/OpenAPI included
- 10-minute seat hold with automatic expiration
- Simple fare calculation from FareClass
- 70% JaCoCo coverage target

## Core Workflow
1. Search flights
2. View seats for a flight
3. Hold a seat (10-min expiration)
4. Confirm booking
5. Cancel booking

## What's Been Implemented (March 2026)

### Entities (6)
- Flight, Seat, Passenger, Booking, SeatHold, FareClass
- Proper JPA relationships with lazy loading

### API Endpoints (9)
- GET /api/flights - List all flights
- GET /api/flights/search - Search flights
- GET /api/flights/{id} - Get flight details
- GET /api/flights/{flightId}/seats - Get all seats
- GET /api/flights/{flightId}/seats/available - Get available seats
- POST /api/seats/{seatId}/hold - Hold a seat
- DELETE /api/seats/hold/{holdToken} - Release hold
- POST /api/bookings - Confirm booking
- GET /api/bookings/{ref} - Get booking
- DELETE /api/bookings/{ref} - Cancel booking

### Features
- Seat state transitions: AVAILABLE → HELD → BOOKED
- 10-minute hold expiration with scheduled job
- Fare calculation from FareClass
- Sample data initialization
- Swagger UI at /swagger-ui.html
- H2 console at /h2-console

### Testing
- 45 unit tests passing
- 82% JaCoCo code coverage (exceeds 70% target)
- Controller tests with MockMvc
- Service tests with Mockito
- Repository integration tests

## Technical Stack
- Java 17
- Spring Boot 3.2.0
- H2 in-memory database
- Lombok
- SpringDoc OpenAPI 2.3.0
- JaCoCo 0.8.11

## Next Action Items (P0)
None - MVP complete

## Potential Enhancements (P1)
- Add integration tests with TestContainers
- Add seat selection by row/class filter
- Add flight capacity management
