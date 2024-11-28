package ru.practicum.workshop.reviewservice.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventIndicators {
    protected Long eventId;
    protected Integer numberOfReviews;
    protected Integer positiveReviewsPercent;
    protected Integer negativeReviewsPercent;
}
