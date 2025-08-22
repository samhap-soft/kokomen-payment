package com.samhap.kokomen.global.exception;

public class ForbiddenException extends KokomenException {

    public ForbiddenException(String message) {
        super(message, 403);
    }
}
