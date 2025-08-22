package com.samhap.kokomen.global.exception;

public class BadRequestException extends KokomenException {

    public BadRequestException(String message) {
        super(message, 400);
    }
}
