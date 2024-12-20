package ru.practicum.workshop.reviewservice.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import ru.practicum.workshop.reviewservice.dto.EventResponse;

@FeignClient(name = "event-service-client", url = "http://localhost:8082")
public interface EventClient {
    @GetMapping("/events/{id}")
    EventResponse readEventById(@RequestHeader("X-User-Id") Long userId, @PathVariable("id") Long id);
}
