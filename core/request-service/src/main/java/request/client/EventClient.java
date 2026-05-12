package request.client;

import common.dto.EventInternalDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "event-service")
public interface EventClient {
    @GetMapping("/internal/events/{id}")
    EventInternalDto get(@PathVariable Long id);
}
