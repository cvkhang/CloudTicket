package com.cloudticket.booking;

import com.cloudticket.booking.dto.ReservationRequest;
import com.cloudticket.booking.dto.ReservationResponse;
import com.cloudticket.event.TicketType;
import com.cloudticket.event.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TicketTypeRepository ticketTypeRepository;

    @Transactional
    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public ReservationResponse reserveTickets(Long userId, String idempotencyKey, ReservationRequest request) {
        Optional<Reservation> existing = reservationRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            log.info("Returning existing reservation for idempotency key: {}", idempotencyKey);
            Reservation res = existing.get();
            return ReservationResponse.builder()
                    .reservationId(res.getId())
                    .status(res.getStatus())
                    .expiresAt(res.getExpiresAt())
                    .build();
        }

        TicketType ticketType = ticketTypeRepository.findById(request.getTicketTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Ticket Type not found"));

        if (ticketType.getAvailableQuantity() < request.getQuantity()) {
            throw new IllegalArgumentException("TICKET_SOLD_OUT: Not enough tickets available");
        }

        ticketType.setAvailableQuantity(ticketType.getAvailableQuantity() - request.getQuantity());
        ticketTypeRepository.save(ticketType);

        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setTicketTypeId(ticketType.getId());
        reservation.setQuantity(request.getQuantity());
        reservation.setStatus("HELD");
        reservation.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        reservation.setIdempotencyKey(idempotencyKey);

        reservation = reservationRepository.save(reservation);
        log.info("Created reservation {} for user {}", reservation.getId(), userId);

        return ReservationResponse.builder()
                .reservationId(reservation.getId())
                .status(reservation.getStatus())
                .expiresAt(reservation.getExpiresAt())
                .build();
    }
}
