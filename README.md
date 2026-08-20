<div align="center">
  <h1>🎟️ CloudTicket</h1>
  <p><strong>A High-Concurrency Event Ticketing Platform</strong></p>
  
  ![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=java)
  ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3-brightgreen?style=flat-square&logo=spring)
  ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=flat-square&logo=postgresql)
  ![JWT](https://img.shields.io/badge/Security-JWT-black?style=flat-square)
</div>

## 📌 Overview

CloudTicket is a robust backend ticketing system designed from the ground up to solve the most critical challenges in the e-commerce and ticketing domains: **High Concurrency**, **Race Conditions (Overselling)**, and **Idempotency**. 

Built with a **Modular Monolith** architecture, the project provides a highly cohesive yet loosely coupled codebase that can easily be migrated to Microservices when the scale demands it.

## ✨ Core Features & Technical Highlights

*   **Concurrency Control (Overselling Prevention):** Implemented **Optimistic Locking** (`@Version`) to guarantee that even if 10,000 users attempt to purchase the last available ticket simultaneously, only exactly 1 user succeeds.
*   **Self-Healing Resilience:** Integrated an **Exponential Backoff Retry** mechanism (`@Retryable`). When database lock conflicts occur, the system automatically delays and retries the transaction in the background, providing a seamless experience without crashing user requests.
*   **Idempotent Transactions:** Engineered custom **Idempotent APIs** using `Idempotency-Key` headers. This completely eliminates the risk of duplicate charges or duplicate reservations if a user double-clicks the payment button or experiences network lag.
*   **Automated Background Processing:** Utilized `@Scheduled` tasks to run periodic background sweeps that auto-invalidate unpaid reservations after 15 minutes and immediately release the inventory back to the pool.
*   **Stateless Authentication:** Secured end-to-end with **JSON Web Tokens (JWT)** and Spring Security filter chains.

## 🛠️ Technology Stack

*   **Language:** Java 21
*   **Framework:** Spring Boot 3.3.3
*   **Database:** PostgreSQL 16
*   **ORM:** Spring Data JPA, Hibernate
*   **Tools:** Lombok, Spring Retry, Springdoc (Swagger)
*   **Testing:** JUnit 5, Mockito

## 🚀 Getting Started

### Prerequisites
*   JDK 21 or higher
*   Maven 3.8+
*   PostgreSQL running on `localhost:5432`

### Database Setup
Create a PostgreSQL database named `cloudticket_db`:
```sql
CREATE DATABASE cloudticket_db;
```
*(Configure your username and password in `src/main/resources/application.yml`)*

### Run the Application
1. Clone the repository:
   ```bash
   git clone https://github.com/cvkhang/CloudTicket.git
   cd CloudTicket
   ```
2. Build and run the project:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
3. The server will start on `http://localhost:8080`.

## 📚 API Documentation (Swagger)

Once the application is running, you can interactively explore and test all RESTful APIs via the integrated Swagger UI:

👉 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

### Authentication Flow in Swagger
1. Register a new user via `POST /api/v1/auth/register`
2. Login via `POST /api/v1/auth/login` to receive an `accessToken`.
3. Click the **Authorize** button at the top of the Swagger UI and paste your token to unlock secured endpoints (Booking & Payment).

## 🧪 Testing

The project contains comprehensive unit tests focusing heavily on concurrency boundaries and payment services using Mockito to mock database interactions.
```bash
mvn test
```

## 🤝 Architecture (Modular Monolith)

The codebase is strictly separated into independent Bounded Contexts:
*   `auth`: User registration, JWT processing.
*   `event`: Event creation, Ticket Type inventory.
*   `booking`: Core reservation engine, Optimistic Locking.
*   `payment`: Idempotent payment processing.

*(Entities from different modules do not share foreign keys at the ORM level, communicating only via IDs to ensure strict microservice-ready boundaries).*
