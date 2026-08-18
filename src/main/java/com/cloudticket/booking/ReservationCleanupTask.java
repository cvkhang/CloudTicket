package com.cloudticket.booking;

import com.cloudticket.event.TicketType;
import com.cloudticket.event.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationCleanupTask {

    private final ReservationRepository reservationRepository;
    private final TicketTypeRepository ticketTypeRepository;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanupExpiredReservations() {
        log.info("Running expired reservations cleanup task");
        
        List<Reservation> expiredReservations = reservationRepository.findByStatusAndExpiresAtBefore("HELD", LocalDateTime.now());
        
        if (expiredReservations.isEmpty()) {
            return;
        }
        
        log.info("Found {} expired reservations to clean up", expiredReservations.size());
        
        for (Reservation reservation : expiredReservations) {
            reservation.setStatus("EXPIRED");
            reservationRepository.save(reservation);
            
            TicketType ticketType = ticketTypeRepository.findById(reservation.getTicketTypeId())
                    .orElseThrow(() -> new IllegalStateException("Ticket Type not found for reservation " + reservation.getId()));
            
            ticketType.setAvailableQuantity(ticketType.getAvailableQuantity() + reservation.getQuantity());
            ticketTypeRepository.save(ticketType);
            
            log.info("Expired reservation {}. Returned {} tickets to ticket type {}", 
                    reservation.getId(), reservation.getQuantity(), ticketType.getId());
        }
    }
}
