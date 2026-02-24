package com.samhap.kokomen.global.exception;

public class NotFoundException extends KokomenException {

  public NotFoundException(String message) {
    super(message, 404);
  }
}
