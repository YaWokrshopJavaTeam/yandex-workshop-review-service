package ru.practicum.workshop.reviewservice.dto;

import lombok.Value;

import java.time.LocalDateTime;

@Value
public class ReviewDtoWithAuthor {
    Long id;
    Long authorId;
    Long eventId;
    String username;
    String title;
    String content;
    LocalDateTime createdOn;
    LocalDateTime updatedOn;
    Integer mark;
}
