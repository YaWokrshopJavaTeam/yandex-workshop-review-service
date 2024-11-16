package ru.practicum.workshop.reviewservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReviewUpdateDto {
    @Size(min = 2, max = 250, message = "Username of review shouldn't be less then 2 and more than 250 characters")
    String username;
    @Size(max = 120, message = "Title of review shouldn't be more than 120 characters")
    String title;
    @Size(min = 3, max = 10000, message = "Review content shouldn't be less then 3 and more than 10 000 characters")
    String content;
}
