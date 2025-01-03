package ru.practicum.workshop.reviewservice.client;

import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import ru.practicum.workshop.reviewservice.client.config.CustomFeignClientConfiguration;

@FeignClient(name = "registration-service-client", url = "http://host.docker.internal:8084",
        configuration = CustomFeignClientConfiguration.class)
public interface RegistrationClient {
    @GetMapping("/registrations/internal/status-of-registration/{eventId}")
    String getStatusOfRegistration(@PathVariable @Positive Long eventId,
                                                 @RequestHeader("X-Review-User-Id") Long userId);
}
