package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.InteractionEntity;
import ru.practicum.model.InteractionsCountProjection;

import java.util.List;

public interface InteractionRepository extends JpaRepository<InteractionEntity, Long> {

    @Modifying
    @Query(value = """
            INSERT INTO interactions (user_id, event_id, rating, ts)
            VALUES (:userId, :eventId, :rating, :timestamp)
            ON CONFLICT (user_id, event_id)
            DO UPDATE SET
                rating = EXCLUDED.rating,
                ts = EXCLUDED.ts
            WHERE interactions.rating < EXCLUDED.rating
            """, nativeQuery = true)
    void upsertInteraction(Long userId, Long eventId, Double rating, java.time.Instant timestamp);

    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    @Query("""
            select i.eventId
            from InteractionEntity i
            where i.userId = :userId
            """)
    List<Long> findEventIdsByUserId(Long userId);

    @Query("""
            select
                i.eventId as eventId,
                sum(i.rating) as score
            from InteractionEntity i
            where i.eventId in :eventIds
            group by i.eventId
            """)
    List<InteractionsCountProjection> getInteractionsCount(List<Long> eventIds);
}