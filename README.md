# CloudTicket

CloudTicket is a backend API for an event ticketing system, built with Spring Boot and PostgreSQL.

I built this project to learn and demonstrate how to handle high-concurrency scenarios, specifically the "overselling" problem that happens when thousands of users try to buy the last ticket at the exact same time.

## Features

- **JWT Authentication:** Simple stateless login and registration.
- **Concurrency Handling:** Uses Optimistic Locking (`@Version` in JPA) to prevent race conditions during ticket booking.
- **Retry Mechanism:** Automatically retries failed transactions (due to lock conflicts) using `Spring Retry` with exponential backoff so the user doesn't just get a 500 Error.
- **Idempotency:** Payment endpoints use an `Idempotency-Key` header to ensure a user is never charged twice, even if they double-click or the network drops.
- **Background Jobs:** A `@Scheduled` task that runs every minute to clean up unpaid reservations and return tickets to the pool.

## Tech Stack

- Java 21
- Spring Boot 3
- PostgreSQL
- Spring Data JPA, Spring Security
- JUnit 5 & Mockito (Unit testing)
- Springdoc / Swagger (API docs)

## How to run locally

1. **Database setup:**
   Make sure you have PostgreSQL running. Create a database called `cloudticket_db`.
   Update the username and password in `src/main/resources/application.yml` if needed.

2. **Run the app:**
   ```bash
   ./mvnw clean install
   ./mvnw spring-boot:run
   ```

3. **View API Docs:**
   Once the app is running, open your browser and go to:
   `http://localhost:8080/swagger-ui.html`

## Architecture Note

The project is structured as a **Modular Monolith**. Instead of jumping straight to microservices, I separated the code into strict domain packages (`auth`, `event`, `booking`, `payment`) that don't share database relationships (no foreign keys between them). This keeps the app easy to deploy while making it simple to split into microservices later if the scale requires it.
