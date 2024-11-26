package ru.practicum.workshop.reviewservice.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.workshop.reviewservice.model.Opinion;

import java.util.Optional;

@Repository
public interface OpinionStorage extends JpaRepository<Opinion, Long> {

    Optional<Opinion> findOneByReview_IdAndEvaluator_Id(long reviewId, long evaluatorId);

}
