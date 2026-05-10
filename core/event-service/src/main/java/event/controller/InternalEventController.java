package event.controller;

import common.dto.EventInternalDto;
import common.exception.NotFoundException;
import event.model.Event;
import event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/events")
@RequiredArgsConstructor
public class InternalEventController {

    private final EventRepository eventRepository;

    @GetMapping("/{id}")
    public EventInternalDto getEvent(@PathVariable Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event not found: " + id));

        EventInternalDto dto = new EventInternalDto();
        dto.setId(event.getId());
        dto.setState(event.getState() == null ? null : event.getState().name());
        dto.setInitiatorId(event.getInitiatorId());
        dto.setParticipantLimit(event.getParticipantLimit());
        dto.setRequestModeration(event.getRequestModeration());

        return dto;
    }
}
