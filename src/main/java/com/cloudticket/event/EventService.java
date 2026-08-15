package com.cloudticket.event;

import com.cloudticket.event.dto.EventResponse;
import com.cloudticket.event.dto.TicketTypeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;

    public Page<EventResponse> getEvents(Pageable pageable) {
        return eventRepository.findAll(pageable)
                .map(event -> EventResponse.builder()
                        .id(event.getId())
                        .name(event.getName())
                        .venue(event.getVenue())
                        .startsAt(event.getStartsAt())
                        .status(event.getStatus())
                        .build());
    }

    public EventResponse getEventDetails(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        List<TicketTypeResponse> ticketTypes = ticketTypeRepository.findByEventId(eventId).stream()
                .map(tt -> TicketTypeResponse.builder()
                        .id(tt.getId())
                        .name(tt.getName())
                        .price(tt.getPrice())
                        .availableQuantity(tt.getAvailableQuantity())
                        .build())
                .collect(Collectors.toList());

        return EventResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .venue(event.getVenue())
                .startsAt(event.getStartsAt())
                .status(event.getStatus())
                .ticketTypes(ticketTypes)
                .build();
    }
}
