package ru.practicum.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.avro.AvroBinaryDeserializer;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.repository.SimilarityRepository;

@Component
@RequiredArgsConstructor
public class AnalyzerSimilarityConsumer {

    private final SimilarityRepository similarityRepository;

    @Transactional
    @KafkaListener(
            topics = "${analyzer.kafka.topic.events-similarity}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void listen(byte[] data) {
        EventSimilarityAvro similarity =
                AvroBinaryDeserializer.fromBytes(data, EventSimilarityAvro.class);

        similarityRepository.upsertSimilarity(
                similarity.getEventA(),
                similarity.getEventB(),
                similarity.getScore(),
                similarity.getTimestamp()
        );
    }
}