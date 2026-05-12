package event.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "request-service")
public interface RequestClient {

    @PostMapping("/internal/events/confirmed-counts")
    Map<Long, Long> getConfirmedCounts(@RequestBody List<Long> eventIds);
}

