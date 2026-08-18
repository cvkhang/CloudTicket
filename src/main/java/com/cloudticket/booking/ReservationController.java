package com.cloudticket.booking;

import com.cloudticket.auth.User;
import com.cloudticket.auth.UserRepository;
import com.cloudticket.booking.dto.ReservationRequest;
import com.cloudticket.booking.dto.ReservationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ReservationResponse> reserve(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody ReservationRequest request,
            Authentication authentication
    ) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return ResponseEntity.ok(reservationService.reserveTickets(user.getId(), idempotencyKey, request));
    }
}
