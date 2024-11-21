package ru.practicum.workshop.reviewservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Value;

import static ru.practicum.workshop.reviewservice.dto.ReviewDtoValidationConstants.*;

@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReviewUpdateDto {
    @Size(min = USERNAME_MIN_SIZE, max = USERNAME_MAX_SIZE, message = USERNAME_SIZE_ERROR_MESSAGE)
    String username;
    @Size(max = TITLE_MAX_SIZE, message = TITLE_SIZE_ERROR_MESSAGE)
    String title;
    @Size(min = CONTENT_MIN_SIZE, max = CONTENT_MAX_SIZE, message = CONTENT_SIZE_ERROR_MESSAGE)
    String content;
    @Min(value = MARK_MIN_VALUE, message = MARK_MIN_ERROR_MESSAGE)
    @Max(value = MARK_MAX_VALUE, message = MARK_MAX_ERROR_MESSAGE)
    Integer mark;
}
