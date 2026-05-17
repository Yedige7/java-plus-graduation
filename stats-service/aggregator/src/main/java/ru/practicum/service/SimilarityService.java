package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.avro.AvroBinarySerializer;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SimilarityService {

    private final KafkaTemplate<Long, byte[]> kafkaTemplate;

    private final Map<Long, Map<Long, Double>> eventUserWeights = new HashMap<>();
    private final Map<Long, Double> eventWeightsSum = new HashMap<>();
    private final Map<Long, Map<Long, Double>> minWeightsSums = new HashMap<>();

    @Value("${aggregator.kafka.topic.events-similarity}")
    private String eventsSimilarityTopic;

    public void handle(UserActionAvro action) {
        long eventId = action.getEventId();
        long userId = action.getUserId();
        double newWeight = toWeight(action.getActionType());

        Map<Long, Double> userWeights =
                eventUserWeights.computeIfAbsent(eventId, id -> new HashMap<>());

        double oldWeight = userWeights.getOrDefault(userId, 0.0);

        if (newWeight <= oldWeight) {
            return;
        }

        double delta = newWeight - oldWeight;

        userWeights.put(userId, newWeight);
        eventWeightsSum.merge(eventId, delta, Double::sum);

        for (Long otherEventId : eventUserWeights.keySet()) {
            if (otherEventId.equals(eventId)) {
                continue;
            }

            Double otherWeight = eventUserWeights
                    .getOrDefault(otherEventId, Map.of())
                    .get(userId);

            if (otherWeight == null) {
                continue;
            }

            double oldMin = Math.min(oldWeight, otherWeight);
            double newMin = Math.min(newWeight, otherWeight);
            double minDelta = newMin - oldMin;

            double sMin;

            if (minDelta > 0) {
                sMin = addMinSum(eventId, otherEventId, minDelta);
            } else {
                sMin = getMinSum(eventId, otherEventId);
            }

            double sumA = eventWeightsSum.getOrDefault(eventId, 0.0);
            double sumB = eventWeightsSum.getOrDefault(otherEventId, 0.0);

            if (sumA == 0.0 || sumB == 0.0 || sMin == 0.0) {
                continue;
            }

            double score = sMin / Math.sqrt(sumA * sumB);

            sendSimilarity(eventId, otherEventId, score, action.getTimestamp());
        }
    }

    private double toWeight(ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }

    private double addMinSum(long eventA, long eventB, double delta) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        Map<Long, Double> inner =
                minWeightsSums.computeIfAbsent(first, id -> new HashMap<>());

        double newValue = inner.getOrDefault(second, 0.0) + delta;
        inner.put(second, newValue);

        return newValue;
    }

    private double getMinSum(long eventA, long eventB) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        return minWeightsSums
                .getOrDefault(first, Map.of())
                .getOrDefault(second, 0.0);
    }

    private void sendSimilarity(long eventA, long eventB, double score, Instant timestamp) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        EventSimilarityAvro avro = EventSimilarityAvro.newBuilder()
                .setEventA(first)
                .setEventB(second)
                .setScore(score)
                .setTimestamp(timestamp)
                .build();

        byte[] payload = AvroBinarySerializer.toBytes(avro);

        System.out.printf(
                "Similarity eventA=%d eventB=%d score=%f%n",
                first,
                second,
                score
        );

        kafkaTemplate.send(eventsSimilarityTopic, first, payload);
    }
}