package com.helper.payment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class PaymentExceptions {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class PaymentNotFoundException extends RuntimeException {
        public PaymentNotFoundException(String msg) { super(msg); }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class InvalidPaymentStateException extends RuntimeException {
        public InvalidPaymentStateException(String msg) { super(msg); }
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    public static class UnauthorizedPaymentAccessException extends RuntimeException {
        public UnauthorizedPaymentAccessException(String msg) { super(msg); }
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    public static class DuplicatePaymentException extends RuntimeException {
        public DuplicatePaymentException(String msg) { super(msg); }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class InsufficientFundsException extends RuntimeException {
        public InsufficientFundsException(String msg) { super(msg); }
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class ConfigNotFoundException extends RuntimeException {
        public ConfigNotFoundException(String msg) { super(msg); }
    }
}
