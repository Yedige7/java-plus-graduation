package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.RecommendedEventProjection;
import ru.practicum.model.SimilarityEntity;

import java.util.List;

public interface SimilarityRepository extends JpaRepository<SimilarityEntity, Long> {

    @Modifying
    @Query(value = """
            INSERT INTO similarities (event1, event2, similarity, ts)
            VALUES (:event1, :event2, :similarity, :timestamp)
            ON CONFLICT (event1, event2)
            DO UPDATE SET
                similarity = EXCLUDED.similarity,
                ts = EXCLUDED.ts
            """, nativeQuery = true)
    void upsertSimilarity(Long event1, Long event2, Double similarity, java.time.Instant timestamp);

    @Query(value = """
            SELECT CASE
                WHEN event1 = :eventId THEN event2
                ELSE event1
            END AS event_id,
            similarity AS score
            FROM similarities
            WHERE event1 = :eventId OR event2 = :eventId
            ORDER BY similarity DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<RecommendedEventProjection> findSimilarEvents(Long eventId, int limit);
}