package ru.practicum.workshop.reviewservice.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.workshop.reviewservice.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage extends JpaRepository<Review, Long> {
    List<Review> findByEventId(Long eventId, Pageable pageable);

    Optional<Review> findByIdAndAuthorId(Long id, Long authorId);
}