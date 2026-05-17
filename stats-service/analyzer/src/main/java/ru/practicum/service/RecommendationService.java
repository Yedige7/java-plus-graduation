package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.model.InteractionsCountProjection;
import ru.practicum.model.RecommendedEventProjection;
import ru.practicum.repository.InteractionRepository;
import ru.practicum.repository.SimilarityRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final SimilarityRepository similarityRepository;
    private final InteractionRepository interactionRepository;

    public List<RecommendedEventProjection> getSimilarEvents(Long eventId, Long userId, int maxResults) {
        return similarityRepository.findSimilarEvents(eventId, maxResults * 3)
                .stream()
                .filter(event -> !interactionRepository.existsByUserIdAndEventId(userId, event.getEventId()))
                .limit(maxResults)
                .toList();
    }

    public List<RecommendedEventProjection> getRecommendationsForUser(
            Long userId,
            int maxResults
    ) {

        List<Long> userEvents = interactionRepository.findEventIdsByUserId(userId);

        if (userEvents.isEmpty()) {
            return List.of();
        }

        return userEvents.stream()
                .flatMap(eventId ->
                        similarityRepository.findSimilarEvents(eventId, maxResults)
                                .stream()
                )
                .filter(event ->
                        !interactionRepository.existsByUserIdAndEventId(
                                userId,
                                event.getEventId()
                        )
                )
                .distinct()
                .limit(maxResults)
                .toList();
    }

    public List<InteractionsCountProjection> getInteractionsCount(List<Long> eventIds) {
        return interactionRepository.getInteractionsCount(eventIds);
    }
}