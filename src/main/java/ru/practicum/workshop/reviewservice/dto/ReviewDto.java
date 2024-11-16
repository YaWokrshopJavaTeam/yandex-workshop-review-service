package ru.practicum.workshop.reviewservice.dto;

import lombok.Value;

import java.time.LocalDateTime;

@Value
public class ReviewDto {
    Long id;
    Long eventId;
    String username;
    String title;
    String content;
    LocalDateTime createdOn;
    LocalDateTime updatedOn;
    Long mark;
}
