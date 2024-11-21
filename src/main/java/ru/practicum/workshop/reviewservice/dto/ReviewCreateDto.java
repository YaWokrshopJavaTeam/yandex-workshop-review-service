package ru.practicum.workshop.reviewservice.dto;

import jakarta.validation.constraints.*;
import lombok.Value;

import static ru.practicum.workshop.reviewservice.dto.ReviewDtoValidationConstants.*;

@Value
public class ReviewCreateDto {
    @NotNull(message = AUTHOR_ID_NOT_NULL_ERROR_MESSAGE)
    @Positive(message = AUTHOR_ID_POSITIVE_ERROR_MESSAGE)
    Long authorId;
    @NotNull(message = EVENT_ID_NOT_NULL_ERROR_MESSAGE)
    @Positive(message = EVENT_ID_POSITIVE_ERROR_MESSAGE)
    Long eventId;
    @NotBlank(message = USERNAME_NOT_BLANK_ERROR_MESSAGE)
    @Size(min = USERNAME_MIN_SIZE, max = USERNAME_MAX_SIZE, message = USERNAME_SIZE_ERROR_MESSAGE)
    String username;
    @Size(max = TITLE_MAX_SIZE, message = TITLE_SIZE_ERROR_MESSAGE)
    String title;
    @NotBlank(message = CONTENT_NOT_BLANK_ERROR_MESSAGE)
    @Size(min = CONTENT_MIN_SIZE, max = CONTENT_MAX_SIZE, message = CONTENT_SIZE_ERROR_MESSAGE)
    String content;
    @NotNull(message = MARK_NOT_NULL_ERROR_MESSAGE)
    @Min(value = MARK_MIN_VALUE, message = MARK_MIN_ERROR_MESSAGE)
    @Max(value = MARK_MAX_VALUE, message = MARK_MAX_ERROR_MESSAGE)
    Integer mark;
}
