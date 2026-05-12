package request.service;

import common.dto.EventInternalDto;
import common.exception.ConflictException;
import common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import request.client.EventClient;
import request.client.UserClient;
import request.dto.EventRequestStatusUpdateRequest;
import request.dto.EventRequestStatusUpdateResult;
import request.dto.ParticipationRequestDto;
import request.mapper.ParticipationRequestMapper;
import request.model.ParticipationRequest;
import request.model.RequestStatus;
import request.repository.ParticipationRequestRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    private final ParticipationRequestRepository requestRepo;
    private final UserClient userClient;
    private final EventClient eventClient;

    @Override
    public ParticipationRequestDto create(Long userId, Long eventId) {

        if (!userClient.exists(userId)) {
            throw new NotFoundException("User not found: " + userId);
        }

        EventInternalDto event = eventClient.get(eventId);
        if (event == null) {
            throw new NotFoundException("Event not found: " + eventId);
        }

        if (Objects.equals(event.getInitiatorId(), userId)) {
            throw new ConflictException("Initiator cannot request own event");
        }

        if (!"PUBLISHED".equals(event.getState())) {
            throw new ConflictException("Event not published");
        }

        if (requestRepo.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Duplicate request");
        }

        long limit = event.getParticipantLimit() == null ? 0 : event.getParticipantLimit();
        long confirmed = requestRepo.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

        if (limit > 0 && confirmed >= limit) {
            throw new ConflictException("Participant limit reached");
        }

        ParticipationRequest req = new ParticipationRequest();
        req.setRequesterId(userId);
        req.setEventId(eventId);

        RequestStatus status;
        if (limit == 0 || Boolean.FALSE.equals(event.getRequestModeration())) {
            status = RequestStatus.CONFIRMED;
        } else {
            status = RequestStatus.PENDING;
        }
        req.setStatus(status);

        // защита от гонки при автоподтверждении
        if (status == RequestStatus.CONFIRMED && limit > 0) {
            long confirmedAfter = requestRepo.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
            if (confirmedAfter >= limit) {
                throw new ConflictException("Participant limit reached");
            }
        }

        return ParticipationRequestMapper.toDto(requestRepo.save(req));
    }

    @Override
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        ParticipationRequest req = requestRepo.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new NotFoundException("Request not found: " + requestId));

        req.setStatus(RequestStatus.CANCELED);
        return ParticipationRequestMapper.toDto(req);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        if (!userClient.exists(userId)) {
            throw new NotFoundException("User not found: " + userId);
        }
        return requestRepo.findAllByRequesterId(userId).stream()
                .map(ParticipationRequestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        EventInternalDto event = eventClient.get(eventId);
        if (event == null) {
            throw new NotFoundException("Event not found: " + eventId);
        }
        if (!Objects.equals(event.getInitiatorId(), userId)) {
            throw new ConflictException("Only initiator can view event requests");
        }

        return requestRepo.findAllByEventId(eventId).stream()
                .map(ParticipationRequestMapper::toDto)
                .toList();
    }

    @Override
    public EventRequestStatusUpdateResult updateEventRequests(
            Long userId,
            Long eventId,
            List<Long> requestIds,
            EventRequestStatusUpdateRequest.RequestUpdateStatus status
    ) {
        EventInternalDto event = eventClient.get(eventId);
        if (event == null) {
            throw new NotFoundException("Event not found: " + eventId);
        }
        if (!Objects.equals(event.getInitiatorId(), userId)) {
            throw new ConflictException("Only initiator can update requests");
        }

        List<ParticipationRequest> requests =
                requestRepo.findAllByIdInAndEventId(requestIds, eventId);

        if (requests.size() != new HashSet<>(requestIds).size()) {
            throw new NotFoundException("Some requests not found for event");
        }

        for (ParticipationRequest r : requests) {
            if (r.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Only PENDING requests can be updated");
            }
        }

        List<ParticipationRequest> confirmedOut = new ArrayList<>();
        List<ParticipationRequest> rejectedOut = new ArrayList<>();

        if (status == EventRequestStatusUpdateRequest.RequestUpdateStatus.REJECTED) {
            for (ParticipationRequest r : requests) {
                r.setStatus(RequestStatus.REJECTED);
                rejectedOut.add(r);
            }
            return toResult(confirmedOut, rejectedOut);
        }

        long limit = event.getParticipantLimit() == null ? 0 : event.getParticipantLimit();
        long confirmed = requestRepo.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

        if (limit > 0 && confirmed >= limit) {
            throw new ConflictException("Participant limit reached");
        }

        long slots = (limit == 0) ? Long.MAX_VALUE : (limit - confirmed);

        for (ParticipationRequest r : requests) {
            if (slots > 0) {
                r.setStatus(RequestStatus.CONFIRMED);
                confirmedOut.add(r);
                slots--;
            } else {
                r.setStatus(RequestStatus.REJECTED);
                rejectedOut.add(r);
            }
        }

        return toResult(confirmedOut, rejectedOut);
    }

    private EventRequestStatusUpdateResult toResult(List<ParticipationRequest> confirmed,
                                                    List<ParticipationRequest> rejected) {
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmed.stream().map(ParticipationRequestMapper::toDto).toList())
                .rejectedRequests(rejected.stream().map(ParticipationRequestMapper::toDto).toList())
                .build();
    }
}