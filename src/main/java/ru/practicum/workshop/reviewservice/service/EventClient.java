package ru.practicum.workshop.reviewservice.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.workshop.reviewservice.dto.EventResponse;

@FeignClient(name = "event-service-client", url = "http://event-service:8082")
public interface EventClient {
    @GetMapping("/events/{id}")
    EventResponse readEventById(@PathVariable("id") Long id);
}
