package com.helper.rating.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class RatingExceptions {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class RatingNotFoundException extends RuntimeException {
        public RatingNotFoundException(String msg) { super(msg); }
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    public static class DuplicateRatingException extends RuntimeException {
        public DuplicateRatingException(String msg) { super(msg); }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class InvalidRatingException extends RuntimeException {
        public InvalidRatingException(String msg) { super(msg); }
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    public static class UnauthorizedRatingException extends RuntimeException {
        public UnauthorizedRatingException(String msg) { super(msg); }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class RatingWindowExpiredException extends RuntimeException {
        public RatingWindowExpiredException(String msg) { super(msg); }
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class FlagNotFoundException extends RuntimeException {
        public FlagNotFoundException(String msg) { super(msg); }
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    public static class DuplicateFlagException extends RuntimeException {
        public DuplicateFlagException(String msg) { super(msg); }
    }
}
