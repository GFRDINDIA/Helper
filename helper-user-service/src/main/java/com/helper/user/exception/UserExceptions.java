package com.helper.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class UserExceptions {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class ProfileNotFoundException extends RuntimeException {
        public ProfileNotFoundException(String msg) { super(msg); }
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class DocumentNotFoundException extends RuntimeException {
        public DocumentNotFoundException(String msg) { super(msg); }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class InvalidProfileException extends RuntimeException {
        public InvalidProfileException(String msg) { super(msg); }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class InvalidKycException extends RuntimeException {
        public InvalidKycException(String msg) { super(msg); }
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    public static class UnauthorizedAccessException extends RuntimeException {
        public UnauthorizedAccessException(String msg) { super(msg); }
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    public static class ProfileAlreadyExistsException extends RuntimeException {
        public ProfileAlreadyExistsException(String msg) { super(msg); }
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    public static class DuplicateSkillException extends RuntimeException {
        public DuplicateSkillException(String msg) { super(msg); }
    }
}
