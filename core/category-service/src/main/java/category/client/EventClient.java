package category.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "event-service")
public interface EventClient {

    @GetMapping("/internal/events/by-category/{categoryId}/exists-events")
    boolean existsByCategory(@PathVariable Long categoryId);
}
