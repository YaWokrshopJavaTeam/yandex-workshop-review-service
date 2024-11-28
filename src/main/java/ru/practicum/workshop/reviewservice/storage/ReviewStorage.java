package ru.practicum.workshop.reviewservice.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.workshop.reviewservice.model.Review;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewStorage extends JpaRepository<Review, Long> {
    List<Review> findByEventId(Long eventId, Pageable pageable);

    Optional<Review> findByIdAndAuthorId(Long id, Long authorId);

    @Query("SELECT AVG(r.mark) " +
            "FROM Review AS r " +
            "WHERE r.eventId = :eventId AND r.id NOT IN " +
                "(SELECT r.id " +
                "FROM Review AS r " +
                "WHERE r.eventId = :eventId AND r.likes + r.dislikes > 10 AND r.dislikes > r.likes) " +
            "GROUP BY r.eventId")
    double getEventAverageMark(@Param("eventId") Long eventId);

    @Query("SELECT AVG(r.mark) " +
            "FROM Review AS r " +
            "WHERE r.author.id = :authorId AND r.id NOT IN " +
                "(SELECT r.id " +
                "FROM Review AS r " +
                "WHERE r.author.id = :authorId AND r.likes + r.dislikes > 10 AND r.dislikes > r.likes) " +
            "GROUP BY r.author.id")
    double getAuthorAverageMark(@Param("authorId") Long authorId);

    @Query("SELECT COUNT(r.eventId) " +
            "FROM Review AS r " +
            "WHERE r.eventId = :eventId AND r.mark < 6 " +
            "GROUP BY r.eventId")
    int getNumberOfNegativeReviews(@Param("eventId") Long eventId);

    @Query("SELECT COUNT(r.eventId) " +
            "FROM Review AS r " +
            "WHERE r.eventId = :eventId AND r.mark > 5 " +
            "GROUP BY r.eventId")
    int getNumberOfPositiveReviews(@Param("eventId") Long eventId);

    @Query("SELECT r " +
            "FROM Review AS r " +
            "WHERE r.eventId = :eventId AND r.mark < :markLimitation " +
            "ORDER BY r.mark ASC " +
            "LIMIT :limit")
    List<Review> findWorstEvents(@Param("eventId") Long eventId,
                                 @Param("markLimitation") Integer markLimitation,
                                 @Param("limit") Integer limit);

    @Query("SELECT r " +
            "FROM Review AS r " +
            "WHERE r.eventId = :eventId AND r.mark > :markLimitation " +
            "ORDER BY r.mark DESC " +
            "LIMIT :limit")
    List<Review> findBestEvents(@Param("eventId") Long eventId,
                                @Param("markLimitation") Integer markLimitation,
                                @Param("limit") Integer limit);
}
