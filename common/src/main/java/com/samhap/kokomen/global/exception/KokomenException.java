package com.samhap.kokomen.global.exception;

import lombok.Getter;

@Getter
public class KokomenException extends RuntimeException {

    private final int httpStatusCode;

    public KokomenException(String message, int httpStatusCode) {
        super(message);
        this.httpStatusCode = httpStatusCode;
    }

    public KokomenException(String message, Throwable cause, int httpStatusCode) {
        super(message, cause);
        this.httpStatusCode = httpStatusCode;
    }
}
