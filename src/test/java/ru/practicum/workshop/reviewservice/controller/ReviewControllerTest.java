package ru.practicum.workshop.reviewservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.practicum.workshop.reviewservice.dto.ReviewCreateDto;
import ru.practicum.workshop.reviewservice.dto.ReviewDto;
import ru.practicum.workshop.reviewservice.dto.ReviewDtoWithAuthor;
import ru.practicum.workshop.reviewservice.dto.ReviewUpdateDto;
import ru.practicum.workshop.reviewservice.mapper.ReviewMapper;
import ru.practicum.workshop.reviewservice.mapper.ReviewMapperImpl;
import ru.practicum.workshop.reviewservice.model.Review;
import ru.practicum.workshop.reviewservice.service.ReviewService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.workshop.reviewservice.dto.ReviewDtoValidationConstants.*;

@WebMvcTest(controllers = ReviewController.class)
@RequiredArgsConstructor(onConstructor_= @Autowired)
@Import(ReviewMapperImpl.class)
class ReviewControllerTest {
    private final ObjectMapper mapper;
    private final MockMvc mvc;

    @MockBean
    private final ReviewService reviewService;

    private static ReviewMapper reviewMapper;

    private ReviewCreateDto createDto;
    private ReviewDtoWithAuthor dtoWithAuthorId;
    private ReviewUpdateDto updateDto;
    private ReviewDto dto;
    private Review review;
    private MockHttpServletResponse response;
    private MvcResult result;
    private Long id = 0L;

    @BeforeAll
    static void beforeAll() {
        reviewMapper = new ReviewMapperImpl();
    }

    private ReviewCreateDto createCreationDto() {
        return new ReviewCreateDto(++id, id, "user", "title", "content", 1);
    }

    private MockHttpServletResponse createReviewResponse(ReviewCreateDto dto) throws Exception {
        result = mvc.perform(post("/reviews")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        return result.getResponse();
    }

    @DisplayName("Добавить отзыв")
    @Test
    void createReview() throws Exception {
        createDto = createCreationDto();
        review = reviewMapper.toEntity(createDto);
        dtoWithAuthorId = reviewMapper.toDtoWithAuthor(review);

        when(reviewService.createReview(any(Review.class)))
                .thenReturn(review);

        response = createReviewResponse(createDto);

        assertEquals(201, response.getStatus());
        assertEquals(mapper.writeValueAsString(dtoWithAuthorId), response.getContentAsString());

        verify(reviewService, times(1))
                .createReview(any(Review.class));
        verifyNoMoreInteractions(reviewService);
    }

    @DisplayName("Валидация ReviewCreateDto null authorId")
    @Test
    void shouldThrowBadRequestWhenCreateReviewNullAuthorId() throws Exception {
        createDto = new ReviewCreateDto(null, ++id, "user", "title", "content", 1);

        response = createReviewResponse(createDto);

        String responseContent = response.getContentAsString();
        assertEquals(400, response.getStatus());
        assertTrue(responseContent.contains(AUTHOR_ID_NOT_NULL_ERROR_MESSAGE));

        verifyNoInteractions(reviewService);
    }

    @DisplayName("Валидация ReviewCreateDto authorId @Positive")
    @Test
    void shouldThrowBadRequestWhenCreateReviewIncorrectAuthorId() throws Exception {
        createDto = new ReviewCreateDto(0L, ++id, "user", "title", "content", 1);

        response = createReviewResponse(createDto);

        String responseContent = response.getContentAsString();
        assertEquals(400, response.getStatus());
        assertTrue(responseContent.contains(AUTHOR_ID_POSITIVE_ERROR_MESSAGE));

        verifyNoInteractions(reviewService);
    }

    @DisplayName("Валидация ReviewCreateDto null eventId")
    @Test
    void shouldThrowBadRequestWhenCreateReviewNullEventId() throws Exception {
        createDto = new ReviewCreateDto(++id, null, "user", "title", "content", 1);

        response = createReviewResponse(createDto);

        String responseContent = response.getContentAsString();
        assertEquals(400, response.getStatus());
        assertTrue(responseContent.contains(EVENT_ID_NOT_NULL_ERROR_MESSAGE));

        verifyNoInteractions(reviewService);
    }

    @DisplayName("Валидация ReviewCreateDto eventId @Positive")
    @Test
    void shouldThrowBadRequestWhenCreateReviewIncorrectEventId() throws Exception {
        createDto = new ReviewCreateDto(++id, 0L, "user", "title", "content", 1);

        response = createReviewResponse(createDto);

        String responseContent = response.getContentAsString();
        assertEquals(400, response.getStatus());
        assertTrue(responseContent.contains(EVENT_ID_POSITIVE_ERROR_MESSAGE));

        verifyNoInteractions(reviewService);
    }

    @DisplayName("Валидация ReviewCreateDto username @NotBlank")
    @Test
    void shouldThrowBadRequestWhenCreateReviewBlankUsername() throws Exception {
        createDto = new ReviewCreateDto(++id, id, " ".repeat(USERNAME_MIN_SIZE + 1), "title", "content", 1);

        response = createReviewResponse(createDto);

        String responseBlankContent = response.getContentAsString();
        assertEquals(400, response.getStatus());
        assertTrue(responseBlankContent.contains(USERNAME_NOT_BLANK_ERROR_MESSAGE));

        ReviewCreateDto nullUsernameDto = new ReviewCreateDto(++id, id, null, "title", "content", 1);
        MockHttpServletResponse nullResponse = createReviewResponse(nullUsernameDto);
        String responseNullContent = nullResponse.getContentAsString();

        assertEquals(400, nullResponse.getStatus());
        assertTrue(responseNullContent.contains(USERNAME_NOT_BLANK_ERROR_MESSAGE));

        verifyNoInteractions(reviewService);
    }

    @DisplayName("Валидация ReviewCreateDto username @Size")
    @Test
    void shouldThrowBadRequestWhenCreateReviewIncorrectUsername() throws Exception {
        createDto = new ReviewCreateDto(++id, id, "i".repeat(USERNAME_MIN_SIZE - 1),
                "title", "content", 1);

        response = createReviewResponse(createDto);

        String responseBlankContent = response.getContentAsString();
        assertEquals(400, response.getStatus());
        assertTrue(responseBlankContent.contains(USERNAME_SIZE_ERROR_MESSAGE));

        ReviewCreateDto maxUsernameDto = new ReviewCreateDto(++id, id, "i".repeat(USERNAME_MAX_SIZE + 1),
                "title", "content", 1);
        MockHttpServletResponse maxResponse = createReviewResponse(maxUsernameDto);
        String responseMaxContent = maxResponse.getContentAsString();

        assertEquals(400, maxResponse.getStatus());
        assertTrue(responseMaxContent.contains(USERNAME_SIZE_ERROR_MESSAGE));

        verifyNoInteractions(reviewService);
    }

    @DisplayName("Валидация ReviewCreateDto title @Size")
    @Test
    void shouldThrowBadRequestWhenCreateReviewIncorrectTitle() throws Exception {
        createDto = new ReviewCreateDto(++id,
                id,
                "user",
                "i".repeat(TITLE_MAX_SIZE + 1),
                "content",
                1);

        response = createReviewResponse(createDto);

        String responseBlankContent = response.getContentAsString();
        assertEquals(400, response.getStatus());
        assertTrue(responseBlankContent.contains(TITLE_SIZE_ERROR_MESSAGE));

        verifyNoInteractions(reviewService);
    }

    @DisplayName("Валидация ReviewCreateDto content @NotBlank")
    @Test
    void shouldThrowBadRequestWhenCreateReviewBlankContent() throws Exception {
        createDto = new ReviewCreateDto(++id, id, "user", "title",
                " ".repeat(CONTENT_MIN_SIZE + 1), 1);

        response = createReviewResponse(createDto);

        String responseBlankContent = response.getContentAsString();
        assertEquals(400, response.getStatus());
        assertTrue(responseBlankContent.contains(CONTENT_NOT_BLANK_ERROR_MESSAGE));

        ReviewCreateDto nullContentDto = new ReviewCreateDto(++id,
                id, "user", "title", null, 1);
        MockHttpServletResponse nullResponse = createReviewResponse(nullContentDto);
        String responseNullContent = nullResponse.getContentAsString();

        assertEquals(400, nullResponse.getStatus());
        assertTrue(responseNullContent.contains(CONTENT_NOT_BLANK_ERROR_MESSAGE));

        verifyNoInteractions(reviewService);
    }

    @DisplayName("Валидация ReviewCreateDto content @Size")
    @Test
    void shouldThrowBadRequestWhenCreateReviewIncorrectContent() throws Exception {
        createDto = new ReviewCreateDto(++id, id, "user", "title",
                "c".repeat(CONTENT_MIN_SIZE - 1), 1);

        response = createReviewResponse(createDto);

        String responseMinContent = response.getContentAsString();
        assertEquals(400, response.getStatus());
        assertTrue(responseMinContent.contains(CONTENT_SIZE_ERROR_MESSAGE));

        ReviewCreateDto maxUsernameDto = new ReviewCreateDto(++id, id, "user", "title",
                "c".repeat(CONTENT_MAX_SIZE + 1), 1);
        MockHttpServletResponse MaxResponse = createReviewResponse(maxUsernameDto);
        String responseMaxContent = MaxResponse.getContentAsString();

        assertEquals(400, MaxResponse.getStatus());
        assertTrue(responseMaxContent.contains(CONTENT_SIZE_ERROR_MESSAGE));

        verifyNoInteractions(reviewService);
    }

    @DisplayName("Валидация ReviewCreateDto mark @NotNull")
    @Test
    void shouldThrowBadRequest_WhenCreateReviewNullMark() throws Exception {
        createDto = new ReviewCreateDto(++id, id, "user", "title",
                "content", null);

        response = createReviewResponse(createDto);

        String responseContent = response.getContentAsString();
        assertEquals(400, response.getStatus());
        assertTrue(responseContent.contains(MARK_NOT_NULL_ERROR_MESSAGE));

        verifyNoInteractions(reviewService);
    }

    @DisplayName("Валидация ReviewCreateDto mark @Min и @Max")
    @Test
    void shouldThrowBadRequest_WhenCreateReviewIncorrectMark() throws Exception {
        createDto = new ReviewCreateDto(++id, id, "user", "title",
                "content", MARK_MIN_VALUE - 1);

        response = createReviewResponse(createDto);

        String responseMinContent = response.getContentAsString();
        assertEquals(400, response.getStatus());
        assertTrue(responseMinContent.contains(MARK_MIN_ERROR_MESSAGE));

        ReviewCreateDto maxUsernameDto = new ReviewCreateDto(++id, id, "user", "title",
                "content", MARK_MAX_VALUE + 1);
        MockHttpServletResponse MaxResponse = createReviewResponse(maxUsernameDto);
        String responseMaxContent = MaxResponse.getContentAsString();

        assertEquals(400, MaxResponse.getStatus());
        assertTrue(responseMaxContent.contains(MARK_MAX_ERROR_MESSAGE));

        verifyNoInteractions(reviewService);
    }

    private ReviewUpdateDto createUpdateDto() {
        return new ReviewUpdateDto("other name", "other title", "other content", 1);
    }

    private MockHttpServletResponse updateReviewResponse(ReviewUpdateDto dto, Long reviewId, Long authorId) throws Exception {
        result = mvc.perform(patch("/reviews/" + reviewId)
                        .content(mapper.writeValueAsString(dto))
                        .header("X-Review-User-Id", authorId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        return result.getResponse();
    }

    @DisplayName("Обновить отзыв")
    @Test
    void updateReview() throws Exception {
        updateDto = createUpdateDto();
        final Long reviewId = ++id;
        final Long authorId = id++;
        review = reviewMapper.toEntity(updateDto, reviewId, authorId);
        dtoWithAuthorId = reviewMapper.toDtoWithAuthor(review);

        when(reviewService.updateReview(any(Review.class)))
                .thenReturn(review);

        response = updateReviewResponse(updateDto, reviewId, authorId);

        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(dtoWithAuthorId), response.getContentAsString());

        verify(reviewService, times(1))
                .updateReview(any(Review.class));
        verifyNoMoreInteractions(reviewService);
    }

    @DisplayName("Валидация обновления отзыва - reviewId @Positive")
    @Test
    void shouldThrowBadRequestWhenUpdateReviewIncorrectReviewId() throws Exception {
        updateDto = createUpdateDto();
        final Long reviewId = 0L;
        final Long authorId = ++id;

        response = updateReviewResponse(updateDto, reviewId, authorId);

        String responseContent = response.getContentAsString();
        assertEquals(400, response.getStatus());
        assertTrue(responseContent.contains("Review's id should be positive"));

        verifyNoInteractions(reviewService);
    }

    @DisplayName("Валидация обновления отзыва - остутствие header")
    @Test
    void shouldThrowBadRequestWhenUpdateReviewNullAuthorId() throws Exception {
        updateDto = createUpdateDto();
        final Long reviewId = ++id;

        response = mvc.perform(patch("/reviews/" + reviewId)
                        .content(mapper.writeValueAsString(updateDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        String responseContent = response.getContentAsString();
        assertEquals(400, response.getStatus());
        assertTrue(responseContent.contains("Required request header "));
        assertTrue(responseContent.contains("is not present"));

        verifyNoInteractions(reviewService);
    }

    @DisplayName("Валидация обновления отзыва - authorId @Positive")
    @Test
    void shouldThrowBadRequestWhenUpdateReviewIncorrectAuthorId() throws Exception {
        updateDto = createUpdateDto();
        final Long reviewId = ++id;
        final Long authorId = 0L;

        response = updateReviewResponse(updateDto, reviewId, authorId);

        String responseContent = response.getContentAsString();
        assertEquals(400, response.getStatus());
        assertTrue(responseContent.contains("User's id should be positive"));

        verifyNoInteractions(reviewService);
    }

    @DisplayName("Валидация ReviewUpdateDto - username @Size")
    @Test
    void shouldThrowBadRequestWhenUpdateReviewIncorrectUsername() throws Exception {
        updateDto = new ReviewUpdateDto("n".repeat(USERNAME_MIN_SIZE - 1), "other title",
                "content", 1);
        final Long reviewId = ++id;
        final Long authorId = id;

        response = updateReviewResponse(updateDto, reviewId, authorId);

        String responseMinContent = response.getContentAsString();
        assertEquals(400, response.getStatus());
        assertTrue(responseMinContent.contains(USERNAME_SIZE_ERROR_MESSAGE));

        ReviewUpdateDto maxUsernameDto = new ReviewUpdateDto("n".repeat(USERNAME_MAX_SIZE + 1),
                "other title", "content", 1);
        MockHttpServletResponse MaxResponse = updateReviewResponse(maxUsernameDto, reviewId, authorId);;
        String responseMaxContent = MaxResponse.getContentAsString();

        assertEquals(400, MaxResponse.getStatus());
        assertTrue(responseMaxContent.contains(USERNAME_SIZE_ERROR_MESSAGE));

        verifyNoInteractions(reviewService);
    }

    @DisplayName("Валидация ReviewUpdateDto - title @Size")
    @Test
    void shouldThrowBadRequestWhenUpdateReviewIncorrectTitle() throws Exception {
        updateDto = new ReviewUpdateDto("other name",
                "t".repeat(TITLE_MAX_SIZE + 1), "content", 1);
        final Long reviewId = ++id;
        final Long authorId = id;

        response = updateReviewResponse(updateDto, reviewId, authorId);

        String responseMinContent = response.getContentAsString();
        assertEquals(400, response.getStatus());
        assertTrue(responseMinContent.contains(TITLE_SIZE_ERROR_MESSAGE));

        verifyNoInteractions(reviewService);
    }

    @DisplayName("Валидация ReviewUpdateDto - content @Size")
    @Test
    void shouldThrowBadRequestWhenUpdateReviewIncorrectContent() throws Exception {
        updateDto = new ReviewUpdateDto("other name", "other title",
                "c".repeat(CONTENT_MIN_SIZE - 1), 1);
        final Long reviewId = ++id;
        final Long authorId = id;

        response = updateReviewResponse(updateDto, reviewId, authorId);

        String responseMinContent = response.getContentAsString();
        assertEquals(400, response.getStatus());
        assertTrue(responseMinContent.contains(CONTENT_SIZE_ERROR_MESSAGE));

        ReviewUpdateDto maxContentDto = new ReviewUpdateDto("other name", "other title",
                "c".repeat(CONTENT_MAX_SIZE + 1), 1);
        MockHttpServletResponse MaxResponse = updateReviewResponse(maxContentDto, reviewId, authorId);;
        String responseMaxContent = MaxResponse.getContentAsString();

        assertEquals(400, MaxResponse.getStatus());
        assertTrue(responseMaxContent.contains(CONTENT_SIZE_ERROR_MESSAGE));

        verifyNoInteractions(reviewService);
    }

    @DisplayName("Валидация ReviewUpdateDto - mark @Min и @Max")
    @Test
    void shouldThrowBadRequest_WhenUpdateReviewIncorrectMark() throws Exception {
        updateDto = new ReviewUpdateDto("user", "title",
                "content", MARK_MIN_VALUE - 1);
        final Long reviewId = ++id;
        final Long authorId = id;

        response = updateReviewResponse(updateDto, reviewId, authorId);

        String responseMinContent = response.getContentAsString();
        assertEquals(400, response.getStatus());
        assertTrue(responseMinContent.contains(MARK_MIN_ERROR_MESSAGE));

        ReviewUpdateDto maxMarkDto = new ReviewUpdateDto("user", "title",
                "content", MARK_MAX_VALUE + 1);
        MockHttpServletResponse MaxResponse = updateReviewResponse(maxMarkDto, reviewId, authorId);
        String responseMaxContent = MaxResponse.getContentAsString();

        assertEquals(400, MaxResponse.getStatus());
        assertTrue(responseMaxContent.contains(MARK_MAX_ERROR_MESSAGE));

        verifyNoInteractions(reviewService);
    }

    private MockHttpServletResponse getReviewResponse(Long id) throws Exception {
        result = mvc.perform(get("/reviews/" + id)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        return result.getResponse();
    }

    @DisplayName("Получить отзыв по id")
    @Test
    void getReviewById() throws Exception {
        createDto = createCreationDto();
        review = reviewMapper.toEntity(createDto);
        dto = reviewMapper.toDtoWithoutAuthor(review);

        when(reviewService.getReviewById(any(Long.class)))
                .thenReturn(review);

        response = getReviewResponse(++id);

        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(dto), response.getContentAsString());

        verify(reviewService, times(1))
                .getReviewById(any(Long.class));
        verifyNoMoreInteractions(reviewService);
    }

    private MockHttpServletResponse getReviewsByEventResponse(Long id, int page, int size) throws Exception {
        result = mvc.perform(get("/reviews")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .param("eventId", String.valueOf(id))
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        return result.getResponse();
    }

    @DisplayName("Получить отзывы по id события")
    @Test
    void getReviewsByEventId() throws Exception {
        createDto = createCreationDto();
        review = reviewMapper.toEntity(createDto);
        dto = reviewMapper.toDtoWithoutAuthor(review);

        when(reviewService.getReviewsByEvent(any(Long.class), any(Pageable.class)))
                .thenReturn(List.of(review));

        response = getReviewsByEventResponse(review.getEventId(), 0, 5);

        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(List.of(dto)), response.getContentAsString());

        verify(reviewService, times(1))
                .getReviewsByEvent(any(Long.class), any(Pageable.class));
        verifyNoMoreInteractions(reviewService);
    }

    private MockHttpServletResponse deleteReviewResponse(Long reviewId, Long authorId) throws Exception {
        result = mvc.perform(delete("/reviews/" + reviewId)
                        .header("X-Review-User-Id", authorId)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        return result.getResponse();
    }

    @DisplayName("Удалить отзыв по id")
    @Test
    void deleteReviewById() throws Exception {
        when(reviewService.deleteReview(any(Long.class), any(Long.class)))
                .thenReturn(++id);

        response = deleteReviewResponse(id, id);

        assertEquals(204, response.getStatus());
        assertEquals(mapper.writeValueAsString(id), response.getContentAsString());

        verify(reviewService, times(1))
                .deleteReview(any(Long.class), any(Long.class));
        verifyNoMoreInteractions(reviewService);
    }

    @DisplayName("Добавить лайк по id ревью и id пользователя")
    @Test
    void putLikeByReviewIdAndEvaluatorId() throws Exception {
        mvc.perform(put("/reviews/{id}/like", 1)
                        .header("X-Review-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @DisplayName("Добавить дизлайк по id ревью и id пользователя")
    @Test
    void putDislikeByReviewIdAndEvaluatorId() throws Exception {
        mvc.perform(put("/reviews/{id}/dislike", 1)
                        .header("X-Review-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @DisplayName("Удалить лайк по id ревью и id пользователя")
    @Test
    void deleteLikeByReviewIdAndEvaluatorId() throws Exception {
        mvc.perform(delete("/reviews/{id}/like", 1)
                        .header("X-Review-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @DisplayName("Удалить дизлайк по id ревью и id пользователя")
    @Test
    void deleteDislikeByReviewIdAndEvaluatorId() throws Exception {
        mvc.perform(delete("/reviews/{id}/dislike", 1)
                        .header("X-Review-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}