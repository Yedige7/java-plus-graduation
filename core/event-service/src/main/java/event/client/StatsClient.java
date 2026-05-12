package event.client;

import common.dto.EndpointHitDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.ewm.stats.dto.ViewStatsDto;

import java.util.List;


@FeignClient(name = "stats-server")
public interface StatsClient {

    @PostMapping("/hit")
    void hit(@RequestBody EndpointHitDto dto);

    @GetMapping("/stats")
    List<ViewStatsDto> getStats(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam List<String> uris,
            @RequestParam Boolean unique
    );
}