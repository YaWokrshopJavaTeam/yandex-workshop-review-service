package ru.practicum.workshop.reviewservice.mapper;

import org.mapstruct.*;
import ru.practicum.workshop.reviewservice.dto.*;
import ru.practicum.workshop.reviewservice.model.Review;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true),
        unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {LocalDateTime.class})
public interface ReviewMapper {
    @Mapping(source = "review.author.id", target = "authorId")
    @Mapping(source = "review.author.username", target = "username")
    ReviewDtoWithAuthor toDtoWithAuthor(Review review);

    @Mapping(source = "review.author.username", target = "username")
    ReviewDto toDtoWithoutAuthor(Review review);

    @Mapping(source = "dto.authorId", target = "author.id")
    @Mapping(source = "dto.username", target = "author.username")
    @Mapping(target = "id", expression = "java(null)")
    @Mapping(target = "updatedOn", expression = "java(null)")
    @Mapping(target = "mark", constant = "0L")
    @Mapping(target = "createdOn", expression = "java(LocalDateTime.now())")
    Review toEntity(ReviewCreateDto dto);

    @Mapping(source = "reviewId", target = "id")
    @Mapping(source = "authorId", target = "author.id")
    @Mapping(source = "dto.username", target = "author.username")
    @Mapping(target = "updatedOn", expression = "java(LocalDateTime.now())")
    Review toEntity(ReviewUpdateDto dto, Long reviewId, Long authorId);
}
