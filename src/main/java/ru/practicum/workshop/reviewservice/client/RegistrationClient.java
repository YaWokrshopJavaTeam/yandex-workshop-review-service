package ru.practicum.workshop.reviewservice.client;

import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "registration-service-client", url = "http://localhost:8084")
public interface RegistrationClient {
    @GetMapping("/registrations/internal/status-of-registration/{eventId}")
    String getStatusOfRegistration(@PathVariable @Positive Long eventId,
                                                 @RequestHeader("X-Review-User-Id") Long userId);
}
