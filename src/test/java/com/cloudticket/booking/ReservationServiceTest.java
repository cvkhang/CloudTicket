package com.cloudticket.booking;

import com.cloudticket.booking.dto.ReservationRequest;
import com.cloudticket.booking.dto.ReservationResponse;
import com.cloudticket.event.TicketType;
import com.cloudticket.event.TicketTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private TicketTypeRepository ticketTypeRepository;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void reserveTickets_Success() {
        Long userId = 1L;
        String idempotencyKey = "key-123";
        ReservationRequest request = new ReservationRequest(10L, 2);

        TicketType ticketType = new TicketType();
        ticketType.setId(10L);
        ticketType.setAvailableQuantity(5);
        ticketType.setPrice(BigDecimal.valueOf(100));

        when(reservationRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(ticketTypeRepository.findById(request.getTicketTypeId())).thenReturn(Optional.of(ticketType));
        
        Reservation savedRes = new Reservation();
        savedRes.setId(100L);
        savedRes.setStatus("HELD");
        savedRes.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedRes);

        ReservationResponse response = reservationService.reserveTickets(userId, idempotencyKey, request);

        assertNotNull(response);
        assertEquals(100L, response.getReservationId());
        assertEquals("HELD", response.getStatus());
        assertEquals(3, ticketType.getAvailableQuantity()); // 5 - 2 = 3
        
        verify(ticketTypeRepository).save(ticketType);
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void reserveTickets_IdempotencyKeyExists() {
        Long userId = 1L;
        String idempotencyKey = "key-existing";
        ReservationRequest request = new ReservationRequest(10L, 2);

        Reservation existingRes = new Reservation();
        existingRes.setId(99L);
        existingRes.setStatus("HELD");
        existingRes.setExpiresAt(LocalDateTime.now().plusMinutes(15));

        when(reservationRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(existingRes));

        ReservationResponse response = reservationService.reserveTickets(userId, idempotencyKey, request);

        assertNotNull(response);
        assertEquals(99L, response.getReservationId());
        
        verify(ticketTypeRepository, never()).findById(any());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void reserveTickets_SoldOut() {
        Long userId = 1L;
        String idempotencyKey = "key-124";
        ReservationRequest request = new ReservationRequest(10L, 10); // Requesting 10

        TicketType ticketType = new TicketType();
        ticketType.setId(10L);
        ticketType.setAvailableQuantity(5); // Only 5 available

        when(reservationRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(ticketTypeRepository.findById(request.getTicketTypeId())).thenReturn(Optional.of(ticketType));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            reservationService.reserveTickets(userId, idempotencyKey, request);
        });

        assertTrue(exception.getMessage().contains("TICKET_SOLD_OUT"));
        verify(ticketTypeRepository, never()).save(any());
        verify(reservationRepository, never()).save(any());
    }
}
