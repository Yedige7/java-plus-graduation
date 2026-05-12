package event.client;

import common.dto.UserShortDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping("/internal/users/{id}")
    UserShortDto get(@PathVariable Long id);

    @GetMapping("/internal/users/{id}/exists")
    boolean exists(@PathVariable Long id);
}
