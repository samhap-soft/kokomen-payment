package com.samhap.kokomen.global.exception;

public class UnauthorizedException extends KokomenException {

    public UnauthorizedException(String message) {
        super(message, 401);
    }
}
