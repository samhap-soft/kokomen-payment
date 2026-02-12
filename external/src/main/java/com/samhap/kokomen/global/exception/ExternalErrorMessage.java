package com.samhap.kokomen.global.exception;

import lombok.Getter;

@Getter
public enum ExternalErrorMessage {

    INVALID_REQUEST("잘못된 요청입니다."),
    MISSING_REQUEST_PARAMETER("필수 요청 파라미터가 누락되었습니다."),
    INVALID_REQUEST_FORMAT("잘못된 요청 형식입니다. JSON 형식을 확인해주세요."),
    JSON_PARSE_ERROR("JSON 파싱 오류: 유효하지 않은 값이 전달되었습니다."),
    INTERNAL_SERVER_ERROR("서버에 문제가 발생하였습니다.");

    private final String message;

    ExternalErrorMessage(String message) {
        this.message = message;
    }
}
