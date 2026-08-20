package com.cloudticket.payment;

import com.cloudticket.booking.Booking;
import com.cloudticket.booking.BookingRepository;
import com.cloudticket.booking.Reservation;
import com.cloudticket.booking.ReservationRepository;
import com.cloudticket.event.TicketType;
import com.cloudticket.event.TicketTypeRepository;
import com.cloudticket.payment.dto.PaymentRequest;
import com.cloudticket.payment.dto.PaymentResponse;
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
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private TicketTypeRepository ticketTypeRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void processPayment_Success() {
        Long userId = 1L;
        String idempotencyKey = "pay-123";
        Long reservationId = 100L;
        PaymentRequest request = new PaymentRequest(reservationId, "CREDIT_CARD");

        Reservation reservation = new Reservation();
        reservation.setId(reservationId);
        reservation.setUserId(userId);
        reservation.setStatus("HELD");
        reservation.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        reservation.setTicketTypeId(10L);
        reservation.setQuantity(2);

        TicketType ticketType = new TicketType();
        ticketType.setId(10L);
        ticketType.setPrice(BigDecimal.valueOf(100)); // 2 * 100 = 200

        when(paymentRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(reservationRepository.findById(request.getReservationId())).thenReturn(Optional.of(reservation));
        when(ticketTypeRepository.findById(reservation.getTicketTypeId())).thenReturn(Optional.of(ticketType));

        Booking savedBooking = new Booking();
        savedBooking.setId(200L);
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        Payment savedPayment = new Payment();
        savedPayment.setId(300L);
        savedPayment.setStatus("SUCCESS");
        savedPayment.setCreatedAt(LocalDateTime.now());
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        PaymentResponse response = paymentService.processPayment(userId, idempotencyKey, request);

        assertNotNull(response);
        assertEquals(300L, response.getPaymentId());
        assertEquals(200L, response.getBookingId());
        assertEquals("SUCCESS", response.getStatus());

        assertEquals("CONFIRMED", reservation.getStatus());
        verify(reservationRepository).save(reservation);
        verify(bookingRepository).save(any(Booking.class));
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void processPayment_ExpiredReservation() {
        Long userId = 1L;
        String idempotencyKey = "pay-124";
        PaymentRequest request = new PaymentRequest(100L, "CREDIT_CARD");

        Reservation reservation = new Reservation();
        reservation.setId(100L);
        reservation.setUserId(userId);
        reservation.setStatus("HELD");
        reservation.setExpiresAt(LocalDateTime.now().minusMinutes(5)); // Expired 5 mins ago

        when(paymentRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(reservationRepository.findById(request.getReservationId())).thenReturn(Optional.of(reservation));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.processPayment(userId, idempotencyKey, request);
        });

        assertTrue(exception.getMessage().contains("expired"));
        verify(ticketTypeRepository, never()).findById(any());
        verify(bookingRepository, never()).save(any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void processPayment_WrongUser() {
        Long userId = 1L;
        String idempotencyKey = "pay-125";
        PaymentRequest request = new PaymentRequest(100L, "CREDIT_CARD");

        Reservation reservation = new Reservation();
        reservation.setId(100L);
        reservation.setUserId(2L); // Different user

        when(paymentRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(reservationRepository.findById(request.getReservationId())).thenReturn(Optional.of(reservation));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.processPayment(userId, idempotencyKey, request);
        });

        assertTrue(exception.getMessage().contains("does not belong to the user"));
    }
}
