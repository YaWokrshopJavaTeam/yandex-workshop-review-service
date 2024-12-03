package ru.practicum.workshop.reviewservice.dto;

public final class ReviewDtoValidationConstants {
    public static final String AUTHOR_ID_NOT_NULL_ERROR_MESSAGE = "Author's id shouldn't be null";
    public static final String AUTHOR_ID_POSITIVE_ERROR_MESSAGE = "Author's id should be positive";
    public static final String EVENT_ID_NOT_NULL_ERROR_MESSAGE = "Event's id shouldn't be null";
    public static final String EVENT_ID_POSITIVE_ERROR_MESSAGE = "Event's id should be positive";

    public static final String USERNAME_NOT_BLANK_ERROR_MESSAGE = "Username shouldn't be blank";
    public static final int USERNAME_MIN_SIZE = 2;
    public static final int USERNAME_MAX_SIZE = 250;
    public static final String USERNAME_SIZE_ERROR_MESSAGE = "Username of review shouldn't be less then "
            + USERNAME_MIN_SIZE + " and more than "+ USERNAME_MAX_SIZE + " characters";

    public static final int TITLE_MAX_SIZE = 120;
    public static final String TITLE_SIZE_ERROR_MESSAGE = "Title of review shouldn't be more than "
            + TITLE_MAX_SIZE + " characters";

    public static final String CONTENT_NOT_BLANK_ERROR_MESSAGE = "Review content shouldn't be blank";
    public static final int CONTENT_MIN_SIZE = 3;
    public static final int CONTENT_MAX_SIZE = 10000;
    public static final String CONTENT_SIZE_ERROR_MESSAGE = "Review content shouldn't be less then "
            + CONTENT_MIN_SIZE + " and more than "+ CONTENT_MAX_SIZE + " characters";

    public static final String MARK_NOT_NULL_ERROR_MESSAGE = "Review's mark shouldn't be null";
    public static final int MARK_MIN_VALUE = 1;
    public static final int MARK_MAX_VALUE = 10;
    public static final String MARK_MIN_ERROR_MESSAGE = "Review's mark shouldn't be less than " + MARK_MIN_VALUE;
    public static final String MARK_MAX_ERROR_MESSAGE = "Review's mark shouldn't be more than " + MARK_MAX_VALUE;
}
