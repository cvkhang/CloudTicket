package com.cloudticket.payment;

import com.cloudticket.booking.Booking;
import com.cloudticket.booking.BookingRepository;
import com.cloudticket.booking.Reservation;
import com.cloudticket.booking.ReservationRepository;
import com.cloudticket.event.TicketType;
import com.cloudticket.event.TicketTypeRepository;
import com.cloudticket.payment.dto.PaymentRequest;
import com.cloudticket.payment.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final ReservationRepository reservationRepository;
    private final TicketTypeRepository ticketTypeRepository;

    @Transactional
    public PaymentResponse processPayment(Long userId, String idempotencyKey, PaymentRequest request) {
        Optional<Payment> existingPayment = paymentRepository.findByIdempotencyKey(idempotencyKey);
        if (existingPayment.isPresent()) {
            Payment p = existingPayment.get();
            log.info("Returning existing payment for idempotency key: {}", idempotencyKey);
            return PaymentResponse.builder()
                    .paymentId(p.getId())
                    .bookingId(p.getBookingId())
                    .status(p.getStatus())
                    .processedAt(p.getCreatedAt())
                    .build();
        }

        Reservation reservation = reservationRepository.findById(request.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        if (!reservation.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Reservation does not belong to the user");
        }

        if (!"HELD".equals(reservation.getStatus())) {
            throw new IllegalArgumentException("Reservation is no longer held (status: " + reservation.getStatus() + ")");
        }

        if (reservation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Reservation has expired");
        }

        TicketType ticketType = ticketTypeRepository.findById(reservation.getTicketTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Ticket Type not found"));

        BigDecimal totalAmount = ticketType.getPrice().multiply(new BigDecimal(reservation.getQuantity()));

        reservation.setStatus("CONFIRMED");
        reservationRepository.save(reservation);

        Booking booking = new Booking();
        booking.setReservationId(reservation.getId());
        booking.setUserId(userId);
        booking.setTotalAmount(totalAmount);
        booking.setStatus("CONFIRMED");
        booking = bookingRepository.save(booking);

        Payment payment = new Payment();
        payment.setBookingId(booking.getId());
        payment.setAmount(totalAmount);
        payment.setStatus("SUCCESS");
        payment.setIdempotencyKey(idempotencyKey);
        payment = paymentRepository.save(payment);

        log.info("Successfully processed payment {} for booking {}", payment.getId(), booking.getId());

        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .bookingId(booking.getId())
                .status(payment.getStatus())
                .processedAt(payment.getCreatedAt())
                .build();
    }
}
