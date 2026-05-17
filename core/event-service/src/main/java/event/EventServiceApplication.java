package event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "event",
        "compilation",
        "client"

})
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaRepositories(basePackages = {
        "event.repository",
        "compilation.repository"
})
@EntityScan(basePackages = {
        "event.model",
        "compilation.model"
})
public class EventServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventServiceApplication.class, args);
    }
}
