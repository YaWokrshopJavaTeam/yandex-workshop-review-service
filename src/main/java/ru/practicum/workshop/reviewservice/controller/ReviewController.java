package ru.practicum.workshop.reviewservice.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.workshop.reviewservice.dto.*;
import ru.practicum.workshop.reviewservice.mapper.ReviewMapper;
import ru.practicum.workshop.reviewservice.model.Review;
import ru.practicum.workshop.reviewservice.service.ReviewService;

import java.util.List;
import java.util.stream.Collectors;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/reviews")
public class ReviewController {
    private final ReviewService reviewService;
    private final ReviewMapper reviewMapper;

    @ResponseStatus(code = HttpStatus.CREATED)
    @PostMapping
    public ReviewDtoWithAuthor createReview(@Valid @RequestBody ReviewCreateDto dto) {
        Review createdReview = reviewService.createReview(reviewMapper.toEntity(dto));
        return reviewMapper.toDtoWithAuthor(createdReview);
    }

    @PatchMapping("/{reviewId}")
    public ReviewDtoWithAuthor updateReview(@PathVariable
                                            @Positive(message = "Review's id should be positive")
                                            Long reviewId,
                                            @RequestHeader("X-Review-User-Id")
                                            @Positive(message = "User's id should be positive")
                                            Long authorId,
                                            @Valid @RequestBody ReviewUpdateDto dto) {
        Review updatedReview = reviewService.updateReview(reviewMapper.toEntity(dto, reviewId, authorId));
        return reviewMapper.toDtoWithAuthor(updatedReview);
    }

    @GetMapping("/{id}")
    public ReviewDto getReviewById(@PathVariable @Positive(message = "Review's id should be positive") Long id) {
        return reviewMapper.toDtoWithoutAuthor(reviewService.getReviewById(id));
    }

    @GetMapping
    public List<ReviewDto> getReviewsByEventId(@RequestParam(defaultValue = "0")
                                                   @PositiveOrZero(message = "Parameter 'page' shouldn't be negative")
                                                   int page,
                                               @RequestParam(defaultValue = "10")
                                               @Positive(message = "Parameter 'size' should be positive")
                                               int size,
                                               @RequestParam
                                                   @Positive(message = "User's id should be positive")
                                                   Long eventId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdOn");
        return reviewService.getReviewsByEvent(eventId, PageRequest.of(page, size, sort)).stream()
                .map(reviewMapper::toDtoWithoutAuthor)
                .collect(Collectors.toList());
    }

    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public Long deleteReviewById(@PathVariable
                                     @Positive(message = "Review's id should be positive")
                                     Long id,
                                 @RequestHeader("X-Review-User-Id")
                                     @Positive(message = "User's id should be positive")
                                     Long authorId) {
        return reviewService.deleteReview(id, authorId);
    }

    @PutMapping("/{id}/like")
    public void addLike(@RequestHeader("X-Review-User-Id")
                            @Positive(message = "User's id should be positive")
                            Long evaluatorId,
                        @PathVariable
                            @Positive(message = "Review's id should be positive")
                            Long id) {
        reviewService.addLike(id, evaluatorId);
    }

    @PutMapping("/{id}/dislike")
    public void addDislike(@RequestHeader("X-Review-User-Id")
                               @Positive(message = "User's id should be positive")
                               Long evaluatorId,
                           @PathVariable
                               @Positive(message = "Review's id should be positive")
                               Long id) {
        reviewService.addDislike(id, evaluatorId);
    }

    @DeleteMapping("/{id}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeLike(@RequestHeader("X-Review-User-Id")
                               @Positive(message = "User's id should be positive")
                               Long evaluatorId,
                           @PathVariable
                               @Positive(message = "Review's id should be positive")
                               Long id) {
        reviewService.removeLike(id, evaluatorId);
    }

    @DeleteMapping("/{id}/dislike")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeDislike(@RequestHeader("X-Review-User-Id")
                                  @Positive(message = "User's id should be positive")
                                  Long evaluatorId,
                              @PathVariable
                                  @Positive(message = "Review's id should be positive")
                                  Long id) {
        reviewService.removeDislike(id, evaluatorId);
    }
}
