package ru.practicum.workshop.reviewservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EventResponse {
    protected Long id;
    protected String name;
    protected String description;
    protected LocalDateTime startDateTime;
    protected LocalDateTime endDateTime;
    protected String location;
    protected Long ownerId;
    protected LocalDateTime createdDateTime;
}
