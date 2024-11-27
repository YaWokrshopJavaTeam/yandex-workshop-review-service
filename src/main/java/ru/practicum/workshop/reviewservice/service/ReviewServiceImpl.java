package ru.practicum.workshop.reviewservice.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.workshop.reviewservice.dto.Constants;
import ru.practicum.workshop.reviewservice.dto.analytics.EventAverageScore;
import ru.practicum.workshop.reviewservice.enums.Label;
import ru.practicum.workshop.reviewservice.exception.ConflictException;
import ru.practicum.workshop.reviewservice.exception.ForbiddenException;
import ru.practicum.workshop.reviewservice.storage.*;
import ru.practicum.workshop.reviewservice.model.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final OpinionStorage opinionStorage;

    private void saveUser(User user) {
        User newUser = userStorage.save(user);

        log.info("User added: {}", newUser);
    }

    @Transactional
    @Override
    public Review createReview(Review review) {
        saveUser(review.getAuthor());

        Review newReview = reviewStorage.save(review);
        log.info("Review added: {}", newReview);

        return newReview;
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
        if (fromUpdateReview.getMark() != null) toUpdateReview.setMark(fromUpdateReview.getMark());
        toUpdateReview.setUpdatedOn(fromUpdateReview.getUpdatedOn());
    }

    @Transactional
    @Override
    public Review updateReview(Review review) {
        Review toUpdateReview = reviewStorage.findByIdAndAuthorId(review.getId(), review.getAuthor().getId())
                .orElseThrow(() -> {
                    log.error("NOT FOUND. Обновление отзыва. Отзыв с id {} пользователя с id {} не найден.",
                            review.getId(), review.getAuthor().getId());
                    return new EntityNotFoundException(String.format(
                            "Review with id = %d for user with id = %d was not found",
                            review.getId(), review.getAuthor().getId()));
        });
        updateFields(toUpdateReview, review);
        Review updatedReview = reviewStorage.save(toUpdateReview);

        log.info("Review updated: {}", updatedReview);

        return updatedReview;
    }

    @Transactional(readOnly = true)
    @Override
    public Review getReviewById(Long id) {
        Review review = reviewStorage.findById(id).orElseThrow(() -> {
            log.error("NOT FOUND. Получение отзыва по id. Отзыв с id {} не найден.", id);
            return new EntityNotFoundException(String.format("Review with id = %d was not found", id));
        });

        log.info("Review got: id={}", review.getId());

        return review;
    }

    @Transactional(readOnly = true)
    @Override
    public List<Review> getReviewsByEvent(Long eventId, Pageable pageable) {
        List<Review> reviews = reviewStorage.findByEventId(eventId, pageable);
        log.info("Review got by eventId={}: count={}", eventId, reviews.size());
        return reviews;
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
            log.info("Review deleted: id={}", reviewId);
            return reviewId;
        }
    }

    @Transactional
    @Override
    public void addLike(Long reviewId, Long evaluatorId) {
        addOpinion(reviewId, evaluatorId, Label.LIKE);
    }

    @Transactional
    @Override
    public void addDislike(Long reviewId, Long evaluatorId) {
        addOpinion(reviewId, evaluatorId, Label.DISLIKE);
    }

    @Transactional
    @Override
    public void removeLike(Long reviewId, Long evaluatorId) {
        removeOpinion(reviewId, evaluatorId, Label.LIKE);
    }

    @Transactional
    @Override
    public void removeDislike(Long reviewId, Long evaluatorId) {
        removeOpinion(reviewId, evaluatorId, Label.DISLIKE);
    }

    private void addOpinion(Long reviewId, Long evaluatorId, Label label) {
        Review review = getReviewById(reviewId);
        if (review.getAuthor().getId().equals(evaluatorId)) {
            log.error("FORBIDDEN. Пользователь с id {} не может поставить {} своему отзыву с id {}.",
                    evaluatorId, label, reviewId);
            throw new ForbiddenException(String.format("As author of review, you can't put %s to review with id = %d", label, reviewId));
        }
        Optional<Opinion> resultOfOpinionRequest = opinionStorage.findOneByReview_IdAndEvaluatorId(reviewId, evaluatorId);
        if (resultOfOpinionRequest.isPresent()) {
            Opinion opinion = resultOfOpinionRequest.get();
            if (opinion.getLabel().equals(label)) {
                log.error("CONFLICT. Пользователь с id {} уже поставил {} отзыву с id {}, и больше" +
                                " поставить не может.", evaluatorId, label, reviewId);
                throw new ConflictException(String.format("You have already put %s review with id = %d and cannot do it again", label, reviewId));
            } else {
                if (label.equals(Label.LIKE)) {
                    dislikesDecrease(review, evaluatorId);
                } else {
                    likesDecrease(review, evaluatorId);
                }
                opinionStorage.delete(opinion);
            }
        } else {
            Opinion opinion = new Opinion(0L, evaluatorId, review, label);
            if (label.equals(Label.LIKE)) {
                likesIncrease(review, evaluatorId);
            } else {
                dislikesIncrease(review, evaluatorId);
            }
            opinionStorage.save(opinion);
        }
    }

    private void dislikesDecrease(Review review, Long evaluatorId) {
        review.setDislikes(review.getDislikes() - Constants.LIKES_DISLIKES_CHANGE_AT_NEW_OPINION_OR_DELETE_OPINION);
        log.info("Dislike from user with id={} to review with id={} deleted", evaluatorId, review.getId());
        reviewStorage.save(review);
    }

    private void dislikesIncrease(Review review, Long evaluatorId) {
        review.setDislikes(review.getDislikes() + Constants.LIKES_DISLIKES_CHANGE_AT_NEW_OPINION_OR_DELETE_OPINION);
        log.info("Dislike from user with id={} to review with id={} added", evaluatorId, review.getId());
        reviewStorage.save(review);
    }

    private void likesDecrease(Review review, Long evaluatorId) {
        review.setLikes(review.getLikes() - Constants.LIKES_DISLIKES_CHANGE_AT_NEW_OPINION_OR_DELETE_OPINION);
        log.info("Like from user with id={} to review with id={} deleted", evaluatorId, review.getId());
        reviewStorage.save(review);
    }

    private void likesIncrease(Review review, Long evaluatorId) {
        review.setLikes(review.getLikes() + Constants.LIKES_DISLIKES_CHANGE_AT_NEW_OPINION_OR_DELETE_OPINION);
        log.info("Like from user with id={} to review with id={} added", evaluatorId, review.getId());
        reviewStorage.save(review);
    }

    private void removeOpinion(Long reviewId, Long evaluatorId, Label label) {
        Opinion opinion = opinionStorage.findOneByReview_IdAndEvaluatorId(reviewId, evaluatorId).orElseThrow(() -> {
            log.error("NOT FOUND. {} на ревью с id={} от пользователя с id={} не найден. Удаление отклонено.", label, reviewId, evaluatorId);
            return new EntityNotFoundException(String.format("%s to review with id=%d from user with id=%d was not found", label, reviewId, evaluatorId));
        });
        if (!opinion.getLabel().equals(label)) {
            log.error("CONFLICT. Пользователь с id {} поставил {} отзыву с id {}, а удалить предлагает {}." +
                    " Операция не может быть выполнена.", evaluatorId, opinion.getLabel(), reviewId, label);
            throw new ConflictException(String.format("You have put %s to review with id = %d, but want to delete %s. " +
                    " The operation cannot be performed.", opinion.getLabel(), reviewId, label));
        }
        Review review = getReviewById(reviewId);
        if (label.equals(Label.LIKE)) {
            likesDecrease(review, evaluatorId);
        } else {
            dislikesDecrease(review, evaluatorId);
        }
        opinionStorage.delete(opinion);
    }

    public EventAverageScore getEventAverageScore(Long eventId) {
        return reviewStorage.getEventAverageScore(eventId);
    }
}
