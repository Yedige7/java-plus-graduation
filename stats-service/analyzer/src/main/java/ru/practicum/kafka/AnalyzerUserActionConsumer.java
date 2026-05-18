package ru.practicum.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.avro.AvroBinaryDeserializer;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.repository.InteractionRepository;

@Component
@RequiredArgsConstructor
public class AnalyzerUserActionConsumer {

    private final InteractionRepository interactionRepository;

    @Transactional
    @KafkaListener(
            topics = "${analyzer.kafka.topic.user-actions}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void listen(byte[] data) {
        UserActionAvro action =
                AvroBinaryDeserializer.fromBytes(data, UserActionAvro.class);

        interactionRepository.upsertInteraction(
                action.getUserId(),
                action.getEventId(),
                toRating(action.getActionType()),
                action.getTimestamp()
        );
    }

    private double toRating(ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }
}