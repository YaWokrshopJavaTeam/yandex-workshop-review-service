package ru.practicum.workshop.reviewservice.dto;

import jakarta.validation.constraints.*;
import lombok.Value;

@Value
public class ReviewCreateDto {
    @NotNull(message = "Author's id shouldn't be null")
    @Positive(message = "Author's id should be positive")
    Long authorId;
    @NotNull(message = "Event's id shouldn't be null")
    @Positive(message = "Event's id should be positive")
    Long eventId;
    @NotBlank(message = "Username shouldn't be blank")
    @Size(min = 2, max = 250, message = "Username of review shouldn't be less then 2 and more than 250 characters")
    String username;
    @Size(max = 120, message = "Title of review shouldn't be more than 120 characters")
    String title;
    @NotBlank(message = "Review content shouldn't be blank")
    @Size(min = 3, max = 10000, message = "Review content shouldn't be less then 3 and more than 10 000 characters")
    String content;
}
