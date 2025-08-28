package com.samhap.kokomen.global.exception;

public class BadRequestException extends KokomenException {

    public BadRequestException(String message) {
        super(message, 400);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause, 400);
    }
}
