package ru.practicum.workshop.reviewservice.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserAverageScore {
    protected Long userId;
    protected Double averageScore;
}
