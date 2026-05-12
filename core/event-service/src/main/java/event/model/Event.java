package event.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "initiator_id", nullable = false)
    private Long initiatorId;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(nullable = false, length = 2000)
    private String annotation;

    @Column(nullable = false, length = 7000)
    private String description;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false)
    private Float lat;

    @Column(nullable = false)
    private Float lon;

    @Column(nullable = false)
    private Boolean paid = false;

    @Column(nullable = false)
    private Integer participantLimit = 0;

    @Column(nullable = false)
    private Boolean requestModeration = true;

    @Column(nullable = false)
    private Long confirmedRequests = 0L;

    @Column(nullable = false)
    private LocalDateTime eventDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdOn;

    private LocalDateTime publishedOn;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventState state;
}
