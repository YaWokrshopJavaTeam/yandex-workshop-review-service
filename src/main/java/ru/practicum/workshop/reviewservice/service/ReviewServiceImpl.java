package ru.practicum.workshop.reviewservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.workshop.reviewservice.exception.ForbiddenException;
import ru.practicum.workshop.reviewservice.exception.NotFoundException;
import ru.practicum.workshop.reviewservice.storage.*;
import ru.practicum.workshop.reviewservice.model.*;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;

    private void saveUser(User user) {
        userStorage.save(user);
    }

    @Override
    public Review createReview(Review review) {
        saveUser(review.getAuthor());
        return reviewStorage.save(review);
    }

    private void updateFields(Review toUpdateReview, Review fromUpdateReview) {
        User author = fromUpdateReview.getAuthor();
        if (author.getUsername() != null
                && !author.getUsername().equals(toUpdateReview.getAuthor().getUsername())) {
            toUpdateReview.getAuthor().setUsername(author.getUsername());
            saveUser(author);
        }
        if (fromUpdateReview.getTitle() != null) toUpdateReview.setTitle(fromUpdateReview.getTitle());
        if (fromUpdateReview.getContent() != null) toUpdateReview.setContent(fromUpdateReview.getContent());
        toUpdateReview.setUpdatedOn(fromUpdateReview.getUpdatedOn());
    }

    @Override
    public Review updateReview(Review review) {
        Review toUpdateReview = reviewStorage.findByIdAndAuthorId(review.getId(), review.getAuthor().getId())
                .orElseThrow(() ->{
                    log.error("NOT FOUND. Обновление отзыва. Отзыв с id {} пользователя с id {} не найден.",
                            review.getId(), review.getAuthor().getId());
                    return new NotFoundException(String.format(
                            "Review with id = %d for user with id = %d was not found",
                            review.getId(), review.getAuthor().getId()));
        });
        updateFields(toUpdateReview, review);
        return reviewStorage.save(toUpdateReview);
    }

    @Transactional(readOnly = true)
    @Override
    public Review getReviewById(Long id) {
        return reviewStorage.findById(id).orElseThrow(() ->{
            log.error("NOT FOUND. Получение отзыва по id. Отзыв с id {} не найден.", id);
            return new NotFoundException(String.format("Review with id = %d was not found", id));
        });
    }

    @Transactional(readOnly = true)
    @Override
    public List<Review> getReviewsByEvent(Long eventId, Pageable pageable) {
        return reviewStorage.findByEventId(eventId, pageable);
    }

    @Override
    public Long deleteReview(Long reviewId, Long authorId) {
        Review review = getReviewById(reviewId);
        if (!review.getAuthor().getId().equals(authorId)) {
            log.error("FORBIDDEN. Удаление отзыва. Пользователь с id {} не имеет доступа к отзыву с id {}.",
                    authorId, reviewId);
            throw new ForbiddenException(String.format("You don't have access to review with id = %d", reviewId));
        } else {
            reviewStorage.deleteById(reviewId);
            return reviewId;
        }
    }
}
