package event.service;


import common.dto.EndpointHitDto;
import common.exception.BadRequestException;
import common.exception.ConflictException;
import common.exception.NotFoundException;
import event.client.CategoryClient;
import event.client.RequestClient;
import event.client.StatsClient;
import event.client.UserClient;
import event.dto.*;
import event.mapper.EventMapper;
import event.model.Event;
import event.model.EventSort;
import event.model.EventState;
import event.model.EventStateActionAdmin;
import event.repository.DatabaseEventSearchRepository;
import event.repository.EventRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final UserClient userClient;
    private final CategoryClient categoryClient;
    private final EventRepository eventRepository;
    private final DatabaseEventSearchRepository databaseEventSearchRepository;
    private final StatsClient statsClient;
    private final RequestClient requestClient;

    @Override
    @Transactional
    public EventFullDto create(Long userId, NewEventDto eventDto) {
        isEventTimeValid(eventDto.getEventDate());

        if (!userClient.exists(userId)) {
            throw new NotFoundException("User not found: " + userId);
        }

        if (!categoryClient.exists(eventDto.getCategory())) {
            throw new NotFoundException("Category not found: " + eventDto.getCategory());
        }

        Event event = EventMapper.mapToEvent(userId, eventDto);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);
        event = eventRepository.save(event);

        List<Event> eventList = List.of(event);

        return this.mapToEventFullDto(eventList).getFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto get(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        if (!event.getInitiatorId().equals(userId)) {
            throw new NotFoundException("Event not found");
        }

        List<Event> eventList = List.of(event);

        return this.mapToEventFullDto(eventList).getFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> get(List<Long> users,
                                  List<EventState> states,
                                  List<Long> categories,
                                  LocalDateTime rangeStart,
                                  LocalDateTime rangeEnd,
                                  int from,
                                  int size) {
        Pageable page = PageRequest.of(from / size, size);

        List<Event> eventList = databaseEventSearchRepository.findForAdmin(users, states, categories, rangeStart, rangeEnd, page);

        return this.mapToEventFullDto(eventList);
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getPublicEvent(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event is not published");
        }

        List<Event> eventList = List.of(event);
        registerHit(request);

        return this.mapToEventFullDto(eventList).getFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEvents(Long userId, int from, int size) {
        if (!userClient.exists(userId)) {
            new NotFoundException("User not found");
        }

        Pageable page = PageRequest.of(from / size, size);

        List<Event> eventList = eventRepository.findByInitiatorId(userId, page);

        return this.mapToEventShortDto(eventList);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getPublicEvents(String text,
                                               List<Long> categories,
                                               Boolean paid,
                                               LocalDateTime rangeStart,
                                               LocalDateTime rangeEnd,
                                               Boolean onlyAvailable,
                                               EventSort sort,
                                               int from,
                                               int size,
                                               HttpServletRequest request) {

        if (rangeStart == null) rangeStart = LocalDateTime.now();
        if (rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new BadRequestException("Range start must be before rangeEnd");
        }

        Pageable page;
        if (sort == EventSort.EVENT_DATE) {
            page = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "eventDate"));
        } else {
            page = PageRequest.of(from / size, size);
        }

        registerHit(request);

        List<Event> eventList = databaseEventSearchRepository.findPublicEvents(
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, page
        );

        List<EventShortDto> dtos = mapToEventShortDto(eventList);

        if (sort == EventSort.VIEWS) {
            dtos.sort(Comparator.comparingLong(EventShortDto::getViews).reversed());
        }

        return dtos;
    }


    @Override
    @Transactional
    public EventFullDto update(Long userId, Long eventId, UpdateEventUserRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Event is already published");
        }

        if (!event.getInitiatorId().equals(userId)) {
            throw new BadRequestException("User not allowed to update event");
        }

        EventMapper.updateEvent(event, request);

        if (request.hasCategory()) {
            if (!categoryClient.exists(request.getCategory())) {
                throw new NotFoundException("Category not found");
            }
            event.setCategoryId(request.getCategory());
        }

        isEventTimeValid(event.getEventDate());

        event = eventRepository.save(event);

        return mapToEventFullDto(List.of(event)).get(0);
    }

    @Override
    @Transactional
    public EventFullDto update(Long eventId, UpdateEventAdminRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (event.getState() == EventState.PUBLISHED
                && request.getEventDate() != null
                && request.getEventDate().isAfter(event.getPublishedOn().minusHours(1))) {
            throw new ConflictException("Invalid event time");
        }

        if (request.getStateAction() != null) {
            if (event.getState() != EventState.PENDING) {
                throw new ConflictException("Invalid event state");
            }

            if (request.getStateAction() == EventStateActionAdmin.PUBLISH_EVENT) {
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else {
                event.setState(EventState.CANCELED);
            }
        }

        EventMapper.updateEvent(event, request);

        if (request.hasCategory()) {
            if (!categoryClient.exists(request.getCategory())) {
                throw new NotFoundException("Category not found");
            }
            event.setCategoryId(request.getCategory());
        }

        isEventTimeValid(event.getEventDate());

        event = eventRepository.save(event);

        return mapToEventFullDto(List.of(event)).get(0);
    }

    private void isEventTimeValid(LocalDateTime eventTime) {
        if (eventTime.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Invalid event time");
        }
    }

    private void registerHit(HttpServletRequest request) {
        EndpointHitDto endpointHitDto = new EndpointHitDto();
        endpointHitDto.setApp("main-service");
        endpointHitDto.setUri(request.getRequestURI());
        endpointHitDto.setIp(request.getRemoteAddr());
        endpointHitDto.setTimestamp(LocalDateTime.now());
        statsClient.hit(endpointHitDto);
    }

    private Map<Long, Integer> getEventsViews(List<Event> eventList) {
        if (eventList == null || eventList.isEmpty()) return Map.of();

        List<String> uris = eventList.stream()
                .map(e -> "/events/" + e.getId())
                .toList();

        LocalDateTime start = eventList.stream()
                .map(Event::getCreatedOn)
                .filter(java.util.Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(LocalDateTime.now().minusYears(1));

        LocalDateTime end = LocalDateTime.now();

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try {
            List<ViewStatsDto> stats = statsClient.getStats(
                    start.format(formatter),
                    end.format(formatter),
                    uris,
                    true
            );

            Map<Long, Integer> map = new HashMap<>();
            for (ViewStatsDto s : stats) {
                String[] parts = s.getUri().split("/");
                if (parts.length >= 3) {
                    long eventId = Long.parseLong(parts[2]);
                    map.put(eventId, (int) s.getHits());
                }
            }
            return map;
        } catch (Exception ex) {
            // критично: не роняем эндпоинт
            return Map.of();
        }
    }

    private List<EventFullDto> mapToEventFullDto(List<Event> eventList) {
        Map<Long, Integer> views = getEventsViews(eventList);
        Map<Long, Long> confirmed = getConfirmedRequests(eventList);

        return eventList.stream()
                .map(e -> EventMapper.mapToEventFullDto(
                        e,
                        categoryClient.get(e.getCategoryId()),
                        userClient.get(e.getInitiatorId()),
                        views.getOrDefault(e.getId(), 0),
                        confirmed.getOrDefault(e.getId(), 0L)
                ))
                .toList();
    }


    private Map<Long, Long> getConfirmedRequests(List<Event> eventList) {
        if (eventList == null || eventList.isEmpty()) return Map.of();

        List<Long> ids = eventList.stream()
                .map(Event::getId)
                .toList();

        return requestClient.getConfirmedCounts(ids);
    }


    private List<EventShortDto> mapToEventShortDto(List<Event> eventList) {
        Map<Long, Integer> views = getEventsViews(eventList);
        Map<Long, Long> confirmed = getConfirmedRequests(eventList);

        return eventList.stream()
                .map(e -> EventMapper.mapToEventShortDto(
                        e,
                        categoryClient.get(e.getCategoryId()),
                        userClient.get(e.getInitiatorId()),
                        views.getOrDefault(e.getId(), 0),
                        confirmed.getOrDefault(e.getId(), 0L)
                ))
                .toList();
    }
}
