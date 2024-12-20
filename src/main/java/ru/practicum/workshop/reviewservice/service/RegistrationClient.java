package ru.practicum.workshop.reviewservice.service;

import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import ru.practicum.workshop.reviewservice.dto.StatusOfRegistration;

@FeignClient(name = "registration-service-client", url = "http://localhost:8084")
public interface RegistrationClient {
    @GetMapping("/registrations/internal/status-of-registration/{eventId}")
    StatusOfRegistration getStatusOfRegistration(@PathVariable @Positive Long eventId,
                                                 @RequestHeader("X-Review-User-Id") Long userId);
}
