package ru.practicum.workshop.reviewservice.mapper;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.workshop.reviewservice.dto.ReviewCreateDto;
import ru.practicum.workshop.reviewservice.dto.ReviewDto;
import ru.practicum.workshop.reviewservice.dto.ReviewDtoWithAuthor;
import ru.practicum.workshop.reviewservice.dto.ReviewUpdateDto;
import ru.practicum.workshop.reviewservice.model.Review;
import ru.practicum.workshop.reviewservice.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles(value = "test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ReviewMapperTest {
    private final ReviewMapper mapper;

    private static Review review;
    private static User author;
    private static Long id = 0L;

    @BeforeEach
    void beforeEach() {
        author = new User(id, "user" + id++);
        review = Review.builder()
                .id(id)
                .author(author)
                .eventId(0L)
                .title("title")
                .content("content")
                .createdOn(LocalDateTime.now())
                .mark(1)
                .likes(0L)
                .dislikes(0L)
                .build();
    }

    @DisplayName("Маппинг из модели Review в DTO с authorId")
    @Test
    void toDtoWithAuthor() {
        ReviewDtoWithAuthor expectedDto = new ReviewDtoWithAuthor(review.getId(),
                author.getId(),
                review.getEventId(),
                author.getUsername(),
                review.getTitle(),
                review.getContent(),
                review.getCreatedOn(),
                review.getUpdatedOn(),
                review.getMark(),
                review.getLikes(),
                review.getDislikes());
        ReviewDtoWithAuthor dto = mapper.toDtoWithAuthor(review);

        assertNotNull(dto);
        assertEquals(expectedDto, dto);
    }

    @DisplayName("Маппинг из модели Review в DTO без authorId")
    @Test
    void toDtoWithoutAuthor() {
        ReviewDto expectedDto = new ReviewDto(review.getId(),
                review.getEventId(),
                author.getUsername(),
                review.getTitle(),
                review.getContent(),
                review.getCreatedOn(),
                review.getUpdatedOn(),
                review.getMark(),
                review.getLikes(),
                review.getDislikes());
        ReviewDto dto = mapper.toDtoWithoutAuthor(review);

        assertNotNull(dto);
        assertEquals(expectedDto, dto);
    }

    @DisplayName("Маппинг из DTO при создании в модель Review ")
    @Test
    void toEntityFromCreate() {
        ReviewCreateDto dto = new ReviewCreateDto(author.getId(),
                review.getEventId(),
                author.getUsername(),
                review.getTitle(),
                review.getContent(),
                review.getMark());

        Review resultReview = mapper.toEntity(dto);

        LocalDateTime now = LocalDateTime.now();
        Review expectedReview = Review.builder()
                .id(null)
                .author(author)
                .eventId(dto.getEventId())
                .title(dto.getTitle())
                .content(dto.getContent())
                .createdOn(now)
                .mark(dto.getMark())
                .build();

        assertNotNull(resultReview);
        assertEquals(expectedReview, resultReview);
    }

    @DisplayName("Маппинг из DTO при обновлении в модель Review ")
    @Test
    void toEntityFromUpdate() {
        ReviewUpdateDto dto = new ReviewUpdateDto(
                "other name",
                "other title",
                "other content",
                2);
        Long reviewId = review.getId();
        Long authorId = review.getAuthor().getId();

        Review resultReview = mapper.toEntity(dto, reviewId, authorId);

        LocalDateTime now = LocalDateTime.now();
        Review expectedReview = Review.builder()
                .id(reviewId)
                .author(new User(authorId, dto.getUsername()))
                .title(dto.getTitle())
                .content(dto.getContent())
                .updatedOn(now)
                .mark(dto.getMark())
                .build();

        assertNotNull(resultReview);
        assertEquals(expectedReview, resultReview);
    }
}
