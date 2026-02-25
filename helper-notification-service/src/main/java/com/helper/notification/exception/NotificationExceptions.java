package com.helper.notification.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class NotificationExceptions {
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class NotificationNotFoundException extends RuntimeException {
        public NotificationNotFoundException(String msg) { super(msg); }
    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class InvalidNotificationException extends RuntimeException {
        public InvalidNotificationException(String msg) { super(msg); }
    }
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public static class UnauthorizedNotificationException extends RuntimeException {
        public UnauthorizedNotificationException(String msg) { super(msg); }
    }
}
