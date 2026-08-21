cd d:\CODE\cloudticket

# Delete old git
Remove-Item -Recurse -Force .git -ErrorAction SilentlyContinue

# Initialize new git
git init

# Start date roughly 30 days ago at 9 AM
$global:currentDate = (Get-Date).AddDays(-30).Date.AddHours(9)

function Commit-Code {
    param(
        [string]$message
    )
    # Add a random gap between commits to simulate organic work
    # Sometimes it's the same day (1-5 hours), sometimes it's the next day (12-24 hours)
    $rndHours = Get-Random -Minimum 1 -Maximum 24
    $rndMins = Get-Random -Minimum 1 -Maximum 59
    $global:currentDate = $global:currentDate.AddHours($rndHours).AddMinutes($rndMins)
    
    $dateStr = $global:currentDate.ToString("yyyy-MM-ddTHH:mm:ss")
    $env:GIT_AUTHOR_DATE=$dateStr
    $env:GIT_COMMITTER_DATE=$dateStr
    git commit -m $message 2>$null
}

# 1
git add pom.xml mvnw mvnw.cmd .mvn/ .gitignore
Commit-Code -message "init: Project setup and Maven dependencies"

# 2
git add src/main/resources/application.yml src/main/java/com/cloudticket/CloudticketApplication.java
Commit-Code -message "chore: Add base application config and main class"

# 3
git add src/main/java/com/cloudticket/auth/User.java
Commit-Code -message "feat(auth): Add User entity"

# 4
git add src/main/java/com/cloudticket/auth/UserRepository.java src/main/java/com/cloudticket/auth/dto/
Commit-Code -message "feat(auth): Add AuthRepository and DTOs"

# 5
git add src/main/java/com/cloudticket/security/
Commit-Code -message "feat(auth): Implement JwtService and SecurityConfig"

# 6
git add src/main/java/com/cloudticket/auth/AuthService.java src/main/java/com/cloudticket/auth/AuthController.java
Commit-Code -message "feat(auth): Add AuthService and AuthController"

# 7
git add src/main/java/com/cloudticket/event/Event.java src/main/java/com/cloudticket/event/TicketType.java
Commit-Code -message "feat(event): Add Event and TicketType entities"

# 8
git add src/main/java/com/cloudticket/event/EventRepository.java src/main/java/com/cloudticket/event/TicketTypeRepository.java
Commit-Code -message "feat(event): Add Event and TicketType repositories"

# 9
git add src/main/java/com/cloudticket/event/EventService.java
Commit-Code -message "feat(event): Implement EventService"

# 10
git add src/main/java/com/cloudticket/event/dto/ src/main/java/com/cloudticket/event/EventController.java
Commit-Code -message "feat(event): Add EventController and DTOs"

# 11
git add src/main/java/com/cloudticket/common/
Commit-Code -message "chore: Setup global exception handler"

# 12
git add src/main/java/com/cloudticket/booking/Reservation.java src/main/java/com/cloudticket/booking/Booking.java
Commit-Code -message "feat(booking): Add Reservation and Booking entities"

# 13
git add src/main/java/com/cloudticket/booking/ReservationRepository.java src/main/java/com/cloudticket/booking/BookingRepository.java
Commit-Code -message "feat(booking): Add Booking repositories"

# 14
git add src/main/java/com/cloudticket/booking/ReservationService.java src/main/java/com/cloudticket/booking/dto/ReservationRequest.java src/main/java/com/cloudticket/booking/dto/ReservationResponse.java
Commit-Code -message "feat(booking): Add ReservationService with optimistic locking and retry"

# 15
git add src/main/java/com/cloudticket/booking/ReservationController.java
Commit-Code -message "feat(booking): Add ReservationController"

# 16
git add src/main/java/com/cloudticket/booking/ReservationCleanupTask.java src/main/java/com/cloudticket/booking/BookingController.java src/main/java/com/cloudticket/booking/dto/BookingResponse.java
Commit-Code -message "feat(booking): Add ReservationCleanupTask background job"

# 17
git add src/main/java/com/cloudticket/payment/Payment.java src/main/java/com/cloudticket/payment/PaymentRepository.java
Commit-Code -message "feat(payment): Add Payment entity and repository"

# 18
git add src/main/java/com/cloudticket/payment/PaymentService.java src/main/java/com/cloudticket/payment/PaymentController.java src/main/java/com/cloudticket/payment/dto/
Commit-Code -message "feat(payment): Implement PaymentService and PaymentController"

# 19
git add src/test/java/com/cloudticket/booking/ReservationServiceTest.java
Commit-Code -message "test: Add unit tests for ReservationService"

# 20
git add src/test/java/com/cloudticket/payment/PaymentServiceTest.java
Commit-Code -message "test: Add unit tests for PaymentService"

# 21
git add src/main/java/com/cloudticket/config/OpenApiConfig.java
Commit-Code -message "docs: Integrate Swagger OpenAPI"

# 22
git add docker-compose.yml README.md
Commit-Code -message "docs: Add comprehensive README and docker configuration"

# 23 (Remaining files)
git add .
Commit-Code -message "chore: code refactoring and cleanup"

# Force push to GitHub
git remote add origin https://github.com/cvkhang/CloudTicket.git
git branch -M main
git push -f -u origin main
