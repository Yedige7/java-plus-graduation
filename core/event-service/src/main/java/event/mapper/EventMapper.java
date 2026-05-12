package event.mapper;

import common.dto.CategoryDto;
import common.dto.LocationDto;
import common.dto.UserShortDto;
import event.dto.*;
import event.model.Event;
import event.model.EventState;
import event.model.EventStateAction;

public class EventMapper {

    public static Event mapToEvent(Long userId, NewEventDto dto) {
        Event event = new Event();
        event.setInitiatorId(userId);
        event.setTitle(dto.getTitle());
        event.setAnnotation(dto.getAnnotation());
        event.setDescription(dto.getDescription());
        event.setCategoryId(dto.getCategory());
        event.setEventDate(dto.getEventDate());

        event.setLat(dto.getLocation().getLat());
        event.setLon(dto.getLocation().getLon());
        ;

        event.setPaid(dto.getPaid() != null ? dto.getPaid() : false);
        event.setParticipantLimit(dto.getParticipantLimit() != null ? dto.getParticipantLimit() : 0);
        event.setRequestModeration(dto.getRequestModeration() != null ? dto.getRequestModeration() : true);

        return event;
    }

    public static EventFullDto mapToEventFullDto(Event event,
                                                 CategoryDto category,
                                                 UserShortDto user,
                                                 long views,
                                                 long confirmed) {
        EventFullDto dto = new EventFullDto();

        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setAnnotation(event.getAnnotation());
        dto.setDescription(event.getDescription());
        dto.setCategory(category);
        dto.setCreatedOn(event.getCreatedOn());
        dto.setEventDate(event.getEventDate());
        dto.setPublishedOn(event.getPublishedOn());
        dto.setInitiator(user);

        LocationDto locationDto = new LocationDto();
        locationDto.setLat(event.getLat());
        locationDto.setLon(event.getLon());
        dto.setLocation(locationDto);

        dto.setPaid(event.getPaid());
        dto.setParticipantLimit(event.getParticipantLimit());
        dto.setRequestModeration(event.getRequestModeration());
        dto.setState(event.getState() == null ? null : event.getState().name());
        dto.setViews(views);
        dto.setConfirmedRequests(confirmed);

        return dto;
    }

    public static EventShortDto mapToEventShortDto(Event event,
                                                   CategoryDto category,
                                                   UserShortDto user,
                                                   long views,
                                                   long confirmed) {
        EventShortDto dto = new EventShortDto();

        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setAnnotation(event.getAnnotation());
        dto.setCategory(category);
        dto.setConfirmedRequests(confirmed);
        dto.setViews(views);
        dto.setEventDate(event.getEventDate());
        dto.setInitiator(user);
        dto.setPaid(event.getPaid());

        return dto;
    }

    public static Event updateEvent(Event event, UpdateEventUserRequest req) {
        if (req.hasTitle()) event.setTitle(req.getTitle());
        if (req.hasAnnotation()) event.setAnnotation(req.getAnnotation());
        if (req.hasDescription()) event.setDescription(req.getDescription());
        if (req.hasEventDate()) event.setEventDate(req.getEventDate());

        if (req.hasLocation()) {
            event.setLat(req.getLocation().getLat());
            event.setLon(req.getLocation().getLon());
        }

        if (req.hasPaid()) event.setPaid(req.getPaid());
        if (req.hasParticipantLimit()) event.setParticipantLimit(req.getParticipantLimit());
        if (req.hasRequestModeration()) event.setRequestModeration(req.getRequestModeration());

        if (req.hasStateAction()) {
            EventState state = req.getStateAction() == EventStateAction.SEND_TO_REVIEW
                    ? EventState.PENDING
                    : EventState.CANCELED;
            event.setState(state);
        }

        return event;
    }

    public static Event updateEvent(Event event, UpdateEventAdminRequest req) {
        if (req.hasTitle()) event.setTitle(req.getTitle());
        if (req.hasAnnotation()) event.setAnnotation(req.getAnnotation());
        if (req.hasDescription()) event.setDescription(req.getDescription());
        if (req.hasEventDate()) event.setEventDate(req.getEventDate());

        if (req.hasLocation()) {
            event.setLat(req.getLocation().getLat());
            event.setLon(req.getLocation().getLon());
        }

        if (req.hasPaid()) event.setPaid(req.getPaid());
        if (req.hasParticipantLimit()) event.setParticipantLimit(req.getParticipantLimit());
        if (req.hasRequestModeration()) event.setRequestModeration(req.getRequestModeration());

        return event;
    }
}