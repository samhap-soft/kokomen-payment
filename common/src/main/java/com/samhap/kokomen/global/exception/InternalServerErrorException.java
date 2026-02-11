package com.samhap.kokomen.global.exception;

public class InternalServerErrorException extends KokomenException {

    public InternalServerErrorException(String message) {
        super(message, 500);
    }
}
