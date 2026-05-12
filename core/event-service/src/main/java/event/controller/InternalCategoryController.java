package event.controller;


import event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/events")
@RequiredArgsConstructor
public class InternalCategoryController {

    private final EventRepository eventRepository;

    @GetMapping("/by-category/{categoryId}/exists-events")
    public boolean existsByCategory(@PathVariable Long categoryId) {
        return eventRepository.existsByCategoryId(categoryId);
    }
}
