package event.repository;

import event.model.Event;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    Event save(Event event);

    List<Event> findByInitiatorId(Long initiatorId, Pageable page);

    Optional<Event> findById(Long eventId);

    boolean existsByCategoryId(Long categoryId);
}
