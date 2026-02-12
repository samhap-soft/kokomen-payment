package com.samhap.kokomen.global.infrastructure;

import com.samhap.kokomen.global.annotation.Authentication;
import com.samhap.kokomen.global.dto.MemberAuth;
import com.samhap.kokomen.global.exception.ApiErrorMessage;
import com.samhap.kokomen.global.exception.InternalServerErrorException;
import com.samhap.kokomen.global.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Slf4j
@Component
public class MemberAuthArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(MemberAuth.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
        Authentication authentication = parameter.getParameterAnnotation(Authentication.class);
        if (authentication == null) {
            throw new InternalServerErrorException(ApiErrorMessage.AUTHENTICATION_ANNOTATION_REQUIRED.getMessage());
        }
        boolean authenticationRequired = authentication.required();
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        HttpSession session = request.getSession(false);

        validateAuthentication(session, authenticationRequired);
        if (session == null) {
            return MemberAuth.notAuthenticated();
        }
        Long memberId = (Long) session.getAttribute("MEMBER_ID");
        validateAuthentication(memberId, authenticationRequired);

        return new MemberAuth(memberId);
    }

    private void validateAuthentication(HttpSession session, boolean authenticationRequired) {
        if (session == null && authenticationRequired) {
            throw new UnauthorizedException(ApiErrorMessage.LOGIN_REQUIRED.getMessage());
        }
    }

    private void validateAuthentication(Long memberId, boolean authenticationRequired) {
        if (memberId == null) {
            log.error(ApiErrorMessage.MEMBER_ID_NOT_IN_SESSION.getMessage());
        }
        if (memberId == null && authenticationRequired) {
            throw new UnauthorizedException(ApiErrorMessage.MEMBER_ID_NOT_IN_SESSION.getMessage());
        }
    }
}
