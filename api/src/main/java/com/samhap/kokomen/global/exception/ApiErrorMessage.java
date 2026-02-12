package com.samhap.kokomen.global.exception;

import lombok.Getter;

@Getter
public enum ApiErrorMessage {

    AUTHENTICATION_ANNOTATION_REQUIRED("MemberAuth 파라미터는 @Authentication 어노테이션이 있어야 합니다."),
    LOGIN_REQUIRED("로그인이 필요합니다"),
    MEMBER_ID_NOT_IN_SESSION("세션에 MEMBER_ID가 없습니다."),
    INVALID_REQUEST("잘못된 요청입니다."),
    MISSING_REQUEST_PARAMETER("필수 요청 파라미터가 누락되었습니다."),
    INVALID_REQUEST_FORMAT("잘못된 요청 형식입니다. JSON 형식을 확인해주세요."),
    JSON_PARSE_ERROR("JSON 파싱 오류: 유효하지 않은 값이 전달되었습니다."),
    INTERNAL_SERVER_ERROR("서버에 문제가 발생하였습니다.");

    private final String message;

    ApiErrorMessage(String message) {
        this.message = message;
    }
}
