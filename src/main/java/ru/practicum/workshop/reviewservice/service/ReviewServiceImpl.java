package ru.practicum.workshop.reviewservice.service;

import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.workshop.reviewservice.client.EventClient;
import ru.practicum.workshop.reviewservice.client.RegistrationClient;
import ru.practicum.workshop.reviewservice.dto.Constants;
import ru.practicum.workshop.reviewservice.dto.EventResponse;
import ru.practicum.workshop.reviewservice.dto.ReviewDto;
import ru.practicum.workshop.reviewservice.dto.analytics.AuthorAverageMark;
import ru.practicum.workshop.reviewservice.dto.analytics.BestAndWorstReviews;
import ru.practicum.workshop.reviewservice.dto.analytics.EventAverageMark;
import ru.practicum.workshop.reviewservice.dto.analytics.EventIndicators;
import ru.practicum.workshop.reviewservice.enums.Label;
import ru.practicum.workshop.reviewservice.exception.ConflictException;
import ru.practicum.workshop.reviewservice.exception.ForbiddenException;
import ru.practicum.workshop.reviewservice.mapper.ReviewMapper;
import ru.practicum.workshop.reviewservice.storage.*;
import ru.practicum.workshop.reviewservice.model.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static ru.practicum.workshop.reviewservice.dto.Constants.LIMIT_OF_REVIEWS_IN_ISSUE;
import static ru.practicum.workshop.reviewservice.dto.Constants.LIMIT_OF_SUM_LIKES_DISLIKES;
import static ru.practicum.workshop.reviewservice.dto.Constants.MARK_LIMITATION_POSITIVE_REVIEWS;
import static ru.practicum.workshop.reviewservice.dto.Constants.MARK_LIMITATION_NEGATIVE_REVIEWS;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewMapper reviewMapper;
    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final OpinionStorage opinionStorage;
    private final EventClient eventClient;
    private final RegistrationClient registrationClient;

    private void saveUser(User user) {
        User newUser = userStorage.save(user);

        log.info("User added: {}", newUser);
    }

    @Transactional
    @Override
    public Review createReview(Review review) {

        checkEvent(review);

        checkRegistration(review);

        saveUser(review.getAuthor());

        Review newReview = reviewStorage.save(review);
        log.info("Review added: {}", newReview);

        return newReview;
    }

    private void checkEvent(Review review) {
        EventResponse eventResponse;
        try {
            eventResponse = eventClient.readEventById(review.getAuthor().getId(), review.getEventId());
        } catch (FeignException.NotFound e) {
            log.error("CONFLICT. Отзыв к событию с id {} отклонен. Событие не найдено.", review.getEventId());
            throw new ConflictException(String.format("Adding of review for event with id = %d is rejected. " +
                    "Event is not found", review.getEventId()));
        }
        if (eventResponse.getEndDateTime().isAfter(LocalDateTime.now())) {
            log.error("CONFLICT. Публикация отзыва. Событие с id {} не завершено.", eventResponse.getId());
            throw new ConflictException(String.format("The event with id = %d is not completed", eventResponse.getId()));
        }
    }

    private void checkRegistration(Review review) {
        String registrationStatus;
        try {
            registrationStatus = registrationClient.getStatusOfRegistration(review.getEventId(), review.getAuthor().getId());
        } catch (FeignException.NotFound e) {
            log.error("FORBIDDEN. Отзыв к событию с id {} отклонен. Регистрация на событие не найдена.", review.getEventId());
            throw new ForbiddenException(String.format("Adding of review for event with id = %d is rejected. " +
                    "Registration to event is not found", review.getEventId()));
        }
        if (!registrationStatus.equals("APPROVED")) {
            log.error("FORBIDDEN. Публикация отзыва. Регистрация на событие с id {} не подтверждена.", review.getEventId());
            throw new ForbiddenException(String.format("Registration to event with id = %d is not APPROVED", review.getEventId()));
        }
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
            log.error("NOT FOUND. {} на ревью с id={} от пользователя с id={} не найден. Удаление отклонено.", label,
                    reviewId, evaluatorId);
            return new EntityNotFoundException(String.format("%s to review with id=%d from user with id=%d was not found",
                    label, reviewId, evaluatorId));
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

    @Transactional(readOnly = true)
    @Override
    public EventAverageMark getEventAverageMark(Long eventId) {
        Optional<Double> eventAverageMarkInOptional = reviewStorage.getEventAverageMark(eventId, LIMIT_OF_SUM_LIKES_DISLIKES);
        Double eventAverageMark = eventAverageMarkInOptional.map(aDouble -> Math.floor(aDouble * 10) / 10).orElse(null);
        return new EventAverageMark(eventId, eventAverageMark);
    }

    @Transactional(readOnly = true)
    @Override
    public AuthorAverageMark getAuthorAverageMark(Long authorId) {
        Optional<Double> authorAverageMarkInOptional = reviewStorage.getAuthorAverageMark(authorId, LIMIT_OF_SUM_LIKES_DISLIKES);
        Double authorAverageMark = authorAverageMarkInOptional.map(aDouble -> Math.floor(aDouble * 10) / 10).orElse(null);
        return new AuthorAverageMark(authorId, authorAverageMark);
    }

    @Transactional(readOnly = true)
    @Override
    public EventIndicators getEventIndicators(Long eventId) {
        Optional<Integer> numberOfNegativeReviewsInOptional = reviewStorage.getNumberOfNegativeReviews(eventId,
                MARK_LIMITATION_NEGATIVE_REVIEWS);
        Integer numberOfNegativeReviews = numberOfNegativeReviewsInOptional.orElse(null);
        Optional<Integer> numberOfPositiveReviewsInOptional = reviewStorage.getNumberOfPositiveReviews(eventId,
                MARK_LIMITATION_POSITIVE_REVIEWS);
        Integer numberOfPositiveReviews = numberOfPositiveReviewsInOptional.orElse(null);
        if (numberOfNegativeReviews == null && numberOfPositiveReviews == null) {
            return new EventIndicators(eventId, null, null, null);
        } else if (numberOfNegativeReviews != null && numberOfPositiveReviews == null) {
            return new EventIndicators(eventId, numberOfNegativeReviews, 0.0, 100.0);
        } else if (numberOfNegativeReviews == null) {
            return new EventIndicators(eventId, numberOfPositiveReviews, 100.0, 0.0);
        } else {
            int numberOfReviews = numberOfNegativeReviews + numberOfPositiveReviews;
            Double positiveReviewsPercent = Math.floor((numberOfPositiveReviews * 100.0 / numberOfReviews) * 10) / 10;
            Double negativeReviewsPercent = Math.floor((numberOfNegativeReviews * 100.0 / numberOfReviews) * 10) / 10;
            return new EventIndicators(eventId, numberOfReviews, positiveReviewsPercent, negativeReviewsPercent);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public BestAndWorstReviews getBestAndWorstReviews(Long eventId) {
        List<ReviewDto> bestReviews = reviewStorage.findBestEvents(eventId, MARK_LIMITATION_POSITIVE_REVIEWS,
                        LIMIT_OF_REVIEWS_IN_ISSUE)
                .stream()
                .map(reviewMapper::toDtoWithoutAuthor)
                .toList();
        List<ReviewDto> worstReviews = reviewStorage.findWorstEvents(eventId, MARK_LIMITATION_NEGATIVE_REVIEWS,
                        LIMIT_OF_REVIEWS_IN_ISSUE)
                .stream()
                .map(reviewMapper::toDtoWithoutAuthor)
                .toList();
        return new BestAndWorstReviews(eventId, bestReviews, worstReviews);
    }
}
