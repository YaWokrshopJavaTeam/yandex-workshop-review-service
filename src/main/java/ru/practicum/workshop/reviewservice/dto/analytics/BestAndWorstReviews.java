package ru.practicum.workshop.reviewservice.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.workshop.reviewservice.dto.ReviewDtoWithAuthor;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BestAndWorstReviews {
    protected Long eventId;
    protected List<ReviewDtoWithAuthor> bestReviews;
    protected List<ReviewDtoWithAuthor> worstReviews;
}
