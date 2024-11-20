package ru.practicum.workshop.reviewservice.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.workshop.reviewservice.exception.*;
import ru.practicum.workshop.reviewservice.model.*;
import ru.practicum.workshop.reviewservice.storage.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_= @Autowired)
public class ReviewServiceImplTest {
    private final ReviewService reviewService;
    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;

    private static Review review;
    private static User author;
    private static Long userId = 0L;

    @BeforeEach
    void beforeEach() {
        author = userStorage.save(new User(userId, "user" + userId++));
        review = reviewService.createReview(Review.builder()
                .author(author)
                .eventId(0L)
                .title("title")
                .content("content")
                .createdOn(LocalDateTime.now())
                .mark(0L)
                .build());
    }

    @DisplayName("Создать отзыв")
    @Test
    void createReview() {
        review = reviewStorage.save(Review.builder()
                .author(author)
                .eventId(0L)
                .title("title")
                .content("content")
                .createdOn(LocalDateTime.now())
                .mark(0L)
                .build());

        assertNotNull(review);
        assertNotNull(review.getId());
    }

    @DisplayName("Обновить отзыв")
    @Test
    void updateReview() {
        User authorWithOtherName = new User(author.getId(), "other name");
        Review updatedReview = Review.builder()
                .id(review.getId())
                .author(authorWithOtherName)
                .title("other title")
                .content("other content")
                .updatedOn(LocalDateTime.now())
                .build();
        Review expectedReview = review.toBuilder()
                .author(updatedReview.getAuthor())
                .title(updatedReview.getTitle())
                .content(updatedReview.getContent())
                .updatedOn(updatedReview.getUpdatedOn())
                .build();
        Review savedReview = reviewService.updateReview(updatedReview);
        assertNotNull(savedReview);
        assertNotNull(savedReview.getAuthor());
        assertEquals(expectedReview, savedReview);
        assertEquals(authorWithOtherName, savedReview.getAuthor());
    }

    @DisplayName("Обновить только имя пользователя при обновлении отзыва")
    @Test
    void updateOnlyReviewUsername() {
        User authorWithOtherName = new User(author.getId(), "other name");
        Review updatedReview = Review.builder()
                .id(review.getId())
                .author(authorWithOtherName)
                .updatedOn(LocalDateTime.now())
                .build();
        Review expectedReview = review.toBuilder()
                .author(updatedReview.getAuthor())
                .updatedOn(updatedReview.getUpdatedOn())
                .build();
        Review savedReview = reviewService.updateReview(updatedReview);
        assertNotNull(savedReview);
        assertNotNull(savedReview.getAuthor());
        assertEquals(expectedReview, savedReview);
        assertEquals(authorWithOtherName, savedReview.getAuthor());
    }

    @DisplayName("Обновить только заголовок отзыва")
    @Test
    void updateOnlyReviewTitle() {
        Review updatedReview = Review.builder()
                .id(review.getId())
                .author(review.getAuthor())
                .title("other title")
                .updatedOn(LocalDateTime.now())
                .build();
        Review expectedReview = review.toBuilder()
                .title(updatedReview.getTitle())
                .updatedOn(updatedReview.getUpdatedOn())
                .build();
        Review savedReview = reviewService.updateReview(updatedReview);
        assertNotNull(savedReview);
        assertEquals(expectedReview, savedReview);
    }

    @DisplayName("Обновить только содержание отзыва")
    @Test
    void updateOnlyReviewContent() {
        Review updatedReview = Review.builder()
                .id(review.getId())
                .author(review.getAuthor())
                .content("other content")
                .updatedOn(LocalDateTime.now())
                .build();
        Review expectedReview = review.toBuilder()
                .content(updatedReview.getContent())
                .updatedOn(updatedReview.getUpdatedOn())
                .build();
        Review savedReview = reviewService.updateReview(updatedReview);
        assertNotNull(savedReview);
        assertEquals(expectedReview, savedReview);
    }

    @DisplayName("Ошибка Not Found при обновлении отзыва")
    @Test
    void shouldThrowNotFoundWhenUpdate() {
        User nonExistentAuthor = new User(Long.MAX_VALUE, "other name");
        Review updatedReview = Review.builder()
                .id(Long.MAX_VALUE)
                .author(nonExistentAuthor)
                .title("other title")
                .content("other content")
                .updatedOn(LocalDateTime.now())
                .build();

        final Exception exception = assertThrows(EntityNotFoundException.class, () -> {
            reviewService.updateReview(updatedReview);
        });
        final String expectedMessage = String.format("Review with id = %d for user with id = %d was not found",
                updatedReview.getId(), nonExistentAuthor.getId());
        final String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @DisplayName("Получение отзыва по id")
    @Test
    void getReviewById() {
        Review savedReview = reviewService.getReviewById(review.getId());

        assertNotNull(review);
        assertNotNull(savedReview.getAuthor());
        assertEquals(review, savedReview);
        assertEquals(review.getAuthor(), savedReview.getAuthor());
    }

    @DisplayName("Ошибка Not Found при получении отзыва по id")
    @Test
    void shouldThrowNotFoundWhenGet() {
        final long id = Long.MAX_VALUE;
        final Exception exception = assertThrows(EntityNotFoundException.class, () -> {
            reviewService.getReviewById(id);
        });
        final String expectedMessage = String.format("Review with id = %d was not found", id);
        final String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    private Review createNewReview(Long eventId) {
        return reviewService.createReview(Review.builder()
                .author(author)
                .eventId(eventId)
                .title("title")
                .content("content")
                .createdOn(LocalDateTime.now())
                .mark(0L)
                .build());
    }

    @DisplayName("Получение отзывов по id события")
    @Test
    void getReviewsByEvent() {
        Long eventId = userId + 1;
        Review review1 = createNewReview(eventId);
        Review review2 = createNewReview(eventId);
        Review review3 = createNewReview(eventId);
        List<Review> expectedList = List.of(review1, review2, review3);

        Sort sort = Sort.by(Sort.Direction.DESC, "createdOn");
        List<Review> savedReviews = reviewService.getReviewsByEvent(eventId, PageRequest.of(0, 5, sort));

        assertNotNull(savedReviews);
        assertFalse(savedReviews.isEmpty());
        assertEquals(savedReviews.size(), expectedList.size());
        assertTrue(savedReviews.containsAll(expectedList));
    }

    @DisplayName("Удаление отзыва по id")
    @Test
    void deleteReview() {
        Long reviewId = reviewService.deleteReview(review.getId(), review.getAuthor().getId());
        assertNotNull(reviewId);
        assertEquals(review.getId(), reviewId);
    }

    @DisplayName("Ошибка Not Found при удалении отзыва по id")
    @Test
    void shouldThrowNotFoundWhenDelete() {
        final long id = Long.MAX_VALUE;
        final Exception exception = assertThrows(EntityNotFoundException.class, () -> {
            reviewService.deleteReview(id, review.getAuthor().getId());
        });
        final String expectedMessage = String.format("Review with id = %d was not found", id);
        final String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @DisplayName("Ошибка Forbidden при удалении отзыва по id")
    @Test
    void shouldThrowForbiddenWhenDelete() {
        final long authorId = Long.MAX_VALUE;
        final Exception exception = assertThrows(ForbiddenException.class, () -> {
            reviewService.deleteReview(review.getId(), authorId);
        });
        final String expectedMessage = String.format("You don't have access to review with id = %d", review.getId());
        final String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }
}