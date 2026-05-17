package ru.practicum.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.avro.AvroBinaryDeserializer;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.service.SimilarityService;

@Component
@RequiredArgsConstructor
public class UserActionConsumer {

    private final SimilarityService similarityService;

    @KafkaListener(
            topics = "${aggregator.kafka.topic.user-actions}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void listen(byte[] data) {
        UserActionAvro action =
                AvroBinaryDeserializer.fromBytes(data, UserActionAvro.class);

        similarityService.handle(action);
    }
}