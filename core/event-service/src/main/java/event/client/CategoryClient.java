package event.client;

import common.dto.CategoryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "category-service")
public interface CategoryClient {
    @GetMapping("/internal/categories/{id}")
    CategoryDto get(@PathVariable Long id);

    @GetMapping("/internal/categories/{id}/exists")
    boolean exists(@PathVariable Long id);
}
