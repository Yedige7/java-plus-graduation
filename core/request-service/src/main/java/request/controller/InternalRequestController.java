package request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import request.repository.ParticipationRequestRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/events")
@RequiredArgsConstructor
public class InternalRequestController {

    private final ParticipationRequestRepository repository;

    @PostMapping("/confirmed-counts")
    public Map<Long, Long> getConfirmedCounts(@RequestBody List<Long> eventIds) {

        Map<Long, Long> map = new HashMap<>();

        for (ParticipationRequestRepository.EventConfirmedCount row
                : repository.countConfirmedByEventIds(eventIds)) {
            map.put(row.getEventId(), row.getCnt());
        }

        return map;
    }
}
