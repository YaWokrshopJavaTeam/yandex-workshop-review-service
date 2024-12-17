package ru.practicum.workshop.reviewservice.service;

import lombok.RequiredArgsConstructor;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springdoc.webmvc.core.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.workshop.reviewservice.dto.ReviewDto;
import ru.practicum.workshop.reviewservice.dto.analytics.AuthorAverageMark;
import ru.practicum.workshop.reviewservice.dto.analytics.BestAndWorstReviews;
import ru.practicum.workshop.reviewservice.dto.analytics.EventAverageMark;
import ru.practicum.workshop.reviewservice.dto.analytics.EventIndicators;
import ru.practicum.workshop.reviewservice.model.Review;
import ru.practicum.workshop.reviewservice.model.User;
import ru.practicum.workshop.reviewservice.storage.ReviewStorage;
import ru.practicum.workshop.reviewservice.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Transactional
@SpringBootTest
@ActiveProfiles(value = "test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ReviewServiceAnalyticsTest {
    private final ReviewService reviewService;
    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    static MockWebServer server;

    private static Review review1;
    private static Review review2;
    private static Review review3;
    private static Review review4;
    private static Review review5;
    private static Review review6;
    private static Review review7;
    private static Review review8;
    private static Review review9;
    private static Review review10;

    private static User author1;
    private static User author2;
    private static User author3;
    private static Long userId = 0L;

    @Autowired
    private RequestService requestBuilder;

    @BeforeEach
    void beforeEach() {
        author1 = userStorage.save(new User(userId, "user" + userId++));
        author2 = userStorage.save(new User(userId, "user" + (userId++) * 2));
        author3 = userStorage.save(new User(userId, "user" + (userId++) * 3));
        review1 = reviewStorage.save(Review.builder()
                .author(author1)
                .eventId(1L)
                .title("title")
                .content("content")
                .createdOn(LocalDateTime.now())
                .mark(1)
                .build());
        review2 = reviewStorage.save(Review.builder()
                .author(author2)
                .eventId(1L)
                .title("title")
                .content("content")
                .createdOn(LocalDateTime.now())
                .mark(2)
                .build());
        review3 = reviewStorage.save(Review.builder()
                .author(author1)
                .eventId(2L)
                .title("title")
                .content("content")
                .createdOn(LocalDateTime.now())
                .mark(3)
                .build());
        review4 = reviewStorage.save(Review.builder()
                .author(author2)
                .eventId(2L)
                .title("title")
                .content("content")
                .createdOn(LocalDateTime.now())
                .mark(4)
                .build());
        review5 = reviewStorage.save(Review.builder()
                .author(author2)
                .eventId(2L)
                .title("title")
                .content("content")
                .createdOn(LocalDateTime.now())
                .mark(7)
                .build());
        review6 = reviewStorage.save(Review.builder()
                .author(author2)
                .eventId(1L)
                .title("title")
                .content("content")
                .createdOn(LocalDateTime.now())
                .mark(8)
                .build());
        review7 = reviewStorage.save(Review.builder()
                .author(author1)
                .eventId(2L)
                .title("title")
                .content("content")
                .createdOn(LocalDateTime.now())
                .mark(9)
                .build());
        review8 = reviewStorage.save(Review.builder()
                .author(author1)
                .eventId(2L)
                .title("title")
                .content("content")
                .createdOn(LocalDateTime.now())
                .mark(10)
                .build());
        review9 = reviewStorage.save(Review.builder()
                .author(author3)
                .eventId(9L)
                .title("title")
                .content("content")
                .createdOn(LocalDateTime.now())
                .mark(10)
                .build());
        review10 = reviewStorage.save(Review.builder()
                .author(author3)
                .eventId(10L)
                .title("title")
                .content("content")
                .createdOn(LocalDateTime.now())
                .mark(1)
                .build());
    }

    @AfterEach
    public void setup() {
        reviewStorage.deleteAll();
        userStorage.deleteAll();
    }

    @DisplayName("Расчёт средней оценки по событию с учётом выполнения правила включения в расчёт по 3 ревью к событию 1")
    @Rollback
    @Test
    public void getEventAverageMark_2And1() {
        for (long i = 12; i <= 18; i++) {
            reviewService.addLike(review6.getId(), i);
        }
        for (long i = 19; i <= 22; i++) {
            reviewService.addDislike(review6.getId(), i);
        }
        double expectedAverageMark = Math.floor((review1.getMark() + review2.getMark() + review6.getMark()) / 3.0 * 10) / 10;
        EventAverageMark eventAverageMark = reviewService.getEventAverageMark(review6.getEventId());
        assertEquals(expectedAverageMark, eventAverageMark.getAverageMark());
    }

    @DisplayName("Расчёт средней оценки по событию с учётом выполнения правила включения в расчёт по 2 ревью к событию 1")
    @Rollback
    @Test
    public void getEventAverageMark_2() {
        for (long i = 12; i <= 18; i++) {
            reviewService.addDislike(review6.getId(), i);
        }
        for (long i = 19; i <= 22; i++) {
            reviewService.addLike(review6.getId(), i);
        }
        double expectedAverageMark = Math.floor((review1.getMark() + review2.getMark()) / 2.0 * 10) / 10;
        EventAverageMark eventAverageMark = reviewService.getEventAverageMark(review6.getEventId());
        assertEquals(expectedAverageMark, eventAverageMark.getAverageMark());
    }

    @DisplayName("Расчёт средней оценки по событию 1 с учётом не выполнения правила включения в расчёт по всем ревью")
    @Rollback
    @Test
    public void getEventAverageMark_0() {
        for (long i = 12; i <= 18; i++) {
            reviewService.addDislike(review1.getId(), i);
        }
        for (long i = 19; i <= 22; i++) {
            reviewService.addLike(review1.getId(), i);
        }
        for (long i = 12; i <= 18; i++) {
            reviewService.addDislike(review2.getId(), i);
        }
        for (long i = 19; i <= 22; i++) {
            reviewService.addLike(review2.getId(), i);
        }
        for (long i = 12; i <= 18; i++) {
            reviewService.addDislike(review6.getId(), i);
        }
        for (long i = 19; i <= 22; i++) {
            reviewService.addLike(review6.getId(), i);
        }
        EventAverageMark eventAverageMark = reviewService.getEventAverageMark(review6.getEventId());
        assertNull(eventAverageMark.getAverageMark());
    }

    @DisplayName("Расчёт средней оценки по автору с id 1 с учётом выполнения правила включения в расчёт по 4 ревью")
    @Rollback
    @Test
    public void getAuthorAverageMark_3And1() {
        for (long i = 12; i <= 18; i++) {
            reviewService.addLike(review8.getId(), i);
        }
        for (long i = 19; i <= 22; i++) {
            reviewService.addDislike(review8.getId(), i);
        }
        double expectedAverageMark = Math.floor((review1.getMark() + review3.getMark() + review7.getMark() + review8.getMark()) / 4.0 * 10) / 10;
        AuthorAverageMark authorAverageMark = reviewService.getAuthorAverageMark(review8.getAuthor().getId());
        assertEquals(expectedAverageMark, authorAverageMark.getAverageMark());
    }

    @DisplayName("Расчёт средней оценки по автору с id 1 с учётом выполнения правила включения в расчёт только по 3 ревью")
    @Rollback(true)
    @Test
    public void getAuthorAverageMark_3() {
        for (long i = 32; i <= 38; i++) {
            reviewService.addDislike(review8.getId(), i);
        }
        for (long i = 39; i <= 42; i++) {
            reviewService.addLike(review8.getId(), i);
        }
        double expectedAverageMark = Math.floor((review1.getMark() + review3.getMark() + review7.getMark()) / 3.0 * 10) / 10;
        AuthorAverageMark authorAverageMark = reviewService.getAuthorAverageMark(review8.getAuthor().getId());
        assertEquals(expectedAverageMark, authorAverageMark.getAverageMark());
    }

    @DisplayName("Расчёт средней оценки по автору с id 1 с учётом не выполнения правила включения в расчёт по всем ревью")
    @Rollback
    @Test
    public void getAuthorAverageMark_0() {
        for (long i = 102; i <= 122; i++) {
            reviewService.addDislike(review1.getId(), i);
        }
        for (long i = 102; i <= 122; i++) {
            reviewService.addDislike(review3.getId(), i);
        }
        for (long i = 102; i <= 122; i++) {
            reviewService.addDislike(review7.getId(), i);
        }
        for (long i = 102; i <= 122; i++) {
            reviewService.addDislike(review8.getId(), i);
        }
        AuthorAverageMark authorAverageMark = reviewService.getAuthorAverageMark(review8.getAuthor().getId());
        assertNull(authorAverageMark.getAverageMark());
    }

    @DisplayName("Проверка расчёта общего количества и соотношения хороших и плохих отзывов по id события при отсутствии отзывов")
    @Rollback
    @Test
    public void getEventIndicatorsWithoutReviews() {
        Long eventIdWithoutReviews = 100L;
        EventIndicators eventIndicators = reviewService.getEventIndicators(eventIdWithoutReviews);
        assertEquals(eventIdWithoutReviews, eventIndicators.getEventId());
        assertNull(eventIndicators.getNumberOfReviews());
        assertNull(eventIndicators.getPositiveReviewsPercent());
        assertNull(eventIndicators.getNegativeReviewsPercent());
    }

    @DisplayName("Проверка расчёта общего количества и соотношения хороших и плохих отзывов по id события при одном хорошем отзыве")
    @Rollback
    @Test
    public void getEventIndicatorsWith1GoodReview() {
        Long eventIdWith1PositiveReview = 9L;
        Integer numberOfPositiveReviewsForEventId9L = 1;
        Double positiveReviewsPercent = 100.0;
        Double negativeReviewsPercent = 0.0;
        EventIndicators eventIndicators = reviewService.getEventIndicators(eventIdWith1PositiveReview);
        assertEquals(eventIdWith1PositiveReview, eventIndicators.getEventId());
        assertEquals(numberOfPositiveReviewsForEventId9L, eventIndicators.getNumberOfReviews());
        assertEquals(positiveReviewsPercent, eventIndicators.getPositiveReviewsPercent());
        assertEquals(negativeReviewsPercent, eventIndicators.getNegativeReviewsPercent());
    }

    @DisplayName("Проверка расчёта общего количества и соотношения хороших и плохих отзывов по id события при одном негативном отзыве")
    @Rollback
    @Test
    public void getEventIndicatorsWith1NegativeReview() {
        Long eventIdWith1NegativeReview = 10L;
        Integer numberOfNegativeReviewsForEventId10L = 1;
        Double positiveReviewsPercent = 0.0;
        Double negativeReviewsPercent = 100.0;
        EventIndicators eventIndicators = reviewService.getEventIndicators(eventIdWith1NegativeReview);
        assertEquals(eventIdWith1NegativeReview, eventIndicators.getEventId());
        assertEquals(numberOfNegativeReviewsForEventId10L, eventIndicators.getNumberOfReviews());
        assertEquals(positiveReviewsPercent, eventIndicators.getPositiveReviewsPercent());
        assertEquals(negativeReviewsPercent, eventIndicators.getNegativeReviewsPercent());
    }

    @DisplayName("Проверка расчёта общего количества и соотношения хороших и плохих отзывов, когда негативных отзывов больше")
    @Rollback
    @Test
    public void getEventIndicatorsWithMostlyNegativeReviews() {
        Long eventIdWith1NegativeReview = 1L;
        Integer numberOfReviewsForEventId1L = 3;
        Double positiveReviewsPercent = Math.floor((1 * 100.0 / 3) * 10) / 10;
        Double negativeReviewsPercent = Math.floor((2 * 100.0 / 3) * 10) / 10;
        EventIndicators eventIndicators = reviewService.getEventIndicators(eventIdWith1NegativeReview);
        assertEquals(eventIdWith1NegativeReview, eventIndicators.getEventId());
        assertEquals(numberOfReviewsForEventId1L, eventIndicators.getNumberOfReviews());
        assertEquals(positiveReviewsPercent, eventIndicators.getPositiveReviewsPercent());
        assertEquals(negativeReviewsPercent, eventIndicators.getNegativeReviewsPercent());
    }

    @DisplayName("Проверка расчёта общего количества и соотношения хороших и плохих отзывов, когда позитивных отзывов больше")
    @Rollback
    @Test
    public void getEventIndicatorsWithPositiveReviews() {
        Long eventIdWith1NegativeReview = 2L;
        Integer numberOfReviewsForEventId1L = 5;
        Double positiveReviewsPercent = Math.floor((3 * 100.0 / 5) * 10) / 10;
        Double negativeReviewsPercent = Math.floor((2 * 100.0 / 5) * 10) / 10;
        EventIndicators eventIndicators = reviewService.getEventIndicators(eventIdWith1NegativeReview);
        assertEquals(eventIdWith1NegativeReview, eventIndicators.getEventId());
        assertEquals(numberOfReviewsForEventId1L, eventIndicators.getNumberOfReviews());
        assertEquals(positiveReviewsPercent, eventIndicators.getPositiveReviewsPercent());
        assertEquals(negativeReviewsPercent, eventIndicators.getNegativeReviewsPercent());
    }

    @DisplayName("Проверка выдачи трёх лучших, и трёх худших отзывов согласно оценкам отзывов для события без отзывов")
    @Rollback
    @Test
    public void getBestAndWorstReviews() {
        Long eventIdWithoutReviews = 100L;
        BestAndWorstReviews bestAndWorstReviews = reviewService.getBestAndWorstReviews(eventIdWithoutReviews);
        List<ReviewDto> bestReviewsWithoutReviews = List.of();
        List<ReviewDto> worstReviewsWithoutReviews = List.of();
        assertEquals(eventIdWithoutReviews, bestAndWorstReviews.getEventId());
        assertEquals(bestReviewsWithoutReviews, bestAndWorstReviews.getBestReviews());
        assertEquals(worstReviewsWithoutReviews, bestAndWorstReviews.getWorstReviews());
    }

    @DisplayName("Проверка выдачи трёх лучших, и трёх худших отзывов")
    @Rollback
    @Test
    public void getBestAndWorstReviewsForEventId2() {
        Long eventId = 2L;
        BestAndWorstReviews bestAndWorstReviews = reviewService.getBestAndWorstReviews(eventId);
        Integer bestReviewsSize = 3;
        Integer worstReviewsSize = 2;
        assertEquals(eventId, bestAndWorstReviews.getEventId());
        assertEquals(bestReviewsSize, bestAndWorstReviews.getBestReviews().size());
        assertEquals(worstReviewsSize, bestAndWorstReviews.getWorstReviews().size());
    }

    @DisplayName("Проверка выдачи трёх лучших, и трёх худших отзывов при увеличении плохих отзывов на 2")
    @Rollback
    @Test
    public void getBestAndWorstReviewsForEventId2With2AdditionalBadReviews() {
        Long eventId = 2L;
        Review review11 = reviewStorage.save(Review.builder()
                .author(author3)
                .eventId(eventId)
                .title("title")
                .content("content")
                .createdOn(LocalDateTime.now())
                .mark(1)
                .build());
        Review review12 = reviewStorage.save(Review.builder()
                .author(author3)
                .eventId(eventId)
                .title("title")
                .content("content")
                .createdOn(LocalDateTime.now())
                .mark(2)
                .build());
        BestAndWorstReviews bestAndWorstReviews = reviewService.getBestAndWorstReviews(eventId);
        Integer bestReviewsSize = 3;
        Integer worstReviewsSize = 3;
        assertEquals(eventId, bestAndWorstReviews.getEventId());
        assertEquals(bestReviewsSize, bestAndWorstReviews.getBestReviews().size());
        assertEquals(worstReviewsSize, bestAndWorstReviews.getWorstReviews().size());
    }
}
