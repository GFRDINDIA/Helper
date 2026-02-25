package com.helper.task.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class TaskExceptions {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class TaskNotFoundException extends RuntimeException {
        public TaskNotFoundException(String message) { super(message); }
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class BidNotFoundException extends RuntimeException {
        public BidNotFoundException(String message) { super(message); }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class InvalidTaskStateException extends RuntimeException {
        public InvalidTaskStateException(String message) { super(message); }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class InvalidBidException extends RuntimeException {
        public InvalidBidException(String message) { super(message); }
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    public static class UnauthorizedTaskAccessException extends RuntimeException {
        public UnauthorizedTaskAccessException(String message) { super(message); }
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    public static class DuplicateBidException extends RuntimeException {
        public DuplicateBidException(String message) { super(message); }
    }

    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public static class BidLimitExceededException extends RuntimeException {
        public BidLimitExceededException(String message) { super(message); }
    }
}
