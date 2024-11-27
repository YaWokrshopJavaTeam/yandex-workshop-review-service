package ru.practicum.workshop.reviewservice.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.workshop.reviewservice.dto.analytics.EventAverageScore;
import ru.practicum.workshop.reviewservice.model.Review;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewStorage extends JpaRepository<Review, Long> {
    List<Review> findByEventId(Long eventId, Pageable pageable);

    Optional<Review> findByIdAndAuthorId(Long id, Long authorId);

    @Query("SELECT new ru.practicum.workshop.reviewservice.dto.EventAverageScore(r.eventId, AVG(r.mark)) " +
            "FROM Review AS r WHERE r.eventId NOT IN " +
            "(SELECT r.eventId FROM Review AS r WHERE r.likes + r.dislikes > 10 AND r.dislikes > r.likes) " +
            "GROUP BY r.eventId")
    EventAverageScore getEventAverageScore(@Param("eventId") Long eventId);
}